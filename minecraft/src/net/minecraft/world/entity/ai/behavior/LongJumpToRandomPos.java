package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.WeighedRandom;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LongJumpToRandomPos<E extends Mob> extends Behavior<E> {
	private static final int FIND_JUMP_TRIES = 20;
	private static final int PREPARE_JUMP_DURATION = 40;
	private static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
	public static final int TIME_OUT_DURATION = 200;
	private final UniformInt timeBetweenLongJumps;
	private final int maxLongJumpHeight;
	private final int maxLongJumpWidth;
	private final float maxJumpVelocity;
	private final List<LongJumpToRandomPos.PossibleJump> jumpCandidates = new ArrayList();
	private Optional<Vec3> initialPosition = Optional.empty();
	private Optional<LongJumpToRandomPos.PossibleJump> chosenJump = Optional.empty();
	private int findJumpTries;
	private long prepareJumpStart;
	private Function<E, SoundEvent> getJumpSound;

	public LongJumpToRandomPos(UniformInt uniformInt, int i, int j, float f, Function<E, SoundEvent> function) {
		super(
			ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.LONG_JUMP_MID_JUMP,
				MemoryStatus.VALUE_ABSENT
			),
			200
		);
		this.timeBetweenLongJumps = uniformInt;
		this.maxLongJumpHeight = i;
		this.maxLongJumpWidth = j;
		this.maxJumpVelocity = f;
		this.getJumpSound = function;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Mob mob) {
		return mob.isOnGround() && !serverLevel.getBlockState(mob.blockPosition()).is(Blocks.HONEY_BLOCK);
	}

	protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
		boolean bl = this.initialPosition.isPresent()
			&& ((Vec3)this.initialPosition.get()).equals(mob.position())
			&& this.findJumpTries > 0
			&& (this.chosenJump.isPresent() || !this.jumpCandidates.isEmpty());
		if (!bl && !mob.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isPresent()) {
			mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(serverLevel.random) / 2);
		}

		return bl;
	}

	protected void start(ServerLevel serverLevel, Mob mob, long l) {
		this.chosenJump = Optional.empty();
		this.findJumpTries = 20;
		this.jumpCandidates.clear();
		this.initialPosition = Optional.of(mob.position());
		BlockPos blockPos = mob.blockPosition();
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		Iterable<BlockPos> iterable = BlockPos.betweenClosed(
			i - this.maxLongJumpWidth,
			j - this.maxLongJumpHeight,
			k - this.maxLongJumpWidth,
			i + this.maxLongJumpWidth,
			j + this.maxLongJumpHeight,
			k + this.maxLongJumpWidth
		);
		PathNavigation pathNavigation = mob.getNavigation();

		for (BlockPos blockPos2 : iterable) {
			double d = blockPos2.distSqr(blockPos);
			if ((i != blockPos2.getX() || k != blockPos2.getZ())
				&& pathNavigation.isStableDestination(blockPos2)
				&& mob.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(mob.level, blockPos2.mutable())) == 0.0F) {
				Optional<Vec3> optional = this.calculateOptimalJumpVector(mob, Vec3.atCenterOf(blockPos2));
				optional.ifPresent(vec3 -> this.jumpCandidates.add(new LongJumpToRandomPos.PossibleJump(new BlockPos(blockPos2), vec3, Mth.ceil(d))));
			}
		}
	}

	protected void tick(ServerLevel serverLevel, E mob, long l) {
		if (this.chosenJump.isPresent()) {
			if (l - this.prepareJumpStart >= 40L) {
				mob.setYRot(mob.yBodyRot);
				mob.setDiscardFriction(true);
				Vec3 vec3 = ((LongJumpToRandomPos.PossibleJump)this.chosenJump.get()).getJumpVector();
				double d = vec3.length();
				double e = d + mob.getJumpBoostPower();
				mob.setDeltaMovement(vec3.scale(e / d));
				mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
				serverLevel.playSound(null, mob, (SoundEvent)this.getJumpSound.apply(mob), SoundSource.NEUTRAL, 1.0F, 1.0F);
			}
		} else {
			this.findJumpTries--;
			Optional<LongJumpToRandomPos.PossibleJump> optional = WeighedRandom.getRandomItem(serverLevel.random, this.jumpCandidates);
			if (optional.isPresent()) {
				this.jumpCandidates.remove(optional.get());
				mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(((LongJumpToRandomPos.PossibleJump)optional.get()).getJumpTarget()));
				PathNavigation pathNavigation = mob.getNavigation();
				Path path = pathNavigation.createPath(((LongJumpToRandomPos.PossibleJump)optional.get()).getJumpTarget(), 0, 8);
				if (path == null || !path.canReach()) {
					this.chosenJump = optional;
					this.prepareJumpStart = l;
				}
			}
		}
	}

	private Optional<Vec3> calculateOptimalJumpVector(Mob mob, Vec3 vec3) {
		Optional<Vec3> optional = Optional.empty();

		for (int i = 65; i < 85; i += 5) {
			Optional<Vec3> optional2 = this.calculateJumpVectorForAngle(mob, vec3, i);
			if (!optional.isPresent() || optional2.isPresent() && ((Vec3)optional2.get()).lengthSqr() < ((Vec3)optional.get()).lengthSqr()) {
				optional = optional2;
			}
		}

		return optional;
	}

	private Optional<Vec3> calculateJumpVectorForAngle(Mob mob, Vec3 vec3, int i) {
		Vec3 vec32 = mob.position();
		Vec3 vec33 = new Vec3(vec3.x - vec32.x, 0.0, vec3.z - vec32.z).normalize().scale(0.5);
		vec3 = vec3.subtract(vec33);
		Vec3 vec34 = vec3.subtract(vec32);
		float f = (float)i * (float) Math.PI / 180.0F;
		double d = Math.atan2(vec34.z, vec34.x);
		double e = vec34.subtract(0.0, vec34.y, 0.0).lengthSqr();
		double g = Math.sqrt(e);
		double h = vec34.y;
		double j = Math.sin((double)(2.0F * f));
		double k = 0.08;
		double l = Math.pow(Math.cos((double)f), 2.0);
		double m = Math.sin((double)f);
		double n = Math.cos((double)f);
		double o = Math.sin(d);
		double p = Math.cos(d);
		double q = e * 0.08 / (g * j - 2.0 * h * l);
		if (q < 0.0) {
			return Optional.empty();
		} else {
			double r = Math.sqrt(q);
			if (r > (double)this.maxJumpVelocity) {
				return Optional.empty();
			} else {
				double s = r * n;
				double t = r * m;
				int u = Mth.ceil(g / s) * 2;
				double v = 0.0;
				Vec3 vec35 = null;

				for (int w = 0; w < u - 1; w++) {
					v += g / (double)u;
					double x = m / n * v - Math.pow(v, 2.0) * 0.08 / (2.0 * q * Math.pow(n, 2.0));
					double y = v * p;
					double z = v * o;
					Vec3 vec36 = new Vec3(vec32.x + y, vec32.y + x, vec32.z + z);
					if (vec35 != null && !this.isClearTransition(mob, vec35, vec36)) {
						return Optional.empty();
					}

					vec35 = vec36;
				}

				return Optional.of(new Vec3(s * p, t, s * o).scale(0.95F));
			}
		}
	}

	private boolean isClearTransition(Mob mob, Vec3 vec3, Vec3 vec32) {
		EntityDimensions entityDimensions = mob.getDimensions(Pose.LONG_JUMPING);
		Vec3 vec33 = vec32.subtract(vec3);
		double d = (double)Math.min(entityDimensions.width, entityDimensions.height);
		int i = Mth.ceil(vec33.length() / d);
		Vec3 vec34 = vec33.normalize();
		Vec3 vec35 = vec3;

		for (int j = 0; j < i; j++) {
			vec35 = j == i - 1 ? vec32 : vec35.add(vec34.scale(d * 0.9F));
			AABB aABB = entityDimensions.makeBoundingBox(vec35);
			if (!mob.level.noCollision(mob, aABB)) {
				return false;
			}
		}

		return true;
	}

	public static class PossibleJump extends WeighedRandom.WeighedRandomItem {
		private final BlockPos jumpTarget;
		private final Vec3 jumpVector;

		public PossibleJump(BlockPos blockPos, Vec3 vec3, int i) {
			super(i);
			this.jumpTarget = blockPos;
			this.jumpVector = vec3;
		}

		public BlockPos getJumpTarget() {
			return this.jumpTarget;
		}

		public Vec3 getJumpVector() {
			return this.jumpVector;
		}
	}
}
