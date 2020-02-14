/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ProjectileWeaponItem;

public class MeleeAttack
extends Behavior<Mob> {
    private final double attackRange;
    private final int cooldown;
    private int remainingCooldown = 0;

    public MeleeAttack(double d, int i) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.attackRange = d;
        this.cooldown = i;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Mob mob) {
        if (this.remainingCooldown > 0) {
            --this.remainingCooldown;
            return false;
        }
        return !this.isHoldingProjectileWeapon(mob) && BehaviorUtils.isAttackTargetVisibleAndInRange(mob, this.attackRange);
    }

    private boolean isHoldingProjectileWeapon(Mob mob) {
        return mob.isHolding(item -> item instanceof ProjectileWeaponItem);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
        return BehaviorUtils.isAttackTargetVisibleAndInRange(mob, this.attackRange);
    }

    @Override
    protected void start(ServerLevel serverLevel, Mob mob, long l) {
        LivingEntity livingEntity = MeleeAttack.getAttackTarget(mob);
        BehaviorUtils.lookAtEntity(mob, livingEntity);
        this.meleeAttack(mob, livingEntity);
        this.remainingCooldown = this.cooldown;
    }

    private void meleeAttack(Mob mob, LivingEntity livingEntity) {
        mob.swing(InteractionHand.MAIN_HAND);
        mob.doHurtTarget(livingEntity);
    }

    private static LivingEntity getAttackTarget(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Mob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Mob)livingEntity, l);
    }
}

