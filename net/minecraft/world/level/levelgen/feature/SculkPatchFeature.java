/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration;

public class SculkPatchFeature
extends Feature<SculkPatchConfiguration> {
    public SculkPatchFeature(Codec<SculkPatchConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SculkPatchConfiguration> featurePlaceContext) {
        int l;
        int k;
        BlockPos blockPos;
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        if (!this.canSpreadFrom(worldGenLevel, blockPos = featurePlaceContext.origin())) {
            return false;
        }
        SculkPatchConfiguration sculkPatchConfiguration = featurePlaceContext.config();
        RandomSource randomSource = featurePlaceContext.random();
        SculkSpreader sculkSpreader = SculkSpreader.createWorldGenSpreader();
        int i = sculkPatchConfiguration.spreadRounds() + sculkPatchConfiguration.growthRounds();
        for (int j = 0; j < i; ++j) {
            for (k = 0; k < sculkPatchConfiguration.chargeCount(); ++k) {
                sculkSpreader.addCursors(blockPos, sculkPatchConfiguration.amountPerCharge());
            }
            boolean bl = j < sculkPatchConfiguration.spreadRounds();
            for (l = 0; l < sculkPatchConfiguration.spreadAttempts(); ++l) {
                sculkSpreader.updateCursors(worldGenLevel, blockPos, randomSource, bl);
            }
            sculkSpreader.clear();
        }
        BlockPos blockPos2 = blockPos.below();
        if (randomSource.nextFloat() <= sculkPatchConfiguration.catalystChance() && worldGenLevel.getBlockState(blockPos2).isCollisionShapeFullBlock(worldGenLevel, blockPos2)) {
            worldGenLevel.setBlock(blockPos, Blocks.SCULK_CATALYST.defaultBlockState(), 3);
        }
        k = sculkPatchConfiguration.extraRareGrowths().sample(randomSource);
        for (l = 0; l < k; ++l) {
            BlockPos blockPos3 = blockPos.offset(randomSource.nextInt(5) - 2, 0, randomSource.nextInt(5) - 2);
            if (!worldGenLevel.getBlockState(blockPos3).isAir() || !worldGenLevel.getBlockState(blockPos3.below()).isFaceSturdy(worldGenLevel, blockPos3.below(), Direction.UP)) continue;
            worldGenLevel.setBlock(blockPos3, (BlockState)Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, true), 3);
        }
        return true;
    }

    private boolean canSpreadFrom(LevelAccessor levelAccessor, BlockPos blockPos2) {
        block5: {
            block4: {
                BlockState blockState = levelAccessor.getBlockState(blockPos2);
                if (blockState.getBlock() instanceof SculkBehaviour) {
                    return true;
                }
                if (blockState.isAir()) break block4;
                if (!blockState.is(Blocks.WATER) || !blockState.getFluidState().isSource()) break block5;
            }
            return Direction.stream().map(blockPos2::relative).anyMatch(blockPos -> levelAccessor.getBlockState((BlockPos)blockPos).isCollisionShapeFullBlock(levelAccessor, (BlockPos)blockPos));
        }
        return false;
    }
}

