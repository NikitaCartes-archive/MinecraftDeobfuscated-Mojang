package net.minecraft.world.level.levelgen.synth;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class PerlinSimplexNoise implements SurfaceNoise {
	private final SimplexNoise[] noiseLevels;
	private final double highestFreqValueFactor;
	private final double highestFreqInputFactor;

	public PerlinSimplexNoise(WorldgenRandom worldgenRandom, IntStream intStream) {
		this(worldgenRandom, (List<Integer>)intStream.boxed().collect(ImmutableList.toImmutableList()));
	}

	public PerlinSimplexNoise(WorldgenRandom worldgenRandom, List<Integer> list) {
		this(worldgenRandom, new IntRBTreeSet(list));
	}

	private PerlinSimplexNoise(WorldgenRandom worldgenRandom, IntSortedSet intSortedSet) {
		if (intSortedSet.isEmpty()) {
			throw new IllegalArgumentException("Need some octaves!");
		} else {
			int i = -intSortedSet.firstInt();
			int j = intSortedSet.lastInt();
			int k = i + j + 1;
			if (k < 1) {
				throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
			} else {
				SimplexNoise simplexNoise = new SimplexNoise(worldgenRandom);
				int l = j;
				this.noiseLevels = new SimplexNoise[k];
				if (j >= 0 && j < k && intSortedSet.contains(0)) {
					this.noiseLevels[j] = simplexNoise;
				}

				for (int m = j + 1; m < k; m++) {
					if (m >= 0 && intSortedSet.contains(l - m)) {
						this.noiseLevels[m] = new SimplexNoise(worldgenRandom);
					} else {
						worldgenRandom.consumeCount(262);
					}
				}

				if (j > 0) {
					long n = (long)(simplexNoise.getValue(simplexNoise.xo, simplexNoise.yo, simplexNoise.zo) * 9.223372E18F);
					WorldgenRandom worldgenRandom2 = new WorldgenRandom(n);

					for (int o = l - 1; o >= 0; o--) {
						if (o < k && intSortedSet.contains(l - o)) {
							this.noiseLevels[o] = new SimplexNoise(worldgenRandom2);
						} else {
							worldgenRandom2.consumeCount(262);
						}
					}
				}

				this.highestFreqInputFactor = Math.pow(2.0, (double)j);
				this.highestFreqValueFactor = 1.0 / (Math.pow(2.0, (double)k) - 1.0);
			}
		}
	}

	public double getValue(double d, double e, boolean bl) {
		double f = 0.0;
		double g = this.highestFreqInputFactor;
		double h = this.highestFreqValueFactor;

		for (SimplexNoise simplexNoise : this.noiseLevels) {
			if (simplexNoise != null) {
				f += simplexNoise.getValue(d * g + (bl ? simplexNoise.xo : 0.0), e * g + (bl ? simplexNoise.yo : 0.0)) * h;
			}

			g /= 2.0;
			h *= 2.0;
		}

		return f;
	}

	@Override
	public double getSurfaceNoiseValue(double d, double e, double f, double g) {
		return this.getValue(d, e, true) * 0.55;
	}
}
