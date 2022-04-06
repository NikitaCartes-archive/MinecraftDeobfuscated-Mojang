package net.minecraft.server.packs.resources;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String PATH_SUFFIX = ".json";
	private static final int PATH_SUFFIX_LENGTH = ".json".length();
	private final Gson gson;
	private final String directory;

	public SimpleJsonResourceReloadListener(Gson gson, String string) {
		this.gson = gson;
		this.directory = string;
	}

	protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		Map<ResourceLocation, JsonElement> map = Maps.<ResourceLocation, JsonElement>newHashMap();
		int i = this.directory.length() + 1;

		for (Entry<ResourceLocation, Resource> entry : resourceManager.listResources(
				this.directory, resourceLocationx -> resourceLocationx.getPath().endsWith(".json")
			)
			.entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
			String string = resourceLocation.getPath();
			ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), string.substring(i, string.length() - PATH_SUFFIX_LENGTH));

			try {
				Reader reader = ((Resource)entry.getValue()).openAsReader();

				try {
					JsonElement jsonElement = GsonHelper.fromJson(this.gson, reader, JsonElement.class);
					if (jsonElement != null) {
						JsonElement jsonElement2 = (JsonElement)map.put(resourceLocation2, jsonElement);
						if (jsonElement2 != null) {
							throw new IllegalStateException("Duplicate data file ignored with ID " + resourceLocation2);
						}
					} else {
						LOGGER.error("Couldn't load data file {} from {} as it's null or empty", resourceLocation2, resourceLocation);
					}
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
				LOGGER.error("Couldn't parse data file {} from {}", resourceLocation2, resourceLocation, var15);
			}
		}

		return map;
	}
}
