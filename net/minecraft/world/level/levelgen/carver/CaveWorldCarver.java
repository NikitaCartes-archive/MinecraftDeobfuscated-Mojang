/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.Dynamic;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.ProbabilityFeatureConfiguration;

public class CaveWorldCarver
extends WorldCarver<ProbabilityFeatureConfiguration> {
    public CaveWorldCarver(Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration> function, int i) {
        super(function, i);
    }

    @Override
    public boolean isStartChunk(Random random, int i, int j, ProbabilityFeatureConfiguration probabilityFeatureConfiguration) {
        return random.nextFloat() <= probabilityFeatureConfiguration.probability;
    }

    @Override
    public boolean carve(ChunkAccess chunkAccess, Random random, int i, int j, int k, int l, int m, BitSet bitSet, ProbabilityFeatureConfiguration probabilityFeatureConfiguration) {
        int n = (this.getRange() * 2 - 1) * 16;
        int o = random.nextInt(random.nextInt(random.nextInt(this.getCaveBound()) + 1) + 1);
        for (int p = 0; p < o; ++p) {
            float h;
            double d = j * 16 + random.nextInt(16);
            double e = this.getCaveY(random);
            double f = k * 16 + random.nextInt(16);
            int q = 1;
            if (random.nextInt(4) == 0) {
                double g = 0.5;
                h = 1.0f + random.nextFloat() * 6.0f;
                this.genRoom(chunkAccess, random.nextLong(), i, l, m, d, e, f, h, 0.5, bitSet);
                q += random.nextInt(4);
            }
            for (int r = 0; r < q; ++r) {
                float s = random.nextFloat() * ((float)Math.PI * 2);
                h = (random.nextFloat() - 0.5f) / 4.0f;
                float t = this.getThickness(random);
                int u = n - random.nextInt(n / 4);
                boolean v = false;
                this.genTunnel(chunkAccess, random.nextLong(), i, l, m, d, e, f, t, s, h, 0, u, this.getYScale(), bitSet);
            }
        }
        return true;
    }

    protected int getCaveBound() {
        return 15;
    }

    protected float getThickness(Random random) {
        float f = random.nextFloat() * 2.0f + random.nextFloat();
        if (random.nextInt(10) == 0) {
            f *= random.nextFloat() * random.nextFloat() * 3.0f + 1.0f;
        }
        return f;
    }

    protected double getYScale() {
        return 1.0;
    }

    protected int getCaveY(Random random) {
        return random.nextInt(random.nextInt(120) + 8);
    }

    protected void genRoom(ChunkAccess chunkAccess, long l, int i, int j, int k, double d, double e, double f, float g, double h, BitSet bitSet) {
        double m = 1.5 + (double)(Mth.sin(1.5707964f) * g);
        double n = m * h;
        this.carveSphere(chunkAccess, l, i, j, k, d + 1.0, e, f, m, n, bitSet);
    }

    protected void genTunnel(ChunkAccess chunkAccess, long l, int i, int j, int k, double d, double e, double f, float g, float h, float m, int n, int o, double p, BitSet bitSet) {
        Random random = new Random(l);
        int q = random.nextInt(o / 2) + o / 4;
        boolean bl = random.nextInt(6) == 0;
        float r = 0.0f;
        float s = 0.0f;
        for (int t = n; t < o; ++t) {
            double u = 1.5 + (double)(Mth.sin((float)Math.PI * (float)t / (float)o) * g);
            double v = u * p;
            float w = Mth.cos(m);
            d += (double)(Mth.cos(h) * w);
            e += (double)Mth.sin(m);
            f += (double)(Mth.sin(h) * w);
            m *= bl ? 0.92f : 0.7f;
            m += s * 0.1f;
            h += r * 0.1f;
            s *= 0.9f;
            r *= 0.75f;
            s += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f;
            r += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f;
            if (t == q && g > 1.0f) {
                this.genTunnel(chunkAccess, random.nextLong(), i, j, k, d, e, f, random.nextFloat() * 0.5f + 0.5f, h - 1.5707964f, m / 3.0f, t, o, 1.0, bitSet);
                this.genTunnel(chunkAccess, random.nextLong(), i, j, k, d, e, f, random.nextFloat() * 0.5f + 0.5f, h + 1.5707964f, m / 3.0f, t, o, 1.0, bitSet);
                return;
            }
            if (random.nextInt(4) == 0) continue;
            if (!this.canReach(j, k, d, f, t, o, g)) {
                return;
            }
            this.carveSphere(chunkAccess, l, i, j, k, d, e, f, u, v, bitSet);
        }
    }

    @Override
    protected boolean skip(double d, double e, double f, int i) {
        return e <= -0.7 || d * d + e * e + f * f >= 1.0;
    }
}

