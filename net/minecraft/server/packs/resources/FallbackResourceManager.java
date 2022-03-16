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
import java.io.FileNotFoundException;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceThunk;
import net.minecraft.server.packs.resources.SimpleResource;
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
    public Resource getResource(ResourceLocation resourceLocation) throws IOException {
        this.validateLocation(resourceLocation);
        PackResources packResources = null;
        ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation);
        boolean bl = false;
        for (PackEntry packEntry : Lists.reverse(this.fallbacks)) {
            PackResources packResources2 = packEntry.resources;
            if (packResources2 != null) {
                if (!bl) {
                    if (packResources2.hasResource(this.type, resourceLocation2)) {
                        packResources = packResources2;
                        bl = true;
                    } else {
                        bl = packEntry.isFiltered(resourceLocation2);
                    }
                }
                if (packResources2.hasResource(this.type, resourceLocation)) {
                    InputStream inputStream = null;
                    if (packResources != null) {
                        inputStream = this.getWrappedResource(resourceLocation2, packResources);
                    }
                    return new SimpleResource(packResources2.getName(), resourceLocation, this.getWrappedResource(resourceLocation, packResources2), inputStream);
                }
            }
            if (!packEntry.isFiltered(resourceLocation)) continue;
            throw new FileNotFoundException(resourceLocation + " (filtered by: " + packEntry.name + ")");
        }
        throw new FileNotFoundException(resourceLocation.toString());
    }

    @Override
    public boolean hasResource(ResourceLocation resourceLocation) {
        if (!this.isValidLocation(resourceLocation)) {
            return false;
        }
        for (PackEntry packEntry : Lists.reverse(this.fallbacks)) {
            if (packEntry.hasResource(this.type, resourceLocation)) {
                return true;
            }
            if (!packEntry.isFiltered(resourceLocation)) continue;
            return false;
        }
        return false;
    }

    protected InputStream getWrappedResource(ResourceLocation resourceLocation, PackResources packResources) throws IOException {
        InputStream inputStream = packResources.getResource(this.type, resourceLocation);
        return LOGGER.isDebugEnabled() ? new LeakedResourceWarningInputStream(inputStream, resourceLocation, packResources.getName()) : inputStream;
    }

    private void validateLocation(ResourceLocation resourceLocation) throws IOException {
        if (!this.isValidLocation(resourceLocation)) {
            throw new IOException("Invalid relative path to resource: " + resourceLocation);
        }
    }

    private boolean isValidLocation(ResourceLocation resourceLocation) {
        return !resourceLocation.getPath().contains("..");
    }

    @Override
    public List<ResourceThunk> getResourceStack(ResourceLocation resourceLocation) throws IOException {
        this.validateLocation(resourceLocation);
        ArrayList<SinglePackResourceThunkSupplier> list = Lists.newArrayList();
        ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation);
        String string = null;
        for (PackEntry packEntry : this.fallbacks) {
            PackResources packResources;
            if (packEntry.isFiltered(resourceLocation)) {
                list.clear();
                string = packEntry.name;
            } else if (packEntry.isFiltered(resourceLocation2)) {
                list.forEach(SinglePackResourceThunkSupplier::ignoreMeta);
            }
            if ((packResources = packEntry.resources) == null || !packResources.hasResource(this.type, resourceLocation)) continue;
            list.add(new SinglePackResourceThunkSupplier(resourceLocation, resourceLocation2, packResources));
        }
        if (list.isEmpty()) {
            if (string != null) {
                throw new FileNotFoundException(resourceLocation + " (filtered by: " + string + ")");
            }
            throw new FileNotFoundException(resourceLocation.toString());
        }
        return list.stream().map(SinglePackResourceThunkSupplier::create).toList();
    }

    @Override
    public Map<ResourceLocation, ResourceThunk> listResources(String string, Predicate<ResourceLocation> predicate) {
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
        TreeMap<ResourceLocation, ResourceThunk> map = Maps.newTreeMap();
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(object2IntMap)) {
            int k = entry.getIntValue();
            ResourceLocation resourceLocation2 = (ResourceLocation)entry.getKey();
            PackResources packResources = this.fallbacks.get((int)k).resources;
            String string2 = packResources.getName();
            map.put(resourceLocation2, new ResourceThunk(string2, () -> {
                ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation2);
                InputStream inputStream = null;
                for (int j = this.fallbacks.size() - 1; j >= k; --j) {
                    PackEntry packEntry = this.fallbacks.get(j);
                    PackResources packResources2 = packEntry.resources;
                    if (packResources2 != null && packResources2.hasResource(this.type, resourceLocation2)) {
                        inputStream = this.getWrappedResource(resourceLocation2, packResources2);
                        break;
                    }
                    if (packEntry.isFiltered(resourceLocation2)) break;
                }
                return new SimpleResource(string2, resourceLocation2, this.getWrappedResource(resourceLocation2, packResources), inputStream);
            }));
        }
        return map;
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
    public Map<ResourceLocation, List<ResourceThunk>> listResourceStacks(String string, Predicate<ResourceLocation> predicate) {
        HashMap<ResourceLocation, EntryStack> map = Maps.newHashMap();
        for (PackEntry packEntry : this.fallbacks) {
            FallbackResourceManager.applyPackFiltersToExistingResources(packEntry, map);
            this.listPackResources(packEntry, string, predicate, map);
        }
        TreeMap<ResourceLocation, List<ResourceThunk>> treeMap = Maps.newTreeMap();
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

        boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
            return this.resources != null && this.resources.hasResource(packType, resourceLocation);
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

        public ResourceThunk create() {
            String string = this.source.getName();
            if (this.shouldGetMeta) {
                return new ResourceThunk(string, () -> {
                    InputStream inputStream = this.source.hasResource(FallbackResourceManager.this.type, this.metadataLocation) ? FallbackResourceManager.this.getWrappedResource(this.metadataLocation, this.source) : null;
                    return new SimpleResource(string, this.location, FallbackResourceManager.this.getWrappedResource(this.location, this.source), inputStream);
                });
            }
            return new ResourceThunk(string, () -> new SimpleResource(string, this.location, FallbackResourceManager.this.getWrappedResource(this.location, this.source), null));
        }
    }

    record EntryStack(ResourceLocation metadataLocation, List<SinglePackResourceThunkSupplier> entries) {
        List<ResourceThunk> createThunks() {
            return this.entries().stream().map(SinglePackResourceThunkSupplier::create).toList();
        }
    }
}

