package net.minecraft.tags;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
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
		Map<ResourceLocation, List<TagLoader.EntryWithSource>> map = new HashMap();
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

	private Either<List<TagLoader.EntryWithSource>, List<T>> tryBuildTag(TagEntry.Lookup<T> lookup, List<TagLoader.EntryWithSource> list) {
		SequencedSet<T> sequencedSet = new LinkedHashSet();
		List<TagLoader.EntryWithSource> list2 = new ArrayList();

		for (TagLoader.EntryWithSource entryWithSource : list) {
			if (!entryWithSource.entry().build(lookup, sequencedSet::add)) {
				list2.add(entryWithSource);
			}
		}

		return list2.isEmpty() ? Either.right(List.copyOf(sequencedSet)) : Either.left(list2);
	}

	public Map<ResourceLocation, List<T>> build(Map<ResourceLocation, List<TagLoader.EntryWithSource>> map) {
		final Map<ResourceLocation, List<T>> map2 = new HashMap();
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
			(resourceLocation, sortingEntry) -> this.tryBuildTag(lookup, sortingEntry.entries)
					.ifLeft(
						list -> LOGGER.error(
								"Couldn't load tag {} as it is missing following references: {}",
								resourceLocation,
								list.stream().map(Objects::toString).collect(Collectors.joining(", "))
							)
					)
					.ifRight(list -> map2.put(resourceLocation, list))
		);
		return map2;
	}

	public static <T> void loadTagsFromNetwork(TagNetworkSerialization.NetworkPayload networkPayload, WritableRegistry<T> writableRegistry) {
		networkPayload.resolve(writableRegistry).tags.forEach(writableRegistry::bindTag);
	}

	public static List<Registry.PendingTags<?>> loadTagsForExistingRegistries(ResourceManager resourceManager, RegistryAccess registryAccess) {
		return (List<Registry.PendingTags<?>>)registryAccess.registries()
			.map(registryEntry -> loadPendingTags(resourceManager, registryEntry.value()))
			.flatMap(Optional::stream)
			.collect(Collectors.toUnmodifiableList());
	}

	public static <T> void loadTagsForRegistry(ResourceManager resourceManager, WritableRegistry<T> writableRegistry) {
		ResourceKey<? extends Registry<T>> resourceKey = writableRegistry.key();
		HolderGetter<T> holderGetter = writableRegistry.createRegistrationLookup();
		TagLoader<Holder<T>> tagLoader = new TagLoader<>(
			resourceLocation -> holderGetter.get(ResourceKey.create(resourceKey, resourceLocation)), Registries.tagsDirPath(resourceKey)
		);
		tagLoader.build(tagLoader.load(resourceManager))
			.forEach((resourceLocation, list) -> writableRegistry.bindTag(TagKey.create(resourceKey, resourceLocation), list));
	}

	private static <T> Map<TagKey<T>, List<Holder<T>>> wrapTags(ResourceKey<? extends Registry<T>> resourceKey, Map<ResourceLocation, List<Holder<T>>> map) {
		return (Map<TagKey<T>, List<Holder<T>>>)map.entrySet()
			.stream()
			.collect(Collectors.toUnmodifiableMap(entry -> TagKey.create(resourceKey, (ResourceLocation)entry.getKey()), Entry::getValue));
	}

	private static <T> Optional<Registry.PendingTags<T>> loadPendingTags(ResourceManager resourceManager, Registry<T> registry) {
		ResourceKey<? extends Registry<T>> resourceKey = registry.key();
		TagLoader<Holder<T>> tagLoader = new TagLoader<>(registry::getHolder, Registries.tagsDirPath(resourceKey));
		TagLoader.LoadResult<T> loadResult = new TagLoader.LoadResult<>(resourceKey, wrapTags(registry.key(), tagLoader.build(tagLoader.load(resourceManager))));
		return loadResult.tags().isEmpty() ? Optional.empty() : Optional.of(registry.prepareTagReload(loadResult));
	}

	public static List<HolderLookup.RegistryLookup<?>> buildUpdatedLookups(RegistryAccess.Frozen frozen, List<Registry.PendingTags<?>> list) {
		List<HolderLookup.RegistryLookup<?>> list2 = new ArrayList();
		frozen.registries().forEach(registryEntry -> {
			Registry.PendingTags<?> pendingTags = findTagsForRegistry(list, registryEntry.key());
			list2.add(pendingTags != null ? pendingTags.lookup() : registryEntry.value().asLookup());
		});
		return list2;
	}

	@Nullable
	private static Registry.PendingTags<?> findTagsForRegistry(List<Registry.PendingTags<?>> list, ResourceKey<? extends Registry<?>> resourceKey) {
		for (Registry.PendingTags<?> pendingTags : list) {
			if (pendingTags.key() == resourceKey) {
				return pendingTags;
			}
		}

		return null;
	}

	public static record EntryWithSource(TagEntry entry, String source) {

		public String toString() {
			return this.entry + " (from " + this.source + ")";
		}
	}

	public static record LoadResult<T>(ResourceKey<? extends Registry<T>> key, Map<TagKey<T>, List<Holder<T>>> tags) {
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
