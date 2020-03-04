/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DragonDeathPhase
extends AbstractDragonPhaseInstance {
    private Vec3 targetLocation;
    private int time;

    public DragonDeathPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public void doClientTick() {
        if (this.time++ % 10 == 0) {
            float f = (this.dragon.getRandom().nextFloat() - 0.5f) * 8.0f;
            float g = (this.dragon.getRandom().nextFloat() - 0.5f) * 4.0f;
            float h = (this.dragon.getRandom().nextFloat() - 0.5f) * 8.0f;
            this.dragon.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.dragon.getX() + (double)f, this.dragon.getY() + 2.0 + (double)g, this.dragon.getZ() + (double)h, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void doServerTick() {
        double d;
        ++this.time;
        if (this.targetLocation == null) {
            BlockPos blockPos = this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION);
            this.targetLocation = Vec3.atBottomCenterOf(blockPos);
        }
        if ((d = this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ())) < 100.0 || d > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            this.dragon.setHealth(0.0f);
        } else {
            this.dragon.setHealth(1.0f);
        }
    }

    @Override
    public void begin() {
        this.targetLocation = null;
        this.time = 0;
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

    public EnderDragonPhase<DragonDeathPhase> getPhase() {
        return EnderDragonPhase.DYING;
    }
}

