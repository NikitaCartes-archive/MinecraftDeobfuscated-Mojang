/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Skeleton
extends AbstractSkeleton {
    public Skeleton(EntityType<? extends Skeleton> entityType, Level level) {
        super((EntityType<? extends AbstractSkeleton>)entityType, level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override
    SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
        Creeper creeper;
        super.dropCustomDeathLoot(damageSource, i, bl);
        Entity entity = damageSource.getEntity();
        if (entity instanceof Creeper && (creeper = (Creeper)entity).canDropMobsSkull()) {
            creeper.increaseDroppedSkulls();
            this.spawnAtLocation(Items.SKELETON_SKULL);
        }
    }
}

