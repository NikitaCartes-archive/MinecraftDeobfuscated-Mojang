package net.minecraft.data.registries;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RegistryPatchGenerator {
	public static CompletableFuture<HolderLookup.Provider> createLookup(
		CompletableFuture<HolderLookup.Provider> completableFuture, RegistrySetBuilder registrySetBuilder
	) {
		return completableFuture.thenApply(
			provider -> {
				RegistryAccess.Frozen frozen = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
				HolderLookup.Provider provider2 = registrySetBuilder.buildPatch(frozen, provider);
				Optional<HolderLookup.RegistryLookup<Biome>> optional = provider2.lookup(Registries.BIOME);
				Optional<HolderLookup.RegistryLookup<PlacedFeature>> optional2 = provider2.lookup(Registries.PLACED_FEATURE);
				if (optional.isPresent() || optional2.isPresent()) {
					VanillaRegistries.validateThatAllBiomeFeaturesHaveBiomeFilter(
						(HolderGetter<PlacedFeature>)optional2.orElseGet(() -> provider.lookupOrThrow(Registries.PLACED_FEATURE)),
						(HolderLookup<Biome>)optional.orElseGet(() -> provider.lookupOrThrow(Registries.BIOME))
					);
				}

				return provider2;
			}
		);
	}
}
