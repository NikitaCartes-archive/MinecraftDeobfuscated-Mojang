package net.minecraft.data.registries;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public class RegistriesDatapackGenerator implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final PackOutput output;
	private final CompletableFuture<HolderLookup.Provider> registries;

	public RegistriesDatapackGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		this.registries = completableFuture;
		this.output = packOutput;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		return this.registries
			.thenCompose(
				provider -> {
					DynamicOps<JsonElement> dynamicOps = provider.createSerializationContext(JsonOps.INSTANCE);
					return CompletableFuture.allOf(
						(CompletableFuture[])RegistryDataLoader.WORLDGEN_REGISTRIES
							.stream()
							.flatMap(registryData -> this.dumpRegistryCap(cachedOutput, provider, dynamicOps, registryData).stream())
							.toArray(CompletableFuture[]::new)
					);
				}
			);
	}

	private <T> Optional<CompletableFuture<?>> dumpRegistryCap(
		CachedOutput cachedOutput, HolderLookup.Provider provider, DynamicOps<JsonElement> dynamicOps, RegistryDataLoader.RegistryData<T> registryData
	) {
		ResourceKey<? extends Registry<T>> resourceKey = registryData.key();
		return provider.lookup(resourceKey)
			.map(
				registryLookup -> {
					PackOutput.PathProvider pathProvider = this.output.createRegistryElementsPathProvider(resourceKey);
					return CompletableFuture.allOf(
						(CompletableFuture[])registryLookup.listElements()
							.map(reference -> dumpValue(pathProvider.json(reference.key().location()), cachedOutput, dynamicOps, registryData.elementCodec(), (T)reference.value()))
							.toArray(CompletableFuture[]::new)
					);
				}
			);
	}

	private static <E> CompletableFuture<?> dumpValue(Path path, CachedOutput cachedOutput, DynamicOps<JsonElement> dynamicOps, Encoder<E> encoder, E object) {
		Optional<JsonElement> optional = encoder.encodeStart(dynamicOps, object)
			.resultOrPartial(string -> LOGGER.error("Couldn't serialize element {}: {}", path, string));
		return optional.isPresent() ? DataProvider.saveStable(cachedOutput, (JsonElement)optional.get(), path) : CompletableFuture.completedFuture(null);
	}

	@Override
	public final String getName() {
		return "Registries";
	}
}
