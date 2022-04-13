/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import org.slf4j.Logger;

public abstract class TagsProvider<T>
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final DataGenerator generator;
    protected final Registry<T> registry;
    private final Map<ResourceLocation, Tag.Builder> builders = Maps.newLinkedHashMap();

    protected TagsProvider(DataGenerator dataGenerator, Registry<T> registry) {
        this.generator = dataGenerator;
        this.registry = registry;
    }

    @Override
    public final String getName() {
        return "Tags for " + this.registry.key().location();
    }

    protected abstract void addTags();

    @Override
    public void run(CachedOutput cachedOutput) {
        this.builders.clear();
        this.addTags();
        this.builders.forEach((resourceLocation, builder) -> {
            List<Tag.BuilderEntry> list = builder.getEntries().filter(builderEntry -> !builderEntry.entry().verifyIfPresent(this.registry::containsKey, this.builders::containsKey)).toList();
            if (!list.isEmpty()) {
                throw new IllegalArgumentException(String.format("Couldn't define tag %s as it is missing following references: %s", resourceLocation, list.stream().map(Objects::toString).collect(Collectors.joining(","))));
            }
            JsonObject jsonObject = builder.serializeToJson();
            Path path = this.getPath((ResourceLocation)resourceLocation);
            try {
                String string = GSON.toJson(jsonObject);
                cachedOutput.writeIfNeeded(path, string);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't save tags to {}", (Object)path, (Object)iOException);
            }
        });
    }

    private Path getPath(ResourceLocation resourceLocation) {
        ResourceKey<Registry<T>> resourceKey = this.registry.key();
        return this.generator.getOutputFolder().resolve("data/" + resourceLocation.getNamespace() + "/" + TagManager.getTagDir(resourceKey) + "/" + resourceLocation.getPath() + ".json");
    }

    protected TagAppender<T> tag(TagKey<T> tagKey) {
        Tag.Builder builder = this.getOrCreateRawBuilder(tagKey);
        return new TagAppender<T>(builder, this.registry, "vanilla");
    }

    protected Tag.Builder getOrCreateRawBuilder(TagKey<T> tagKey) {
        return this.builders.computeIfAbsent(tagKey.location(), resourceLocation -> new Tag.Builder());
    }

    protected static class TagAppender<T> {
        private final Tag.Builder builder;
        private final Registry<T> registry;
        private final String source;

        TagAppender(Tag.Builder builder, Registry<T> registry, String string) {
            this.builder = builder;
            this.registry = registry;
            this.source = string;
        }

        public TagAppender<T> add(T object) {
            this.builder.addElement(this.registry.getKey(object), this.source);
            return this;
        }

        @SafeVarargs
        public final TagAppender<T> add(ResourceKey<T> ... resourceKeys) {
            for (ResourceKey<T> resourceKey : resourceKeys) {
                this.builder.addElement(resourceKey.location(), this.source);
            }
            return this;
        }

        public TagAppender<T> addOptional(ResourceLocation resourceLocation) {
            this.builder.addOptionalElement(resourceLocation, this.source);
            return this;
        }

        public TagAppender<T> addTag(TagKey<T> tagKey) {
            this.builder.addTag(tagKey.location(), this.source);
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

