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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

public abstract class TagsProvider<T>
implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final DataGenerator generator;
    protected final Registry<T> registry;
    protected final Map<Tag<T>, Tag.Builder<T>> builders = Maps.newLinkedHashMap();

    protected TagsProvider(DataGenerator dataGenerator, Registry<T> registry) {
        this.generator = dataGenerator;
        this.registry = registry;
    }

    protected abstract void addTags();

    @Override
    public void run(HashCache hashCache) {
        this.builders.clear();
        this.addTags();
        TagCollection tagCollection = new TagCollection(resourceLocation -> Optional.empty(), "", false, "generated");
        Map map = this.builders.entrySet().stream().collect(Collectors.toMap(entry -> ((Tag)entry.getKey()).getId(), Map.Entry::getValue));
        tagCollection.load(map);
        tagCollection.getAllTags().forEach((resourceLocation, tag) -> {
            JsonObject jsonObject = tag.serializeToJson(this.registry::getKey);
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
        this.useTags(tagCollection);
    }

    protected abstract void useTags(TagCollection<T> var1);

    protected abstract Path getPath(ResourceLocation var1);

    protected Tag.Builder<T> tag(Tag<T> tag2) {
        return this.builders.computeIfAbsent(tag2, tag -> Tag.Builder.tag());
    }
}

