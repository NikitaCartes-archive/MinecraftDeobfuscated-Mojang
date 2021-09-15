/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class GroundPathNavigation
extends PathNavigation {
    private boolean avoidSun;

    public GroundPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int i) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, i);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.mob.isOnGround() || this.isInLiquid() || this.mob.isPassenger();
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.getSurfaceY(), this.mob.getZ());
    }

    @Override
    public Path createPath(BlockPos blockPos, int i) {
        BlockPos blockPos2;
        if (this.level.getBlockState(blockPos).isAir()) {
            blockPos2 = blockPos.below();
            while (blockPos2.getY() > this.level.getMinBuildHeight() && this.level.getBlockState(blockPos2).isAir()) {
                blockPos2 = blockPos2.below();
            }
            if (blockPos2.getY() > this.level.getMinBuildHeight()) {
                return super.createPath(blockPos2.above(), i);
            }
            while (blockPos2.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockPos2).isAir()) {
                blockPos2 = blockPos2.above();
            }
            blockPos = blockPos2;
        }
        if (this.level.getBlockState(blockPos).getMaterial().isSolid()) {
            blockPos2 = blockPos.above();
            while (blockPos2.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockPos2).getMaterial().isSolid()) {
                blockPos2 = blockPos2.above();
            }
            return super.createPath(blockPos2, i);
        }
        return super.createPath(blockPos, i);
    }

    @Override
    public Path createPath(Entity entity, int i) {
        return this.createPath(entity.blockPosition(), i);
    }

    private int getSurfaceY() {
        if (!this.mob.isInWater() || !this.canFloat()) {
            return Mth.floor(this.mob.getY() + 0.5);
        }
        int i = this.mob.getBlockY();
        BlockState blockState = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)i, this.mob.getZ()));
        int j = 0;
        while (blockState.is(Blocks.WATER)) {
            blockState = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)(++i), this.mob.getZ()));
            if (++j <= 16) continue;
            return this.mob.getBlockY();
        }
        return i;
    }

    @Override
    protected void trimPath() {
        super.trimPath();
        if (this.avoidSun) {
            if (this.level.canSeeSky(new BlockPos(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ()))) {
                return;
            }
            for (int i = 0; i < this.path.getNodeCount(); ++i) {
                Node node = this.path.getNode(i);
                if (!this.level.canSeeSky(new BlockPos(node.x, node.y, node.z))) continue;
                this.path.truncateNodes(i);
                return;
            }
        }
    }

    protected boolean hasValidPathType(BlockPathTypes blockPathTypes) {
        if (blockPathTypes == BlockPathTypes.WATER) {
            return false;
        }
        if (blockPathTypes == BlockPathTypes.LAVA) {
            return false;
        }
        return blockPathTypes != BlockPathTypes.OPEN;
    }

    public void setCanOpenDoors(boolean bl) {
        this.nodeEvaluator.setCanOpenDoors(bl);
    }

    public boolean canPassDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setCanPassDoors(boolean bl) {
        this.nodeEvaluator.setCanPassDoors(bl);
    }

    public boolean canOpenDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setAvoidSun(boolean bl) {
        this.avoidSun = bl;
    }
}

