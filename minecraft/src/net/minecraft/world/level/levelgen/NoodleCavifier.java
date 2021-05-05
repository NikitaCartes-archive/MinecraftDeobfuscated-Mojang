package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.NoiseUtils;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoodleCavifier {
	private static final int NOODLES_MAX_Y = 30;
	private static final double SPACING_AND_STRAIGHTNESS = 1.5;
	private static final double XZ_FREQUENCY = 2.6666666666666665;
	private static final double Y_FREQUENCY = 2.6666666666666665;
	private final NormalNoise toggleNoiseSource;
	private final NormalNoise thicknessNoiseSource;
	private final NormalNoise noodleANoiseSource;
	private final NormalNoise noodleBNoiseSource;

	public NoodleCavifier(long l) {
		Random random = new Random(l);
		this.toggleNoiseSource = NormalNoise.create(new SimpleRandomSource(random.nextLong()), -8, 1.0);
		this.thicknessNoiseSource = NormalNoise.create(new SimpleRandomSource(random.nextLong()), -8, 1.0);
		this.noodleANoiseSource = NormalNoise.create(new SimpleRandomSource(random.nextLong()), -7, 1.0);
		this.noodleBNoiseSource = NormalNoise.create(new SimpleRandomSource(random.nextLong()), -7, 1.0);
	}

	public void fillToggleNoiseColumn(double[] ds, int i, int j, int k, int l) {
		this.fillNoiseColumn(ds, i, j, k, l, this.toggleNoiseSource, 1.0);
	}

	public void fillThicknessNoiseColumn(double[] ds, int i, int j, int k, int l) {
		this.fillNoiseColumn(ds, i, j, k, l, this.thicknessNoiseSource, 1.0);
	}

	public void fillRidgeANoiseColumn(double[] ds, int i, int j, int k, int l) {
		this.fillNoiseColumn(ds, i, j, k, l, this.noodleANoiseSource, 2.6666666666666665, 2.6666666666666665);
	}

	public void fillRidgeBNoiseColumn(double[] ds, int i, int j, int k, int l) {
		this.fillNoiseColumn(ds, i, j, k, l, this.noodleBNoiseSource, 2.6666666666666665, 2.6666666666666665);
	}

	public void fillNoiseColumn(double[] ds, int i, int j, int k, int l, NormalNoise normalNoise, double d) {
		this.fillNoiseColumn(ds, i, j, k, l, normalNoise, d, d);
	}

	public void fillNoiseColumn(double[] ds, int i, int j, int k, int l, NormalNoise normalNoise, double d, double e) {
		int m = 8;
		int n = 4;

		for (int o = 0; o < l; o++) {
			int p = o + k;
			int q = i * 4;
			int r = p * 8;
			int s = j * 4;
			double f;
			if (r < 38) {
				f = NoiseUtils.sampleNoiseAndMapToRange(normalNoise, (double)q * d, (double)r * e, (double)s * d, -1.0, 1.0);
			} else {
				f = 1.0;
			}

			ds[o] = f;
		}
	}

	public double noodleCavify(double d, int i, int j, int k, double e, double f, double g, double h, int l) {
		if (j > 30 || j < l + 4) {
			return d;
		} else if (d < 0.0) {
			return d;
		} else if (e < 0.0) {
			return d;
		} else {
			double m = 0.05;
			double n = 0.1;
			double o = Mth.clampedMap(f, -1.0, 1.0, 0.05, 0.1);
			double p = Math.abs(1.5 * g) - o;
			double q = Math.abs(1.5 * h) - o;
			double r = Math.max(p, q);
			return Math.min(d, r);
		}
	}
}
