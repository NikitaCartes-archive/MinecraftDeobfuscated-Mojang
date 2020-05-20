package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

public class MeleeAttackGoal extends Goal {
	protected final PathfinderMob mob;
	private final double speedModifier;
	private final boolean followingTargetEvenIfNotSeen;
	private Path path;
	private double pathedTargetX;
	private double pathedTargetY;
	private double pathedTargetZ;
	private int ticksUntilNextPathRecalculation;
	private int ticksUntilNextAttack;
	private final int attackInterval = 20;
	private long lastCanUseCheck;

	public MeleeAttackGoal(PathfinderMob pathfinderMob, double d, boolean bl) {
		this.mob = pathfinderMob;
		this.speedModifier = d;
		this.followingTargetEvenIfNotSeen = bl;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		long l = this.mob.level.getGameTime();
		if (l - this.lastCanUseCheck < 20L) {
			return false;
		} else {
			this.lastCanUseCheck = l;
			LivingEntity livingEntity = this.mob.getTarget();
			if (livingEntity == null) {
				return false;
			} else if (!livingEntity.isAlive()) {
				return false;
			} else {
				this.path = this.mob.getNavigation().createPath(livingEntity, 0);
				return this.path != null
					? true
					: this.getAttackReachSqr(livingEntity) >= this.mob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
			}
		}
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity livingEntity = this.mob.getTarget();
		if (livingEntity == null) {
			return false;
		} else if (!livingEntity.isAlive()) {
			return false;
		} else if (!this.followingTargetEvenIfNotSeen) {
			return !this.mob.getNavigation().isDone();
		} else {
			return !this.mob.isWithinRestriction(livingEntity.blockPosition())
				? false
				: !(livingEntity instanceof Player) || !livingEntity.isSpectator() && !((Player)livingEntity).isCreative();
		}
	}

	@Override
	public void start() {
		this.mob.getNavigation().moveTo(this.path, this.speedModifier);
		this.mob.setAggressive(true);
		this.ticksUntilNextPathRecalculation = 0;
		this.ticksUntilNextAttack = 0;
	}

	@Override
	public void stop() {
		LivingEntity livingEntity = this.mob.getTarget();
		if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingEntity)) {
			this.mob.setTarget(null);
		}

		this.mob.setAggressive(false);
		this.mob.getNavigation().stop();
	}

	@Override
	public void tick() {
		LivingEntity livingEntity = this.mob.getTarget();
		this.mob.getLookControl().setLookAt(livingEntity, 30.0F, 30.0F);
		double d = this.mob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
		this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
		if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().canSee(livingEntity))
			&& this.ticksUntilNextPathRecalculation <= 0
			&& (
				this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0
					|| livingEntity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0
					|| this.mob.getRandom().nextFloat() < 0.05F
			)) {
			this.pathedTargetX = livingEntity.getX();
			this.pathedTargetY = livingEntity.getY();
			this.pathedTargetZ = livingEntity.getZ();
			this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
			if (d > 1024.0) {
				this.ticksUntilNextPathRecalculation += 10;
			} else if (d > 256.0) {
				this.ticksUntilNextPathRecalculation += 5;
			}

			if (!this.mob.getNavigation().moveTo(livingEntity, this.speedModifier)) {
				this.ticksUntilNextPathRecalculation += 15;
			}
		}

		this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
		this.checkAndPerformAttack(livingEntity, d);
	}

	protected void checkAndPerformAttack(LivingEntity livingEntity, double d) {
		double e = this.getAttackReachSqr(livingEntity);
		if (d <= e && this.ticksUntilNextAttack <= 0) {
			this.resetAttackCooldown();
			this.mob.swing(InteractionHand.MAIN_HAND);
			this.mob.doHurtTarget(livingEntity);
		}
	}

	protected void resetAttackCooldown() {
		this.ticksUntilNextAttack = 20;
	}

	protected boolean isTimeToAttack() {
		return this.ticksUntilNextAttack <= 0;
	}

	protected int getTicksUntilNextAttack() {
		return this.ticksUntilNextAttack;
	}

	protected int getAttackInterval() {
		return 20;
	}

	protected double getAttackReachSqr(LivingEntity livingEntity) {
		return (double)(this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + livingEntity.getBbWidth());
	}
}
