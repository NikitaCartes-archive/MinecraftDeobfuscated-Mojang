/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class CoralFeature
extends Feature<NoneFeatureConfiguration> {
    public CoralFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        Random random = featurePlaceContext.random();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        Optional optional = BlockTags.CORAL_BLOCKS.getRandomElement(random);
        if (optional.isEmpty()) {
            return false;
        }
        return this.placeFeature(worldGenLevel, random, blockPos, ((Block)optional.get()).defaultBlockState());
    }

    protected abstract boolean placeFeature(LevelAccessor var1, Random var2, BlockPos var3, BlockState var4);

    protected boolean placeCoralBlock(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState) {
        BlockPos blockPos2 = blockPos.above();
        BlockState blockState2 = levelAccessor.getBlockState(blockPos);
        if (!blockState2.is(Blocks.WATER) && !blockState2.is(BlockTags.CORALS) || !levelAccessor.getBlockState(blockPos2).is(Blocks.WATER)) {
            return false;
        }
        levelAccessor.setBlock(blockPos, blockState, 3);
        if (random.nextFloat() < 0.25f) {
            BlockTags.CORALS.getRandomElement(random).ifPresent(block -> levelAccessor.setBlock(blockPos2, block.defaultBlockState(), 2));
        } else if (random.nextFloat() < 0.05f) {
            levelAccessor.setBlock(blockPos2, (BlockState)Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, random.nextInt(4) + 1), 2);
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos3;
            if (!(random.nextFloat() < 0.2f) || !levelAccessor.getBlockState(blockPos3 = blockPos.relative(direction)).is(Blocks.WATER)) continue;
            BlockTags.WALL_CORALS.getRandomElement(random).ifPresent(block -> {
                BlockState blockState = block.defaultBlockState();
                if (blockState.hasProperty(BaseCoralWallFanBlock.FACING)) {
                    blockState = (BlockState)blockState.setValue(BaseCoralWallFanBlock.FACING, direction);
                }
                levelAccessor.setBlock(blockPos3, blockState, 2);
            });
        }
        return true;
    }
}

