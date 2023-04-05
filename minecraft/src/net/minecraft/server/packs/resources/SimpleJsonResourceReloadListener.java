package net.minecraft.server.packs.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Gson gson;
	private final String directory;

	public SimpleJsonResourceReloadListener(Gson gson, String string) {
		this.gson = gson;
		this.directory = string;
	}

	protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		Map<ResourceLocation, JsonElement> map = new HashMap();
		scanDirectory(resourceManager, this.directory, this.gson, map);
		return map;
	}

	public static void scanDirectory(ResourceManager resourceManager, String string, Gson gson, Map<ResourceLocation, JsonElement> map) {
		FileToIdConverter fileToIdConverter = FileToIdConverter.json(string);

		for (Entry<ResourceLocation, Resource> entry : fileToIdConverter.listMatchingResources(resourceManager).entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
			ResourceLocation resourceLocation2 = fileToIdConverter.fileToId(resourceLocation);

			try {
				Reader reader = ((Resource)entry.getValue()).openAsReader();

				try {
					JsonElement jsonElement = GsonHelper.fromJson(gson, reader, JsonElement.class);
					JsonElement jsonElement2 = (JsonElement)map.put(resourceLocation2, jsonElement);
					if (jsonElement2 != null) {
						throw new IllegalStateException("Duplicate data file ignored with ID " + resourceLocation2);
					}
				} catch (Throwable var13) {
					if (reader != null) {
						try {
							reader.close();
						} catch (Throwable var12) {
							var13.addSuppressed(var12);
						}
					}

					throw var13;
				}

				if (reader != null) {
					reader.close();
				}
			} catch (IllegalArgumentException | IOException | JsonParseException var14) {
				LOGGER.error("Couldn't parse data file {} from {}", resourceLocation2, resourceLocation, var14);
			}
		}
	}
}
