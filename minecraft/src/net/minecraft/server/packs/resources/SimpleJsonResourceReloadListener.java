package net.minecraft.server.packs.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener<T> extends SimplePreparableReloadListener<Map<ResourceLocation, T>> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final DynamicOps<JsonElement> ops;
	private final Codec<T> codec;
	private final String directory;

	protected SimpleJsonResourceReloadListener(HolderLookup.Provider provider, Codec<T> codec, String string) {
		this(provider.createSerializationContext(JsonOps.INSTANCE), codec, string);
	}

	protected SimpleJsonResourceReloadListener(Codec<T> codec, String string) {
		this(JsonOps.INSTANCE, codec, string);
	}

	private SimpleJsonResourceReloadListener(DynamicOps<JsonElement> dynamicOps, Codec<T> codec, String string) {
		this.ops = dynamicOps;
		this.codec = codec;
		this.directory = string;
	}

	protected Map<ResourceLocation, T> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		Map<ResourceLocation, T> map = new HashMap();
		scanDirectory(resourceManager, this.directory, this.ops, this.codec, map);
		return map;
	}

	public static <T> void scanDirectory(
		ResourceManager resourceManager, String string, DynamicOps<JsonElement> dynamicOps, Codec<T> codec, Map<ResourceLocation, T> map
	) {
		FileToIdConverter fileToIdConverter = FileToIdConverter.json(string);

		for (Entry<ResourceLocation, Resource> entry : fileToIdConverter.listMatchingResources(resourceManager).entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
			ResourceLocation resourceLocation2 = fileToIdConverter.fileToId(resourceLocation);

			try {
				Reader reader = ((Resource)entry.getValue()).openAsReader();

				try {
					codec.parse(dynamicOps, JsonParser.parseReader(reader)).ifSuccess(object -> {
						if (map.putIfAbsent(resourceLocation2, object) != null) {
							throw new IllegalStateException("Duplicate data file ignored with ID " + resourceLocation2);
						}
					}).ifError(error -> LOGGER.error("Couldn't parse data file '{}' from '{}': {}", resourceLocation2, resourceLocation, error));
				} catch (Throwable var14) {
					if (reader != null) {
						try {
							reader.close();
						} catch (Throwable var13) {
							var14.addSuppressed(var13);
						}
					}

					throw var14;
				}

				if (reader != null) {
					reader.close();
				}
			} catch (IllegalArgumentException | IOException | JsonParseException var15) {
				LOGGER.error("Couldn't parse data file '{}' from '{}'", resourceLocation2, resourceLocation, var15);
			}
		}
	}
}
