/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.flag;

import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;

public interface FeatureElement {
    public static final Set<ResourceKey<? extends Registry<? extends FeatureElement>>> FILTERED_REGISTRIES = Set.of(Registry.ITEM_REGISTRY, Registry.BLOCK_REGISTRY, Registry.ENTITY_TYPE_REGISTRY);

    public FeatureFlagSet requiredFeatures();

    default public boolean isEnabled(FeatureFlagSet featureFlagSet) {
        return this.requiredFeatures().isSubsetOf(featureFlagSet);
    }
}

