/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

public abstract class TagsProvider<T>
implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final DataGenerator generator;
    protected final Registry<T> registry;
    private final Map<ResourceLocation, Tag.Builder> builders = Maps.newLinkedHashMap();

    protected TagsProvider(DataGenerator dataGenerator, Registry<T> registry) {
        this.generator = dataGenerator;
        this.registry = registry;
    }

    protected abstract void addTags();

    @Override
    public void run(HashCache hashCache) {
        this.builders.clear();
        this.addTags();
        this.builders.forEach((resourceLocation, builder) -> {
            List list = builder.getEntries().filter(builderEntry -> !builderEntry.getEntry().verifyIfPresent(this.registry::containsKey, this.builders::containsKey)).collect(Collectors.toList());
            if (!list.isEmpty()) {
                throw new IllegalArgumentException(String.format("Couldn't define tag %s as it is missing following references: %s", resourceLocation, list.stream().map(Objects::toString).collect(Collectors.joining(","))));
            }
            JsonObject jsonObject = builder.serializeToJson();
            Path path = this.getPath((ResourceLocation)resourceLocation);
            try {
                String string = GSON.toJson(jsonObject);
                String string2 = SHA1.hashUnencodedChars(string).toString();
                if (!Objects.equals(hashCache.getHash(path), string2) || !Files.exists(path, new LinkOption[0])) {
                    Files.createDirectories(path.getParent(), new FileAttribute[0]);
                    try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, new OpenOption[0]);){
                        bufferedWriter.write(string);
                    }
                }
                hashCache.putNew(path, string2);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't save tags to {}", (Object)path, (Object)iOException);
            }
        });
    }

    protected abstract Path getPath(ResourceLocation var1);

    protected TagAppender<T> tag(Tag.Named<T> named) {
        Tag.Builder builder = this.getOrCreateRawBuilder(named);
        return new TagAppender(builder, this.registry, "vanilla");
    }

    protected Tag.Builder getOrCreateRawBuilder(Tag.Named<T> named) {
        return this.builders.computeIfAbsent(named.getName(), resourceLocation -> new Tag.Builder());
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

        public TagAppender<T> add(T object) {
            this.builder.addElement(this.registry.getKey(object), this.source);
            return this;
        }

        public TagAppender<T> addOptional(ResourceLocation resourceLocation) {
            this.builder.addOptionalElement(resourceLocation, this.source);
            return this;
        }

        public TagAppender<T> addTag(Tag.Named<T> named) {
            this.builder.addTag(named.getName(), this.source);
            return this;
        }

        public TagAppender<T> addOptionalTag(ResourceLocation resourceLocation) {
            this.builder.addOptionalTag(resourceLocation, this.source);
            return this;
        }

        @SafeVarargs
        public final TagAppender<T> add(T ... objects) {
            Stream.of(objects).map(this.registry::getKey).forEach(resourceLocation -> this.builder.addElement((ResourceLocation)resourceLocation, this.source));
            return this;
        }
    }
}

