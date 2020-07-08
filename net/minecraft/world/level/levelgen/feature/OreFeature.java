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
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, OreConfiguration oreConfiguration) {
        float f = random.nextFloat() * (float)Math.PI;
        float g = (float)oreConfiguration.size / 8.0f;
        int i = Mth.ceil(((float)oreConfiguration.size / 16.0f * 2.0f + 1.0f) / 2.0f);
        double d = (double)blockPos.getX() + Math.sin(f) * (double)g;
        double e = (double)blockPos.getX() - Math.sin(f) * (double)g;
        double h = (double)blockPos.getZ() + Math.cos(f) * (double)g;
        double j = (double)blockPos.getZ() - Math.cos(f) * (double)g;
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
            v = random.nextDouble() * (double)p / 16.0;
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
        for (q = 0; q < p; ++q) {
            double y = ds[q * 4 + 3];
            if (y < 0.0) continue;
            double z = ds[q * 4 + 0];
            double aa = ds[q * 4 + 1];
            double ab = ds[q * 4 + 2];
            int ac = Math.max(Mth.floor(z - y), j);
            int ad = Math.max(Mth.floor(aa - y), k);
            int ae = Math.max(Mth.floor(ab - y), l);
            int af = Math.max(Mth.floor(z + y), ac);
            int ag = Math.max(Mth.floor(aa + y), ad);
            int ah = Math.max(Mth.floor(ab + y), ae);
            for (int ai = ac; ai <= af; ++ai) {
                double aj = ((double)ai + 0.5 - z) / y;
                if (!(aj * aj < 1.0)) continue;
                for (int ak = ad; ak <= ag; ++ak) {
                    double al = ((double)ak + 0.5 - aa) / y;
                    if (!(aj * aj + al * al < 1.0)) continue;
                    for (int am = ae; am <= ah; ++am) {
                        int ao;
                        double an = ((double)am + 0.5 - ab) / y;
                        if (!(aj * aj + al * al + an * an < 1.0) || bitSet.get(ao = ai - j + (ak - k) * m + (am - l) * m * n)) continue;
                        bitSet.set(ao);
                        mutableBlockPos.set(ai, ak, am);
                        if (!oreConfiguration.target.test(levelAccessor.getBlockState(mutableBlockPos), random)) continue;
                        levelAccessor.setBlock(mutableBlockPos, oreConfiguration.state, 2);
                        ++o;
                    }
                }
            }
        }
        return o > 0;
    }
}

