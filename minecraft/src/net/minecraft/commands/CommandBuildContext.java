package net.minecraft.commands;

import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;

public final class CommandBuildContext {
	private final RegistryAccess registryAccess;
	private final FeatureFlagSet enabledFeatures;
	CommandBuildContext.MissingTagAccessPolicy missingTagAccessPolicy = CommandBuildContext.MissingTagAccessPolicy.FAIL;

	public CommandBuildContext(RegistryAccess registryAccess, FeatureFlagSet featureFlagSet) {
		this.registryAccess = registryAccess;
		this.enabledFeatures = featureFlagSet;
	}

	public void missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy missingTagAccessPolicy) {
		this.missingTagAccessPolicy = missingTagAccessPolicy;
	}

	public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourceKey) {
		HolderLookup.RegistryLookup<T> registryLookup = new HolderLookup.RegistryLookup<T>(this.registryAccess.registryOrThrow(resourceKey)) {
			@Override
			public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
				return switch (CommandBuildContext.this.missingTagAccessPolicy) {
					case FAIL -> this.registry.getTag(tagKey);
					case CREATE_NEW -> Optional.of(this.registry.getOrCreateTag(tagKey));
					case RETURN_EMPTY -> {
						Optional<HolderSet.Named<T>> optional = this.registry.getTag(tagKey);
						yield Optional.of((HolderSet.Named)optional.orElseGet(() -> HolderSet.emptyNamed(this.registry, tagKey)));
					}
				};
			}
		};
		return registryLookup.filterFeatures(this.enabledFeatures);
	}

	public static enum MissingTagAccessPolicy {
		CREATE_NEW,
		RETURN_EMPTY,
		FAIL;
	}
}
