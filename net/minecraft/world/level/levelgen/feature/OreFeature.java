/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class OreFeature
extends Feature<OreConfiguration> {
    public OreFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> featurePlaceContext) {
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        OreConfiguration oreConfiguration = featurePlaceContext.config();
        float f = randomSource.nextFloat() * (float)Math.PI;
        float g = (float)oreConfiguration.size / 8.0f;
        int i = Mth.ceil(((float)oreConfiguration.size / 16.0f * 2.0f + 1.0f) / 2.0f);
        double d = (double)blockPos.getX() + Math.sin(f) * (double)g;
        double e = (double)blockPos.getX() - Math.sin(f) * (double)g;
        double h = (double)blockPos.getZ() + Math.cos(f) * (double)g;
        double j = (double)blockPos.getZ() - Math.cos(f) * (double)g;
        int k = 2;
        double l = blockPos.getY() + randomSource.nextInt(3) - 2;
        double m = blockPos.getY() + randomSource.nextInt(3) - 2;
        int n = blockPos.getX() - Mth.ceil(g) - i;
        int o = blockPos.getY() - 2 - i;
        int p = blockPos.getZ() - Mth.ceil(g) - i;
        int q = 2 * (Mth.ceil(g) + i);
        int r = 2 * (2 + i);
        for (int s = n; s <= n + q; ++s) {
            for (int t = p; t <= p + q; ++t) {
                if (o > worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, s, t)) continue;
                return this.doPlace(worldGenLevel, randomSource, oreConfiguration, d, e, h, j, l, m, n, o, p, q, r);
            }
        }
        return false;
    }

    protected boolean doPlace(WorldGenLevel worldGenLevel, RandomSource randomSource, OreConfiguration oreConfiguration, double d, double e, double f, double g, double h, double i, int j, int k, int l, int m, int n) {
        double v;
        double u;
        double t;
        double s;
        int q;
        int o = 0;
        BitSet bitSet = new BitSet(m * n * m);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int p = oreConfiguration.size;
        double[] ds = new double[p * 4];
        for (q = 0; q < p; ++q) {
            float r = (float)q / (float)p;
            s = Mth.lerp((double)r, d, e);
            t = Mth.lerp((double)r, h, i);
            u = Mth.lerp((double)r, f, g);
            v = randomSource.nextDouble() * (double)p / 16.0;
            double w = ((double)(Mth.sin((float)Math.PI * r) + 1.0f) * v + 1.0) / 2.0;
            ds[q * 4 + 0] = s;
            ds[q * 4 + 1] = t;
            ds[q * 4 + 2] = u;
            ds[q * 4 + 3] = w;
        }
        for (q = 0; q < p - 1; ++q) {
            if (ds[q * 4 + 3] <= 0.0) continue;
            for (int x = q + 1; x < p; ++x) {
                if (ds[x * 4 + 3] <= 0.0 || !((v = ds[q * 4 + 3] - ds[x * 4 + 3]) * v > (s = ds[q * 4 + 0] - ds[x * 4 + 0]) * s + (t = ds[q * 4 + 1] - ds[x * 4 + 1]) * t + (u = ds[q * 4 + 2] - ds[x * 4 + 2]) * u)) continue;
                if (v > 0.0) {
                    ds[x * 4 + 3] = -1.0;
                    continue;
                }
                ds[q * 4 + 3] = -1.0;
            }
        }
        try (BulkSectionAccess bulkSectionAccess = new BulkSectionAccess(worldGenLevel);){
            for (int x = 0; x < p; ++x) {
                s = ds[x * 4 + 3];
                if (s < 0.0) continue;
                t = ds[x * 4 + 0];
                u = ds[x * 4 + 1];
                v = ds[x * 4 + 2];
                int y = Math.max(Mth.floor(t - s), j);
                int z = Math.max(Mth.floor(u - s), k);
                int aa = Math.max(Mth.floor(v - s), l);
                int ab = Math.max(Mth.floor(t + s), y);
                int ac = Math.max(Mth.floor(u + s), z);
                int ad = Math.max(Mth.floor(v + s), aa);
                for (int ae = y; ae <= ab; ++ae) {
                    double af = ((double)ae + 0.5 - t) / s;
                    if (!(af * af < 1.0)) continue;
                    for (int ag = z; ag <= ac; ++ag) {
                        double ah = ((double)ag + 0.5 - u) / s;
                        if (!(af * af + ah * ah < 1.0)) continue;
                        block11: for (int ai = aa; ai <= ad; ++ai) {
                            LevelChunkSection levelChunkSection;
                            int ak;
                            double aj = ((double)ai + 0.5 - v) / s;
                            if (!(af * af + ah * ah + aj * aj < 1.0) || worldGenLevel.isOutsideBuildHeight(ag) || bitSet.get(ak = ae - j + (ag - k) * m + (ai - l) * m * n)) continue;
                            bitSet.set(ak);
                            mutableBlockPos.set(ae, ag, ai);
                            if (!worldGenLevel.ensureCanWrite(mutableBlockPos) || (levelChunkSection = bulkSectionAccess.getSection(mutableBlockPos)) == null) continue;
                            int al = SectionPos.sectionRelative(ae);
                            int am = SectionPos.sectionRelative(ag);
                            int an = SectionPos.sectionRelative(ai);
                            BlockState blockState = levelChunkSection.getBlockState(al, am, an);
                            for (OreConfiguration.TargetBlockState targetBlockState : oreConfiguration.targetStates) {
                                if (!OreFeature.canPlaceOre(blockState, bulkSectionAccess::getBlockState, randomSource, oreConfiguration, targetBlockState, mutableBlockPos)) continue;
                                levelChunkSection.setBlockState(al, am, an, targetBlockState.state, false);
                                ++o;
                                continue block11;
                            }
                        }
                    }
                }
            }
        }
        return o > 0;
    }

    public static boolean canPlaceOre(BlockState blockState, Function<BlockPos, BlockState> function, RandomSource randomSource, OreConfiguration oreConfiguration, OreConfiguration.TargetBlockState targetBlockState, BlockPos.MutableBlockPos mutableBlockPos) {
        if (!targetBlockState.target.test(blockState, randomSource)) {
            return false;
        }
        if (OreFeature.shouldSkipAirCheck(randomSource, oreConfiguration.discardChanceOnAirExposure)) {
            return true;
        }
        return !OreFeature.isAdjacentToAir(function, mutableBlockPos);
    }

    protected static boolean shouldSkipAirCheck(RandomSource randomSource, float f) {
        if (f <= 0.0f) {
            return true;
        }
        if (f >= 1.0f) {
            return false;
        }
        return randomSource.nextFloat() >= f;
    }
}

