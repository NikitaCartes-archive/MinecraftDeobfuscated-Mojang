package net.minecraft.commands;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;

public interface CommandBuildContext {
	<T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourceKey);

	static CommandBuildContext simple(HolderLookup.Provider provider, FeatureFlagSet featureFlagSet) {
		return new CommandBuildContext() {
			@Override
			public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourceKey) {
				return provider.lookupOrThrow(resourceKey).filterFeatures(featureFlagSet);
			}
		};
	}

	static CommandBuildContext.Configurable configurable(RegistryAccess registryAccess, FeatureFlagSet featureFlagSet) {
		return new CommandBuildContext.Configurable() {
			CommandBuildContext.MissingTagAccessPolicy missingTagAccessPolicy = CommandBuildContext.MissingTagAccessPolicy.FAIL;

			@Override
			public void missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy missingTagAccessPolicy) {
				this.missingTagAccessPolicy = missingTagAccessPolicy;
			}

			@Override
			public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourceKey) {
				Registry<T> registry = registryAccess.registryOrThrow(resourceKey);
				final HolderLookup.RegistryLookup<T> registryLookup = registry.asLookup();
				final HolderLookup.RegistryLookup<T> registryLookup2 = registry.asTagAddingLookup();
				HolderLookup.RegistryLookup<T> registryLookup3 = new HolderLookup.RegistryLookup.Delegate<T>() {
					@Override
					protected HolderLookup.RegistryLookup<T> parent() {
						return switch (missingTagAccessPolicy) {
							case FAIL -> registryLookup;
							case CREATE_NEW -> registryLookup2;
						};
					}
				};
				return registryLookup3.filterFeatures(featureFlagSet);
			}
		};
	}

	public interface Configurable extends CommandBuildContext {
		void missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy missingTagAccessPolicy);
	}

	public static enum MissingTagAccessPolicy {
		CREATE_NEW,
		FAIL;
	}
}
