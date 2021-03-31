/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WaterAvoidingRandomFlyingGoal
extends WaterAvoidingRandomStrollGoal {
    public WaterAvoidingRandomFlyingGoal(PathfinderMob pathfinderMob, double d) {
        super(pathfinderMob, d);
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        Vec3 vec3 = null;
        if (this.mob.isInWater()) {
            vec3 = LandRandomPos.getPos(this.mob, 15, 15);
        }
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            vec3 = this.getTreePos();
        }
        return vec3 == null ? super.getPosition() : vec3;
    }

    @Nullable
    private Vec3 getTreePos() {
        BlockPos blockPos = this.mob.blockPosition();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        Iterable<BlockPos> iterable = BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 3.0), Mth.floor(this.mob.getY() - 6.0), Mth.floor(this.mob.getZ() - 3.0), Mth.floor(this.mob.getX() + 3.0), Mth.floor(this.mob.getY() + 6.0), Mth.floor(this.mob.getZ() + 3.0));
        for (BlockPos blockPos2 : iterable) {
            BlockState blockState;
            boolean bl;
            if (blockPos.equals(blockPos2) || !(bl = (blockState = this.mob.level.getBlockState(mutableBlockPos2.setWithOffset((Vec3i)blockPos2, Direction.DOWN))).getBlock() instanceof LeavesBlock || blockState.is(BlockTags.LOGS)) || !this.mob.level.isEmptyBlock(blockPos2) || !this.mob.level.isEmptyBlock(mutableBlockPos.setWithOffset((Vec3i)blockPos2, Direction.UP))) continue;
            return Vec3.atBottomCenterOf(blockPos2);
        }
        return null;
    }
}

