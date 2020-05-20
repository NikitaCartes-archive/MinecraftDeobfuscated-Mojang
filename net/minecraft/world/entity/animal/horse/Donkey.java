/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.horse;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class Donkey
extends AbstractChestedHorse {
    public Donkey(EntityType<? extends Donkey> entityType, Level level) {
        super((EntityType<? extends AbstractChestedHorse>)entityType, level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        return SoundEvents.DONKEY_AMBIENT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        super.getAngrySound();
        return SoundEvents.DONKEY_ANGRY;
    }

    @Override
    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.DONKEY_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getEatingSound() {
        return SoundEvents.DONKEY_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        super.getHurtSound(damageSource);
        return SoundEvents.DONKEY_HURT;
    }

    @Override
    public boolean canMate(Animal animal) {
        if (animal == this) {
            return false;
        }
        if (animal instanceof Donkey || animal instanceof Horse) {
            return this.canParent() && ((AbstractHorse)animal).canParent();
        }
        return false;
    }

    @Override
    public AgableMob getBreedOffspring(AgableMob agableMob) {
        EntityType<AbstractChestedHorse> entityType = agableMob instanceof Horse ? EntityType.MULE : EntityType.DONKEY;
        AbstractHorse abstractHorse = entityType.create(this.level);
        this.setOffspringAttributes(agableMob, abstractHorse);
        return abstractHorse;
    }
}

