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

public class CaveWorldCarver extends WorldCarver<CaveCarverConfiguration> {
	public CaveWorldCarver(Codec<CaveCarverConfiguration> codec) {
		super(codec);
	}

	public boolean isStartChunk(CaveCarverConfiguration caveCarverConfiguration, RandomSource randomSource) {
		return randomSource.nextFloat() <= caveCarverConfiguration.probability;
	}

	public boolean carve(
		CarvingContext carvingContext,
		CaveCarverConfiguration caveCarverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Holder<Biome>> function,
		RandomSource randomSource,
		Aquifer aquifer,
		ChunkPos chunkPos,
		CarvingMask carvingMask
	) {
		int i = SectionPos.sectionToBlockCoord(this.getRange() * 2 - 1);
		int j = randomSource.nextInt(randomSource.nextInt(randomSource.nextInt(this.getCaveBound()) + 1) + 1);

		for (int k = 0; k < j; k++) {
			double d = (double)chunkPos.getBlockX(randomSource.nextInt(16));
			double e = (double)caveCarverConfiguration.y.sample(randomSource, carvingContext);
			double f = (double)chunkPos.getBlockZ(randomSource.nextInt(16));
			double g = (double)caveCarverConfiguration.horizontalRadiusMultiplier.sample(randomSource);
			double h = (double)caveCarverConfiguration.verticalRadiusMultiplier.sample(randomSource);
			double l = (double)caveCarverConfiguration.floorLevel.sample(randomSource);
			WorldCarver.CarveSkipChecker carveSkipChecker = (carvingContextx, ex, fx, gx, ix) -> shouldSkip(ex, fx, gx, l);
			int m = 1;
			if (randomSource.nextInt(4) == 0) {
				double n = (double)caveCarverConfiguration.yScale.sample(randomSource);
				float o = 1.0F + randomSource.nextFloat() * 6.0F;
				this.createRoom(carvingContext, caveCarverConfiguration, chunkAccess, function, aquifer, d, e, f, o, n, carvingMask, carveSkipChecker);
				m += randomSource.nextInt(4);
			}

			for (int p = 0; p < m; p++) {
				float q = randomSource.nextFloat() * (float) (Math.PI * 2);
				float o = (randomSource.nextFloat() - 0.5F) / 4.0F;
				float r = this.getThickness(randomSource);
				int s = i - randomSource.nextInt(i / 4);
				int t = 0;
				this.createTunnel(
					carvingContext,
					caveCarverConfiguration,
					chunkAccess,
					function,
					randomSource.nextLong(),
					aquifer,
					d,
					e,
					f,
					g,
					h,
					r,
					q,
					o,
					0,
					s,
					this.getYScale(),
					carvingMask,
					carveSkipChecker
				);
			}
		}

		return true;
	}

	protected int getCaveBound() {
		return 15;
	}

	protected float getThickness(RandomSource randomSource) {
		float f = randomSource.nextFloat() * 2.0F + randomSource.nextFloat();
		if (randomSource.nextInt(10) == 0) {
			f *= randomSource.nextFloat() * randomSource.nextFloat() * 3.0F + 1.0F;
		}

		return f;
	}

	protected double getYScale() {
		return 1.0;
	}

	protected void createRoom(
		CarvingContext carvingContext,
		CaveCarverConfiguration caveCarverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Holder<Biome>> function,
		Aquifer aquifer,
		double d,
		double e,
		double f,
		float g,
		double h,
		CarvingMask carvingMask,
		WorldCarver.CarveSkipChecker carveSkipChecker
	) {
		double i = 1.5 + (double)(Mth.sin((float) (Math.PI / 2)) * g);
		double j = i * h;
		this.carveEllipsoid(carvingContext, caveCarverConfiguration, chunkAccess, function, aquifer, d + 1.0, e, f, i, j, carvingMask, carveSkipChecker);
	}

	protected void createTunnel(
		CarvingContext carvingContext,
		CaveCarverConfiguration caveCarverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Holder<Biome>> function,
		long l,
		Aquifer aquifer,
		double d,
		double e,
		double f,
		double g,
		double h,
		float i,
		float j,
		float k,
		int m,
		int n,
		double o,
		CarvingMask carvingMask,
		WorldCarver.CarveSkipChecker carveSkipChecker
	) {
		RandomSource randomSource = RandomSource.create(l);
		int p = randomSource.nextInt(n / 2) + n / 4;
		boolean bl = randomSource.nextInt(6) == 0;
		float q = 0.0F;
		float r = 0.0F;

		for (int s = m; s < n; s++) {
			double t = 1.5 + (double)(Mth.sin((float) Math.PI * (float)s / (float)n) * i);
			double u = t * o;
			float v = Mth.cos(k);
			d += (double)(Mth.cos(j) * v);
			e += (double)Mth.sin(k);
			f += (double)(Mth.sin(j) * v);
			k *= bl ? 0.92F : 0.7F;
			k += r * 0.1F;
			j += q * 0.1F;
			r *= 0.9F;
			q *= 0.75F;
			r += (randomSource.nextFloat() - randomSource.nextFloat()) * randomSource.nextFloat() * 2.0F;
			q += (randomSource.nextFloat() - randomSource.nextFloat()) * randomSource.nextFloat() * 4.0F;
			if (s == p && i > 1.0F) {
				this.createTunnel(
					carvingContext,
					caveCarverConfiguration,
					chunkAccess,
					function,
					randomSource.nextLong(),
					aquifer,
					d,
					e,
					f,
					g,
					h,
					randomSource.nextFloat() * 0.5F + 0.5F,
					j - (float) (Math.PI / 2),
					k / 3.0F,
					s,
					n,
					1.0,
					carvingMask,
					carveSkipChecker
				);
				this.createTunnel(
					carvingContext,
					caveCarverConfiguration,
					chunkAccess,
					function,
					randomSource.nextLong(),
					aquifer,
					d,
					e,
					f,
					g,
					h,
					randomSource.nextFloat() * 0.5F + 0.5F,
					j + (float) (Math.PI / 2),
					k / 3.0F,
					s,
					n,
					1.0,
					carvingMask,
					carveSkipChecker
				);
				return;
			}

			if (randomSource.nextInt(4) != 0) {
				if (!canReach(chunkAccess.getPos(), d, f, s, n, i)) {
					return;
				}

				this.carveEllipsoid(carvingContext, caveCarverConfiguration, chunkAccess, function, aquifer, d, e, f, t * g, u * h, carvingMask, carveSkipChecker);
			}
		}
	}

	private static boolean shouldSkip(double d, double e, double f, double g) {
		return e <= g ? true : d * d + e * e + f * f >= 1.0;
	}
}
