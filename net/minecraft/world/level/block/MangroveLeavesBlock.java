/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MangroveLeavesBlock
extends LeavesBlock
implements BonemealableBlock {
    public static final int GROWTH_CHANCE = 5;

    public MangroveLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return true;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        super.randomTick(blockState, serverLevel, blockPos, random);
        if (random.nextInt(5) != 0 || blockState.getValue(PERSISTENT).booleanValue() || this.decaying(blockState)) {
            return;
        }
        BlockPos blockPos2 = blockPos.below();
        if (serverLevel.getBlockState(blockPos2).isAir() && serverLevel.getBlockState(blockPos2.below()).isAir() && !MangroveLeavesBlock.isTooCloseToAnotherPropagule(serverLevel, blockPos2)) {
            serverLevel.setBlockAndUpdate(blockPos2, MangrovePropaguleBlock.createNewHangingPropagule());
        }
    }

    private static boolean isTooCloseToAnotherPropagule(LevelAccessor levelAccessor, BlockPos blockPos) {
        Iterable<BlockPos> iterable = BlockPos.betweenClosed(blockPos.above().north().east(), blockPos.below().south().west());
        for (BlockPos blockPos2 : iterable) {
            if (!levelAccessor.getBlockState(blockPos2).is(Blocks.MANGROVE_PROPAGULE)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
        return blockGetter.getBlockState(blockPos.below()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        serverLevel.setBlock(blockPos.below(), MangrovePropaguleBlock.createNewHangingPropagule(), 2);
    }
}

