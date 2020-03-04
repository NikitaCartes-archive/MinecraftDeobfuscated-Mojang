/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeepingVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class WeepingVinesFeature
extends Feature<NoneFeatureConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public WeepingVinesFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        if (!levelAccessor.isEmptyBlock(blockPos)) {
            return false;
        }
        Block block = levelAccessor.getBlockState(blockPos.above()).getBlock();
        if (block != Blocks.NETHERRACK && block != Blocks.NETHER_WART_BLOCK) {
            return false;
        }
        this.placeRoofNetherWart(levelAccessor, random, blockPos);
        this.placeRoofWeepingVines(levelAccessor, random, blockPos);
        return true;
    }

    private void placeRoofNetherWart(LevelAccessor levelAccessor, Random random, BlockPos blockPos) {
        levelAccessor.setBlock(blockPos, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 200; ++i) {
            mutableBlockPos.setWithOffset(blockPos, random.nextInt(6) - random.nextInt(6), random.nextInt(2) - random.nextInt(5), random.nextInt(6) - random.nextInt(6));
            if (!levelAccessor.isEmptyBlock(mutableBlockPos)) continue;
            int j = 0;
            for (Direction direction : DIRECTIONS) {
                Block block = levelAccessor.getBlockState(mutableBlockPos2.setWithOffset(mutableBlockPos, direction)).getBlock();
                if (block == Blocks.NETHERRACK || block == Blocks.NETHER_WART_BLOCK) {
                    ++j;
                }
                if (j > 1) break;
            }
            if (j != true) continue;
            levelAccessor.setBlock(mutableBlockPos, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
        }
    }

    private void placeRoofWeepingVines(LevelAccessor levelAccessor, Random random, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 100; ++i) {
            Block block;
            mutableBlockPos.setWithOffset(blockPos, random.nextInt(8) - random.nextInt(8), random.nextInt(2) - random.nextInt(7), random.nextInt(8) - random.nextInt(8));
            if (!levelAccessor.isEmptyBlock(mutableBlockPos) || (block = levelAccessor.getBlockState(mutableBlockPos.above()).getBlock()) != Blocks.NETHERRACK && block != Blocks.NETHER_WART_BLOCK) continue;
            int j = Mth.nextInt(random, 1, 8);
            if (random.nextInt(6) == 0) {
                j *= 2;
            }
            if (random.nextInt(5) == 0) {
                j = 1;
            }
            int k = 17;
            int l = 25;
            WeepingVinesFeature.placeWeepingVinesColumn(levelAccessor, random, mutableBlockPos, j, 17, 25);
        }
    }

    public static void placeWeepingVinesColumn(LevelAccessor levelAccessor, Random random, BlockPos.MutableBlockPos mutableBlockPos, int i, int j, int k) {
        for (int l = 0; l <= i; ++l) {
            if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
                if (l == i) {
                    levelAccessor.setBlock(mutableBlockPos, (BlockState)Blocks.WEEPING_VINES.defaultBlockState().setValue(WeepingVines.AGE, Mth.nextInt(random, j, k)), 2);
                } else {
                    levelAccessor.setBlock(mutableBlockPos, Blocks.WEEPING_VINES_PLANT.defaultBlockState(), 2);
                }
            } else if (levelAccessor.getBlockState(mutableBlockPos.above()).getBlock() == Blocks.WEEPING_VINES_PLANT) {
                levelAccessor.setBlock(mutableBlockPos.above(), (BlockState)Blocks.WEEPING_VINES.defaultBlockState().setValue(WeepingVines.AGE, Mth.nextInt(random, j, k)), 2);
                break;
            }
            mutableBlockPos.move(Direction.DOWN);
        }
    }
}

