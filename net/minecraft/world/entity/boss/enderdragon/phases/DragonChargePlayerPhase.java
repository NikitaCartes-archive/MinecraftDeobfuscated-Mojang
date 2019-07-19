/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DragonChargePlayerPhase
extends AbstractDragonPhaseInstance {
    private static final Logger LOGGER = LogManager.getLogger();
    private Vec3 targetLocation;
    private int timeSinceCharge;

    public DragonChargePlayerPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public void doServerTick() {
        if (this.targetLocation == null) {
            LOGGER.warn("Aborting charge player as no target was set.");
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            return;
        }
        if (this.timeSinceCharge > 0 && this.timeSinceCharge++ >= 10) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            return;
        }
        double d = this.targetLocation.distanceToSqr(this.dragon.x, this.dragon.y, this.dragon.z);
        if (d < 100.0 || d > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            ++this.timeSinceCharge;
        }
    }

    @Override
    public void begin() {
        this.targetLocation = null;
        this.timeSinceCharge = 0;
    }

    public void setTarget(Vec3 vec3) {
        this.targetLocation = vec3;
    }

    @Override
    public float getFlySpeed() {
        return 3.0f;
    }

    @Override
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    public EnderDragonPhase<DragonChargePlayerPhase> getPhase() {
        return EnderDragonPhase.CHARGING_PLAYER;
    }
}

