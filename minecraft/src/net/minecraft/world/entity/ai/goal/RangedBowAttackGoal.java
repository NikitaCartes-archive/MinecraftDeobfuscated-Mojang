package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;

public class RangedBowAttackGoal<T extends Monster & RangedAttackMob> extends Goal {
	private final T mob;
	private final double speedModifier;
	private int attackIntervalMin;
	private final float attackRadiusSqr;
	private int attackTime = -1;
	private int seeTime;
	private boolean strafingClockwise;
	private boolean strafingBackwards;
	private int strafingTime = -1;

	public RangedBowAttackGoal(T monster, double d, int i, float f) {
		this.mob = monster;
		this.speedModifier = d;
		this.attackIntervalMin = i;
		this.attackRadiusSqr = f * f;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	public void setMinAttackInterval(int i) {
		this.attackIntervalMin = i;
	}

	@Override
	public boolean canUse() {
		return this.mob.getTarget() == null ? false : this.isHoldingBow();
	}

	protected boolean isHoldingBow() {
		return this.mob.isHolding(Items.BOW);
	}

	@Override
	public boolean canContinueToUse() {
		return (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingBow();
	}

	@Override
	public void start() {
		super.start();
		this.mob.setAggressive(true);
	}

	@Override
	public void stop() {
		super.stop();
		this.mob.setAggressive(false);
		this.seeTime = 0;
		this.attackTime = -1;
		this.mob.stopUsingItem();
	}

	@Override
	public void tick() {
		LivingEntity livingEntity = this.mob.getTarget();
		if (livingEntity != null) {
			double d = this.mob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
			boolean bl = this.mob.getSensing().canSee(livingEntity);
			boolean bl2 = this.seeTime > 0;
			if (bl != bl2) {
				this.seeTime = 0;
			}

			if (bl) {
				this.seeTime++;
			} else {
				this.seeTime--;
			}

			if (!(d > (double)this.attackRadiusSqr) && this.seeTime >= 20) {
				this.mob.getNavigation().stop();
				this.strafingTime++;
			} else {
				this.mob.getNavigation().moveTo(livingEntity, this.speedModifier);
				this.strafingTime = -1;
			}

			if (this.strafingTime >= 20) {
				if ((double)this.mob.getRandom().nextFloat() < 0.3) {
					this.strafingClockwise = !this.strafingClockwise;
				}

				if ((double)this.mob.getRandom().nextFloat() < 0.3) {
					this.strafingBackwards = !this.strafingBackwards;
				}

				this.strafingTime = 0;
			}

			if (this.strafingTime > -1) {
				if (d > (double)(this.attackRadiusSqr * 0.75F)) {
					this.strafingBackwards = false;
				} else if (d < (double)(this.attackRadiusSqr * 0.25F)) {
					this.strafingBackwards = true;
				}

				this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
				this.mob.lookAt(livingEntity, 30.0F, 30.0F);
			} else {
				this.mob.getLookControl().setLookAt(livingEntity, 30.0F, 30.0F);
			}

			if (this.mob.isUsingItem()) {
				if (!bl && this.seeTime < -60) {
					this.mob.stopUsingItem();
				} else if (bl) {
					int i = this.mob.getTicksUsingItem();
					if (i >= 20) {
						this.mob.stopUsingItem();
						this.mob.performRangedAttack(livingEntity, BowItem.getPowerForTime(i));
						this.attackTime = this.attackIntervalMin;
					}
				}
			} else if (--this.attackTime <= 0 && this.seeTime >= -60) {
				this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.BOW));
			}
		}
	}
}
