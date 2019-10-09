/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public abstract class AbstractHugeMushroomFeature
extends Feature<HugeMushroomFeatureConfiguration> {
    public AbstractHugeMushroomFeature(Function<Dynamic<?>, ? extends HugeMushroomFeatureConfiguration> function) {
        super(function);
    }

    protected void placeTrunk(LevelAccessor levelAccessor, Random random, BlockPos blockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration, int i, BlockPos.MutableBlockPos mutableBlockPos) {
        for (int j = 0; j < i; ++j) {
            mutableBlockPos.set(blockPos).move(Direction.UP, j);
            if (levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) continue;
            this.setBlock(levelAccessor, mutableBlockPos, hugeMushroomFeatureConfiguration.stemProvider.getState(random, blockPos));
        }
    }

    protected int getTreeHeight(Random random) {
        int i = random.nextInt(3) + 4;
        if (random.nextInt(12) == 0) {
            i *= 2;
        }
        return i;
    }

    protected boolean isValidPosition(LevelAccessor levelAccessor, BlockPos blockPos, int i, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration) {
        int j = blockPos.getY();
        if (j < 1 || j + i + 1 >= 256) {
            return false;
        }
        Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
        if (!AbstractHugeMushroomFeature.isDirt(block)) {
            return false;
        }
        for (int k = 0; k <= i; ++k) {
            int l = this.getTreeRadiusForHeight(-1, -1, hugeMushroomFeatureConfiguration.foliageRadius, k);
            for (int m = -l; m <= l; ++m) {
                for (int n = -l; n <= l; ++n) {
                    BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.set(blockPos).move(m, k, n));
                    if (blockState.isAir() || blockState.is(BlockTags.LEAVES)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration) {
        BlockPos.MutableBlockPos mutableBlockPos;
        int i = this.getTreeHeight(random);
        if (!this.isValidPosition(levelAccessor, blockPos, i, mutableBlockPos = new BlockPos.MutableBlockPos(), hugeMushroomFeatureConfiguration)) {
            return false;
        }
        this.makeCap(levelAccessor, random, blockPos, i, mutableBlockPos, hugeMushroomFeatureConfiguration);
        this.placeTrunk(levelAccessor, random, blockPos, hugeMushroomFeatureConfiguration, i, mutableBlockPos);
        return true;
    }

    protected abstract int getTreeRadiusForHeight(int var1, int var2, int var3, int var4);

    protected abstract void makeCap(LevelAccessor var1, Random var2, BlockPos var3, int var4, BlockPos.MutableBlockPos var5, HugeMushroomFeatureConfiguration var6);
}

