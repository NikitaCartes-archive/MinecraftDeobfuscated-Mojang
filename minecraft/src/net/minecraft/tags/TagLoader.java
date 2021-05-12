package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagLoader<T> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new Gson();
	private static final String PATH_SUFFIX = ".json";
	private static final int PATH_SUFFIX_LENGTH = ".json".length();
	private final Function<ResourceLocation, Optional<T>> idToValue;
	private final String directory;

	public TagLoader(Function<ResourceLocation, Optional<T>> function, String string) {
		this.idToValue = function;
		this.directory = string;
	}

	public Map<ResourceLocation, Tag.Builder> load(ResourceManager resourceManager) {
		Map<ResourceLocation, Tag.Builder> map = Maps.<ResourceLocation, Tag.Builder>newHashMap();

		for (ResourceLocation resourceLocation : resourceManager.listResources(this.directory, stringx -> stringx.endsWith(".json"))) {
			String string = resourceLocation.getPath();
			ResourceLocation resourceLocation2 = new ResourceLocation(
				resourceLocation.getNamespace(), string.substring(this.directory.length() + 1, string.length() - PATH_SUFFIX_LENGTH)
			);

			try {
				for (Resource resource : resourceManager.getResources(resourceLocation)) {
					try {
						InputStream inputStream = resource.getInputStream();

						try {
							Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

							try {
								JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
								if (jsonObject == null) {
									LOGGER.error("Couldn't load tag list {} from {} in data pack {} as it is empty or null", resourceLocation2, resourceLocation, resource.getSourceName());
								} else {
									((Tag.Builder)map.computeIfAbsent(resourceLocation2, resourceLocationx -> Tag.Builder.tag())).addFromJson(jsonObject, resource.getSourceName());
								}
							} catch (Throwable var23) {
								try {
									reader.close();
								} catch (Throwable var22) {
									var23.addSuppressed(var22);
								}

								throw var23;
							}

							reader.close();
						} catch (Throwable var24) {
							if (inputStream != null) {
								try {
									inputStream.close();
								} catch (Throwable var21) {
									var24.addSuppressed(var21);
								}
							}

							throw var24;
						}

						if (inputStream != null) {
							inputStream.close();
						}
					} catch (RuntimeException | IOException var25) {
						LOGGER.error("Couldn't read tag list {} from {} in data pack {}", resourceLocation2, resourceLocation, resource.getSourceName(), var25);
					} finally {
						IOUtils.closeQuietly(resource);
					}
				}
			} catch (IOException var27) {
				LOGGER.error("Couldn't read tag list {} from {}", resourceLocation2, resourceLocation, var27);
			}
		}

		return map;
	}

	private static void visitDependenciesAndElement(
		Map<ResourceLocation, Tag.Builder> map,
		Multimap<ResourceLocation, ResourceLocation> multimap,
		Set<ResourceLocation> set,
		ResourceLocation resourceLocation,
		BiConsumer<ResourceLocation, Tag.Builder> biConsumer
	) {
		if (set.add(resourceLocation)) {
			multimap.get(resourceLocation).forEach(resourceLocationx -> visitDependenciesAndElement(map, multimap, set, resourceLocationx, biConsumer));
			Tag.Builder builder = (Tag.Builder)map.get(resourceLocation);
			if (builder != null) {
				biConsumer.accept(resourceLocation, builder);
			}
		}
	}

	private static boolean isCyclic(Multimap<ResourceLocation, ResourceLocation> multimap, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		Collection<ResourceLocation> collection = multimap.get(resourceLocation2);
		return collection.contains(resourceLocation)
			? true
			: collection.stream().anyMatch(resourceLocation2x -> isCyclic(multimap, resourceLocation, resourceLocation2x));
	}

	private static void addDependencyIfNotCyclic(
		Multimap<ResourceLocation, ResourceLocation> multimap, ResourceLocation resourceLocation, ResourceLocation resourceLocation2
	) {
		if (!isCyclic(multimap, resourceLocation, resourceLocation2)) {
			multimap.put(resourceLocation, resourceLocation2);
		}
	}

	public TagCollection<T> build(Map<ResourceLocation, Tag.Builder> map) {
		Map<ResourceLocation, Tag<T>> map2 = Maps.<ResourceLocation, Tag<T>>newHashMap();
		Function<ResourceLocation, Tag<T>> function = map2::get;
		Function<ResourceLocation, T> function2 = resourceLocation -> ((Optional)this.idToValue.apply(resourceLocation)).orElse(null);
		Multimap<ResourceLocation, ResourceLocation> multimap = HashMultimap.create();
		map.forEach(
			(resourceLocation, builder) -> builder.visitRequiredDependencies(
					resourceLocation2 -> addDependencyIfNotCyclic(multimap, resourceLocation, resourceLocation2)
				)
		);
		map.forEach(
			(resourceLocation, builder) -> builder.visitOptionalDependencies(
					resourceLocation2 -> addDependencyIfNotCyclic(multimap, resourceLocation, resourceLocation2)
				)
		);
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();
		map.keySet()
			.forEach(
				resourceLocation -> visitDependenciesAndElement(
						map,
						multimap,
						set,
						resourceLocation,
						(resourceLocationx, builder) -> builder.build(function, function2)
								.ifLeft(
									collection -> LOGGER.error(
											"Couldn't load tag {} as it is missing following references: {}",
											resourceLocationx,
											collection.stream().map(Objects::toString).collect(Collectors.joining(","))
										)
								)
								.ifRight(tag -> map2.put(resourceLocationx, tag))
					)
			);
		return TagCollection.of(map2);
	}

	public TagCollection<T> loadAndBuild(ResourceManager resourceManager) {
		return this.build(this.load(resourceManager));
	}
}
