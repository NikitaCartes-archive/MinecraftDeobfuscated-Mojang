/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FallbackResourceManager
implements ResourceManager {
    static final Logger LOGGER = LogUtils.getLogger();
    protected final List<PackEntry> fallbacks = Lists.newArrayList();
    private final PackType type;
    private final String namespace;

    public FallbackResourceManager(PackType packType, String string) {
        this.type = packType;
        this.namespace = string;
    }

    public void push(PackResources packResources) {
        this.pushInternal(packResources.packId(), packResources, null);
    }

    public void push(PackResources packResources, Predicate<ResourceLocation> predicate) {
        this.pushInternal(packResources.packId(), packResources, predicate);
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
        for (int i = this.fallbacks.size() - 1; i >= 0; --i) {
            IoSupplier<InputStream> ioSupplier;
            PackEntry packEntry = this.fallbacks.get(i);
            PackResources packResources = packEntry.resources;
            if (packResources != null && (ioSupplier = packResources.getResource(this.type, resourceLocation)) != null) {
                IoSupplier<ResourceMetadata> ioSupplier2 = this.createStackMetadataFinder(resourceLocation, i);
                return Optional.of(FallbackResourceManager.createResource(packResources, resourceLocation, ioSupplier, ioSupplier2));
            }
            if (!packEntry.isFiltered(resourceLocation)) continue;
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)resourceLocation, (Object)packEntry.name);
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static Resource createResource(PackResources packResources, ResourceLocation resourceLocation, IoSupplier<InputStream> ioSupplier, IoSupplier<ResourceMetadata> ioSupplier2) {
        return new Resource(packResources, FallbackResourceManager.wrapForDebug(resourceLocation, packResources, ioSupplier), ioSupplier2);
    }

    private static IoSupplier<InputStream> wrapForDebug(ResourceLocation resourceLocation, PackResources packResources, IoSupplier<InputStream> ioSupplier) {
        if (LOGGER.isDebugEnabled()) {
            return () -> new LeakedResourceWarningInputStream((InputStream)ioSupplier.get(), resourceLocation, packResources.packId());
        }
        return ioSupplier;
    }

    @Override
    public List<Resource> getResourceStack(ResourceLocation resourceLocation) {
        ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation);
        ArrayList<Resource> list = new ArrayList<Resource>();
        boolean bl = false;
        String string = null;
        for (int i = this.fallbacks.size() - 1; i >= 0; --i) {
            IoSupplier<InputStream> ioSupplier;
            PackEntry packEntry = this.fallbacks.get(i);
            PackResources packResources = packEntry.resources;
            if (packResources != null && (ioSupplier = packResources.getResource(this.type, resourceLocation)) != null) {
                IoSupplier<ResourceMetadata> ioSupplier2 = bl ? ResourceMetadata.EMPTY_SUPPLIER : () -> {
                    IoSupplier<InputStream> ioSupplier = packResources.getResource(this.type, resourceLocation2);
                    return ioSupplier != null ? FallbackResourceManager.parseMetadata(ioSupplier) : ResourceMetadata.EMPTY;
                };
                list.add(new Resource(packResources, ioSupplier, ioSupplier2));
            }
            if (packEntry.isFiltered(resourceLocation)) {
                string = packEntry.name;
                break;
            }
            if (!packEntry.isFiltered(resourceLocation2)) continue;
            bl = true;
        }
        if (list.isEmpty() && string != null) {
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)resourceLocation, (Object)string);
        }
        return Lists.reverse(list);
    }

    private static boolean isMetadata(ResourceLocation resourceLocation) {
        return resourceLocation.getPath().endsWith(".mcmeta");
    }

    private static ResourceLocation getResourceLocationFromMetadata(ResourceLocation resourceLocation) {
        String string = resourceLocation.getPath().substring(0, resourceLocation.getPath().length() - ".mcmeta".length());
        return resourceLocation.withPath(string);
    }

    static ResourceLocation getMetadataLocation(ResourceLocation resourceLocation) {
        return resourceLocation.withPath(resourceLocation.getPath() + ".mcmeta");
    }

    @Override
    public Map<ResourceLocation, Resource> listResources(String string, Predicate<ResourceLocation> predicate) {
        record ResourceWithSourceAndIndex(PackResources packResources, IoSupplier<InputStream> resource, int packIndex) {
        }
        HashMap<ResourceLocation, ResourceWithSourceAndIndex> map = new HashMap<ResourceLocation, ResourceWithSourceAndIndex>();
        HashMap map2 = new HashMap();
        int i = this.fallbacks.size();
        for (int j = 0; j < i; ++j) {
            PackEntry packEntry = this.fallbacks.get(j);
            packEntry.filterAll(map.keySet());
            packEntry.filterAll(map2.keySet());
            PackResources packResources = packEntry.resources;
            if (packResources == null) continue;
            int k = j;
            packResources.listResources(this.type, this.namespace, string, (resourceLocation, ioSupplier) -> {
                if (FallbackResourceManager.isMetadata(resourceLocation)) {
                    if (predicate.test(FallbackResourceManager.getResourceLocationFromMetadata(resourceLocation))) {
                        map2.put(resourceLocation, new ResourceWithSourceAndIndex(packResources, (IoSupplier<InputStream>)ioSupplier, k));
                    }
                } else if (predicate.test((ResourceLocation)resourceLocation)) {
                    map.put((ResourceLocation)resourceLocation, new ResourceWithSourceAndIndex(packResources, (IoSupplier<InputStream>)ioSupplier, k));
                }
            });
        }
        TreeMap<ResourceLocation, Resource> map3 = Maps.newTreeMap();
        map.forEach((resourceLocation, arg) -> {
            ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation);
            ResourceWithSourceAndIndex lv = (ResourceWithSourceAndIndex)map2.get(resourceLocation2);
            IoSupplier<ResourceMetadata> ioSupplier = lv != null && lv.packIndex >= arg.packIndex ? FallbackResourceManager.convertToMetadata(lv.resource) : ResourceMetadata.EMPTY_SUPPLIER;
            map3.put((ResourceLocation)resourceLocation, FallbackResourceManager.createResource(arg.packResources, resourceLocation, arg.resource, ioSupplier));
        });
        return map3;
    }

    private IoSupplier<ResourceMetadata> createStackMetadataFinder(ResourceLocation resourceLocation, int i) {
        return () -> {
            ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation);
            for (int j = this.fallbacks.size() - 1; j >= i; --j) {
                IoSupplier<InputStream> ioSupplier;
                PackEntry packEntry = this.fallbacks.get(j);
                PackResources packResources = packEntry.resources;
                if (packResources != null && (ioSupplier = packResources.getResource(this.type, resourceLocation2)) != null) {
                    return FallbackResourceManager.parseMetadata(ioSupplier);
                }
                if (packEntry.isFiltered(resourceLocation2)) break;
            }
            return ResourceMetadata.EMPTY;
        };
    }

    private static IoSupplier<ResourceMetadata> convertToMetadata(IoSupplier<InputStream> ioSupplier) {
        return () -> FallbackResourceManager.parseMetadata(ioSupplier);
    }

    private static ResourceMetadata parseMetadata(IoSupplier<InputStream> ioSupplier) throws IOException {
        try (InputStream inputStream = ioSupplier.get();){
            ResourceMetadata resourceMetadata = ResourceMetadata.fromJsonStream(inputStream);
            return resourceMetadata;
        }
    }

    private static void applyPackFiltersToExistingResources(PackEntry packEntry, Map<ResourceLocation, EntryStack> map) {
        for (EntryStack entryStack : map.values()) {
            if (packEntry.isFiltered(entryStack.fileLocation)) {
                entryStack.fileSources.clear();
                continue;
            }
            if (!packEntry.isFiltered(entryStack.metadataLocation())) continue;
            entryStack.metaSources.clear();
        }
    }

    private void listPackResources(PackEntry packEntry, String string, Predicate<ResourceLocation> predicate, Map<ResourceLocation, EntryStack> map) {
        PackResources packResources = packEntry.resources;
        if (packResources == null) {
            return;
        }
        packResources.listResources(this.type, this.namespace, string, (resourceLocation, ioSupplier) -> {
            if (FallbackResourceManager.isMetadata(resourceLocation)) {
                ResourceLocation resourceLocation2 = FallbackResourceManager.getResourceLocationFromMetadata(resourceLocation);
                if (!predicate.test(resourceLocation2)) {
                    return;
                }
                map.computeIfAbsent(resourceLocation2, (Function<ResourceLocation, EntryStack>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, <init>(net.minecraft.resources.ResourceLocation ), (Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/FallbackResourceManager$EntryStack;)()).metaSources.put(packResources, (IoSupplier<InputStream>)ioSupplier);
            } else {
                if (!predicate.test((ResourceLocation)resourceLocation)) {
                    return;
                }
                map.computeIfAbsent(resourceLocation, (Function<ResourceLocation, EntryStack>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, <init>(net.minecraft.resources.ResourceLocation ), (Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/FallbackResourceManager$EntryStack;)()).fileSources.add(new ResourceWithSource(packResources, (IoSupplier<InputStream>)ioSupplier));
            }
        });
    }

    @Override
    public Map<ResourceLocation, List<Resource>> listResourceStacks(String string, Predicate<ResourceLocation> predicate) {
        HashMap<ResourceLocation, EntryStack> map = Maps.newHashMap();
        for (PackEntry packEntry : this.fallbacks) {
            FallbackResourceManager.applyPackFiltersToExistingResources(packEntry, map);
            this.listPackResources(packEntry, string, predicate, map);
        }
        TreeMap<ResourceLocation, List<Resource>> treeMap = Maps.newTreeMap();
        for (EntryStack entryStack : map.values()) {
            if (entryStack.fileSources.isEmpty()) continue;
            ArrayList<Resource> list = new ArrayList<Resource>();
            for (ResourceWithSource resourceWithSource : entryStack.fileSources) {
                PackResources packResources = resourceWithSource.source;
                IoSupplier<InputStream> ioSupplier = entryStack.metaSources.get(packResources);
                IoSupplier<ResourceMetadata> ioSupplier2 = ioSupplier != null ? FallbackResourceManager.convertToMetadata(ioSupplier) : ResourceMetadata.EMPTY_SUPPLIER;
                list.add(FallbackResourceManager.createResource(packResources, entryStack.fileLocation, resourceWithSource.resource, ioSupplier2));
            }
            treeMap.put(entryStack.fileLocation, list);
        }
        return treeMap;
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.fallbacks.stream().map(packEntry -> packEntry.resources).filter(Objects::nonNull);
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

    record EntryStack(ResourceLocation fileLocation, ResourceLocation metadataLocation, List<ResourceWithSource> fileSources, Map<PackResources, IoSupplier<InputStream>> metaSources) {
        EntryStack(ResourceLocation resourceLocation) {
            this(resourceLocation, FallbackResourceManager.getMetadataLocation(resourceLocation), new ArrayList<ResourceWithSource>(), new Object2ObjectArrayMap<PackResources, IoSupplier<InputStream>>());
        }
    }

    record ResourceWithSource(PackResources source, IoSupplier<InputStream> resource) {
    }

    static class LeakedResourceWarningInputStream
    extends FilterInputStream {
        private final Supplier<String> message;
        private boolean closed;

        public LeakedResourceWarningInputStream(InputStream inputStream, ResourceLocation resourceLocation, String string) {
            super(inputStream);
            Exception exception = new Exception("Stacktrace");
            this.message = () -> {
                StringWriter stringWriter = new StringWriter();
                exception.printStackTrace(new PrintWriter(stringWriter));
                return "Leaked resource: '" + resourceLocation + "' loaded from pack: '" + string + "'\n" + stringWriter;
            };
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        protected void finalize() throws Throwable {
            if (!this.closed) {
                LOGGER.warn("{}", (Object)this.message.get());
            }
            super.finalize();
        }
    }
}

