/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DragonLandingPhase
extends AbstractDragonPhaseInstance {
    @Nullable
    private Vec3 targetLocation;

    public DragonLandingPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public void doClientTick() {
        Vec3 vec3 = this.dragon.getHeadLookVector(1.0f).normalize();
        vec3.yRot(-0.7853982f);
        double d = this.dragon.head.getX();
        double e = this.dragon.head.getY(0.5);
        double f = this.dragon.head.getZ();
        for (int i = 0; i < 8; ++i) {
            RandomSource randomSource = this.dragon.getRandom();
            double g = d + randomSource.nextGaussian() / 2.0;
            double h = e + randomSource.nextGaussian() / 2.0;
            double j = f + randomSource.nextGaussian() / 2.0;
            Vec3 vec32 = this.dragon.getDeltaMovement();
            this.dragon.level.addParticle(ParticleTypes.DRAGON_BREATH, g, h, j, -vec3.x * (double)0.08f + vec32.x, -vec3.y * (double)0.3f + vec32.y, -vec3.z * (double)0.08f + vec32.z);
            vec3.yRot(0.19634955f);
        }
    }

    @Override
    public void doServerTick() {
        if (this.targetLocation == null) {
            this.targetLocation = Vec3.atBottomCenterOf(this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION));
        }
        if (this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()) < 1.0) {
            this.dragon.getPhaseManager().getPhase(EnderDragonPhase.SITTING_FLAMING).resetFlameCount();
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_SCANNING);
        }
    }

    @Override
    public float getFlySpeed() {
        return 1.5f;
    }

    @Override
    public float getTurnSpeed() {
        float f = (float)this.dragon.getDeltaMovement().horizontalDistance() + 1.0f;
        float g = Math.min(f, 40.0f);
        return g / f;
    }

    @Override
    public void begin() {
        this.targetLocation = null;
    }

    @Override
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    public EnderDragonPhase<DragonLandingPhase> getPhase() {
        return EnderDragonPhase.LANDING;
    }
}

