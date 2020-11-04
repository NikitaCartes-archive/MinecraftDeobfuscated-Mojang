/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.material.Material;

public class IcebergFeature
extends Feature<BlockStateConfiguration> {
    public IcebergFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, BlockStateConfiguration blockStateConfiguration) {
        boolean bl3;
        int s;
        int r;
        int q;
        int p;
        int l;
        blockPos = new BlockPos(blockPos.getX(), chunkGenerator.getSeaLevel(), blockPos.getZ());
        boolean bl = random.nextDouble() > 0.7;
        BlockState blockState = blockStateConfiguration.state;
        double d = random.nextDouble() * 2.0 * Math.PI;
        int i = 11 - random.nextInt(5);
        int j = 3 + random.nextInt(3);
        boolean bl2 = random.nextDouble() > 0.7;
        int k = 11;
        int n = l = bl2 ? random.nextInt(6) + 6 : random.nextInt(15) + 3;
        if (!bl2 && random.nextDouble() > 0.9) {
            l += random.nextInt(19) + 7;
        }
        int m = Math.min(l + random.nextInt(11), 18);
        int n2 = Math.min(l + random.nextInt(7) - random.nextInt(5), 11);
        int o = bl2 ? i : 11;
        for (p = -o; p < o; ++p) {
            for (q = -o; q < o; ++q) {
                for (r = 0; r < l; ++r) {
                    int n3 = s = bl2 ? this.heightDependentRadiusEllipse(r, l, n2) : this.heightDependentRadiusRound(random, r, l, n2);
                    if (!bl2 && p >= s) continue;
                    this.generateIcebergBlock(worldGenLevel, random, blockPos, l, p, r, q, s, o, bl2, j, d, bl, blockState);
                }
            }
        }
        this.smooth(worldGenLevel, blockPos, n2, l, bl2, i);
        for (p = -o; p < o; ++p) {
            for (q = -o; q < o; ++q) {
                for (r = -1; r > -m; --r) {
                    s = bl2 ? Mth.ceil((float)o * (1.0f - (float)Math.pow(r, 2.0) / ((float)m * 8.0f))) : o;
                    int t = this.heightDependentRadiusSteep(random, -r, m, n2);
                    if (p >= t) continue;
                    this.generateIcebergBlock(worldGenLevel, random, blockPos, m, p, r, q, t, s, bl2, j, d, bl, blockState);
                }
            }
        }
        boolean bl4 = bl2 ? random.nextDouble() > 0.1 : (bl3 = random.nextDouble() > 0.7);
        if (bl3) {
            this.generateCutOut(random, worldGenLevel, n2, l, blockPos, bl2, i, d, j);
        }
        return true;
    }

    private void generateCutOut(Random random, LevelAccessor levelAccessor, int i, int j, BlockPos blockPos, boolean bl, int k, double d, int l) {
        int r;
        int q;
        int m = random.nextBoolean() ? -1 : 1;
        int n = random.nextBoolean() ? -1 : 1;
        int o = random.nextInt(Math.max(i / 2 - 2, 1));
        if (random.nextBoolean()) {
            o = i / 2 + 1 - random.nextInt(Math.max(i - i / 2 - 1, 1));
        }
        int p = random.nextInt(Math.max(i / 2 - 2, 1));
        if (random.nextBoolean()) {
            p = i / 2 + 1 - random.nextInt(Math.max(i - i / 2 - 1, 1));
        }
        if (bl) {
            o = p = random.nextInt(Math.max(k - 5, 1));
        }
        BlockPos blockPos2 = new BlockPos(m * o, 0, n * p);
        double e = bl ? d + 1.5707963267948966 : random.nextDouble() * 2.0 * Math.PI;
        for (q = 0; q < j - 3; ++q) {
            r = this.heightDependentRadiusRound(random, q, j, i);
            this.carve(r, q, blockPos, levelAccessor, false, e, blockPos2, k, l);
        }
        for (q = -1; q > -j + random.nextInt(5); --q) {
            r = this.heightDependentRadiusSteep(random, -q, j, i);
            this.carve(r, q, blockPos, levelAccessor, true, e, blockPos2, k, l);
        }
    }

    private void carve(int i, int j, BlockPos blockPos, LevelAccessor levelAccessor, boolean bl, double d, BlockPos blockPos2, int k, int l) {
        int m = i + 1 + k / 3;
        int n = Math.min(i - 3, 3) + l / 2 - 1;
        for (int o = -m; o < m; ++o) {
            for (int p = -m; p < m; ++p) {
                BlockPos blockPos3;
                BlockState blockState;
                double e = this.signedDistanceEllipse(o, p, blockPos2, m, n, d);
                if (!(e < 0.0) || !IcebergFeature.isIcebergState(blockState = levelAccessor.getBlockState(blockPos3 = blockPos.offset(o, j, p))) && !blockState.is(Blocks.SNOW_BLOCK)) continue;
                if (bl) {
                    this.setBlock(levelAccessor, blockPos3, Blocks.WATER.defaultBlockState());
                    continue;
                }
                this.setBlock(levelAccessor, blockPos3, Blocks.AIR.defaultBlockState());
                this.removeFloatingSnowLayer(levelAccessor, blockPos3);
            }
        }
    }

    private void removeFloatingSnowLayer(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (levelAccessor.getBlockState(blockPos.above()).is(Blocks.SNOW)) {
            this.setBlock(levelAccessor, blockPos.above(), Blocks.AIR.defaultBlockState());
        }
    }

    private void generateIcebergBlock(LevelAccessor levelAccessor, Random random, BlockPos blockPos, int i, int j, int k, int l, int m, int n, boolean bl, int o, double d, boolean bl2, BlockState blockState) {
        double e;
        double d2 = e = bl ? this.signedDistanceEllipse(j, l, BlockPos.ZERO, n, this.getEllipseC(k, i, o), d) : this.signedDistanceCircle(j, l, BlockPos.ZERO, m, random);
        if (e < 0.0) {
            double f;
            BlockPos blockPos2 = blockPos.offset(j, k, l);
            double d3 = f = bl ? -0.5 : (double)(-6 - random.nextInt(3));
            if (e > f && random.nextDouble() > 0.9) {
                return;
            }
            this.setIcebergBlock(blockPos2, levelAccessor, random, i - k, i, bl, bl2, blockState);
        }
    }

    private void setIcebergBlock(BlockPos blockPos, LevelAccessor levelAccessor, Random random, int i, int j, boolean bl, boolean bl2, BlockState blockState) {
        BlockState blockState2 = levelAccessor.getBlockState(blockPos);
        if (blockState2.getMaterial() == Material.AIR || blockState2.is(Blocks.SNOW_BLOCK) || blockState2.is(Blocks.ICE) || blockState2.is(Blocks.WATER)) {
            int k;
            boolean bl3 = !bl || random.nextDouble() > 0.05;
            int n = k = bl ? 3 : 2;
            if (bl2 && !blockState2.is(Blocks.WATER) && (double)i <= (double)random.nextInt(Math.max(1, j / k)) + (double)j * 0.6 && bl3) {
                this.setBlock(levelAccessor, blockPos, Blocks.SNOW_BLOCK.defaultBlockState());
            } else {
                this.setBlock(levelAccessor, blockPos, blockState);
            }
        }
    }

    private int getEllipseC(int i, int j, int k) {
        int l = k;
        if (i > 0 && j - i <= 3) {
            l -= 4 - (j - i);
        }
        return l;
    }

    private double signedDistanceCircle(int i, int j, BlockPos blockPos, int k, Random random) {
        float f = 10.0f * Mth.clamp(random.nextFloat(), 0.2f, 0.8f) / (float)k;
        return (double)f + Math.pow(i - blockPos.getX(), 2.0) + Math.pow(j - blockPos.getZ(), 2.0) - Math.pow(k, 2.0);
    }

    private double signedDistanceEllipse(int i, int j, BlockPos blockPos, int k, int l, double d) {
        return Math.pow(((double)(i - blockPos.getX()) * Math.cos(d) - (double)(j - blockPos.getZ()) * Math.sin(d)) / (double)k, 2.0) + Math.pow(((double)(i - blockPos.getX()) * Math.sin(d) + (double)(j - blockPos.getZ()) * Math.cos(d)) / (double)l, 2.0) - 1.0;
    }

    private int heightDependentRadiusRound(Random random, int i, int j, int k) {
        float f = 3.5f - random.nextFloat();
        float g = (1.0f - (float)Math.pow(i, 2.0) / ((float)j * f)) * (float)k;
        if (j > 15 + random.nextInt(5)) {
            int l = i < 3 + random.nextInt(6) ? i / 2 : i;
            g = (1.0f - (float)l / ((float)j * f * 0.4f)) * (float)k;
        }
        return Mth.ceil(g / 2.0f);
    }

    private int heightDependentRadiusEllipse(int i, int j, int k) {
        float f = 1.0f;
        float g = (1.0f - (float)Math.pow(i, 2.0) / ((float)j * 1.0f)) * (float)k;
        return Mth.ceil(g / 2.0f);
    }

    private int heightDependentRadiusSteep(Random random, int i, int j, int k) {
        float f = 1.0f + random.nextFloat() / 2.0f;
        float g = (1.0f - (float)i / ((float)j * f)) * (float)k;
        return Mth.ceil(g / 2.0f);
    }

    private static boolean isIcebergState(BlockState blockState) {
        return blockState.is(Blocks.PACKED_ICE) || blockState.is(Blocks.SNOW_BLOCK) || blockState.is(Blocks.BLUE_ICE);
    }

    private boolean belowIsAir(BlockGetter blockGetter, BlockPos blockPos) {
        return blockGetter.getBlockState(blockPos.below()).getMaterial() == Material.AIR;
    }

    private void smooth(LevelAccessor levelAccessor, BlockPos blockPos, int i, int j, boolean bl, int k) {
        int l = bl ? k : i / 2;
        for (int m = -l; m <= l; ++m) {
            for (int n = -l; n <= l; ++n) {
                for (int o = 0; o <= j; ++o) {
                    BlockPos blockPos2 = blockPos.offset(m, o, n);
                    BlockState blockState = levelAccessor.getBlockState(blockPos2);
                    if (!IcebergFeature.isIcebergState(blockState) && !blockState.is(Blocks.SNOW)) continue;
                    if (this.belowIsAir(levelAccessor, blockPos2)) {
                        this.setBlock(levelAccessor, blockPos2, Blocks.AIR.defaultBlockState());
                        this.setBlock(levelAccessor, blockPos2.above(), Blocks.AIR.defaultBlockState());
                        continue;
                    }
                    if (!IcebergFeature.isIcebergState(blockState)) continue;
                    BlockState[] blockStates = new BlockState[]{levelAccessor.getBlockState(blockPos2.west()), levelAccessor.getBlockState(blockPos2.east()), levelAccessor.getBlockState(blockPos2.north()), levelAccessor.getBlockState(blockPos2.south())};
                    int p = 0;
                    for (BlockState blockState2 : blockStates) {
                        if (IcebergFeature.isIcebergState(blockState2)) continue;
                        ++p;
                    }
                    if (p < 3) continue;
                    this.setBlock(levelAccessor, blockPos2, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }
}

