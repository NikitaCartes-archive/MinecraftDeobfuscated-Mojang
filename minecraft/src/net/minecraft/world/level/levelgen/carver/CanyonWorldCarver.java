package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CanyonWorldCarver extends WorldCarver<CanyonCarverConfiguration> {
	private static final Logger LOGGER = LogManager.getLogger();

	public CanyonWorldCarver(Codec<CanyonCarverConfiguration> codec) {
		super(codec);
	}

	public boolean isStartChunk(CanyonCarverConfiguration canyonCarverConfiguration, Random random) {
		return random.nextFloat() <= canyonCarverConfiguration.probability;
	}

	public boolean carve(
		CarvingContext carvingContext,
		CanyonCarverConfiguration canyonCarverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		Random random,
		int i,
		ChunkPos chunkPos,
		BitSet bitSet
	) {
		int j = (this.getRange() * 2 - 1) * 16;
		double d = (double)chunkPos.getBlockX(random.nextInt(16));
		int k = this.getY(carvingContext, canyonCarverConfiguration, random);
		double e = (double)chunkPos.getBlockZ(random.nextInt(16));
		float f = random.nextFloat() * (float) (Math.PI * 2);
		float g = canyonCarverConfiguration.getVerticalRotation().sample(random);
		double h = (double)canyonCarverConfiguration.getYScale().sample(random);
		float l = canyonCarverConfiguration.getThickness().sample(random);
		int m = (int)((float)j * canyonCarverConfiguration.getDistanceFactor().sample(random));
		int n = 0;
		this.doCarve(carvingContext, canyonCarverConfiguration, chunkAccess, function, random.nextLong(), i, d, (double)k, e, l, f, g, 0, m, h, bitSet);
		return true;
	}

	private void doCarve(
		CarvingContext carvingContext,
		CanyonCarverConfiguration canyonCarverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		long l,
		int i,
		double d,
		double e,
		double f,
		float g,
		float h,
		float j,
		int k,
		int m,
		double n,
		BitSet bitSet
	) {
		Random random = new Random(l);
		float[] fs = this.initWidthFactors(carvingContext, canyonCarverConfiguration, random);
		float o = 0.0F;
		float p = 0.0F;

		for (int q = k; q < m; q++) {
			double r = 1.5 + (double)(Mth.sin((float)q * (float) Math.PI / (float)m) * g);
			double s = r * n;
			r *= (double)canyonCarverConfiguration.getHorizontalRadiusFactor().sample(random);
			s = this.updateVerticalRadius(canyonCarverConfiguration, random, s, (float)m, (float)q);
			float t = Mth.cos(j);
			float u = Mth.sin(j);
			d += (double)(Mth.cos(h) * t);
			e += (double)u;
			f += (double)(Mth.sin(h) * t);
			j *= 0.7F;
			j += p * 0.05F;
			h += o * 0.05F;
			p *= 0.8F;
			o *= 0.5F;
			p += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			o += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
			if (random.nextInt(4) != 0) {
				if (!canReach(chunkAccess.getPos(), d, f, q, m, g)) {
					return;
				}

				this.carveEllipsoid(
					carvingContext,
					canyonCarverConfiguration,
					chunkAccess,
					function,
					l,
					i,
					d,
					e,
					f,
					r,
					s,
					bitSet,
					(carvingContextx, dx, ex, fx, ix) -> this.shouldSkip(carvingContextx, fs, dx, ex, fx, ix)
				);
			}
		}
	}

	private int getY(CarvingContext carvingContext, CanyonCarverConfiguration canyonCarverConfiguration, Random random) {
		int i = canyonCarverConfiguration.getBottomInclusive().resolveY(carvingContext);
		int j = canyonCarverConfiguration.getTopInclusive().resolveY(carvingContext);
		if (i >= j) {
			LOGGER.warn("Empty carver: {} [{}-{}]", this, i, j);
			return i;
		} else {
			return Mth.randomBetweenInclusive(random, i, j);
		}
	}

	private float[] initWidthFactors(CarvingContext carvingContext, CanyonCarverConfiguration canyonCarverConfiguration, Random random) {
		int i = carvingContext.getGenDepth();
		float[] fs = new float[i];
		float f = 1.0F;

		for (int j = 0; j < i; j++) {
			if (j == 0 || random.nextInt(canyonCarverConfiguration.getWidthSmoothness()) == 0) {
				f = 1.0F + random.nextFloat() * random.nextFloat();
			}

			fs[j] = f * f;
		}

		return fs;
	}

	private double updateVerticalRadius(CanyonCarverConfiguration canyonCarverConfiguration, Random random, double d, float f, float g) {
		float h = 1.0F - Mth.abs(0.5F - g / f) * 2.0F;
		float i = canyonCarverConfiguration.getVerticalRadiusDefaultFactor() + canyonCarverConfiguration.getVerticalRadiusCenterFactor() * h;
		return (double)i * d * (double)Mth.randomBetween(random, 0.75F, 1.0F);
	}

	private boolean shouldSkip(CarvingContext carvingContext, float[] fs, double d, double e, double f, int i) {
		int j = i - carvingContext.getMinGenY();
		return (d * d + f * f) * (double)fs[j - 1] + e * e / 6.0 >= 1.0;
	}
}
