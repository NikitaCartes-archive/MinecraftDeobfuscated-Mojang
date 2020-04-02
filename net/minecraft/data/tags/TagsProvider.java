/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.tags;

import com.google.common.collect.ImmutableSet;
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
import java.util.function.Function;
import java.util.stream.Collectors;
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
    protected final Map<ResourceLocation, Tag.TypedBuilder<T>> builders = Maps.newLinkedHashMap();

    protected TagsProvider(DataGenerator dataGenerator, Registry<T> registry) {
        this.generator = dataGenerator;
        this.registry = registry;
    }

    protected abstract void addTags();

    @Override
    public void run(HashCache hashCache) {
        this.builders.clear();
        this.addTags();
        Tag tag = Tag.fromSet(ImmutableSet.of());
        Function<ResourceLocation, Tag> function = resourceLocation -> this.builders.containsKey(resourceLocation) ? tag : null;
        Function<ResourceLocation, Object> function2 = resourceLocation -> this.registry.getOptional((ResourceLocation)resourceLocation).orElse(null);
        this.builders.forEach((resourceLocation, typedBuilder) -> {
            List list = typedBuilder.getUnresolvedEntries(function, function2).collect(Collectors.toList());
            if (!list.isEmpty()) {
                throw new IllegalArgumentException(String.format("Couldn't define tag %s as it is missing following references: %s", resourceLocation, list.stream().map(Objects::toString).collect(Collectors.joining(","))));
            }
            JsonObject jsonObject = typedBuilder.serializeToJson();
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

    protected Tag.TypedBuilder<T> tag(Tag.Named<T> named) {
        return this.tag(named.getName());
    }

    protected Tag.TypedBuilder<T> tag(ResourceLocation resourceLocation2) {
        return this.builders.computeIfAbsent(resourceLocation2, resourceLocation -> new Tag.TypedBuilder<Object>(this.registry::getKey));
    }
}

