package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
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
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import org.slf4j.Logger;

public class TagLoader<T> {
	private static final Logger LOGGER = LogUtils.getLogger();
	final Function<ResourceLocation, Optional<? extends T>> idToValue;
	private final String directory;

	public TagLoader(Function<ResourceLocation, Optional<? extends T>> function, String string) {
		this.idToValue = function;
		this.directory = string;
	}

	public Map<ResourceLocation, List<TagLoader.EntryWithSource>> load(ResourceManager resourceManager) {
		Map<ResourceLocation, List<TagLoader.EntryWithSource>> map = Maps.<ResourceLocation, List<TagLoader.EntryWithSource>>newHashMap();
		FileToIdConverter fileToIdConverter = FileToIdConverter.json(this.directory);

		for (Entry<ResourceLocation, List<Resource>> entry : fileToIdConverter.listMatchingResourceStacks(resourceManager).entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
			ResourceLocation resourceLocation2 = fileToIdConverter.fileToId(resourceLocation);

			for (Resource resource : (List)entry.getValue()) {
				try {
					Reader reader = resource.openAsReader();

					try {
						JsonElement jsonElement = JsonParser.parseReader(reader);
						List<TagLoader.EntryWithSource> list = (List<TagLoader.EntryWithSource>)map.computeIfAbsent(resourceLocation2, resourceLocationx -> new ArrayList());
						TagFile tagFile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, jsonElement)).getOrThrow();
						if (tagFile.replace()) {
							list.clear();
						}

						String string = resource.sourcePackId();
						tagFile.entries().forEach(tagEntry -> list.add(new TagLoader.EntryWithSource(tagEntry, string)));
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
		DependencySorter<ResourceLocation, TagLoader.SortingEntry> dependencySorter = new DependencySorter<>();
		map.forEach((resourceLocation, list) -> dependencySorter.addEntry(resourceLocation, new TagLoader.SortingEntry(list)));
		dependencySorter.orderByDependencies(
			(resourceLocation, sortingEntry) -> this.build(lookup, sortingEntry.entries)
					.ifLeft(
						collection -> LOGGER.error(
								"Couldn't load tag {} as it is missing following references: {}",
								resourceLocation,
								collection.stream().map(Objects::toString).collect(Collectors.joining(", "))
							)
					)
					.ifRight(collection -> map2.put(resourceLocation, collection))
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

	static record SortingEntry(List<TagLoader.EntryWithSource> entries) implements DependencySorter.Entry<ResourceLocation> {

		@Override
		public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
			this.entries.forEach(entryWithSource -> entryWithSource.entry.visitRequiredDependencies(consumer));
		}

		@Override
		public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
			this.entries.forEach(entryWithSource -> entryWithSource.entry.visitOptionalDependencies(consumer));
		}
	}
}
