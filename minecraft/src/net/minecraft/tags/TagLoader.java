package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public class TagLoader<T> {
	private static final Logger LOGGER = LogUtils.getLogger();
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

		for (Entry<ResourceLocation, List<Resource>> entry : resourceManager.listResourceStacks(
				this.directory, resourceLocationx -> resourceLocationx.getPath().endsWith(".json")
			)
			.entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
			String string = resourceLocation.getPath();
			ResourceLocation resourceLocation2 = new ResourceLocation(
				resourceLocation.getNamespace(), string.substring(this.directory.length() + 1, string.length() - PATH_SUFFIX_LENGTH)
			);

			for (Resource resource : (List)entry.getValue()) {
				try {
					Reader reader = resource.openAsReader();

					try {
						JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
						if (jsonObject == null) {
							throw new NullPointerException("Invalid JSON contents");
						}

						((Tag.Builder)map.computeIfAbsent(resourceLocation2, resourceLocationx -> Tag.Builder.tag())).addFromJson(jsonObject, resource.sourcePackId());
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
				} catch (Exception var15) {
					LOGGER.error("Couldn't read tag list {} from {} in data pack {}", resourceLocation2, resourceLocation, resource.sourcePackId(), var15);
				}
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

	public Map<ResourceLocation, Tag<T>> build(Map<ResourceLocation, Tag.Builder> map) {
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
											collection.stream().map(Objects::toString).collect(Collectors.joining(", "))
										)
								)
								.ifRight(tag -> map2.put(resourceLocationx, tag))
					)
			);
		return map2;
	}

	public Map<ResourceLocation, Tag<T>> loadAndBuild(ResourceManager resourceManager) {
		return this.build(this.load(resourceManager));
	}
}
