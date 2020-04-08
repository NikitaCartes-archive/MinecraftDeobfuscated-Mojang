/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class TagCollection<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private final Tag<T> empty = Tag.fromSet(ImmutableSet.of());
    private BiMap<ResourceLocation, Tag<T>> tags = HashBiMap.create();
    private final Function<ResourceLocation, Optional<T>> idToValue;
    private final String directory;
    private final String name;

    public TagCollection(Function<ResourceLocation, Optional<T>> function, String string, String string2) {
        this.idToValue = function;
        this.directory = string;
        this.name = string2;
    }

    @Nullable
    public Tag<T> getTag(ResourceLocation resourceLocation) {
        return (Tag)this.tags.get(resourceLocation);
    }

    public Tag<T> getTagOrEmpty(ResourceLocation resourceLocation) {
        return this.tags.getOrDefault(resourceLocation, this.empty);
    }

    @Environment(value=EnvType.CLIENT)
    public Tag<T> getEmptyTag() {
        return this.empty;
    }

    @Nullable
    public ResourceLocation getId(Tag<T> tag) {
        if (tag instanceof Tag.Named) {
            return ((Tag.Named)tag).getName();
        }
        return (ResourceLocation)this.tags.inverse().get(tag);
    }

    public ResourceLocation getIdOrThrow(Tag<T> tag) {
        ResourceLocation resourceLocation = this.getId(tag);
        if (resourceLocation == null) {
            throw new IllegalStateException("Unrecognized tag");
        }
        return resourceLocation;
    }

    public Collection<ResourceLocation> getAvailableTags() {
        return this.tags.keySet();
    }

    @Environment(value=EnvType.CLIENT)
    public Collection<ResourceLocation> getMatchingTags(T object) {
        ArrayList<ResourceLocation> list = Lists.newArrayList();
        for (Map.Entry entry : this.tags.entrySet()) {
            if (!((Tag)entry.getValue()).contains(object)) continue;
            list.add((ResourceLocation)entry.getKey());
        }
        return list;
    }

    public CompletableFuture<Map<ResourceLocation, Tag.Builder>> prepare(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            HashMap<ResourceLocation, Tag.Builder> map = Maps.newHashMap();
            for (ResourceLocation resourceLocation2 : resourceManager.listResources(this.directory, string -> string.endsWith(".json"))) {
                String string2 = resourceLocation2.getPath();
                ResourceLocation resourceLocation22 = new ResourceLocation(resourceLocation2.getNamespace(), string2.substring(this.directory.length() + 1, string2.length() - PATH_SUFFIX_LENGTH));
                try {
                    for (Resource resource : resourceManager.getResources(resourceLocation2)) {
                        try {
                            InputStream inputStream = resource.getInputStream();
                            Throwable throwable = null;
                            try {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                                Throwable throwable2 = null;
                                try {
                                    JsonObject jsonObject = GsonHelper.fromJson(GSON, (Reader)reader, JsonObject.class);
                                    if (jsonObject == null) {
                                        LOGGER.error("Couldn't load {} tag list {} from {} in data pack {} as it is empty or null", (Object)this.name, (Object)resourceLocation22, (Object)resourceLocation2, (Object)resource.getSourceName());
                                        continue;
                                    }
                                    map.computeIfAbsent(resourceLocation22, resourceLocation -> Tag.Builder.tag()).addFromJson(jsonObject, resource.getSourceName());
                                } catch (Throwable throwable3) {
                                    throwable2 = throwable3;
                                    throw throwable3;
                                } finally {
                                    if (reader == null) continue;
                                    if (throwable2 != null) {
                                        try {
                                            ((Reader)reader).close();
                                        } catch (Throwable throwable4) {
                                            throwable2.addSuppressed(throwable4);
                                        }
                                        continue;
                                    }
                                    ((Reader)reader).close();
                                }
                            } catch (Throwable throwable5) {
                                throwable = throwable5;
                                throw throwable5;
                            } finally {
                                if (inputStream == null) continue;
                                if (throwable != null) {
                                    try {
                                        inputStream.close();
                                    } catch (Throwable throwable6) {
                                        throwable.addSuppressed(throwable6);
                                    }
                                    continue;
                                }
                                inputStream.close();
                            }
                        } catch (IOException | RuntimeException exception) {
                            LOGGER.error("Couldn't read {} tag list {} from {} in data pack {}", (Object)this.name, (Object)resourceLocation22, (Object)resourceLocation2, (Object)resource.getSourceName(), (Object)exception);
                        } finally {
                            IOUtils.closeQuietly((Closeable)resource);
                        }
                    }
                } catch (IOException iOException) {
                    LOGGER.error("Couldn't read {} tag list {} from {}", (Object)this.name, (Object)resourceLocation22, (Object)resourceLocation2, (Object)iOException);
                }
            }
            return map;
        }, executor);
    }

    public void load(Map<ResourceLocation, Tag.Builder> map) {
        HashMap<ResourceLocation, Tag<T>> map2 = Maps.newHashMap();
        Function function = map2::get;
        Function<ResourceLocation, Object> function2 = resourceLocation -> this.idToValue.apply((ResourceLocation)resourceLocation).orElse(null);
        while (!map.isEmpty()) {
            boolean bl = false;
            Iterator<Map.Entry<ResourceLocation, Tag.Builder>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ResourceLocation, Tag.Builder> entry = iterator.next();
                Optional<Tag<Object>> optional = entry.getValue().build(function, function2);
                if (!optional.isPresent()) continue;
                map2.put(entry.getKey(), optional.get());
                iterator.remove();
                bl = true;
            }
            if (bl) continue;
            break;
        }
        map.forEach((resourceLocation, builder) -> LOGGER.error("Couldn't load {} tag {} as it is missing following references: {}", (Object)this.name, resourceLocation, (Object)builder.getUnresolvedEntries(function, function2).map(Objects::toString).collect(Collectors.joining(","))));
        this.replace(map2);
    }

    protected void replace(Map<ResourceLocation, Tag<T>> map) {
        this.tags = ImmutableBiMap.copyOf(map);
    }

    public Map<ResourceLocation, Tag<T>> getAllTags() {
        return this.tags;
    }
}

