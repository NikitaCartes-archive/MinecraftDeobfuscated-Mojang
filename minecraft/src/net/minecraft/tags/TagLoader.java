package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
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
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public class TagLoader<T> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String PATH_SUFFIX = ".json";
	private static final int PATH_SUFFIX_LENGTH = ".json".length();
	final Function<ResourceLocation, Optional<T>> idToValue;
	private final String directory;

	public TagLoader(Function<ResourceLocation, Optional<T>> function, String string) {
		this.idToValue = function;
		this.directory = string;
	}

	public Map<ResourceLocation, List<TagLoader.EntryWithSource>> load(ResourceManager resourceManager) {
		Map<ResourceLocation, List<TagLoader.EntryWithSource>> map = Maps.<ResourceLocation, List<TagLoader.EntryWithSource>>newHashMap();

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
						JsonElement jsonElement = JsonParser.parseReader(reader);
						List<TagLoader.EntryWithSource> list = (List<TagLoader.EntryWithSource>)map.computeIfAbsent(resourceLocation2, resourceLocationx -> new ArrayList());
						TagFile tagFile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, jsonElement)).getOrThrow(false, LOGGER::error);
						if (tagFile.replace()) {
							list.clear();
						}

						String string2 = resource.sourcePackId();
						tagFile.entries().forEach(tagEntry -> list.add(new TagLoader.EntryWithSource(tagEntry, string2)));
					} catch (Throwable var16) {
						if (reader != null) {
							try {
								reader.close();
							} catch (Throwable var15) {
								var16.addSuppressed(var15);
							}
						}

						throw var16;
					}

					if (reader != null) {
						reader.close();
					}
				} catch (Exception var17) {
					LOGGER.error("Couldn't read tag list {} from {} in data pack {}", resourceLocation2, resourceLocation, resource.sourcePackId(), var17);
				}
			}
		}

		return map;
	}

	private static void visitDependenciesAndElement(
		Map<ResourceLocation, List<TagLoader.EntryWithSource>> map,
		Multimap<ResourceLocation, ResourceLocation> multimap,
		Set<ResourceLocation> set,
		ResourceLocation resourceLocation,
		BiConsumer<ResourceLocation, List<TagLoader.EntryWithSource>> biConsumer
	) {
		if (set.add(resourceLocation)) {
			multimap.get(resourceLocation).forEach(resourceLocationx -> visitDependenciesAndElement(map, multimap, set, resourceLocationx, biConsumer));
			List<TagLoader.EntryWithSource> list = (List<TagLoader.EntryWithSource>)map.get(resourceLocation);
			if (list != null) {
				biConsumer.accept(resourceLocation, list);
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

	private Either<Collection<TagLoader.EntryWithSource>, Collection<T>> build(TagEntry.Lookup<T> lookup, List<TagLoader.EntryWithSource> list) {
		Builder<T> builder = ImmutableSet.builder();
		List<TagLoader.EntryWithSource> list2 = new ArrayList();

		for (TagLoader.EntryWithSource entryWithSource : list) {
			if (!entryWithSource.entry().build(lookup, builder::add)) {
				list2.add(entryWithSource);
			}
		}

		return list2.isEmpty() ? Either.right(builder.build()) : Either.left(list2);
	}

	public Map<ResourceLocation, Collection<T>> build(Map<ResourceLocation, List<TagLoader.EntryWithSource>> map) {
		final Map<ResourceLocation, Collection<T>> map2 = Maps.<ResourceLocation, Collection<T>>newHashMap();
		TagEntry.Lookup<T> lookup = new TagEntry.Lookup<T>() {
			@Nullable
			@Override
			public T element(ResourceLocation resourceLocation) {
				return (T)((Optional)TagLoader.this.idToValue.apply(resourceLocation)).orElse(null);
			}

			@Nullable
			@Override
			public Collection<T> tag(ResourceLocation resourceLocation) {
				return (Collection<T>)map2.get(resourceLocation);
			}
		};
		Multimap<ResourceLocation, ResourceLocation> multimap = HashMultimap.create();
		map.forEach(
			(resourceLocation, list) -> list.forEach(
					entryWithSource -> entryWithSource.entry
							.visitRequiredDependencies(resourceLocation2 -> addDependencyIfNotCyclic(multimap, resourceLocation, resourceLocation2))
				)
		);
		map.forEach(
			(resourceLocation, list) -> list.forEach(
					entryWithSource -> entryWithSource.entry
							.visitOptionalDependencies(resourceLocation2 -> addDependencyIfNotCyclic(multimap, resourceLocation, resourceLocation2))
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
						(resourceLocationx, list) -> this.build(lookup, list)
								.ifLeft(
									collection -> LOGGER.error(
											"Couldn't load tag {} as it is missing following references: {}",
											resourceLocationx,
											collection.stream().map(Objects::toString).collect(Collectors.joining(", "))
										)
								)
								.ifRight(collection -> map2.put(resourceLocationx, collection))
					)
			);
		return map2;
	}

	public Map<ResourceLocation, Collection<T>> loadAndBuild(ResourceManager resourceManager) {
		return this.build(this.load(resourceManager));
	}

	public static record EntryWithSource(TagEntry entry, String source) {

		public String toString() {
			return this.entry + " (from " + this.source + ")";
		}
	}
}
