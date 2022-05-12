/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
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
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import org.slf4j.Logger;

public abstract class TagsProvider<T>
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final DataGenerator.PathProvider pathProvider;
    protected final Registry<T> registry;
    private final Map<ResourceLocation, TagBuilder> builders = Maps.newLinkedHashMap();

    protected TagsProvider(DataGenerator dataGenerator, Registry<T> registry) {
        this.pathProvider = dataGenerator.createPathProvider(DataGenerator.Target.DATA_PACK, TagManager.getTagDir(registry.key()));
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
        this.builders.forEach((resourceLocation, tagBuilder) -> {
            List<TagEntry> list = tagBuilder.build();
            List<TagEntry> list2 = list.stream().filter(tagEntry -> !tagEntry.verifyIfPresent(this.registry::containsKey, this.builders::containsKey)).toList();
            if (!list2.isEmpty()) {
                throw new IllegalArgumentException(String.format("Couldn't define tag %s as it is missing following references: %s", resourceLocation, list2.stream().map(Objects::toString).collect(Collectors.joining(","))));
            }
            JsonElement jsonElement = TagFile.CODEC.encodeStart(JsonOps.INSTANCE, new TagFile(list, false)).getOrThrow(false, LOGGER::error);
            Path path = this.pathProvider.json((ResourceLocation)resourceLocation);
            try {
                DataProvider.saveStable(cachedOutput, jsonElement, path);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't save tags to {}", (Object)path, (Object)iOException);
            }
        });
    }

    protected TagAppender<T> tag(TagKey<T> tagKey) {
        TagBuilder tagBuilder = this.getOrCreateRawBuilder(tagKey);
        return new TagAppender<T>(tagBuilder, this.registry);
    }

    protected TagBuilder getOrCreateRawBuilder(TagKey<T> tagKey) {
        return this.builders.computeIfAbsent(tagKey.location(), resourceLocation -> TagBuilder.create());
    }

    protected static class TagAppender<T> {
        private final TagBuilder builder;
        private final Registry<T> registry;

        TagAppender(TagBuilder tagBuilder, Registry<T> registry) {
            this.builder = tagBuilder;
            this.registry = registry;
        }

        public TagAppender<T> add(T object) {
            this.builder.addElement(this.registry.getKey(object));
            return this;
        }

        @SafeVarargs
        public final TagAppender<T> add(ResourceKey<T> ... resourceKeys) {
            for (ResourceKey<T> resourceKey : resourceKeys) {
                this.builder.addElement(resourceKey.location());
            }
            return this;
        }

        public TagAppender<T> addOptional(ResourceLocation resourceLocation) {
            this.builder.addOptionalElement(resourceLocation);
            return this;
        }

        public TagAppender<T> addTag(TagKey<T> tagKey) {
            this.builder.addTag(tagKey.location());
            return this;
        }

        public TagAppender<T> addOptionalTag(ResourceLocation resourceLocation) {
            this.builder.addOptionalTag(resourceLocation);
            return this;
        }

        @SafeVarargs
        public final TagAppender<T> add(T ... objects) {
            Stream.of(objects).map(this.registry::getKey).forEach(resourceLocation -> this.builder.addElement((ResourceLocation)resourceLocation));
            return this;
        }
    }
}

