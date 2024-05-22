package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.projectile.windcharge.BreezeWindCharge;
import net.minecraft.world.phys.Vec3;

public class Shoot extends Behavior<Breeze> {
	private static final int ATTACK_RANGE_MIN_SQRT = 4;
	private static final int ATTACK_RANGE_MAX_SQRT = 256;
	private static final int UNCERTAINTY_BASE = 5;
	private static final int UNCERTAINTY_MULTIPLIER = 4;
	private static final float PROJECTILE_MOVEMENT_SCALE = 0.7F;
	private static final int SHOOT_INITIAL_DELAY_TICKS = Math.round(15.0F);
	private static final int SHOOT_RECOVER_DELAY_TICKS = Math.round(4.0F);
	private static final int SHOOT_COOLDOWN_TICKS = Math.round(10.0F);

	@VisibleForTesting
	public Shoot() {
		super(
			ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.BREEZE_SHOOT_COOLDOWN,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.BREEZE_SHOOT_CHARGING,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.BREEZE_SHOOT_RECOVERING,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.BREEZE_SHOOT,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.BREEZE_JUMP_TARGET,
				MemoryStatus.VALUE_ABSENT
			),
			SHOOT_INITIAL_DELAY_TICKS + 1 + SHOOT_RECOVER_DELAY_TICKS
		);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Breeze breeze) {
		return breeze.getPose() != Pose.STANDING
			? false
			: (Boolean)breeze.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).map(livingEntity -> isTargetWithinRange(breeze, livingEntity)).map(boolean_ -> {
				if (!boolean_) {
					breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
				}

				return boolean_;
			}).orElse(false);
	}

	protected boolean canStillUse(ServerLevel serverLevel, Breeze breeze, long l) {
		return breeze.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && breeze.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_SHOOT);
	}

	protected void start(ServerLevel serverLevel, Breeze breeze, long l) {
		breeze.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(livingEntity -> breeze.setPose(Pose.SHOOTING));
		breeze.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_CHARGING, Unit.INSTANCE, (long)SHOOT_INITIAL_DELAY_TICKS);
		breeze.playSound(SoundEvents.BREEZE_INHALE, 1.0F, 1.0F);
	}

	protected void stop(ServerLevel serverLevel, Breeze breeze, long l) {
		if (breeze.getPose() == Pose.SHOOTING) {
			breeze.setPose(Pose.STANDING);
		}

		breeze.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_COOLDOWN, Unit.INSTANCE, (long)SHOOT_COOLDOWN_TICKS);
		breeze.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
	}

	protected void tick(ServerLevel serverLevel, Breeze breeze, long l) {
		Brain<Breeze> brain = breeze.getBrain();
		LivingEntity livingEntity = (LivingEntity)brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
		if (livingEntity != null) {
			breeze.lookAt(EntityAnchorArgument.Anchor.EYES, livingEntity.position());
			if (!brain.getMemory(MemoryModuleType.BREEZE_SHOOT_CHARGING).isPresent() && !brain.getMemory(MemoryModuleType.BREEZE_SHOOT_RECOVERING).isPresent()) {
				brain.setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_RECOVERING, Unit.INSTANCE, (long)SHOOT_RECOVER_DELAY_TICKS);
				if (isFacingTarget(breeze, livingEntity)) {
					double d = livingEntity.getX() - breeze.getX();
					double e = livingEntity.getY(livingEntity.isPassenger() ? 0.8 : 0.3) - breeze.getY(0.5);
					double f = livingEntity.getZ() - breeze.getZ();
					BreezeWindCharge breezeWindCharge = new BreezeWindCharge(breeze, serverLevel);
					breeze.playSound(SoundEvents.BREEZE_SHOOT, 1.5F, 1.0F);
					breezeWindCharge.shoot(d, e, f, 0.7F, (float)(5 - serverLevel.getDifficulty().getId() * 4));
					serverLevel.addFreshEntity(breezeWindCharge);
				}
			}
		}
	}

	@VisibleForTesting
	public static boolean isFacingTarget(Breeze breeze, LivingEntity livingEntity) {
		Vec3 vec3 = breeze.getViewVector(1.0F);
		Vec3 vec32 = livingEntity.position().subtract(breeze.position()).normalize();
		return vec3.dot(vec32) > 0.5;
	}

	private static boolean isTargetWithinRange(Breeze breeze, LivingEntity livingEntity) {
		double d = breeze.position().distanceToSqr(livingEntity.position());
		return d > 4.0 && d < 256.0;
	}
}
