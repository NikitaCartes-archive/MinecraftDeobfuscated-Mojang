/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public class CaveWorldCarver
extends WorldCarver<CaveCarverConfiguration> {
    public CaveWorldCarver(Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration caveCarverConfiguration, RandomSource randomSource) {
        return randomSource.nextFloat() <= caveCarverConfiguration.probability;
    }

    @Override
    public boolean carve(CarvingContext carvingContext2, CaveCarverConfiguration caveCarverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Holder<Biome>> function, RandomSource randomSource, Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {
        int i2 = SectionPos.sectionToBlockCoord(this.getRange() * 2 - 1);
        int j = randomSource.nextInt(randomSource.nextInt(randomSource.nextInt(this.getCaveBound()) + 1) + 1);
        for (int k = 0; k < j; ++k) {
            float o;
            double d = chunkPos.getBlockX(randomSource.nextInt(16));
            double e2 = caveCarverConfiguration.y.sample(randomSource, carvingContext2);
            double f2 = chunkPos.getBlockZ(randomSource.nextInt(16));
            double g2 = caveCarverConfiguration.horizontalRadiusMultiplier.sample(randomSource);
            double h = caveCarverConfiguration.verticalRadiusMultiplier.sample(randomSource);
            double l = caveCarverConfiguration.floorLevel.sample(randomSource);
            WorldCarver.CarveSkipChecker carveSkipChecker = (carvingContext, e, f, g, i) -> CaveWorldCarver.shouldSkip(e, f, g, l);
            int m = 1;
            if (randomSource.nextInt(4) == 0) {
                double n = caveCarverConfiguration.yScale.sample(randomSource);
                o = 1.0f + randomSource.nextFloat() * 6.0f;
                this.createRoom(carvingContext2, caveCarverConfiguration, chunkAccess, function, aquifer, d, e2, f2, o, n, carvingMask, carveSkipChecker);
                m += randomSource.nextInt(4);
            }
            for (int p = 0; p < m; ++p) {
                float q = randomSource.nextFloat() * ((float)Math.PI * 2);
                o = (randomSource.nextFloat() - 0.5f) / 4.0f;
                float r = this.getThickness(randomSource);
                int s = i2 - randomSource.nextInt(i2 / 4);
                boolean t = false;
                this.createTunnel(carvingContext2, caveCarverConfiguration, chunkAccess, function, randomSource.nextLong(), aquifer, d, e2, f2, g2, h, r, q, o, 0, s, this.getYScale(), carvingMask, carveSkipChecker);
            }
        }
        return true;
    }

    protected int getCaveBound() {
        return 15;
    }

    protected float getThickness(RandomSource randomSource) {
        float f = randomSource.nextFloat() * 2.0f + randomSource.nextFloat();
        if (randomSource.nextInt(10) == 0) {
            f *= randomSource.nextFloat() * randomSource.nextFloat() * 3.0f + 1.0f;
        }
        return f;
    }

    protected double getYScale() {
        return 1.0;
    }

    protected void createRoom(CarvingContext carvingContext, CaveCarverConfiguration caveCarverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Holder<Biome>> function, Aquifer aquifer, double d, double e, double f, float g, double h, CarvingMask carvingMask, WorldCarver.CarveSkipChecker carveSkipChecker) {
        double i = 1.5 + (double)(Mth.sin(1.5707964f) * g);
        double j = i * h;
        this.carveEllipsoid(carvingContext, caveCarverConfiguration, chunkAccess, function, aquifer, d + 1.0, e, f, i, j, carvingMask, carveSkipChecker);
    }

    protected void createTunnel(CarvingContext carvingContext, CaveCarverConfiguration caveCarverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Holder<Biome>> function, long l, Aquifer aquifer, double d, double e, double f, double g, double h, float i, float j, float k, int m, int n, double o, CarvingMask carvingMask, WorldCarver.CarveSkipChecker carveSkipChecker) {
        RandomSource randomSource = RandomSource.create(l);
        int p = randomSource.nextInt(n / 2) + n / 4;
        boolean bl = randomSource.nextInt(6) == 0;
        float q = 0.0f;
        float r = 0.0f;
        for (int s = m; s < n; ++s) {
            double t = 1.5 + (double)(Mth.sin((float)Math.PI * (float)s / (float)n) * i);
            double u = t * o;
            float v = Mth.cos(k);
            d += (double)(Mth.cos(j) * v);
            e += (double)Mth.sin(k);
            f += (double)(Mth.sin(j) * v);
            k *= bl ? 0.92f : 0.7f;
            k += r * 0.1f;
            j += q * 0.1f;
            r *= 0.9f;
            q *= 0.75f;
            r += (randomSource.nextFloat() - randomSource.nextFloat()) * randomSource.nextFloat() * 2.0f;
            q += (randomSource.nextFloat() - randomSource.nextFloat()) * randomSource.nextFloat() * 4.0f;
            if (s == p && i > 1.0f) {
                this.createTunnel(carvingContext, caveCarverConfiguration, chunkAccess, function, randomSource.nextLong(), aquifer, d, e, f, g, h, randomSource.nextFloat() * 0.5f + 0.5f, j - 1.5707964f, k / 3.0f, s, n, 1.0, carvingMask, carveSkipChecker);
                this.createTunnel(carvingContext, caveCarverConfiguration, chunkAccess, function, randomSource.nextLong(), aquifer, d, e, f, g, h, randomSource.nextFloat() * 0.5f + 0.5f, j + 1.5707964f, k / 3.0f, s, n, 1.0, carvingMask, carveSkipChecker);
                return;
            }
            if (randomSource.nextInt(4) == 0) continue;
            if (!CaveWorldCarver.canReach(chunkAccess.getPos(), d, f, s, n, i)) {
                return;
            }
            this.carveEllipsoid(carvingContext, caveCarverConfiguration, chunkAccess, function, aquifer, d, e, f, t * g, u * h, carvingMask, carveSkipChecker);
        }
    }

    private static boolean shouldSkip(double d, double e, double f, double g) {
        if (e <= g) {
            return true;
        }
        return d * d + e * e + f * f >= 1.0;
    }
}

