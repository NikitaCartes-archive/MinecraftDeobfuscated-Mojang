/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.flag;

import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagRegistry;
import net.minecraft.world.flag.FeatureFlagSet;

public class FeatureFlags {
    public static final FeatureFlag VANILLA;
    public static final FeatureFlag BUNDLE;
    public static final FeatureFlag UPDATE_1_20;
    public static final FeatureFlagRegistry REGISTRY;
    public static final Codec<FeatureFlagSet> CODEC;
    public static final FeatureFlagSet VANILLA_SET;
    public static final FeatureFlagSet DEFAULT_FLAGS;

    public static String printMissingFlags(FeatureFlagSet featureFlagSet, FeatureFlagSet featureFlagSet2) {
        return FeatureFlags.printMissingFlags(REGISTRY, featureFlagSet, featureFlagSet2);
    }

    public static String printMissingFlags(FeatureFlagRegistry featureFlagRegistry, FeatureFlagSet featureFlagSet, FeatureFlagSet featureFlagSet2) {
        Set<ResourceLocation> set = featureFlagRegistry.toNames(featureFlagSet2);
        Set<ResourceLocation> set2 = featureFlagRegistry.toNames(featureFlagSet);
        return set.stream().filter(resourceLocation -> !set2.contains(resourceLocation)).map(ResourceLocation::toString).collect(Collectors.joining(", "));
    }

    public static boolean isExperimental(FeatureFlagSet featureFlagSet) {
        return !featureFlagSet.isSubsetOf(VANILLA_SET);
    }

    static {
        FeatureFlagRegistry.Builder builder = new FeatureFlagRegistry.Builder("main");
        VANILLA = builder.createVanilla("vanilla");
        BUNDLE = builder.createVanilla("bundle");
        UPDATE_1_20 = builder.createVanilla("update_1_20");
        REGISTRY = builder.build();
        CODEC = REGISTRY.codec();
        DEFAULT_FLAGS = VANILLA_SET = FeatureFlagSet.of(VANILLA);
    }
}

