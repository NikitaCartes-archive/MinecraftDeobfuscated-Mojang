/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
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
        RandomSource randomSource = featurePlaceContext.random();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        Optional<Block> optional = BuiltInRegistries.BLOCK.getTag(BlockTags.CORAL_BLOCKS).flatMap(named -> named.getRandomElement(randomSource)).map(Holder::value);
        if (optional.isEmpty()) {
            return false;
        }
        return this.placeFeature(worldGenLevel, randomSource, blockPos, optional.get().defaultBlockState());
    }

    protected abstract boolean placeFeature(LevelAccessor var1, RandomSource var2, BlockPos var3, BlockState var4);

    protected boolean placeCoralBlock(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        BlockPos blockPos2 = blockPos.above();
        BlockState blockState2 = levelAccessor.getBlockState(blockPos);
        if (!blockState2.is(Blocks.WATER) && !blockState2.is(BlockTags.CORALS) || !levelAccessor.getBlockState(blockPos2).is(Blocks.WATER)) {
            return false;
        }
        levelAccessor.setBlock(blockPos, blockState, 3);
        if (randomSource.nextFloat() < 0.25f) {
            BuiltInRegistries.BLOCK.getTag(BlockTags.CORALS).flatMap(named -> named.getRandomElement(randomSource)).map(Holder::value).ifPresent(block -> levelAccessor.setBlock(blockPos2, block.defaultBlockState(), 2));
        } else if (randomSource.nextFloat() < 0.05f) {
            levelAccessor.setBlock(blockPos2, (BlockState)Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, randomSource.nextInt(4) + 1), 2);
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos3;
            if (!(randomSource.nextFloat() < 0.2f) || !levelAccessor.getBlockState(blockPos3 = blockPos.relative(direction)).is(Blocks.WATER)) continue;
            BuiltInRegistries.BLOCK.getTag(BlockTags.WALL_CORALS).flatMap(named -> named.getRandomElement(randomSource)).map(Holder::value).ifPresent(block -> {
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

