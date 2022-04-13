package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class WorldgenRegistryDumpReport implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final DataGenerator generator;

	public WorldgenRegistryDumpReport(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	@Override
	public void run(CachedOutput cachedOutput) {
		Path path = this.generator.getOutputFolder();
		RegistryAccess registryAccess = (RegistryAccess)RegistryAccess.BUILTIN.get();
		DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
		RegistryAccess.knownRegistries().forEach(registryData -> dumpRegistryCap(cachedOutput, path, registryAccess, dynamicOps, registryData));
	}

	private static <T> void dumpRegistryCap(
		CachedOutput cachedOutput, Path path, RegistryAccess registryAccess, DynamicOps<JsonElement> dynamicOps, RegistryAccess.RegistryData<T> registryData
	) {
		dumpRegistry(path, cachedOutput, dynamicOps, registryData.key(), registryAccess.ownedRegistryOrThrow(registryData.key()), registryData.codec());
	}

	private static <E, T extends Registry<E>> void dumpRegistry(
		Path path, CachedOutput cachedOutput, DynamicOps<JsonElement> dynamicOps, ResourceKey<? extends T> resourceKey, T registry, Encoder<E> encoder
	) {
		for (Entry<ResourceKey<E>, E> entry : registry.entrySet()) {
			Path path2 = createPath(path, resourceKey.location(), ((ResourceKey)entry.getKey()).location());
			dumpValue(path2, cachedOutput, dynamicOps, encoder, (E)entry.getValue());
		}
	}

	private static <E> void dumpValue(Path path, CachedOutput cachedOutput, DynamicOps<JsonElement> dynamicOps, Encoder<E> encoder, E object) {
		try {
			Optional<JsonElement> optional = encoder.encodeStart(dynamicOps, object)
				.resultOrPartial(string -> LOGGER.error("Couldn't serialize element {}: {}", path, string));
			if (optional.isPresent()) {
				DataProvider.save(GSON, cachedOutput, (JsonElement)optional.get(), path);
			}
		} catch (IOException var6) {
			LOGGER.error("Couldn't save element {}", path, var6);
		}
	}

	private static Path createPath(Path path, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		return resolveTopPath(path).resolve(resourceLocation2.getNamespace()).resolve(resourceLocation.getPath()).resolve(resourceLocation2.getPath() + ".json");
	}

	private static Path resolveTopPath(Path path) {
		return path.resolve("reports").resolve("worldgen");
	}

	@Override
	public String getName() {
		return "Worldgen";
	}
}
