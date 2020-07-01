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
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class OreFeature extends Feature<OreConfiguration> {
	public OreFeature(Codec<OreConfiguration> codec) {
		super(codec);
	}

	public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, OreConfiguration oreConfiguration) {
		float f = random.nextFloat() * (float) Math.PI;
		float g = (float)oreConfiguration.size / 8.0F;
		int i = Mth.ceil(((float)oreConfiguration.size / 16.0F * 2.0F + 1.0F) / 2.0F);
		double d = (double)((float)blockPos.getX() + Mth.sin(f) * g);
		double e = (double)((float)blockPos.getX() - Mth.sin(f) * g);
		double h = (double)((float)blockPos.getZ() + Mth.cos(f) * g);
		double j = (double)((float)blockPos.getZ() - Mth.cos(f) * g);
		int k = 2;
		double l = (double)(blockPos.getY() + random.nextInt(3) - 2);
		double m = (double)(blockPos.getY() + random.nextInt(3) - 2);
		int n = blockPos.getX() - Mth.ceil(g) - i;
		int o = blockPos.getY() - 2 - i;
		int p = blockPos.getZ() - Mth.ceil(g) - i;
		int q = 2 * (Mth.ceil(g) + i);
		int r = 2 * (2 + i);

		for (int s = n; s <= n + q; s++) {
			for (int t = p; t <= p + q; t++) {
				if (o <= worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, s, t)) {
					return this.doPlace(worldGenLevel, random, oreConfiguration, d, e, h, j, l, m, n, o, p, q, r);
				}
			}
		}

		return false;
	}

	protected boolean doPlace(
		LevelAccessor levelAccessor,
		Random random,
		OreConfiguration oreConfiguration,
		double d,
		double e,
		double f,
		double g,
		double h,
		double i,
		int j,
		int k,
		int l,
		int m,
		int n
	) {
		int o = 0;
		BitSet bitSet = new BitSet(m * n * m);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		double[] ds = new double[oreConfiguration.size * 4];

		for (int p = 0; p < oreConfiguration.size; p++) {
			float q = (float)p / (float)oreConfiguration.size;
			double r = Mth.lerp((double)q, d, e);
			double s = Mth.lerp((double)q, h, i);
			double t = Mth.lerp((double)q, f, g);
			double u = random.nextDouble() * (double)oreConfiguration.size / 16.0;
			double v = ((double)(Mth.sin((float) Math.PI * q) + 1.0F) * u + 1.0) / 2.0;
			ds[p * 4 + 0] = r;
			ds[p * 4 + 1] = s;
			ds[p * 4 + 2] = t;
			ds[p * 4 + 3] = v;
		}

		for (int p = 0; p < oreConfiguration.size - 1; p++) {
			if (!(ds[p * 4 + 3] <= 0.0)) {
				for (int w = p + 1; w < oreConfiguration.size; w++) {
					if (!(ds[w * 4 + 3] <= 0.0)) {
						double r = ds[p * 4 + 0] - ds[w * 4 + 0];
						double s = ds[p * 4 + 1] - ds[w * 4 + 1];
						double t = ds[p * 4 + 2] - ds[w * 4 + 2];
						double u = ds[p * 4 + 3] - ds[w * 4 + 3];
						if (u * u > r * r + s * s + t * t) {
							if (u > 0.0) {
								ds[w * 4 + 3] = -1.0;
							} else {
								ds[p * 4 + 3] = -1.0;
							}
						}
					}
				}
			}
		}

		for (int px = 0; px < oreConfiguration.size; px++) {
			double x = ds[px * 4 + 3];
			if (!(x < 0.0)) {
				double y = ds[px * 4 + 0];
				double z = ds[px * 4 + 1];
				double aa = ds[px * 4 + 2];
				int ab = Math.max(Mth.floor(y - x), j);
				int ac = Math.max(Mth.floor(z - x), k);
				int ad = Math.max(Mth.floor(aa - x), l);
				int ae = Math.max(Mth.floor(y + x), ab);
				int af = Math.max(Mth.floor(z + x), ac);
				int ag = Math.max(Mth.floor(aa + x), ad);

				for (int ah = ab; ah <= ae; ah++) {
					double ai = ((double)ah + 0.5 - y) / x;
					if (ai * ai < 1.0) {
						for (int aj = ac; aj <= af; aj++) {
							double ak = ((double)aj + 0.5 - z) / x;
							if (ai * ai + ak * ak < 1.0) {
								for (int al = ad; al <= ag; al++) {
									double am = ((double)al + 0.5 - aa) / x;
									if (ai * ai + ak * ak + am * am < 1.0) {
										int an = ah - j + (aj - k) * m + (al - l) * m * n;
										if (!bitSet.get(an)) {
											bitSet.set(an);
											mutableBlockPos.set(ah, aj, al);
											if (oreConfiguration.target.getPredicate().test(levelAccessor.getBlockState(mutableBlockPos))) {
												levelAccessor.setBlock(mutableBlockPos, oreConfiguration.state, 2);
												o++;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return o > 0;
	}
}
