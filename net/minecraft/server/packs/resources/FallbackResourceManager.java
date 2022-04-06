/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FallbackResourceManager
implements ResourceManager {
    static final Logger LOGGER = LogUtils.getLogger();
    protected final List<PackEntry> fallbacks = Lists.newArrayList();
    final PackType type;
    private final String namespace;

    public FallbackResourceManager(PackType packType, String string) {
        this.type = packType;
        this.namespace = string;
    }

    public void push(PackResources packResources) {
        this.pushInternal(packResources.getName(), packResources, null);
    }

    public void push(PackResources packResources, Predicate<ResourceLocation> predicate) {
        this.pushInternal(packResources.getName(), packResources, predicate);
    }

    public void pushFilterOnly(String string, Predicate<ResourceLocation> predicate) {
        this.pushInternal(string, null, predicate);
    }

    private void pushInternal(String string, @Nullable PackResources packResources, @Nullable Predicate<ResourceLocation> predicate) {
        this.fallbacks.add(new PackEntry(string, packResources, predicate));
    }

    @Override
    public Set<String> getNamespaces() {
        return ImmutableSet.of(this.namespace);
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation resourceLocation) {
        if (!this.isValidLocation(resourceLocation)) {
            return Optional.empty();
        }
        for (int i = this.fallbacks.size() - 1; i >= 0; --i) {
            PackEntry packEntry = this.fallbacks.get(i);
            PackResources packResources = packEntry.resources;
            if (packResources != null && packResources.hasResource(this.type, resourceLocation)) {
                return Optional.of(new Resource(packResources.getName(), this.createResourceGetter(resourceLocation, packResources), this.createStackMetadataFinder(resourceLocation, i)));
            }
            if (!packEntry.isFiltered(resourceLocation)) continue;
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)resourceLocation, (Object)packEntry.name);
            return Optional.empty();
        }
        return Optional.empty();
    }

    Resource.IoSupplier<InputStream> createResourceGetter(ResourceLocation resourceLocation, PackResources packResources) {
        if (LOGGER.isDebugEnabled()) {
            return () -> {
                InputStream inputStream = packResources.getResource(this.type, resourceLocation);
                return new LeakedResourceWarningInputStream(inputStream, resourceLocation, packResources.getName());
            };
        }
        return () -> packResources.getResource(this.type, resourceLocation);
    }

    private boolean isValidLocation(ResourceLocation resourceLocation) {
        return !resourceLocation.getPath().contains("..");
    }

    @Override
    public List<Resource> getResourceStack(ResourceLocation resourceLocation) {
        if (!this.isValidLocation(resourceLocation)) {
            return List.of();
        }
        ArrayList<SinglePackResourceThunkSupplier> list = Lists.newArrayList();
        ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation);
        String string = null;
        for (PackEntry packEntry : this.fallbacks) {
            PackResources packResources;
            if (packEntry.isFiltered(resourceLocation)) {
                if (!list.isEmpty()) {
                    string = packEntry.name;
                }
                list.clear();
            } else if (packEntry.isFiltered(resourceLocation2)) {
                list.forEach(SinglePackResourceThunkSupplier::ignoreMeta);
            }
            if ((packResources = packEntry.resources) == null || !packResources.hasResource(this.type, resourceLocation)) continue;
            list.add(new SinglePackResourceThunkSupplier(resourceLocation, resourceLocation2, packResources));
        }
        if (list.isEmpty() && string != null) {
            LOGGER.info("Resource {} was filtered by pack {}", (Object)resourceLocation, (Object)string);
        }
        return list.stream().map(SinglePackResourceThunkSupplier::create).toList();
    }

    @Override
    public Map<ResourceLocation, Resource> listResources(String string, Predicate<ResourceLocation> predicate) {
        Object2IntOpenHashMap<ResourceLocation> object2IntMap = new Object2IntOpenHashMap<ResourceLocation>();
        int i = this.fallbacks.size();
        for (int j = 0; j < i; ++j) {
            PackEntry packEntry = this.fallbacks.get(j);
            packEntry.filterAll(object2IntMap.keySet());
            if (packEntry.resources == null) continue;
            for (ResourceLocation resourceLocation : packEntry.resources.getResources(this.type, this.namespace, string, predicate)) {
                object2IntMap.put(resourceLocation, j);
            }
        }
        TreeMap<ResourceLocation, Resource> map = Maps.newTreeMap();
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(object2IntMap)) {
            int k = entry.getIntValue();
            ResourceLocation resourceLocation2 = (ResourceLocation)entry.getKey();
            PackResources packResources = this.fallbacks.get((int)k).resources;
            map.put(resourceLocation2, new Resource(packResources.getName(), this.createResourceGetter(resourceLocation2, packResources), this.createStackMetadataFinder(resourceLocation2, k)));
        }
        return map;
    }

    private Resource.IoSupplier<ResourceMetadata> createStackMetadataFinder(ResourceLocation resourceLocation, int i) {
        return () -> {
            ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation);
            for (int j = this.fallbacks.size() - 1; j >= i; --j) {
                PackEntry packEntry = this.fallbacks.get(j);
                PackResources packResources = packEntry.resources;
                if (packResources != null && packResources.hasResource(this.type, resourceLocation2)) {
                    try (InputStream inputStream = packResources.getResource(this.type, resourceLocation2);){
                        ResourceMetadata resourceMetadata = ResourceMetadata.fromJsonStream(inputStream);
                        return resourceMetadata;
                    }
                }
                if (packEntry.isFiltered(resourceLocation2)) break;
            }
            return ResourceMetadata.EMPTY;
        };
    }

    private static void applyPackFiltersToExistingResources(PackEntry packEntry, Map<ResourceLocation, EntryStack> map) {
        Iterator<Map.Entry<ResourceLocation, EntryStack>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ResourceLocation, EntryStack> entry = iterator.next();
            ResourceLocation resourceLocation = entry.getKey();
            EntryStack entryStack = entry.getValue();
            if (packEntry.isFiltered(resourceLocation)) {
                iterator.remove();
                continue;
            }
            if (!packEntry.isFiltered(entryStack.metadataLocation())) continue;
            entryStack.entries.forEach(SinglePackResourceThunkSupplier::ignoreMeta);
        }
    }

    private void listPackResources(PackEntry packEntry, String string, Predicate<ResourceLocation> predicate, Map<ResourceLocation, EntryStack> map) {
        PackResources packResources = packEntry.resources;
        if (packResources == null) {
            return;
        }
        for (ResourceLocation resourceLocation : packResources.getResources(this.type, this.namespace, string, predicate)) {
            ResourceLocation resourceLocation22 = FallbackResourceManager.getMetadataLocation(resourceLocation);
            map.computeIfAbsent(resourceLocation, resourceLocation2 -> new EntryStack(resourceLocation22, Lists.newArrayList())).entries().add(new SinglePackResourceThunkSupplier(resourceLocation, resourceLocation22, packResources));
        }
    }

    @Override
    public Map<ResourceLocation, List<Resource>> listResourceStacks(String string, Predicate<ResourceLocation> predicate) {
        HashMap<ResourceLocation, EntryStack> map = Maps.newHashMap();
        for (PackEntry packEntry : this.fallbacks) {
            FallbackResourceManager.applyPackFiltersToExistingResources(packEntry, map);
            this.listPackResources(packEntry, string, predicate, map);
        }
        TreeMap<ResourceLocation, List<Resource>> treeMap = Maps.newTreeMap();
        map.forEach((resourceLocation, entryStack) -> treeMap.put((ResourceLocation)resourceLocation, entryStack.createThunks()));
        return treeMap;
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.fallbacks.stream().map(packEntry -> packEntry.resources).filter(Objects::nonNull);
    }

    static ResourceLocation getMetadataLocation(ResourceLocation resourceLocation) {
        return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + ".mcmeta");
    }

    record PackEntry(String name, @Nullable PackResources resources, @Nullable Predicate<ResourceLocation> filter) {
        public void filterAll(Collection<ResourceLocation> collection) {
            if (this.filter != null) {
                collection.removeIf(this.filter);
            }
        }

        public boolean isFiltered(ResourceLocation resourceLocation) {
            return this.filter != null && this.filter.test(resourceLocation);
        }

        @Nullable
        public PackResources resources() {
            return this.resources;
        }

        @Nullable
        public Predicate<ResourceLocation> filter() {
            return this.filter;
        }
    }

    class SinglePackResourceThunkSupplier {
        private final ResourceLocation location;
        private final ResourceLocation metadataLocation;
        private final PackResources source;
        private boolean shouldGetMeta = true;

        SinglePackResourceThunkSupplier(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, PackResources packResources) {
            this.source = packResources;
            this.location = resourceLocation;
            this.metadataLocation = resourceLocation2;
        }

        public void ignoreMeta() {
            this.shouldGetMeta = false;
        }

        public Resource create() {
            String string = this.source.getName();
            if (this.shouldGetMeta) {
                return new Resource(string, FallbackResourceManager.this.createResourceGetter(this.location, this.source), () -> {
                    if (this.source.hasResource(FallbackResourceManager.this.type, this.metadataLocation)) {
                        try (InputStream inputStream = this.source.getResource(FallbackResourceManager.this.type, this.metadataLocation);){
                            ResourceMetadata resourceMetadata = ResourceMetadata.fromJsonStream(inputStream);
                            return resourceMetadata;
                        }
                    }
                    return ResourceMetadata.EMPTY;
                });
            }
            return new Resource(string, FallbackResourceManager.this.createResourceGetter(this.location, this.source));
        }
    }

    record EntryStack(ResourceLocation metadataLocation, List<SinglePackResourceThunkSupplier> entries) {
        List<Resource> createThunks() {
            return this.entries().stream().map(SinglePackResourceThunkSupplier::create).toList();
        }
    }

    static class LeakedResourceWarningInputStream
    extends FilterInputStream {
        private final String message;
        private boolean closed;

        public LeakedResourceWarningInputStream(InputStream inputStream, ResourceLocation resourceLocation, String string) {
            super(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            new Exception().printStackTrace(new PrintStream(byteArrayOutputStream));
            this.message = "Leaked resource: '" + resourceLocation + "' loaded from pack: '" + string + "'\n" + byteArrayOutputStream;
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        protected void finalize() throws Throwable {
            if (!this.closed) {
                LOGGER.warn(this.message);
            }
            super.finalize();
        }
    }
}

