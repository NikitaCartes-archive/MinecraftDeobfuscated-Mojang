/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
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
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TagLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PATH_SUFFIX = ".json";
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    final Function<ResourceLocation, Optional<T>> idToValue;
    private final String directory;

    public TagLoader(Function<ResourceLocation, Optional<T>> function, String string) {
        this.idToValue = function;
        this.directory = string;
    }

    public Map<ResourceLocation, List<EntryWithSource>> load(ResourceManager resourceManager) {
        HashMap<ResourceLocation, List<EntryWithSource>> map = Maps.newHashMap();
        for (Map.Entry<ResourceLocation, List<Resource>> entry : resourceManager.listResourceStacks(this.directory, resourceLocation -> resourceLocation.getPath().endsWith(PATH_SUFFIX)).entrySet()) {
            ResourceLocation resourceLocation2 = entry.getKey();
            String string = resourceLocation2.getPath();
            ResourceLocation resourceLocation22 = new ResourceLocation(resourceLocation2.getNamespace(), string.substring(this.directory.length() + 1, string.length() - PATH_SUFFIX_LENGTH));
            for (Resource resource : entry.getValue()) {
                try {
                    BufferedReader reader = resource.openAsReader();
                    try {
                        JsonElement jsonElement = JsonParser.parseReader(reader);
                        List list = map.computeIfAbsent(resourceLocation22, resourceLocation -> new ArrayList());
                        TagFile tagFile = (TagFile)TagFile.CODEC.parse(new Dynamic<JsonElement>(JsonOps.INSTANCE, jsonElement)).getOrThrow(false, LOGGER::error);
                        if (tagFile.replace()) {
                            list.clear();
                        }
                        String string2 = resource.sourcePackId();
                        tagFile.entries().forEach(tagEntry -> list.add(new EntryWithSource((TagEntry)tagEntry, string2)));
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

    private static void visitDependenciesAndElement(Map<ResourceLocation, List<EntryWithSource>> map, Multimap<ResourceLocation, ResourceLocation> multimap, Set<ResourceLocation> set, ResourceLocation resourceLocation2, BiConsumer<ResourceLocation, List<EntryWithSource>> biConsumer) {
        if (!set.add(resourceLocation2)) {
            return;
        }
        multimap.get(resourceLocation2).forEach(resourceLocation -> TagLoader.visitDependenciesAndElement(map, multimap, set, resourceLocation, biConsumer));
        List<EntryWithSource> list = map.get(resourceLocation2);
        if (list != null) {
            biConsumer.accept(resourceLocation2, list);
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

    private Either<Collection<EntryWithSource>, Collection<T>> build(TagEntry.Lookup<T> lookup, List<EntryWithSource> list) {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        ArrayList<EntryWithSource> list2 = new ArrayList<EntryWithSource>();
        for (EntryWithSource entryWithSource : list) {
            if (entryWithSource.entry().build(lookup, builder::add)) continue;
            list2.add(entryWithSource);
        }
        return list2.isEmpty() ? Either.right(builder.build()) : Either.left(list2);
    }

    public Map<ResourceLocation, Collection<T>> build(Map<ResourceLocation, List<EntryWithSource>> map) {
        final HashMap map2 = Maps.newHashMap();
        TagEntry.Lookup lookup = new TagEntry.Lookup<T>(){

            @Override
            @Nullable
            public T element(ResourceLocation resourceLocation) {
                return TagLoader.this.idToValue.apply(resourceLocation).orElse(null);
            }

            @Override
            @Nullable
            public Collection<T> tag(ResourceLocation resourceLocation) {
                return (Collection)map2.get(resourceLocation);
            }
        };
        HashMultimap multimap = HashMultimap.create();
        map.forEach((resourceLocation, list) -> list.forEach(entryWithSource -> entryWithSource.entry.visitRequiredDependencies(resourceLocation2 -> TagLoader.addDependencyIfNotCyclic(multimap, resourceLocation, resourceLocation2))));
        map.forEach((resourceLocation, list) -> list.forEach(entryWithSource -> entryWithSource.entry.visitOptionalDependencies(resourceLocation2 -> TagLoader.addDependencyIfNotCyclic(multimap, resourceLocation, resourceLocation2))));
        HashSet set = Sets.newHashSet();
        map.keySet().forEach(resourceLocation2 -> TagLoader.visitDependenciesAndElement(map, multimap, set, resourceLocation2, (resourceLocation, list) -> this.build(lookup, (List<EntryWithSource>)list).ifLeft(collection -> LOGGER.error("Couldn't load tag {} as it is missing following references: {}", resourceLocation, (Object)collection.stream().map(Objects::toString).collect(Collectors.joining(", ")))).ifRight(collection -> map2.put((ResourceLocation)resourceLocation, (Collection)collection))));
        return map2;
    }

    public Map<ResourceLocation, Collection<T>> loadAndBuild(ResourceManager resourceManager) {
        return this.build(this.load(resourceManager));
    }

    public record EntryWithSource(TagEntry entry, String source) {
        @Override
        public String toString() {
            return this.entry + " (from " + this.source + ")";
        }
    }
}

