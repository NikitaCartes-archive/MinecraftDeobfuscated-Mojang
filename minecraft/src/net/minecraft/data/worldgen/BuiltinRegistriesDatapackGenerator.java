package net.minecraft.data.worldgen;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public class BuiltinRegistriesDatapackGenerator implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final PackOutput output;

	public BuiltinRegistriesDatapackGenerator(PackOutput packOutput) {
		this.output = packOutput;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		RegistryAccess registryAccess = BuiltinRegistries.createAccess();
		DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
		return CompletableFuture.allOf(
			(CompletableFuture[])RegistryDataLoader.WORLDGEN_REGISTRIES
				.stream()
				.map(registryData -> this.dumpRegistryCap(cachedOutput, registryAccess, dynamicOps, registryData))
				.toArray(CompletableFuture[]::new)
		);
	}

	private <T> CompletableFuture<?> dumpRegistryCap(
		CachedOutput cachedOutput, RegistryAccess registryAccess, DynamicOps<JsonElement> dynamicOps, RegistryDataLoader.RegistryData<T> registryData
	) {
		ResourceKey<? extends Registry<T>> resourceKey = registryData.key();
		Registry<T> registry = registryAccess.registryOrThrow(resourceKey);
		PackOutput.PathProvider pathProvider = this.output.createPathProvider(PackOutput.Target.DATA_PACK, resourceKey.location().getPath());
		return CompletableFuture.allOf(
			(CompletableFuture[])registry.entrySet()
				.stream()
				.map(
					entry -> dumpValue(pathProvider.json(((ResourceKey)entry.getKey()).location()), cachedOutput, dynamicOps, registryData.elementCodec(), (T)entry.getValue())
				)
				.toArray(CompletableFuture[]::new)
		);
	}

	private static <E> CompletableFuture<?> dumpValue(Path path, CachedOutput cachedOutput, DynamicOps<JsonElement> dynamicOps, Encoder<E> encoder, E object) {
		Optional<JsonElement> optional = encoder.encodeStart(dynamicOps, object)
			.resultOrPartial(string -> LOGGER.error("Couldn't serialize element {}: {}", path, string));
		return optional.isPresent() ? DataProvider.saveStable(cachedOutput, (JsonElement)optional.get(), path) : CompletableFuture.completedFuture(null);
	}

	@Override
	public final String getName() {
		return "Worldgen";
	}
}
