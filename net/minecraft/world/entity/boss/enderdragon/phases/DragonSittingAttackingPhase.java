/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;

public class DragonSittingAttackingPhase
extends AbstractDragonSittingPhase {
    private int attackingTicks;

    public DragonSittingAttackingPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public void doClientTick() {
        this.dragon.level.playLocalSound(this.dragon.x, this.dragon.y, this.dragon.z, SoundEvents.ENDER_DRAGON_GROWL, this.dragon.getSoundSource(), 2.5f, 0.8f + this.dragon.getRandom().nextFloat() * 0.3f, false);
    }

    @Override
    public void doServerTick() {
        if (this.attackingTicks++ >= 40) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_FLAMING);
        }
    }

    @Override
    public void begin() {
        this.attackingTicks = 0;
    }

    public EnderDragonPhase<DragonSittingAttackingPhase> getPhase() {
        return EnderDragonPhase.SITTING_ATTACKING;
    }
}

