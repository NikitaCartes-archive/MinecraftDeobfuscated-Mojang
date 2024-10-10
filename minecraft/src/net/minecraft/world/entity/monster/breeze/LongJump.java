package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.LongJumpUtil;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LongJump extends Behavior<Breeze> {
	private static final int REQUIRED_AIR_BLOCKS_ABOVE = 4;
	private static final int JUMP_COOLDOWN_TICKS = 10;
	private static final int JUMP_COOLDOWN_WHEN_HURT_TICKS = 2;
	private static final int INHALING_DURATION_TICKS = Math.round(10.0F);
	private static final float DEFAULT_FOLLOW_RANGE = 24.0F;
	private static final float DEFAULT_MAX_JUMP_VELOCITY = 1.4F;
	private static final float MAX_JUMP_VELOCITY_MULTIPLIER = 0.058333334F;
	private static final ObjectArrayList<Integer> ALLOWED_ANGLES = new ObjectArrayList<>(Lists.newArrayList(40, 55, 60, 75, 80));

	@VisibleForTesting
	public LongJump() {
		super(
			Map.of(
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.BREEZE_JUMP_COOLDOWN,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.BREEZE_JUMP_INHALING,
				MemoryStatus.REGISTERED,
				MemoryModuleType.BREEZE_JUMP_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.BREEZE_SHOOT,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.BREEZE_LEAVING_WATER,
				MemoryStatus.REGISTERED
			),
			200
		);
	}

	public static boolean canRun(ServerLevel serverLevel, Breeze breeze) {
		if (!breeze.onGround() && !breeze.isInWater()) {
			return false;
		} else if (Swim.shouldSwim(breeze)) {
			return false;
		} else if (breeze.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_TARGET, MemoryStatus.VALUE_PRESENT)) {
			return true;
		} else {
			LivingEntity livingEntity = (LivingEntity)breeze.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
			if (livingEntity == null) {
				return false;
			} else if (outOfAggroRange(breeze, livingEntity)) {
				breeze.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
				return false;
			} else if (tooCloseForJump(breeze, livingEntity)) {
				return false;
			} else if (!canJumpFromCurrentPosition(serverLevel, breeze)) {
				return false;
			} else {
				BlockPos blockPos = snapToSurface(breeze, BreezeUtil.randomPointBehindTarget(livingEntity, breeze.getRandom()));
				if (blockPos == null) {
					return false;
				} else {
					BlockState blockState = serverLevel.getBlockState(blockPos.below());
					if (breeze.getType().isBlockDangerous(blockState)) {
						return false;
					} else if (!BreezeUtil.hasLineOfSight(breeze, blockPos.getCenter()) && !BreezeUtil.hasLineOfSight(breeze, blockPos.above(4).getCenter())) {
						return false;
					} else {
						breeze.getBrain().setMemory(MemoryModuleType.BREEZE_JUMP_TARGET, blockPos);
						return true;
					}
				}
			}
		}
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Breeze breeze) {
		return canRun(serverLevel, breeze);
	}

	protected boolean canStillUse(ServerLevel serverLevel, Breeze breeze, long l) {
		return breeze.getPose() != Pose.STANDING && !breeze.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_JUMP_COOLDOWN);
	}

	protected void start(ServerLevel serverLevel, Breeze breeze, long l) {
		if (breeze.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_INHALING, MemoryStatus.VALUE_ABSENT)) {
			breeze.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_INHALING, Unit.INSTANCE, (long)INHALING_DURATION_TICKS);
		}

		breeze.setPose(Pose.INHALING);
		serverLevel.playSound(null, breeze, SoundEvents.BREEZE_CHARGE, SoundSource.HOSTILE, 1.0F, 1.0F);
		breeze.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_TARGET).ifPresent(blockPos -> breeze.lookAt(EntityAnchorArgument.Anchor.EYES, blockPos.getCenter()));
	}

	protected void tick(ServerLevel serverLevel, Breeze breeze, long l) {
		boolean bl = breeze.isInWater();
		if (!bl && breeze.getBrain().checkMemory(MemoryModuleType.BREEZE_LEAVING_WATER, MemoryStatus.VALUE_PRESENT)) {
			breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_LEAVING_WATER);
		}

		if (isFinishedInhaling(breeze)) {
			Vec3 vec3 = (Vec3)breeze.getBrain()
				.getMemory(MemoryModuleType.BREEZE_JUMP_TARGET)
				.flatMap(blockPos -> calculateOptimalJumpVector(breeze, breeze.getRandom(), Vec3.atBottomCenterOf(blockPos)))
				.orElse(null);
			if (vec3 == null) {
				breeze.setPose(Pose.STANDING);
				return;
			}

			if (bl) {
				breeze.getBrain().setMemory(MemoryModuleType.BREEZE_LEAVING_WATER, Unit.INSTANCE);
			}

			breeze.playSound(SoundEvents.BREEZE_JUMP, 1.0F, 1.0F);
			breeze.setPose(Pose.LONG_JUMPING);
			breeze.setYRot(breeze.yBodyRot);
			breeze.setDiscardFriction(true);
			breeze.setDeltaMovement(vec3);
		} else if (isFinishedJumping(breeze)) {
			breeze.playSound(SoundEvents.BREEZE_LAND, 1.0F, 1.0F);
			breeze.setPose(Pose.STANDING);
			breeze.setDiscardFriction(false);
			boolean bl2 = breeze.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
			breeze.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_COOLDOWN, Unit.INSTANCE, bl2 ? 2L : 10L);
			breeze.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 100L);
		}
	}

	protected void stop(ServerLevel serverLevel, Breeze breeze, long l) {
		if (breeze.getPose() == Pose.LONG_JUMPING || breeze.getPose() == Pose.INHALING) {
			breeze.setPose(Pose.STANDING);
		}

		breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_TARGET);
		breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_INHALING);
		breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_LEAVING_WATER);
	}

	private static boolean isFinishedInhaling(Breeze breeze) {
		return breeze.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_INHALING).isEmpty() && breeze.getPose() == Pose.INHALING;
	}

	private static boolean isFinishedJumping(Breeze breeze) {
		boolean bl = breeze.getPose() == Pose.LONG_JUMPING;
		boolean bl2 = breeze.onGround();
		boolean bl3 = breeze.isInWater() && breeze.getBrain().checkMemory(MemoryModuleType.BREEZE_LEAVING_WATER, MemoryStatus.VALUE_ABSENT);
		return bl && (bl2 || bl3);
	}

	@Nullable
	private static BlockPos snapToSurface(LivingEntity livingEntity, Vec3 vec3) {
		ClipContext clipContext = new ClipContext(vec3, vec3.relative(Direction.DOWN, 10.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, livingEntity);
		HitResult hitResult = livingEntity.level().clip(clipContext);
		if (hitResult.getType() == HitResult.Type.BLOCK) {
			return BlockPos.containing(hitResult.getLocation()).above();
		} else {
			ClipContext clipContext2 = new ClipContext(vec3, vec3.relative(Direction.UP, 10.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, livingEntity);
			HitResult hitResult2 = livingEntity.level().clip(clipContext2);
			return hitResult2.getType() == HitResult.Type.BLOCK ? BlockPos.containing(hitResult2.getLocation()).above() : null;
		}
	}

	private static boolean outOfAggroRange(Breeze breeze, LivingEntity livingEntity) {
		return !livingEntity.closerThan(breeze, breeze.getAttributeValue(Attributes.FOLLOW_RANGE));
	}

	private static boolean tooCloseForJump(Breeze breeze, LivingEntity livingEntity) {
		return livingEntity.distanceTo(breeze) - 4.0F <= 0.0F;
	}

	private static boolean canJumpFromCurrentPosition(ServerLevel serverLevel, Breeze breeze) {
		BlockPos blockPos = breeze.blockPosition();
		if (serverLevel.getBlockState(blockPos).is(Blocks.HONEY_BLOCK)) {
			return false;
		} else {
			for (int i = 1; i <= 4; i++) {
				BlockPos blockPos2 = blockPos.relative(Direction.UP, i);
				if (!serverLevel.getBlockState(blockPos2).isAir() && !serverLevel.getFluidState(blockPos2).is(FluidTags.WATER)) {
					return false;
				}
			}

			return true;
		}
	}

	private static Optional<Vec3> calculateOptimalJumpVector(Breeze breeze, RandomSource randomSource, Vec3 vec3) {
		for (int i : Util.shuffledCopy(ALLOWED_ANGLES, randomSource)) {
			float f = 0.058333334F * (float)breeze.getAttributeValue(Attributes.FOLLOW_RANGE);
			Optional<Vec3> optional = LongJumpUtil.calculateJumpVectorForAngle(breeze, vec3, f, i, false);
			if (optional.isPresent()) {
				if (breeze.hasEffect(MobEffects.JUMP)) {
					double d = ((Vec3)optional.get()).normalize().y * (double)breeze.getJumpBoostPower();
					return optional.map(vec3x -> vec3x.add(0.0, d, 0.0));
				}

				return optional;
			}
		}

		return Optional.empty();
	}
}
