package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

public class MeleeAttackGoal extends Goal {
	protected final PathfinderMob mob;
	protected int attackTime;
	private final double speedModifier;
	private final boolean trackTarget;
	private Path path;
	private int timeToRecalcPath;
	private double pathedTargetX;
	private double pathedTargetY;
	private double pathedTargetZ;
	protected final int attackInterval = 20;
	private long lastUpdate;

	public MeleeAttackGoal(PathfinderMob pathfinderMob, double d, boolean bl) {
		this.mob = pathfinderMob;
		this.speedModifier = d;
		this.trackTarget = bl;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		long l = this.mob.level.getGameTime();
		if (l - this.lastUpdate < 20L) {
			return false;
		} else {
			this.lastUpdate = l;
			LivingEntity livingEntity = this.mob.getTarget();
			if (livingEntity == null) {
				return false;
			} else if (!livingEntity.isAlive()) {
				return false;
			} else {
				this.path = this.mob.getNavigation().createPath(livingEntity, 0);
				return this.path != null
					? true
					: this.getAttackReachSqr(livingEntity) >= this.mob.distanceToSqr(livingEntity.x, livingEntity.getBoundingBox().minY, livingEntity.z);
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
		} else if (!this.trackTarget) {
			return !this.mob.getNavigation().isDone();
		} else {
			return !this.mob.isWithinRestriction(new BlockPos(livingEntity))
				? false
				: !(livingEntity instanceof Player) || !livingEntity.isSpectator() && !((Player)livingEntity).isCreative();
		}
	}

	@Override
	public void start() {
		this.mob.getNavigation().moveTo(this.path, this.speedModifier);
		this.mob.setAggressive(true);
		this.timeToRecalcPath = 0;
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
		double d = this.mob.distanceToSqr(livingEntity.x, livingEntity.getBoundingBox().minY, livingEntity.z);
		this.timeToRecalcPath--;
		if ((this.trackTarget || this.mob.getSensing().canSee(livingEntity))
			&& this.timeToRecalcPath <= 0
			&& (
				this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0
					|| livingEntity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0
					|| this.mob.getRandom().nextFloat() < 0.05F
			)) {
			this.pathedTargetX = livingEntity.x;
			this.pathedTargetY = livingEntity.getBoundingBox().minY;
			this.pathedTargetZ = livingEntity.z;
			this.timeToRecalcPath = 4 + this.mob.getRandom().nextInt(7);
			if (d > 1024.0) {
				this.timeToRecalcPath += 10;
			} else if (d > 256.0) {
				this.timeToRecalcPath += 5;
			}

			if (!this.mob.getNavigation().moveTo(livingEntity, this.speedModifier)) {
				this.timeToRecalcPath += 15;
			}
		}

		this.attackTime = Math.max(this.attackTime - 1, 0);
		this.checkAndPerformAttack(livingEntity, d);
	}

	protected void checkAndPerformAttack(LivingEntity livingEntity, double d) {
		double e = this.getAttackReachSqr(livingEntity);
		if (d <= e && this.attackTime <= 0) {
			this.attackTime = 20;
			this.mob.swing(InteractionHand.MAIN_HAND);
			this.mob.doHurtTarget(livingEntity);
		}
	}

	protected double getAttackReachSqr(LivingEntity livingEntity) {
		return (double)(this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + livingEntity.getBbWidth());
	}
}
