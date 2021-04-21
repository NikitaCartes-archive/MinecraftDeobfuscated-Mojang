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
import net.minecraft.world.level.levelgen.Aquifer;
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
		Aquifer aquifer,
		ChunkPos chunkPos,
		BitSet bitSet
	) {
		int i = (this.getRange() * 2 - 1) * 16;
		double d = (double)chunkPos.getBlockX(random.nextInt(16));
		int j = canyonCarverConfiguration.y.sample(random, carvingContext);
		double e = (double)chunkPos.getBlockZ(random.nextInt(16));
		float f = random.nextFloat() * (float) (Math.PI * 2);
		float g = canyonCarverConfiguration.verticalRotation.sample(random);
		double h = (double)canyonCarverConfiguration.yScale.sample(random);
		float k = canyonCarverConfiguration.shape.thickness.sample(random);
		int l = (int)((float)i * canyonCarverConfiguration.shape.distanceFactor.sample(random));
		int m = 0;
		this.doCarve(carvingContext, canyonCarverConfiguration, chunkAccess, function, random.nextLong(), aquifer, d, (double)j, e, k, f, g, 0, l, h, bitSet);
		return true;
	}

	private void doCarve(
		CarvingContext carvingContext,
		CanyonCarverConfiguration canyonCarverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		long l,
		Aquifer aquifer,
		double d,
		double e,
		double f,
		float g,
		float h,
		float i,
		int j,
		int k,
		double m,
		BitSet bitSet
	) {
		Random random = new Random(l);
		float[] fs = this.initWidthFactors(carvingContext, canyonCarverConfiguration, random);
		float n = 0.0F;
		float o = 0.0F;

		for (int p = j; p < k; p++) {
			double q = 1.5 + (double)(Mth.sin((float)p * (float) Math.PI / (float)k) * g);
			double r = q * m;
			q *= (double)canyonCarverConfiguration.shape.horizontalRadiusFactor.sample(random);
			r = this.updateVerticalRadius(canyonCarverConfiguration, random, r, (float)k, (float)p);
			float s = Mth.cos(i);
			float t = Mth.sin(i);
			d += (double)(Mth.cos(h) * s);
			e += (double)t;
			f += (double)(Mth.sin(h) * s);
			i *= 0.7F;
			i += o * 0.05F;
			h += n * 0.05F;
			o *= 0.8F;
			n *= 0.5F;
			o += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			n += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
			if (random.nextInt(4) != 0) {
				if (!canReach(chunkAccess.getPos(), d, f, p, k, g)) {
					return;
				}

				this.carveEllipsoid(
					carvingContext,
					canyonCarverConfiguration,
					chunkAccess,
					function,
					l,
					aquifer,
					d,
					e,
					f,
					q,
					r,
					bitSet,
					(carvingContextx, dx, ex, fx, ix) -> this.shouldSkip(carvingContextx, fs, dx, ex, fx, ix)
				);
			}
		}
	}

	private float[] initWidthFactors(CarvingContext carvingContext, CanyonCarverConfiguration canyonCarverConfiguration, Random random) {
		int i = carvingContext.getGenDepth();
		float[] fs = new float[i];
		float f = 1.0F;

		for (int j = 0; j < i; j++) {
			if (j == 0 || random.nextInt(canyonCarverConfiguration.shape.widthSmoothness) == 0) {
				f = 1.0F + random.nextFloat() * random.nextFloat();
			}

			fs[j] = f * f;
		}

		return fs;
	}

	private double updateVerticalRadius(CanyonCarverConfiguration canyonCarverConfiguration, Random random, double d, float f, float g) {
		float h = 1.0F - Mth.abs(0.5F - g / f) * 2.0F;
		float i = canyonCarverConfiguration.shape.verticalRadiusDefaultFactor + canyonCarverConfiguration.shape.verticalRadiusCenterFactor * h;
		return (double)i * d * (double)Mth.randomBetween(random, 0.75F, 1.0F);
	}

	private boolean shouldSkip(CarvingContext carvingContext, float[] fs, double d, double e, double f, int i) {
		int j = i - carvingContext.getMinGenY();
		return (d * d + f * f) * (double)fs[j - 1] + e * e / 6.0 >= 1.0;
	}
}
