/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DragonLandingApproachPhase
extends AbstractDragonPhaseInstance {
    private static final TargetingConditions NEAR_EGG_TARGETING = new TargetingConditions().range(128.0);
    private Path currentPath;
    private Vec3 targetLocation;

    public DragonLandingApproachPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    public EnderDragonPhase<DragonLandingApproachPhase> getPhase() {
        return EnderDragonPhase.LANDING_APPROACH;
    }

    @Override
    public void begin() {
        this.currentPath = null;
        this.targetLocation = null;
    }

    @Override
    public void doServerTick() {
        double d;
        double d2 = d = this.targetLocation == null ? 0.0 : this.targetLocation.distanceToSqr(this.dragon.x, this.dragon.y, this.dragon.z);
        if (d < 100.0 || d > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            this.findNewTarget();
        }
    }

    @Override
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    private void findNewTarget() {
        if (this.currentPath == null || this.currentPath.isDone()) {
            int j;
            int i = this.dragon.findClosestNode();
            BlockPos blockPos = this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
            Player player = this.dragon.level.getNearestPlayer(NEAR_EGG_TARGETING, blockPos.getX(), blockPos.getY(), blockPos.getZ());
            if (player != null) {
                Vec3 vec3 = new Vec3(player.x, 0.0, player.z).normalize();
                j = this.dragon.findClosestNode(-vec3.x * 40.0, 105.0, -vec3.z * 40.0);
            } else {
                j = this.dragon.findClosestNode(40.0, blockPos.getY(), 0.0);
            }
            Node node = new Node(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            this.currentPath = this.dragon.findPath(i, j, node);
            if (this.currentPath != null) {
                this.currentPath.next();
            }
        }
        this.navigateToNextPathNode();
        if (this.currentPath != null && this.currentPath.isDone()) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING);
        }
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isDone()) {
            double f;
            Vec3 vec3 = this.currentPath.currentPos();
            this.currentPath.next();
            double d = vec3.x;
            double e = vec3.z;
            while ((f = vec3.y + (double)(this.dragon.getRandom().nextFloat() * 20.0f)) < vec3.y) {
            }
            this.targetLocation = new Vec3(d, f, e);
        }
    }
}

