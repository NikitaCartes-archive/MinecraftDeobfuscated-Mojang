/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SmallFireball
extends Fireball {
    public SmallFireball(EntityType<? extends SmallFireball> entityType, Level level) {
        super((EntityType<? extends Fireball>)entityType, level);
    }

    public SmallFireball(Level level, LivingEntity livingEntity, double d, double e, double f) {
        super((EntityType<? extends Fireball>)EntityType.SMALL_FIREBALL, livingEntity, d, e, f, level);
    }

    public SmallFireball(Level level, double d, double e, double f, double g, double h, double i) {
        super((EntityType<? extends Fireball>)EntityType.SMALL_FIREBALL, d, e, f, g, h, i, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.level.isClientSide) {
            return;
        }
        Entity entity = entityHitResult.getEntity();
        if (!entity.fireImmune()) {
            Entity entity2 = this.getOwner();
            int i = entity.getRemainingFireTicks();
            entity.setSecondsOnFire(5);
            boolean bl = entity.hurt(DamageSource.fireball(this, entity2), 5.0f);
            if (!bl) {
                entity.setRemainingFireTicks(i);
            } else if (entity2 instanceof LivingEntity) {
                this.doEnchantDamageEffects((LivingEntity)entity2, entity);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockPos blockPos;
        super.onHitBlock(blockHitResult);
        if (this.level.isClientSide) {
            return;
        }
        Entity entity = this.getOwner();
        if ((!(entity instanceof Mob) || this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) && this.level.isEmptyBlock(blockPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection()))) {
            this.level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level, blockPos));
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level.isClientSide) {
            this.discard();
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        return false;
    }
}

