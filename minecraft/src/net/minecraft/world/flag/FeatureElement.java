package net.minecraft.world.flag;

import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface FeatureElement {
	Set<ResourceKey<? extends Registry<? extends FeatureElement>>> FILTERED_REGISTRIES = Set.of(
		Registry.ITEM_REGISTRY, Registry.BLOCK_REGISTRY, Registry.ENTITY_TYPE_REGISTRY
	);

	FeatureFlagSet requiredFeatures();

	default boolean isEnabled(FeatureFlagSet featureFlagSet) {
		return this.requiredFeatures().isSubsetOf(featureFlagSet);
	}
}
