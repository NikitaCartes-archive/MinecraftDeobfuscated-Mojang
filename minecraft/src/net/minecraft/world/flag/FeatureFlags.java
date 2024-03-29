package net.minecraft.world.flag;

import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

public class FeatureFlags {
	public static final FeatureFlag VANILLA;
	public static final FeatureFlag BUNDLE;
	public static final FeatureFlag UPDATE_1_21;
	public static final FeatureFlag TRADE_REBALANCE;
	public static final FeatureFlagRegistry REGISTRY;
	public static final Codec<FeatureFlagSet> CODEC = REGISTRY.codec();
	public static final FeatureFlagSet VANILLA_SET = FeatureFlagSet.of(VANILLA);
	public static final FeatureFlagSet DEFAULT_FLAGS = VANILLA_SET;

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
		TRADE_REBALANCE = builder.createVanilla("trade_rebalance");
		UPDATE_1_21 = builder.createVanilla("update_1_21");
		REGISTRY = builder.build();
	}
}
