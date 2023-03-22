package net.minecraft.world.flag;

import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

public class FeatureFlags {
	public static final FeatureFlag VANILLA;
	public static final FeatureFlag BUNDLE;
	public static final FeatureFlagRegistry REGISTRY;
	public static final Codec<FeatureFlagSet> CODEC;
	public static final FeatureFlagSet VANILLA_SET;
	public static final FeatureFlagSet DEFAULT_FLAGS;

	public static String printMissingFlags(FeatureFlagSet featureFlagSet, FeatureFlagSet featureFlagSet2) {
		return printMissingFlags(REGISTRY, featureFlagSet, featureFlagSet2);
	}

	public static String printMissingFlags(FeatureFlagRegistry featureFlagRegistry, FeatureFlagSet featureFlagSet, FeatureFlagSet featureFlagSet2) {
		Set<ResourceLocation> set = featureFlagRegistry.toNames(featureFlagSet2);
		Set<ResourceLocation> set2 = featureFlagRegistry.toNames(featureFlagSet);
		return (String)set.stream().filter(resourceLocation -> !set2.contains(resourceLocation)).map(ResourceLocation::toString).collect(Collectors.joining(", "));
	}

	public static boolean isExperimental(FeatureFlagSet featureFlagSet) {
		return !featureFlagSet.isSubsetOf(VANILLA_SET);
	}

	static {
		FeatureFlagRegistry.Builder builder = new FeatureFlagRegistry.Builder("main");
		VANILLA = builder.createVanilla("vanilla");
		BUNDLE = builder.createVanilla("bundle");
		REGISTRY = builder.build();
		CODEC = REGISTRY.codec();
		VANILLA_SET = FeatureFlagSet.of(VANILLA);
		DEFAULT_FLAGS = VANILLA_SET;
	}
}
