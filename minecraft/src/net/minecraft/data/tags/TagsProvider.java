package net.minecraft.data.tags;

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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TagsProvider<T> implements DataProvider {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	protected final DataGenerator generator;
	protected final Registry<T> registry;
	protected final Map<Tag<T>, Tag.Builder<T>> builders = Maps.<Tag<T>, Tag.Builder<T>>newLinkedHashMap();

	protected TagsProvider(DataGenerator dataGenerator, Registry<T> registry) {
		this.generator = dataGenerator;
		this.registry = registry;
	}

	protected abstract void addTags();

	@Override
	public void run(HashCache hashCache) {
		this.builders.clear();
		this.addTags();
		TagCollection<T> tagCollection = new TagCollection<>(resourceLocation -> Optional.empty(), "", false, "generated");
		Map<ResourceLocation, Tag.Builder<T>> map = (Map<ResourceLocation, Tag.Builder<T>>)this.builders
			.entrySet()
			.stream()
			.collect(Collectors.toMap(entry -> ((Tag)entry.getKey()).getId(), Entry::getValue));
		tagCollection.load(map);
		tagCollection.getAllTags().forEach((resourceLocation, tag) -> {
			JsonObject jsonObject = tag.serializeToJson(this.registry::getKey);
			Path path = this.getPath(resourceLocation);

			try {
				String string = GSON.toJson((JsonElement)jsonObject);
				String string2 = SHA1.hashUnencodedChars(string).toString();
				if (!Objects.equals(hashCache.getHash(path), string2) || !Files.exists(path, new LinkOption[0])) {
					Files.createDirectories(path.getParent());
					BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
					Throwable var9 = null;

					try {
						bufferedWriter.write(string);
					} catch (Throwable var19) {
						var9 = var19;
						throw var19;
					} finally {
						if (bufferedWriter != null) {
							if (var9 != null) {
								try {
									bufferedWriter.close();
								} catch (Throwable var18) {
									var9.addSuppressed(var18);
								}
							} else {
								bufferedWriter.close();
							}
						}
					}
				}

				hashCache.putNew(path, string2);
			} catch (IOException var21) {
				LOGGER.error("Couldn't save tags to {}", path, var21);
			}
		});
		this.useTags(tagCollection);
	}

	protected abstract void useTags(TagCollection<T> tagCollection);

	protected abstract Path getPath(ResourceLocation resourceLocation);

	protected Tag.Builder<T> tag(Tag<T> tag) {
		return (Tag.Builder<T>)this.builders.computeIfAbsent(tag, tagx -> Tag.Builder.tag());
	}
}
