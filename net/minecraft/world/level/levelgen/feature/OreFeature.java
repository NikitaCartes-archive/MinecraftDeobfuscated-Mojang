/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class OreFeature
extends Feature<OreConfiguration> {
    public OreFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, OreConfiguration oreConfiguration) {
        float f = random.nextFloat() * (float)Math.PI;
        float g = (float)oreConfiguration.size / 8.0f;
        int i = Mth.ceil(((float)oreConfiguration.size / 16.0f * 2.0f + 1.0f) / 2.0f);
        double d = (float)blockPos.getX() + Mth.sin(f) * g;
        double e = (float)blockPos.getX() - Mth.sin(f) * g;
        double h = (float)blockPos.getZ() + Mth.cos(f) * g;
        double j = (float)blockPos.getZ() - Mth.cos(f) * g;
        int k = 2;
        double l = blockPos.getY() + random.nextInt(3) - 2;
        double m = blockPos.getY() + random.nextInt(3) - 2;
        int n = blockPos.getX() - Mth.ceil(g) - i;
        int o = blockPos.getY() - 2 - i;
        int p = blockPos.getZ() - Mth.ceil(g) - i;
        int q = 2 * (Mth.ceil(g) + i);
        int r = 2 * (2 + i);
        for (int s = n; s <= n + q; ++s) {
            for (int t = p; t <= p + q; ++t) {
                if (o > worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, s, t)) continue;
                return this.doPlace(worldGenLevel, random, oreConfiguration, d, e, h, j, l, m, n, o, p, q, r);
            }
        }
        return false;
    }

    protected boolean doPlace(LevelAccessor levelAccessor, Random random, OreConfiguration oreConfiguration, double d, double e, double f, double g, double h, double i, int j, int k, int l, int m, int n) {
        double u;
        double t;
        double s;
        double r;
        int p;
        int o = 0;
        BitSet bitSet = new BitSet(m * n * m);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        double[] ds = new double[oreConfiguration.size * 4];
        for (p = 0; p < oreConfiguration.size; ++p) {
            float q = (float)p / (float)oreConfiguration.size;
            r = Mth.lerp((double)q, d, e);
            s = Mth.lerp((double)q, h, i);
            t = Mth.lerp((double)q, f, g);
            u = random.nextDouble() * (double)oreConfiguration.size / 16.0;
            double v = ((double)(Mth.sin((float)Math.PI * q) + 1.0f) * u + 1.0) / 2.0;
            ds[p * 4 + 0] = r;
            ds[p * 4 + 1] = s;
            ds[p * 4 + 2] = t;
            ds[p * 4 + 3] = v;
        }
        for (p = 0; p < oreConfiguration.size - 1; ++p) {
            if (ds[p * 4 + 3] <= 0.0) continue;
            for (int w = p + 1; w < oreConfiguration.size; ++w) {
                if (ds[w * 4 + 3] <= 0.0 || !((u = ds[p * 4 + 3] - ds[w * 4 + 3]) * u > (r = ds[p * 4 + 0] - ds[w * 4 + 0]) * r + (s = ds[p * 4 + 1] - ds[w * 4 + 1]) * s + (t = ds[p * 4 + 2] - ds[w * 4 + 2]) * t)) continue;
                if (u > 0.0) {
                    ds[w * 4 + 3] = -1.0;
                    continue;
                }
                ds[p * 4 + 3] = -1.0;
            }
        }
        for (p = 0; p < oreConfiguration.size; ++p) {
            double x = ds[p * 4 + 3];
            if (x < 0.0) continue;
            double y = ds[p * 4 + 0];
            double z = ds[p * 4 + 1];
            double aa = ds[p * 4 + 2];
            int ab = Math.max(Mth.floor(y - x), j);
            int ac = Math.max(Mth.floor(z - x), k);
            int ad = Math.max(Mth.floor(aa - x), l);
            int ae = Math.max(Mth.floor(y + x), ab);
            int af = Math.max(Mth.floor(z + x), ac);
            int ag = Math.max(Mth.floor(aa + x), ad);
            for (int ah = ab; ah <= ae; ++ah) {
                double ai = ((double)ah + 0.5 - y) / x;
                if (!(ai * ai < 1.0)) continue;
                for (int aj = ac; aj <= af; ++aj) {
                    double ak = ((double)aj + 0.5 - z) / x;
                    if (!(ai * ai + ak * ak < 1.0)) continue;
                    for (int al = ad; al <= ag; ++al) {
                        int an;
                        double am = ((double)al + 0.5 - aa) / x;
                        if (!(ai * ai + ak * ak + am * am < 1.0) || bitSet.get(an = ah - j + (aj - k) * m + (al - l) * m * n)) continue;
                        bitSet.set(an);
                        mutableBlockPos.set(ah, aj, al);
                        if (!oreConfiguration.target.getPredicate().test(levelAccessor.getBlockState(mutableBlockPos))) continue;
                        levelAccessor.setBlock(mutableBlockPos, oreConfiguration.state, 2);
                        ++o;
                    }
                }
            }
        }
        return o > 0;
    }
}

