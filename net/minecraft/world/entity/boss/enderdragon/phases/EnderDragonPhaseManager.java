/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EnderDragonPhaseManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final EnderDragon dragon;
    private final DragonPhaseInstance[] phases = new DragonPhaseInstance[EnderDragonPhase.getCount()];
    @Nullable
    private DragonPhaseInstance currentPhase;

    public EnderDragonPhaseManager(EnderDragon enderDragon) {
        this.dragon = enderDragon;
        this.setPhase(EnderDragonPhase.HOVERING);
    }

    public void setPhase(EnderDragonPhase<?> enderDragonPhase) {
        if (this.currentPhase != null && enderDragonPhase == this.currentPhase.getPhase()) {
            return;
        }
        if (this.currentPhase != null) {
            this.currentPhase.end();
        }
        this.currentPhase = this.getPhase(enderDragonPhase);
        if (!this.dragon.level.isClientSide) {
            this.dragon.getEntityData().set(EnderDragon.DATA_PHASE, enderDragonPhase.getId());
        }
        LOGGER.debug("Dragon is now in phase {} on the {}", (Object)enderDragonPhase, (Object)(this.dragon.level.isClientSide ? "client" : "server"));
        this.currentPhase.begin();
    }

    public DragonPhaseInstance getCurrentPhase() {
        return this.currentPhase;
    }

    public <T extends DragonPhaseInstance> T getPhase(EnderDragonPhase<T> enderDragonPhase) {
        int i = enderDragonPhase.getId();
        if (this.phases[i] == null) {
            this.phases[i] = enderDragonPhase.createInstance(this.dragon);
        }
        return (T)this.phases[i];
    }
}

