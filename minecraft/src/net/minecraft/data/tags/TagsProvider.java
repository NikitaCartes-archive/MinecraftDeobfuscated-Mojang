package net.minecraft.data.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TagsProvider<T> implements DataProvider {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	protected final DataGenerator generator;
	protected final Registry<T> registry;
	private final Map<ResourceLocation, Tag.Builder> builders = Maps.<ResourceLocation, Tag.Builder>newLinkedHashMap();

	protected TagsProvider(DataGenerator dataGenerator, Registry<T> registry) {
		this.generator = dataGenerator;
		this.registry = registry;
	}

	protected abstract void addTags();

	@Override
	public void run(HashCache hashCache) {
		this.builders.clear();
		this.addTags();
		Tag<T> tag = Tag.fromSet(ImmutableSet.of());
		Function<ResourceLocation, Tag<T>> function = resourceLocation -> this.builders.containsKey(resourceLocation) ? tag : null;
		Function<ResourceLocation, T> function2 = resourceLocation -> this.registry.getOptional(resourceLocation).orElse(null);
		this.builders
			.forEach(
				(resourceLocation, builder) -> {
					List<Tag.BuilderEntry> list = (List<Tag.BuilderEntry>)builder.getUnresolvedEntries(function, function2).collect(Collectors.toList());
					if (!list.isEmpty()) {
						throw new IllegalArgumentException(
							String.format(
								"Couldn't define tag %s as it is missing following references: %s",
								resourceLocation,
								list.stream().map(Objects::toString).collect(Collectors.joining(","))
							)
						);
					} else {
						JsonObject jsonObject = builder.serializeToJson();
						Path path = this.getPath(resourceLocation);

						try {
							String string = GSON.toJson((JsonElement)jsonObject);
							String string2 = SHA1.hashUnencodedChars(string).toString();
							if (!Objects.equals(hashCache.getHash(path), string2) || !Files.exists(path, new LinkOption[0])) {
								Files.createDirectories(path.getParent());
								BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
								Throwable var12 = null;

								try {
									bufferedWriter.write(string);
								} catch (Throwable var22) {
									var12 = var22;
									throw var22;
								} finally {
									if (bufferedWriter != null) {
										if (var12 != null) {
											try {
												bufferedWriter.close();
											} catch (Throwable var21) {
												var12.addSuppressed(var21);
											}
										} else {
											bufferedWriter.close();
										}
									}
								}
							}

							hashCache.putNew(path, string2);
						} catch (IOException var24) {
							LOGGER.error("Couldn't save tags to {}", path, var24);
						}
					}
				}
			);
	}

	protected abstract Path getPath(ResourceLocation resourceLocation);

	protected TagsProvider.TagAppender<T> tag(Tag.Named<T> named) {
		Tag.Builder builder = this.getOrCreateRawBuilder(named);
		return new TagsProvider.TagAppender<>(builder, this.registry, "vanilla");
	}

	protected Tag.Builder getOrCreateRawBuilder(Tag.Named<T> named) {
		return (Tag.Builder)this.builders.computeIfAbsent(named.getName(), resourceLocation -> new Tag.Builder());
	}

	public static class TagAppender<T> {
		private final Tag.Builder builder;
		private final Registry<T> registry;
		private final String source;

		private TagAppender(Tag.Builder builder, Registry<T> registry, String string) {
			this.builder = builder;
			this.registry = registry;
			this.source = string;
		}

		public TagsProvider.TagAppender<T> add(T object) {
			this.builder.addElement(this.registry.getKey(object), this.source);
			return this;
		}

		public TagsProvider.TagAppender<T> addTag(Tag.Named<T> named) {
			this.builder.addTag(named.getName(), this.source);
			return this;
		}

		@SafeVarargs
		public final TagsProvider.TagAppender<T> add(T... objects) {
			Stream.of(objects).map(this.registry::getKey).forEach(resourceLocation -> this.builder.addElement(resourceLocation, this.source));
			return this;
		}
	}
}
