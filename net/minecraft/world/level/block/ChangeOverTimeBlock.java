/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface ChangeOverTimeBlock<T extends Enum<T>> {
    public static final int SCAN_DISTANCE = 4;

    public Optional<BlockState> getNext(BlockState var1);

    public float getChanceModifier();

    default public void onRandomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        float f = 0.05688889f;
        if (random.nextFloat() < 0.05688889f) {
            this.applyChangeOverTime(blockState, serverLevel, blockPos, random);
        }
    }

    public T getAge();

    default public void applyChangeOverTime(BlockState blockState2, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        BlockPos blockPos2;
        int l;
        int i = ((Enum)this.getAge()).ordinal();
        int j = 0;
        int k = 0;
        Iterator<BlockPos> iterator = BlockPos.withinManhattan(blockPos, 4, 4, 4).iterator();
        while (iterator.hasNext() && (l = (blockPos2 = iterator.next()).distManhattan(blockPos)) <= 4) {
            BlockState blockState22;
            Block block;
            if (blockPos2.equals(blockPos) || !((block = (blockState22 = serverLevel.getBlockState(blockPos2)).getBlock()) instanceof ChangeOverTimeBlock)) continue;
            T enum_ = ((ChangeOverTimeBlock)((Object)block)).getAge();
            if (this.getAge().getClass() != enum_.getClass()) continue;
            int m = ((Enum)enum_).ordinal();
            if (m < i) {
                return;
            }
            if (m > i) {
                ++k;
                continue;
            }
            ++j;
        }
        float f = (float)(k + 1) / (float)(k + j + 1);
        float g = f * f * this.getChanceModifier();
        if (random.nextFloat() < g) {
            this.getNext(blockState2).ifPresent(blockState -> serverLevel.setBlockAndUpdate(blockPos, (BlockState)blockState));
        }
    }
}

