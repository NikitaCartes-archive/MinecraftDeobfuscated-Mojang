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
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public class TagLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final String PATH_SUFFIX = ".json";
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private final Function<ResourceLocation, Optional<T>> idToValue;
    private final String directory;

    public TagLoader(Function<ResourceLocation, Optional<T>> function, String string) {
        this.idToValue = function;
        this.directory = string;
    }

    public Map<ResourceLocation, Tag.Builder> load(ResourceManager resourceManager) {
        HashMap<ResourceLocation, Tag.Builder> map = Maps.newHashMap();
        for (Map.Entry<ResourceLocation, List<Resource>> entry : resourceManager.listResourceStacks(this.directory, resourceLocation -> resourceLocation.getPath().endsWith(PATH_SUFFIX)).entrySet()) {
            ResourceLocation resourceLocation2 = entry.getKey();
            String string = resourceLocation2.getPath();
            ResourceLocation resourceLocation22 = new ResourceLocation(resourceLocation2.getNamespace(), string.substring(this.directory.length() + 1, string.length() - PATH_SUFFIX_LENGTH));
            for (Resource resource : entry.getValue()) {
                try {
                    BufferedReader reader = resource.openAsReader();
                    try {
                        JsonObject jsonObject = GsonHelper.fromJson(GSON, (Reader)reader, JsonObject.class);
                        if (jsonObject == null) {
                            throw new NullPointerException("Invalid JSON contents");
                        }
                        map.computeIfAbsent(resourceLocation22, resourceLocation -> Tag.Builder.tag()).addFromJson(jsonObject, resource.sourcePackId());
                    } finally {
                        if (reader == null) continue;
                        ((Reader)reader).close();
                    }
                } catch (Exception exception) {
                    LOGGER.error("Couldn't read tag list {} from {} in data pack {}", resourceLocation22, resourceLocation2, resource.sourcePackId(), exception);
                }
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

    public Map<ResourceLocation, Tag<T>> build(Map<ResourceLocation, Tag.Builder> map) {
        HashMap map2 = Maps.newHashMap();
        Function<ResourceLocation, Tag> function = map2::get;
        Function<ResourceLocation, Object> function2 = resourceLocation -> this.idToValue.apply((ResourceLocation)resourceLocation).orElse(null);
        HashMultimap multimap = HashMultimap.create();
        map.forEach((resourceLocation, builder) -> builder.visitRequiredDependencies(resourceLocation2 -> TagLoader.addDependencyIfNotCyclic(multimap, resourceLocation, resourceLocation2)));
        map.forEach((resourceLocation, builder) -> builder.visitOptionalDependencies(resourceLocation2 -> TagLoader.addDependencyIfNotCyclic(multimap, resourceLocation, resourceLocation2)));
        HashSet set = Sets.newHashSet();
        map.keySet().forEach(resourceLocation2 -> TagLoader.visitDependenciesAndElement(map, multimap, set, resourceLocation2, (resourceLocation, builder) -> builder.build(function, function2).ifLeft(collection -> LOGGER.error("Couldn't load tag {} as it is missing following references: {}", resourceLocation, (Object)collection.stream().map(Objects::toString).collect(Collectors.joining(", ")))).ifRight(tag -> map2.put((ResourceLocation)resourceLocation, (Tag)tag))));
        return map2;
    }

    public Map<ResourceLocation, Tag<T>> loadAndBuild(ResourceManager resourceManager) {
        return this.build(this.load(resourceManager));
    }
}

