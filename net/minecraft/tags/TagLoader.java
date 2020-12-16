/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagLoader<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private final Function<ResourceLocation, Optional<T>> idToValue;
    private final String directory;

    public TagLoader(Function<ResourceLocation, Optional<T>> function, String string) {
        this.idToValue = function;
        this.directory = string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Map<ResourceLocation, Tag.Builder> load(ResourceManager resourceManager) {
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
                                    LOGGER.error("Couldn't load tag list {} from {} in data pack {} as it is empty or null", (Object)resourceLocation22, (Object)resourceLocation2, (Object)resource.getSourceName());
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
                        LOGGER.error("Couldn't read tag list {} from {} in data pack {}", (Object)resourceLocation22, (Object)resourceLocation2, (Object)resource.getSourceName(), (Object)exception);
                    } finally {
                        IOUtils.closeQuietly((Closeable)resource);
                    }
                }
            } catch (IOException iOException) {
                LOGGER.error("Couldn't read tag list {} from {}", (Object)resourceLocation22, (Object)resourceLocation2, (Object)iOException);
            }
        }
        return map;
    }

    private static void visitDependenciesAndElement(Map<ResourceLocation, Tag.Builder> map, Multimap<ResourceLocation, ResourceLocation> multimap, Set<ResourceLocation> set, ResourceLocation resourceLocation2, BiConsumer<ResourceLocation, Tag.Builder> biConsumer) {
        if (!set.add(resourceLocation2)) {
            return;
        }
        multimap.get(resourceLocation2).forEach(resourceLocation -> TagLoader.visitDependenciesAndElement(map, multimap, set, resourceLocation, biConsumer));
        Tag.Builder builder = map.get(resourceLocation2);
        if (builder != null) {
            biConsumer.accept(resourceLocation2, builder);
        }
    }

    private static boolean isCyclic(Multimap<ResourceLocation, ResourceLocation> multimap, ResourceLocation resourceLocation, ResourceLocation resourceLocation22) {
        Collection<ResourceLocation> collection = multimap.get(resourceLocation22);
        if (collection.contains(resourceLocation)) {
            return true;
        }
        return collection.stream().anyMatch(resourceLocation2 -> TagLoader.isCyclic(multimap, resourceLocation, resourceLocation2));
    }

    private static void addDependencyIfNotCyclic(Multimap<ResourceLocation, ResourceLocation> multimap, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        if (!TagLoader.isCyclic(multimap, resourceLocation, resourceLocation2)) {
            multimap.put(resourceLocation, resourceLocation2);
        }
    }

    public TagCollection<T> build(Map<ResourceLocation, Tag.Builder> map) {
        HashMap map2 = Maps.newHashMap();
        Function<ResourceLocation, Tag> function = map2::get;
        Function<ResourceLocation, Object> function2 = resourceLocation -> this.idToValue.apply((ResourceLocation)resourceLocation).orElse(null);
        HashMultimap multimap = HashMultimap.create();
        map.forEach((resourceLocation, builder) -> builder.visitRequiredDependencies(resourceLocation2 -> TagLoader.addDependencyIfNotCyclic(multimap, resourceLocation, resourceLocation2)));
        map.forEach((resourceLocation, builder) -> builder.visitOptionalDependencies(resourceLocation2 -> TagLoader.addDependencyIfNotCyclic(multimap, resourceLocation, resourceLocation2)));
        HashSet set = Sets.newHashSet();
        map.keySet().forEach(resourceLocation2 -> TagLoader.visitDependenciesAndElement(map, multimap, set, resourceLocation2, (resourceLocation, builder) -> builder.build(function, function2).ifLeft(collection -> LOGGER.error("Couldn't load tag {} as it is missing following references: {}", resourceLocation, (Object)collection.stream().map(Objects::toString).collect(Collectors.joining(",")))).ifRight(tag -> map2.put((ResourceLocation)resourceLocation, tag))));
        return TagCollection.of(map2);
    }

    public TagCollection<T> loadAndBuild(ResourceManager resourceManager) {
        return this.build(this.load(resourceManager));
    }
}

