package net.minecraft.server.packs.resources;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SimpleJsonResourceReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
	private static final Logger LOGGER = LogManager.getLogger();
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

		for (ResourceLocation resourceLocation : resourceManager.listResources(this.directory, stringx -> stringx.endsWith(".json"))) {
			String string = resourceLocation.getPath();
			ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), string.substring(i, string.length() - PATH_SUFFIX_LENGTH));

			try {
				Resource resource = resourceManager.getResource(resourceLocation);
				Throwable var10 = null;

				try {
					InputStream inputStream = resource.getInputStream();
					Throwable var12 = null;

					try {
						Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
						Throwable var14 = null;

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
						} catch (Throwable var62) {
							var14 = var62;
							throw var62;
						} finally {
							if (reader != null) {
								if (var14 != null) {
									try {
										reader.close();
									} catch (Throwable var61) {
										var14.addSuppressed(var61);
									}
								} else {
									reader.close();
								}
							}
						}
					} catch (Throwable var64) {
						var12 = var64;
						throw var64;
					} finally {
						if (inputStream != null) {
							if (var12 != null) {
								try {
									inputStream.close();
								} catch (Throwable var60) {
									var12.addSuppressed(var60);
								}
							} else {
								inputStream.close();
							}
						}
					}
				} catch (Throwable var66) {
					var10 = var66;
					throw var66;
				} finally {
					if (resource != null) {
						if (var10 != null) {
							try {
								resource.close();
							} catch (Throwable var59) {
								var10.addSuppressed(var59);
							}
						} else {
							resource.close();
						}
					}
				}
			} catch (IllegalArgumentException | IOException | JsonParseException var68) {
				LOGGER.error("Couldn't parse data file {} from {}", resourceLocation2, resourceLocation, var68);
			}
		}

		return map;
	}
}
