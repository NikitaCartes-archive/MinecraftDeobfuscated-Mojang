package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public abstract class ProbabilityCarverBase extends WorldCarver<ProbabilityFeatureConfiguration> {
	public ProbabilityCarverBase(Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration> function, int i) {
		super(function, i);
	}

	public ProbabilityFeatureConfiguration randomConfig(Random random) {
		return new ProbabilityFeatureConfiguration(random.nextFloat() / 2.0F);
	}
}
