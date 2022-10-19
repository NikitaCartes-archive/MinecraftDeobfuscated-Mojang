package net.minecraft.data.worldgen;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Map.Entry;
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
	public void run(CachedOutput cachedOutput) {
		RegistryAccess registryAccess = BuiltinRegistries.createAccess();
		DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
		RegistryDataLoader.WORLDGEN_REGISTRIES.forEach(registryData -> this.dumpRegistryCap(cachedOutput, registryAccess, dynamicOps, registryData));
	}

	private <T> void dumpRegistryCap(
		CachedOutput cachedOutput, RegistryAccess registryAccess, DynamicOps<JsonElement> dynamicOps, RegistryDataLoader.RegistryData<T> registryData
	) {
		ResourceKey<? extends Registry<T>> resourceKey = registryData.key();
		Registry<T> registry = registryAccess.registryOrThrow(resourceKey);
		PackOutput.PathProvider pathProvider = this.output.createPathProvider(PackOutput.Target.DATA_PACK, resourceKey.location().getPath());

		for (Entry<ResourceKey<T>, T> entry : registry.entrySet()) {
			dumpValue(pathProvider.json(((ResourceKey)entry.getKey()).location()), cachedOutput, dynamicOps, registryData.elementCodec(), (T)entry.getValue());
		}
	}

	private static <E> void dumpValue(Path path, CachedOutput cachedOutput, DynamicOps<JsonElement> dynamicOps, Encoder<E> encoder, E object) {
		try {
			Optional<JsonElement> optional = encoder.encodeStart(dynamicOps, object)
				.resultOrPartial(string -> LOGGER.error("Couldn't serialize element {}: {}", path, string));
			if (optional.isPresent()) {
				DataProvider.saveStable(cachedOutput, (JsonElement)optional.get(), path);
			}
		} catch (IOException var6) {
			LOGGER.error("Couldn't save element {}", path, var6);
		}
	}

	@Override
	public String getName() {
		return "Worldgen";
	}
}
