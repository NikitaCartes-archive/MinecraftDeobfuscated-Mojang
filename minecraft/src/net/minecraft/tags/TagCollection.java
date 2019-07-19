package net.minecraft.tags;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagCollection<T> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new Gson();
	private static final int PATH_SUFFIX_LENGTH = ".json".length();
	private Map<ResourceLocation, Tag<T>> tags = ImmutableMap.of();
	private final Function<ResourceLocation, Optional<T>> idToValue;
	private final String directory;
	private final boolean ordered;
	private final String name;

	public TagCollection(Function<ResourceLocation, Optional<T>> function, String string, boolean bl, String string2) {
		this.idToValue = function;
		this.directory = string;
		this.ordered = bl;
		this.name = string2;
	}

	@Nullable
	public Tag<T> getTag(ResourceLocation resourceLocation) {
		return (Tag<T>)this.tags.get(resourceLocation);
	}

	public Tag<T> getTagOrEmpty(ResourceLocation resourceLocation) {
		Tag<T> tag = (Tag<T>)this.tags.get(resourceLocation);
		return tag == null ? new Tag<>(resourceLocation) : tag;
	}

	public Collection<ResourceLocation> getAvailableTags() {
		return this.tags.keySet();
	}

	@Environment(EnvType.CLIENT)
	public Collection<ResourceLocation> getMatchingTags(T object) {
		List<ResourceLocation> list = Lists.<ResourceLocation>newArrayList();

		for (Entry<ResourceLocation, Tag<T>> entry : this.tags.entrySet()) {
			if (((Tag)entry.getValue()).contains(object)) {
				list.add(entry.getKey());
			}
		}

		return list;
	}

	public CompletableFuture<Map<ResourceLocation, Tag.Builder<T>>> prepare(ResourceManager resourceManager, Executor executor) {
		return CompletableFuture.supplyAsync(
			() -> {
				Map<ResourceLocation, Tag.Builder<T>> map = Maps.<ResourceLocation, Tag.Builder<T>>newHashMap();

				for (ResourceLocation resourceLocation : resourceManager.listResources(this.directory, stringx -> stringx.endsWith(".json"))) {
					String string = resourceLocation.getPath();
					ResourceLocation resourceLocation2 = new ResourceLocation(
						resourceLocation.getNamespace(), string.substring(this.directory.length() + 1, string.length() - PATH_SUFFIX_LENGTH)
					);

					try {
						for (Resource resource : resourceManager.getResources(resourceLocation)) {
							try {
								InputStream inputStream = resource.getInputStream();
								Throwable var10 = null;

								try {
									Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
									Throwable var12 = null;

									try {
										JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
										if (jsonObject == null) {
											LOGGER.error(
												"Couldn't load {} tag list {} from {} in data pack {} as it's empty or null",
												this.name,
												resourceLocation2,
												resourceLocation,
												resource.getSourceName()
											);
										} else {
											((Tag.Builder)map.computeIfAbsent(resourceLocation2, resourceLocationx -> Util.make(Tag.Builder.tag(), builder -> builder.keepOrder(this.ordered))))
												.addFromJson(this.idToValue, jsonObject);
										}
									} catch (Throwable var53) {
										var12 = var53;
										throw var53;
									} finally {
										if (reader != null) {
											if (var12 != null) {
												try {
													reader.close();
												} catch (Throwable var52) {
													var12.addSuppressed(var52);
												}
											} else {
												reader.close();
											}
										}
									}
								} catch (Throwable var55) {
									var10 = var55;
									throw var55;
								} finally {
									if (inputStream != null) {
										if (var10 != null) {
											try {
												inputStream.close();
											} catch (Throwable var51) {
												var10.addSuppressed(var51);
											}
										} else {
											inputStream.close();
										}
									}
								}
							} catch (RuntimeException | IOException var57) {
								LOGGER.error("Couldn't read {} tag list {} from {} in data pack {}", this.name, resourceLocation2, resourceLocation, resource.getSourceName(), var57);
							} finally {
								IOUtils.closeQuietly(resource);
							}
						}
					} catch (IOException var59) {
						LOGGER.error("Couldn't read {} tag list {} from {}", this.name, resourceLocation2, resourceLocation, var59);
					}
				}

				return map;
			},
			executor
		);
	}

	public void load(Map<ResourceLocation, Tag.Builder<T>> map) {
		Map<ResourceLocation, Tag<T>> map2 = Maps.<ResourceLocation, Tag<T>>newHashMap();

		while (!map.isEmpty()) {
			boolean bl = false;
			Iterator<Entry<ResourceLocation, Tag.Builder<T>>> iterator = map.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<ResourceLocation, Tag.Builder<T>> entry = (Entry<ResourceLocation, Tag.Builder<T>>)iterator.next();
				Tag.Builder<T> builder = (Tag.Builder<T>)entry.getValue();
				if (builder.canBuild(map2::get)) {
					bl = true;
					ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
					map2.put(resourceLocation, builder.build(resourceLocation));
					iterator.remove();
				}
			}

			if (!bl) {
				map.forEach(
					(resourceLocationx, builderx) -> LOGGER.error(
							"Couldn't load {} tag {} as it either references another tag that doesn't exist, or ultimately references itself", this.name, resourceLocationx
						)
				);
				break;
			}
		}

		map.forEach((resourceLocationx, builderx) -> {
			Tag var10000 = (Tag)map2.put(resourceLocationx, builderx.build(resourceLocationx));
		});
		this.replace(map2);
	}

	protected void replace(Map<ResourceLocation, Tag<T>> map) {
		this.tags = ImmutableMap.copyOf(map);
	}

	public Map<ResourceLocation, Tag<T>> getAllTags() {
		return this.tags;
	}
}
