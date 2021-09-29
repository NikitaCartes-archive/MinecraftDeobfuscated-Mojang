/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public class CanyonWorldCarver
extends WorldCarver<CanyonCarverConfiguration> {
    public CanyonWorldCarver(Codec<CanyonCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean isStartChunk(CanyonCarverConfiguration canyonCarverConfiguration, Random random) {
        return random.nextFloat() <= canyonCarverConfiguration.probability;
    }

    @Override
    public boolean carve(CarvingContext carvingContext, CanyonCarverConfiguration canyonCarverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Biome> function, Random random, Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {
        int i = (this.getRange() * 2 - 1) * 16;
        double d = chunkPos.getBlockX(random.nextInt(16));
        int j = canyonCarverConfiguration.y.sample(random, carvingContext);
        double e = chunkPos.getBlockZ(random.nextInt(16));
        float f = random.nextFloat() * ((float)Math.PI * 2);
        float g = canyonCarverConfiguration.verticalRotation.sample(random);
        double h = canyonCarverConfiguration.yScale.sample(random);
        float k = canyonCarverConfiguration.shape.thickness.sample(random);
        int l = (int)((float)i * canyonCarverConfiguration.shape.distanceFactor.sample(random));
        boolean m = false;
        this.doCarve(carvingContext, canyonCarverConfiguration, chunkAccess, function, random.nextLong(), aquifer, d, j, e, k, f, g, 0, l, h, carvingMask);
        return true;
    }

    private void doCarve(CarvingContext carvingContext2, CanyonCarverConfiguration canyonCarverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Biome> function, long l, Aquifer aquifer, double d2, double e2, double f2, float g, float h, float i2, int j, int k, double m, CarvingMask carvingMask) {
        Random random = new Random(l);
        float[] fs = this.initWidthFactors(carvingContext2, canyonCarverConfiguration, random);
        float n = 0.0f;
        float o = 0.0f;
        for (int p = j; p < k; ++p) {
            double q = 1.5 + (double)(Mth.sin((float)p * (float)Math.PI / (float)k) * g);
            double r = q * m;
            q *= (double)canyonCarverConfiguration.shape.horizontalRadiusFactor.sample(random);
            r = this.updateVerticalRadius(canyonCarverConfiguration, random, r, k, p);
            float s = Mth.cos(i2);
            float t = Mth.sin(i2);
            d2 += (double)(Mth.cos(h) * s);
            e2 += (double)t;
            f2 += (double)(Mth.sin(h) * s);
            i2 *= 0.7f;
            i2 += o * 0.05f;
            h += n * 0.05f;
            o *= 0.8f;
            n *= 0.5f;
            o += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f;
            n += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f;
            if (random.nextInt(4) == 0) continue;
            if (!CanyonWorldCarver.canReach(chunkAccess.getPos(), d2, f2, p, k, g)) {
                return;
            }
            this.carveEllipsoid(carvingContext2, canyonCarverConfiguration, chunkAccess, function, aquifer, d2, e2, f2, q, r, carvingMask, (carvingContext, d, e, f, i) -> this.shouldSkip(carvingContext, fs, d, e, f, i));
        }
    }

    private float[] initWidthFactors(CarvingContext carvingContext, CanyonCarverConfiguration canyonCarverConfiguration, Random random) {
        int i = carvingContext.getGenDepth();
        float[] fs = new float[i];
        float f = 1.0f;
        for (int j = 0; j < i; ++j) {
            if (j == 0 || random.nextInt(canyonCarverConfiguration.shape.widthSmoothness) == 0) {
                f = 1.0f + random.nextFloat() * random.nextFloat();
            }
            fs[j] = f * f;
        }
        return fs;
    }

    private double updateVerticalRadius(CanyonCarverConfiguration canyonCarverConfiguration, Random random, double d, float f, float g) {
        float h = 1.0f - Mth.abs(0.5f - g / f) * 2.0f;
        float i = canyonCarverConfiguration.shape.verticalRadiusDefaultFactor + canyonCarverConfiguration.shape.verticalRadiusCenterFactor * h;
        return (double)i * d * (double)Mth.randomBetween(random, 0.75f, 1.0f);
    }

    private boolean shouldSkip(CarvingContext carvingContext, float[] fs, double d, double e, double f, int i) {
        int j = i - carvingContext.getMinGenY();
        return (d * d + f * f) * (double)fs[j - 1] + e * e / 6.0 >= 1.0;
    }
}

