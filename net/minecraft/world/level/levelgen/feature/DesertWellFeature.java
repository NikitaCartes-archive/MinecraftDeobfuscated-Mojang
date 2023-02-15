/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.loot.packs.UpdateOneTwentyBuiltInLootTables;
import net.minecraft.util.RandomSource;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.apache.commons.lang3.mutable.MutableInt;

public class DesertWellFeature
extends Feature<NoneFeatureConfiguration> {
    private static final BlockStatePredicate IS_SAND = BlockStatePredicate.forBlock(Blocks.SAND);
    private final BlockState sandSlab = Blocks.SANDSTONE_SLAB.defaultBlockState();
    private final BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
    private final BlockState water = Blocks.WATER.defaultBlockState();

    public DesertWellFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        int i;
        int j;
        int i2;
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        BlockPos blockPos = featurePlaceContext.origin();
        blockPos = blockPos.above();
        while (worldGenLevel.isEmptyBlock(blockPos) && blockPos.getY() > worldGenLevel.getMinBuildHeight() + 2) {
            blockPos = blockPos.below();
        }
        if (!IS_SAND.test(worldGenLevel.getBlockState(blockPos))) {
            return false;
        }
        for (i2 = -2; i2 <= 2; ++i2) {
            for (j = -2; j <= 2; ++j) {
                if (!worldGenLevel.isEmptyBlock(blockPos.offset(i2, -1, j)) || !worldGenLevel.isEmptyBlock(blockPos.offset(i2, -2, j))) continue;
                return false;
            }
        }
        for (i2 = -1; i2 <= 0; ++i2) {
            for (j = -2; j <= 2; ++j) {
                for (int k = -2; k <= 2; ++k) {
                    worldGenLevel.setBlock(blockPos.offset(j, i2, k), this.sandstone, 2);
                }
            }
        }
        if (worldGenLevel.enabledFeatures().contains(FeatureFlags.UPDATE_1_20)) {
            DesertWellFeature.placeSandFloor(worldGenLevel, blockPos, featurePlaceContext.random());
        }
        worldGenLevel.setBlock(blockPos, this.water, 2);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            worldGenLevel.setBlock(blockPos.relative(direction), this.water, 2);
        }
        for (i = -2; i <= 2; ++i) {
            for (int j2 = -2; j2 <= 2; ++j2) {
                if (i != -2 && i != 2 && j2 != -2 && j2 != 2) continue;
                worldGenLevel.setBlock(blockPos.offset(i, 1, j2), this.sandstone, 2);
            }
        }
        worldGenLevel.setBlock(blockPos.offset(2, 1, 0), this.sandSlab, 2);
        worldGenLevel.setBlock(blockPos.offset(-2, 1, 0), this.sandSlab, 2);
        worldGenLevel.setBlock(blockPos.offset(0, 1, 2), this.sandSlab, 2);
        worldGenLevel.setBlock(blockPos.offset(0, 1, -2), this.sandSlab, 2);
        for (i = -1; i <= 1; ++i) {
            for (int j3 = -1; j3 <= 1; ++j3) {
                if (i == 0 && j3 == 0) {
                    worldGenLevel.setBlock(blockPos.offset(i, 4, j3), this.sandstone, 2);
                    continue;
                }
                worldGenLevel.setBlock(blockPos.offset(i, 4, j3), this.sandSlab, 2);
            }
        }
        for (i = 1; i <= 3; ++i) {
            worldGenLevel.setBlock(blockPos.offset(-1, i, -1), this.sandstone, 2);
            worldGenLevel.setBlock(blockPos.offset(-1, i, 1), this.sandstone, 2);
            worldGenLevel.setBlock(blockPos.offset(1, i, -1), this.sandstone, 2);
            worldGenLevel.setBlock(blockPos.offset(1, i, 1), this.sandstone, 2);
        }
        return true;
    }

    private static void placeSandFloor(WorldGenLevel worldGenLevel, BlockPos blockPos2, RandomSource randomSource) {
        BlockPos blockPos22 = blockPos2.offset(0, -1, 0);
        ObjectArrayList objectArrayList2 = Util.make(new ObjectArrayList(), objectArrayList -> {
            objectArrayList.add(blockPos22.east());
            objectArrayList.add(blockPos22.south());
            objectArrayList.add(blockPos22.west());
            objectArrayList.add(blockPos22.north());
        });
        Util.shuffle(objectArrayList2, randomSource);
        MutableInt mutableInt = new MutableInt(randomSource.nextInt(2, 4));
        Stream.concat(Stream.of(blockPos22), objectArrayList2.stream()).forEach(blockPos -> {
            if (mutableInt.getAndDecrement() > 0) {
                worldGenLevel.setBlock((BlockPos)blockPos, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 3);
                worldGenLevel.getBlockEntity((BlockPos)blockPos, BlockEntityType.SUSPICIOUS_SAND).ifPresent(suspiciousSandBlockEntity -> suspiciousSandBlockEntity.setLootTable(UpdateOneTwentyBuiltInLootTables.DESERT_WELL_ARCHAEOLOGY, blockPos.asLong()));
            } else {
                worldGenLevel.setBlock((BlockPos)blockPos, Blocks.SAND.defaultBlockState(), 3);
            }
        });
    }
}

