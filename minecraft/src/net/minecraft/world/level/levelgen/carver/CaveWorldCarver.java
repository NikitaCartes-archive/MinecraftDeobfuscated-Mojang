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
import net.minecraft.world.level.levelgen.Aquifer;

public class CaveWorldCarver extends WorldCarver<CaveCarverConfiguration> {
	public CaveWorldCarver(Codec<CaveCarverConfiguration> codec) {
		super(codec);
	}

	public boolean isStartChunk(CaveCarverConfiguration caveCarverConfiguration, Random random) {
		return random.nextFloat() <= caveCarverConfiguration.probability;
	}

	public boolean carve(
		CarvingContext carvingContext,
		CaveCarverConfiguration caveCarverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		Random random,
		Aquifer aquifer,
		ChunkPos chunkPos,
		BitSet bitSet
	) {
		int i = SectionPos.sectionToBlockCoord(this.getRange() * 2 - 1);
		int j = random.nextInt(random.nextInt(random.nextInt(this.getCaveBound()) + 1) + 1);

		for (int k = 0; k < j; k++) {
			double d = (double)chunkPos.getBlockX(random.nextInt(16));
			double e = (double)caveCarverConfiguration.y.sample(random, carvingContext);
			double f = (double)chunkPos.getBlockZ(random.nextInt(16));
			double g = (double)caveCarverConfiguration.horizontalRadiusMultiplier.sample(random);
			double h = (double)caveCarverConfiguration.verticalRadiusMultiplier.sample(random);
			double l = (double)caveCarverConfiguration.floorLevel.sample(random);
			WorldCarver.CarveSkipChecker carveSkipChecker = (carvingContextx, ex, fx, gx, ix) -> shouldSkip(ex, fx, gx, l);
			int m = 1;
			if (random.nextInt(4) == 0) {
				double n = (double)caveCarverConfiguration.yScale.sample(random);
				float o = 1.0F + random.nextFloat() * 6.0F;
				this.createRoom(carvingContext, caveCarverConfiguration, chunkAccess, function, random.nextLong(), aquifer, d, e, f, o, n, bitSet, carveSkipChecker);
				m += random.nextInt(4);
			}

			for (int p = 0; p < m; p++) {
				float q = random.nextFloat() * (float) (Math.PI * 2);
				float o = (random.nextFloat() - 0.5F) / 4.0F;
				float r = this.getThickness(random);
				int s = i - random.nextInt(i / 4);
				int t = 0;
				this.createTunnel(
					carvingContext,
					caveCarverConfiguration,
					chunkAccess,
					function,
					random.nextLong(),
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
					bitSet,
					carveSkipChecker
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

	protected void createRoom(
		CarvingContext carvingContext,
		CaveCarverConfiguration caveCarverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		long l,
		Aquifer aquifer,
		double d,
		double e,
		double f,
		float g,
		double h,
		BitSet bitSet,
		WorldCarver.CarveSkipChecker carveSkipChecker
	) {
		double i = 1.5 + (double)(Mth.sin((float) (Math.PI / 2)) * g);
		double j = i * h;
		this.carveEllipsoid(carvingContext, caveCarverConfiguration, chunkAccess, function, l, aquifer, d + 1.0, e, f, i, j, bitSet, carveSkipChecker);
	}

	protected void createTunnel(
		CarvingContext carvingContext,
		CaveCarverConfiguration caveCarverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
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
		BitSet bitSet,
		WorldCarver.CarveSkipChecker carveSkipChecker
	) {
		Random random = new Random(l);
		int p = random.nextInt(n / 2) + n / 4;
		boolean bl = random.nextInt(6) == 0;
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
			r += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			q += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
			if (s == p && i > 1.0F) {
				this.createTunnel(
					carvingContext,
					caveCarverConfiguration,
					chunkAccess,
					function,
					random.nextLong(),
					aquifer,
					d,
					e,
					f,
					g,
					h,
					random.nextFloat() * 0.5F + 0.5F,
					j - (float) (Math.PI / 2),
					k / 3.0F,
					s,
					n,
					1.0,
					bitSet,
					carveSkipChecker
				);
				this.createTunnel(
					carvingContext,
					caveCarverConfiguration,
					chunkAccess,
					function,
					random.nextLong(),
					aquifer,
					d,
					e,
					f,
					g,
					h,
					random.nextFloat() * 0.5F + 0.5F,
					j + (float) (Math.PI / 2),
					k / 3.0F,
					s,
					n,
					1.0,
					bitSet,
					carveSkipChecker
				);
				return;
			}

			if (random.nextInt(4) != 0) {
				if (!canReach(chunkAccess.getPos(), d, f, s, n, i)) {
					return;
				}

				this.carveEllipsoid(carvingContext, caveCarverConfiguration, chunkAccess, function, l, aquifer, d, e, f, t * g, u * h, bitSet, carveSkipChecker);
			}
		}
	}

	private static boolean shouldSkip(double d, double e, double f, double g) {
		return e <= g ? true : d * d + e * e + f * f >= 1.0;
	}
}
