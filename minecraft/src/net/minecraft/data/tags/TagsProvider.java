package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import org.slf4j.Logger;

public abstract class TagsProvider<T> implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	protected final PackOutput.PathProvider pathProvider;
	private final CompletableFuture<HolderLookup.Provider> contentsProvider;
	private final CompletableFuture<TagsProvider.TagLookup<T>> parentProvider;
	protected final ResourceKey<? extends Registry<T>> registryKey;
	private final Map<ResourceLocation, TagBuilder> builders = Maps.<ResourceLocation, TagBuilder>newLinkedHashMap();

	protected TagsProvider(PackOutput packOutput, ResourceKey<? extends Registry<T>> resourceKey, CompletableFuture<HolderLookup.Provider> completableFuture) {
		this(packOutput, resourceKey, completableFuture, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()));
	}

	protected TagsProvider(
		PackOutput packOutput,
		ResourceKey<? extends Registry<T>> resourceKey,
		CompletableFuture<HolderLookup.Provider> completableFuture,
		CompletableFuture<TagsProvider.TagLookup<T>> completableFuture2
	) {
		this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, TagManager.getTagDir(resourceKey));
		this.registryKey = resourceKey;
		this.parentProvider = completableFuture2;
		this.contentsProvider = completableFuture.thenApply(provider -> {
			this.builders.clear();
			this.addTags(provider);
			return provider;
		});
	}

	@Override
	public final String getName() {
		return "Tags for " + this.registryKey.location();
	}

	protected abstract void addTags(HolderLookup.Provider provider);

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		record CombinedData<T>(HolderLookup.Provider contents, TagsProvider.TagLookup<T> parent) {
		}

		return this.contentsProvider()
			.thenCombineAsync(this.parentProvider, (provider, tagLookup) -> new CombinedData(provider, tagLookup))
			.thenCompose(
				arg -> {
					HolderLookup.RegistryLookup<T> registryLookup = arg.contents.lookupOrThrow(this.registryKey);
					Predicate<ResourceLocation> predicate = resourceLocation -> registryLookup.get(ResourceKey.create(this.registryKey, resourceLocation)).isPresent();
					Predicate<ResourceLocation> predicate2 = resourceLocation -> this.builders.containsKey(resourceLocation)
							|| arg.parent.contains(TagKey.create(this.registryKey, resourceLocation));
					return CompletableFuture.allOf(
						(CompletableFuture[])this.builders
							.entrySet()
							.stream()
							.map(
								entry -> {
									ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
									TagBuilder tagBuilder = (TagBuilder)entry.getValue();
									List<TagEntry> list = tagBuilder.build();
									List<TagEntry> list2 = list.stream().filter(tagEntry -> !tagEntry.verifyIfPresent(predicate, predicate2)).toList();
									if (!list2.isEmpty()) {
										throw new IllegalArgumentException(
											String.format(
												Locale.ROOT,
												"Couldn't define tag %s as it is missing following references: %s",
												resourceLocation,
												list2.stream().map(Objects::toString).collect(Collectors.joining(","))
											)
										);
									} else {
										JsonElement jsonElement = TagFile.CODEC.encodeStart(JsonOps.INSTANCE, new TagFile(list, false)).getOrThrow(false, LOGGER::error);
										Path path = this.pathProvider.json(resourceLocation);
										return DataProvider.saveStable(cachedOutput, jsonElement, path);
									}
								}
							)
							.toArray(CompletableFuture[]::new)
					);
				}
			);
	}

	protected TagsProvider.TagAppender<T> tag(TagKey<T> tagKey) {
		TagBuilder tagBuilder = this.getOrCreateRawBuilder(tagKey);
		return new TagsProvider.TagAppender<>(tagBuilder);
	}

	protected TagBuilder getOrCreateRawBuilder(TagKey<T> tagKey) {
		return (TagBuilder)this.builders.computeIfAbsent(tagKey.location(), resourceLocation -> TagBuilder.create());
	}

	public CompletableFuture<TagsProvider.TagLookup<T>> contentsGetter() {
		return this.contentsProvider().thenApply(provider -> tagKey -> Optional.ofNullable((TagBuilder)this.builders.get(tagKey.location())));
	}

	protected CompletableFuture<HolderLookup.Provider> contentsProvider() {
		return this.contentsProvider;
	}

	protected static class TagAppender<T> {
		private final TagBuilder builder;

		protected TagAppender(TagBuilder tagBuilder) {
			this.builder = tagBuilder;
		}

		public final TagsProvider.TagAppender<T> add(ResourceKey<T> resourceKey) {
			this.builder.addElement(resourceKey.location());
			return this;
		}

		@SafeVarargs
		public final TagsProvider.TagAppender<T> add(ResourceKey<T>... resourceKeys) {
			for (ResourceKey<T> resourceKey : resourceKeys) {
				this.builder.addElement(resourceKey.location());
			}

			return this;
		}

		public TagsProvider.TagAppender<T> addOptional(ResourceLocation resourceLocation) {
			this.builder.addOptionalElement(resourceLocation);
			return this;
		}

		public TagsProvider.TagAppender<T> addTag(TagKey<T> tagKey) {
			this.builder.addTag(tagKey.location());
			return this;
		}

		public TagsProvider.TagAppender<T> addOptionalTag(ResourceLocation resourceLocation) {
			this.builder.addOptionalTag(resourceLocation);
			return this;
		}
	}

	@FunctionalInterface
	public interface TagLookup<T> extends Function<TagKey<T>, Optional<TagBuilder>> {
		static <T> TagsProvider.TagLookup<T> empty() {
			return tagKey -> Optional.empty();
		}

		default boolean contains(TagKey<T> tagKey) {
			return ((Optional)this.apply(tagKey)).isPresent();
		}
	}
}
