package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

public class CaveWorldCarver extends WorldCarver<CarverConfiguration> {
	public CaveWorldCarver(Codec<CarverConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean isStartChunk(CarverConfiguration carverConfiguration, Random random) {
		return random.nextFloat() <= carverConfiguration.probability;
	}

	@Override
	public boolean carve(
		CarvingContext carvingContext,
		CarverConfiguration carverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		Random random,
		int i,
		ChunkPos chunkPos,
		BitSet bitSet
	) {
		int j = SectionPos.sectionToBlockCoord(this.getRange() * 2 - 1);
		int k = random.nextInt(random.nextInt(random.nextInt(this.getCaveBound()) + 1) + 1);

		for (int l = 0; l < k; l++) {
			double d = (double)chunkPos.getBlockX(random.nextInt(16));
			double e = (double)this.getCaveY(carvingContext, random);
			double f = (double)chunkPos.getBlockZ(random.nextInt(16));
			double g = (double)Mth.randomBetween(random, 0.3F, 1.8F);
			double h = (double)Mth.randomBetween(random, 0.3F, 1.8F);
			double m = (double)Mth.randomBetween(random, -1.0F, 0.0F);
			WorldCarver.CarveSkipChecker carveSkipChecker = (carvingContextx, ex, fx, gx, ix) -> shouldSkip(ex, fx, gx, m);
			int n = 1;
			if (random.nextInt(4) == 0) {
				double o = (double)Mth.randomBetween(random, 0.1F, 0.9F);
				float p = 1.0F + random.nextFloat() * 6.0F;
				this.createRoom(carvingContext, carverConfiguration, chunkAccess, function, random.nextLong(), i, d, e, f, p, o, bitSet, carveSkipChecker);
				n += random.nextInt(4);
			}

			for (int q = 0; q < n; q++) {
				float r = random.nextFloat() * (float) (Math.PI * 2);
				float p = (random.nextFloat() - 0.5F) / 4.0F;
				float s = this.getThickness(random);
				int t = j - random.nextInt(j / 4);
				int u = 0;
				this.createTunnel(
					carvingContext, carverConfiguration, chunkAccess, function, random.nextLong(), i, d, e, f, g, h, s, r, p, 0, t, this.getYScale(), bitSet, carveSkipChecker
				);
			}
		}

		return true;
	}

	protected int getCaveBound() {
		return 15;
	}

	protected float getThickness(Random random) {
		float f = random.nextFloat() * 2.0F + random.nextFloat();
		if (random.nextInt(10) == 0) {
			f *= random.nextFloat() * random.nextFloat() * 3.0F + 1.0F;
		}

		return f;
	}

	protected double getYScale() {
		return 1.0;
	}

	protected int getCaveY(CarvingContext carvingContext, Random random) {
		int i = carvingContext.getMinGenY() + 8;
		int j = 126;
		return i > 126 ? i : Mth.randomBetweenInclusive(random, i, 126);
	}

	protected void createRoom(
		CarvingContext carvingContext,
		CarverConfiguration carverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		long l,
		int i,
		double d,
		double e,
		double f,
		float g,
		double h,
		BitSet bitSet,
		WorldCarver.CarveSkipChecker carveSkipChecker
	) {
		double j = 1.5 + (double)(Mth.sin((float) (Math.PI / 2)) * g);
		double k = j * h;
		this.carveEllipsoid(carvingContext, carverConfiguration, chunkAccess, function, l, i, d + 1.0, e, f, j, k, bitSet, carveSkipChecker);
	}

	protected void createTunnel(
		CarvingContext carvingContext,
		CarverConfiguration carverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		long l,
		int i,
		double d,
		double e,
		double f,
		double g,
		double h,
		float j,
		float k,
		float m,
		int n,
		int o,
		double p,
		BitSet bitSet,
		WorldCarver.CarveSkipChecker carveSkipChecker
	) {
		Random random = new Random(l);
		int q = random.nextInt(o / 2) + o / 4;
		boolean bl = random.nextInt(6) == 0;
		float r = 0.0F;
		float s = 0.0F;

		for (int t = n; t < o; t++) {
			double u = 1.5 + (double)(Mth.sin((float) Math.PI * (float)t / (float)o) * j);
			double v = u * p;
			float w = Mth.cos(m);
			d += (double)(Mth.cos(k) * w);
			e += (double)Mth.sin(m);
			f += (double)(Mth.sin(k) * w);
			m *= bl ? 0.92F : 0.7F;
			m += s * 0.1F;
			k += r * 0.1F;
			s *= 0.9F;
			r *= 0.75F;
			s += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			r += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
			if (t == q && j > 1.0F) {
				this.createTunnel(
					carvingContext,
					carverConfiguration,
					chunkAccess,
					function,
					random.nextLong(),
					i,
					d,
					e,
					f,
					g,
					h,
					random.nextFloat() * 0.5F + 0.5F,
					k - (float) (Math.PI / 2),
					m / 3.0F,
					t,
					o,
					1.0,
					bitSet,
					carveSkipChecker
				);
				this.createTunnel(
					carvingContext,
					carverConfiguration,
					chunkAccess,
					function,
					random.nextLong(),
					i,
					d,
					e,
					f,
					g,
					h,
					random.nextFloat() * 0.5F + 0.5F,
					k + (float) (Math.PI / 2),
					m / 3.0F,
					t,
					o,
					1.0,
					bitSet,
					carveSkipChecker
				);
				return;
			}

			if (random.nextInt(4) != 0) {
				if (!canReach(chunkAccess.getPos(), d, f, t, o, j)) {
					return;
				}

				this.carveEllipsoid(carvingContext, carverConfiguration, chunkAccess, function, l, i, d, e, f, u * g, v * h, bitSet, carveSkipChecker);
			}
		}
	}

	private static boolean shouldSkip(double d, double e, double f, double g) {
		return e <= g ? true : d * d + e * e + f * f >= 1.0;
	}
}
