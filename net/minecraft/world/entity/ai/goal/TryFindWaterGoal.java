/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class TryFindWaterGoal
extends Goal {
    private final PathfinderMob mob;

    public TryFindWaterGoal(PathfinderMob pathfinderMob) {
        this.mob = pathfinderMob;
    }

    @Override
    public boolean canUse() {
        return this.mob.onGround && !this.mob.level.getFluidState(new BlockPos(this.mob)).is(FluidTags.WATER);
    }

    @Override
    public void start() {
        Vec3i blockPos = null;
        Iterable<BlockPos> iterable = BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 2.0), Mth.floor(this.mob.getY() - 2.0), Mth.floor(this.mob.getZ() - 2.0), Mth.floor(this.mob.getX() + 2.0), Mth.floor(this.mob.getY()), Mth.floor(this.mob.getZ() + 2.0));
        for (BlockPos blockPos2 : iterable) {
            if (!this.mob.level.getFluidState(blockPos2).is(FluidTags.WATER)) continue;
            blockPos = blockPos2;
            break;
        }
        if (blockPos != null) {
            this.mob.getMoveControl().setWantedPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0);
        }
    }
}

