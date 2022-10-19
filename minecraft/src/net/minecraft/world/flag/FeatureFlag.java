package net.minecraft.world.flag;

public class FeatureFlag {
	final FeatureFlagUniverse universe;
	final long mask;

	FeatureFlag(FeatureFlagUniverse featureFlagUniverse, int i) {
		this.universe = featureFlagUniverse;
		this.mask = 1L << i;
	}
}
