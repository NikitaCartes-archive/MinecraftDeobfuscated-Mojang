package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;

public class CrossbowAttack<E extends Mob & CrossbowAttackMob, T extends LivingEntity> extends Behavior<E> {
	private static final int TIMEOUT = 1200;
	private int attackDelay;
	private CrossbowAttack.CrossbowState crossbowState = CrossbowAttack.CrossbowState.UNCHARGED;

	public CrossbowAttack() {
		super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1200);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E mob) {
		LivingEntity livingEntity = getAttackTarget(mob);
		return mob.isHolding(Items.CROSSBOW) && BehaviorUtils.canSee(mob, livingEntity) && BehaviorUtils.isWithinAttackRange(mob, livingEntity, 0);
	}

	protected boolean canStillUse(ServerLevel serverLevel, E mob, long l) {
		return mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions(serverLevel, mob);
	}

	protected void tick(ServerLevel serverLevel, E mob, long l) {
		LivingEntity livingEntity = getAttackTarget(mob);
		this.lookAtTarget(mob, livingEntity);
		this.crossbowAttack(mob, livingEntity);
	}

	protected void stop(ServerLevel serverLevel, E mob, long l) {
		if (mob.isUsingItem()) {
			mob.stopUsingItem();
		}

		if (mob.isHolding(Items.CROSSBOW)) {
			mob.setChargingCrossbow(false);
			mob.getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
		}
	}

	private void crossbowAttack(E mob, LivingEntity livingEntity) {
		if (this.crossbowState == CrossbowAttack.CrossbowState.UNCHARGED) {
			mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(mob, Items.CROSSBOW));
			this.crossbowState = CrossbowAttack.CrossbowState.CHARGING;
			mob.setChargingCrossbow(true);
		} else if (this.crossbowState == CrossbowAttack.CrossbowState.CHARGING) {
			if (!mob.isUsingItem()) {
				this.crossbowState = CrossbowAttack.CrossbowState.UNCHARGED;
			}

			int i = mob.getTicksUsingItem();
			ItemStack itemStack = mob.getUseItem();
			if (i >= CrossbowItem.getChargeDuration(itemStack, mob)) {
				mob.releaseUsingItem();
				this.crossbowState = CrossbowAttack.CrossbowState.CHARGED;
				this.attackDelay = 20 + mob.getRandom().nextInt(20);
				mob.setChargingCrossbow(false);
			}
		} else if (this.crossbowState == CrossbowAttack.CrossbowState.CHARGED) {
			this.attackDelay--;
			if (this.attackDelay == 0) {
				this.crossbowState = CrossbowAttack.CrossbowState.READY_TO_ATTACK;
			}
		} else if (this.crossbowState == CrossbowAttack.CrossbowState.READY_TO_ATTACK) {
			mob.performRangedAttack(livingEntity, 1.0F);
			this.crossbowState = CrossbowAttack.CrossbowState.UNCHARGED;
		}
	}

	private void lookAtTarget(Mob mob, LivingEntity livingEntity) {
		mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntity, true));
	}

	private static LivingEntity getAttackTarget(LivingEntity livingEntity) {
		return (LivingEntity)livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
	}

	static enum CrossbowState {
		UNCHARGED,
		CHARGING,
		CHARGED,
		READY_TO_ATTACK;
	}
}
