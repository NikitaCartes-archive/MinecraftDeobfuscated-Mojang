/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public abstract class DoorInteractGoal
extends Goal {
    protected Mob mob;
    protected BlockPos doorPos = BlockPos.ZERO;
    protected boolean hasDoor;
    private boolean passed;
    private float doorOpenDirX;
    private float doorOpenDirZ;

    public DoorInteractGoal(Mob mob) {
        this.mob = mob;
        if (!(mob.getNavigation() instanceof GroundPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
        }
    }

    protected boolean isOpen() {
        if (!this.hasDoor) {
            return false;
        }
        BlockState blockState = this.mob.level.getBlockState(this.doorPos);
        if (!(blockState.getBlock() instanceof DoorBlock)) {
            this.hasDoor = false;
            return false;
        }
        return blockState.getValue(DoorBlock.OPEN);
    }

    protected void setOpen(boolean bl) {
        BlockState blockState;
        if (this.hasDoor && (blockState = this.mob.level.getBlockState(this.doorPos)).getBlock() instanceof DoorBlock) {
            ((DoorBlock)blockState.getBlock()).setOpen(this.mob.level, this.doorPos, bl);
        }
    }

    @Override
    public boolean canUse() {
        if (!this.mob.horizontalCollision) {
            return false;
        }
        GroundPathNavigation groundPathNavigation = (GroundPathNavigation)this.mob.getNavigation();
        Path path = groundPathNavigation.getPath();
        if (path == null || path.isDone() || !groundPathNavigation.canOpenDoors()) {
            return false;
        }
        for (int i = 0; i < Math.min(path.getIndex() + 2, path.getSize()); ++i) {
            Node node = path.get(i);
            this.doorPos = new BlockPos(node.x, node.y + 1, node.z);
            if (this.mob.distanceToSqr(this.doorPos.getX(), this.mob.getY(), this.doorPos.getZ()) > 2.25) continue;
            this.hasDoor = DoorBlock.isWoodenDoor(this.mob.level, this.doorPos);
            if (!this.hasDoor) continue;
            return true;
        }
        this.doorPos = this.mob.blockPosition().above();
        this.hasDoor = DoorBlock.isWoodenDoor(this.mob.level, this.doorPos);
        return this.hasDoor;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.passed;
    }

    @Override
    public void start() {
        this.passed = false;
        this.doorOpenDirX = (float)((double)((float)this.doorPos.getX() + 0.5f) - this.mob.getX());
        this.doorOpenDirZ = (float)((double)((float)this.doorPos.getZ() + 0.5f) - this.mob.getZ());
    }

    @Override
    public void tick() {
        float g;
        float f = (float)((double)((float)this.doorPos.getX() + 0.5f) - this.mob.getX());
        float h = this.doorOpenDirX * f + this.doorOpenDirZ * (g = (float)((double)((float)this.doorPos.getZ() + 0.5f) - this.mob.getZ()));
        if (h < 0.0f) {
            this.passed = true;
        }
    }
}

