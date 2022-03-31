/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public class SculkBlock
extends DropExperienceBlock
implements SculkBehaviour {
    public SculkBlock(BlockBehaviour.Properties properties) {
        super(properties, ConstantInt.of(1));
    }

    @Override
    public int attemptUseCharge(SculkSpreader.ChargeCursor chargeCursor, LevelAccessor levelAccessor, BlockPos blockPos, Random random, SculkSpreader sculkSpreader, boolean bl) {
        int i = chargeCursor.getCharge();
        if (i == 0 || random.nextInt(sculkSpreader.chargeDecayRate()) != 0) {
            return i;
        }
        BlockPos blockPos2 = chargeCursor.getPos();
        boolean bl2 = blockPos2.closerThan(blockPos, sculkSpreader.noGrowthRadius());
        if (bl2 || !SculkBlock.canPlaceGrowth(levelAccessor, blockPos2)) {
            if (random.nextInt(sculkSpreader.additionalDecayRate()) != 0) {
                return i;
            }
            return i - (bl2 ? 1 : SculkBlock.getDecayPenalty(sculkSpreader, blockPos2, blockPos, i));
        }
        int j = sculkSpreader.growthSpawnCost();
        if (random.nextInt(j) < i) {
            BlockPos blockPos3 = blockPos2.above();
            BlockState blockState = this.getRandomGrowthState(levelAccessor, blockPos3, random, sculkSpreader.isWorldGeneration());
            levelAccessor.setBlock(blockPos3, blockState, 3);
            levelAccessor.playSound(null, blockPos2, blockState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return Math.max(0, i - j);
    }

    private static int getDecayPenalty(SculkSpreader sculkSpreader, BlockPos blockPos, BlockPos blockPos2, int i) {
        int j = sculkSpreader.noGrowthRadius();
        float f = Mth.square((float)Math.sqrt(blockPos.distSqr(blockPos2)) - (float)j);
        int k = Mth.square(24 - j);
        float g = Math.min(1.0f, f / (float)k);
        return Math.max(1, (int)((float)i * g * 0.5f));
    }

    private BlockState getRandomGrowthState(LevelAccessor levelAccessor, BlockPos blockPos, Random random, boolean bl) {
        BlockState blockState = random.nextInt(11) == 0 ? (BlockState)Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, bl) : Blocks.SCULK_SENSOR.defaultBlockState();
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && !levelAccessor.getFluidState(blockPos).isEmpty()) {
            return (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, true);
        }
        return blockState;
    }

    private static boolean canPlaceGrowth(LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos.above());
        if (!(blockState.isAir() || blockState.is(Blocks.WATER) && blockState.getFluidState().is(Fluids.WATER))) {
            return false;
        }
        int i = 0;
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-4, 0, -4), blockPos.offset(4, 2, 4))) {
            BlockState blockState2 = levelAccessor.getBlockState(blockPos2);
            if (blockState2.is(Blocks.SCULK_SENSOR) || blockState2.is(Blocks.SCULK_SHRIEKER)) {
                ++i;
            }
            if (i <= 2) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean canChangeBlockStateOnSpread() {
        return false;
    }
}

