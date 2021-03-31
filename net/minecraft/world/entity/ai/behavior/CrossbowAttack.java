/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CrossbowAttack<E extends Mob, T extends LivingEntity>
extends Behavior<E> {
    private static final int TIMEOUT = 1200;
    private int attackDelay;
    private CrossbowState crossbowState = CrossbowState.UNCHARGED;

    public CrossbowAttack() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E mob) {
        LivingEntity livingEntity = CrossbowAttack.getAttackTarget(mob);
        return ((LivingEntity)mob).isHolding(Items.CROSSBOW) && BehaviorUtils.canSee(mob, livingEntity) && BehaviorUtils.isWithinAttackRange(mob, livingEntity, 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, E mob, long l) {
        return ((LivingEntity)mob).getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions(serverLevel, mob);
    }

    @Override
    protected void tick(ServerLevel serverLevel, E mob, long l) {
        LivingEntity livingEntity = CrossbowAttack.getAttackTarget(mob);
        this.lookAtTarget((Mob)mob, livingEntity);
        this.crossbowAttack(mob, livingEntity);
    }

    @Override
    protected void stop(ServerLevel serverLevel, E mob, long l) {
        if (((LivingEntity)mob).isUsingItem()) {
            ((LivingEntity)mob).stopUsingItem();
        }
        if (((LivingEntity)mob).isHolding(Items.CROSSBOW)) {
            ((CrossbowAttackMob)mob).setChargingCrossbow(false);
            CrossbowItem.setCharged(((LivingEntity)mob).getUseItem(), false);
        }
    }

    private void crossbowAttack(E mob, LivingEntity livingEntity) {
        if (this.crossbowState == CrossbowState.UNCHARGED) {
            ((LivingEntity)mob).startUsingItem(ProjectileUtil.getWeaponHoldingHand(mob, Items.CROSSBOW));
            this.crossbowState = CrossbowState.CHARGING;
            ((CrossbowAttackMob)mob).setChargingCrossbow(true);
        } else if (this.crossbowState == CrossbowState.CHARGING) {
            ItemStack itemStack;
            int i;
            if (!((LivingEntity)mob).isUsingItem()) {
                this.crossbowState = CrossbowState.UNCHARGED;
            }
            if ((i = ((LivingEntity)mob).getTicksUsingItem()) >= CrossbowItem.getChargeDuration(itemStack = ((LivingEntity)mob).getUseItem())) {
                ((LivingEntity)mob).releaseUsingItem();
                this.crossbowState = CrossbowState.CHARGED;
                this.attackDelay = 20 + ((LivingEntity)mob).getRandom().nextInt(20);
                ((CrossbowAttackMob)mob).setChargingCrossbow(false);
            }
        } else if (this.crossbowState == CrossbowState.CHARGED) {
            --this.attackDelay;
            if (this.attackDelay == 0) {
                this.crossbowState = CrossbowState.READY_TO_ATTACK;
            }
        } else if (this.crossbowState == CrossbowState.READY_TO_ATTACK) {
            ((RangedAttackMob)mob).performRangedAttack(livingEntity, 1.0f);
            ItemStack itemStack2 = ((LivingEntity)mob).getItemInHand(ProjectileUtil.getWeaponHoldingHand(mob, Items.CROSSBOW));
            CrossbowItem.setCharged(itemStack2, false);
            this.crossbowState = CrossbowState.UNCHARGED;
        }
    }

    private void lookAtTarget(Mob mob, LivingEntity livingEntity) {
        mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntity, true));
    }

    private static LivingEntity getAttackTarget(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (E)((Mob)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (E)((Mob)livingEntity), l);
    }

    static enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;

    }
}

