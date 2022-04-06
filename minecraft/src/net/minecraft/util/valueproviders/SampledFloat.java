package net.minecraft.util.valueproviders;

import net.minecraft.util.RandomSource;

public interface SampledFloat {
	float sample(RandomSource randomSource);
}
