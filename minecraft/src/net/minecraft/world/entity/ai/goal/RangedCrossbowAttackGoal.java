package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RangedCrossbowAttackGoal<T extends Monster & RangedAttackMob & CrossbowAttackMob> extends Goal {
	public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
	private final T mob;
	private RangedCrossbowAttackGoal.CrossbowState crossbowState = RangedCrossbowAttackGoal.CrossbowState.UNCHARGED;
	private final double speedModifier;
	private final float attackRadiusSqr;
	private int seeTime;
	private int attackDelay;
	private int updatePathDelay;

	public RangedCrossbowAttackGoal(T monster, double d, float f) {
		this.mob = monster;
		this.speedModifier = d;
		this.attackRadiusSqr = f * f;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return this.isValidTarget() && this.isHoldingCrossbow();
	}

	private boolean isHoldingCrossbow() {
		return this.mob.isHolding(Items.CROSSBOW);
	}

	@Override
	public boolean canContinueToUse() {
		return this.isValidTarget() && (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingCrossbow();
	}

	private boolean isValidTarget() {
		return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
	}

	@Override
	public void stop() {
		super.stop();
		this.mob.setAggressive(false);
		this.mob.setTarget(null);
		this.seeTime = 0;
		if (this.mob.isUsingItem()) {
			this.mob.stopUsingItem();
			this.mob.setChargingCrossbow(false);
			CrossbowItem.setCharged(this.mob.getUseItem(), false);
		}
	}

	@Override
	public void tick() {
		LivingEntity livingEntity = this.mob.getTarget();
		if (livingEntity != null) {
			boolean bl = this.mob.getSensing().hasLineOfSight(livingEntity);
			boolean bl2 = this.seeTime > 0;
			if (bl != bl2) {
				this.seeTime = 0;
			}

			if (bl) {
				this.seeTime++;
			} else {
				this.seeTime--;
			}

			double d = this.mob.distanceToSqr(livingEntity);
			boolean bl3 = (d > (double)this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;
			if (bl3) {
				this.updatePathDelay--;
				if (this.updatePathDelay <= 0) {
					this.mob.getNavigation().moveTo(livingEntity, this.canRun() ? this.speedModifier : this.speedModifier * 0.5);
					this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.mob.getRandom());
				}
			} else {
				this.updatePathDelay = 0;
				this.mob.getNavigation().stop();
			}

			this.mob.getLookControl().setLookAt(livingEntity, 30.0F, 30.0F);
			if (this.crossbowState == RangedCrossbowAttackGoal.CrossbowState.UNCHARGED) {
				if (!bl3) {
					this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.CROSSBOW));
					this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.CHARGING;
					this.mob.setChargingCrossbow(true);
				}
			} else if (this.crossbowState == RangedCrossbowAttackGoal.CrossbowState.CHARGING) {
				if (!this.mob.isUsingItem()) {
					this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.UNCHARGED;
				}

				int i = this.mob.getTicksUsingItem();
				ItemStack itemStack = this.mob.getUseItem();
				if (i >= CrossbowItem.getChargeDuration(itemStack)) {
					this.mob.releaseUsingItem();
					this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.CHARGED;
					this.attackDelay = 20 + this.mob.getRandom().nextInt(20);
					this.mob.setChargingCrossbow(false);
				}
			} else if (this.crossbowState == RangedCrossbowAttackGoal.CrossbowState.CHARGED) {
				this.attackDelay--;
				if (this.attackDelay == 0) {
					this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.READY_TO_ATTACK;
				}
			} else if (this.crossbowState == RangedCrossbowAttackGoal.CrossbowState.READY_TO_ATTACK && bl) {
				this.mob.performRangedAttack(livingEntity, 1.0F);
				ItemStack itemStack2 = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.CROSSBOW));
				CrossbowItem.setCharged(itemStack2, false);
				this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.UNCHARGED;
			}
		}
	}

	private boolean canRun() {
		return this.crossbowState == RangedCrossbowAttackGoal.CrossbowState.UNCHARGED;
	}

	static enum CrossbowState {
		UNCHARGED,
		CHARGING,
		CHARGED,
		READY_TO_ATTACK;
	}
}
