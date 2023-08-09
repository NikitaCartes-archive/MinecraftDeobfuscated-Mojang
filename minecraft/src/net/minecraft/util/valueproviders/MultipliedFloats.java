package net.minecraft.util.valueproviders;

import java.util.Arrays;
import net.minecraft.util.RandomSource;

public class MultipliedFloats implements SampledFloat {
	private final SampledFloat[] values;

	public MultipliedFloats(SampledFloat... sampledFloats) {
		this.values = sampledFloats;
	}

	@Override
	public float sample(RandomSource randomSource) {
		float f = 1.0F;

		for (SampledFloat sampledFloat : this.values) {
			f *= sampledFloat.sample(randomSource);
		}

		return f;
	}

	public String toString() {
		return "MultipliedFloats" + Arrays.toString(this.values);
	}
}
