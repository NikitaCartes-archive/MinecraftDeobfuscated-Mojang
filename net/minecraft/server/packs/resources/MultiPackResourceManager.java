/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceFilterSection;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MultiPackResourceManager
implements CloseableResourceManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, FallbackResourceManager> namespacedManagers;
    private final List<PackResources> packs;

    public MultiPackResourceManager(PackType packType, List<PackResources> list) {
        this.packs = List.copyOf(list);
        HashMap<String, FallbackResourceManager> map = new HashMap<String, FallbackResourceManager>();
        List list2 = list.stream().flatMap(packResources -> packResources.getNamespaces(packType).stream()).distinct().toList();
        for (PackResources packResources2 : list) {
            ResourceFilterSection resourceFilterSection = this.getPackFilterSection(packResources2);
            Set<String> set = packResources2.getNamespaces(packType);
            Predicate<ResourceLocation> predicate = resourceFilterSection != null ? resourceLocation -> resourceFilterSection.isPathFiltered(resourceLocation.getPath()) : null;
            for (String string : list2) {
                boolean bl2;
                boolean bl = set.contains(string);
                boolean bl3 = bl2 = resourceFilterSection != null && resourceFilterSection.isNamespaceFiltered(string);
                if (!bl && !bl2) continue;
                FallbackResourceManager fallbackResourceManager = (FallbackResourceManager)map.get(string);
                if (fallbackResourceManager == null) {
                    fallbackResourceManager = new FallbackResourceManager(packType, string);
                    map.put(string, fallbackResourceManager);
                }
                if (bl && bl2) {
                    fallbackResourceManager.push(packResources2, predicate);
                    continue;
                }
                if (bl) {
                    fallbackResourceManager.push(packResources2);
                    continue;
                }
                fallbackResourceManager.pushFilterOnly(packResources2.packId(), predicate);
            }
        }
        this.namespacedManagers = map;
    }

    @Nullable
    private ResourceFilterSection getPackFilterSection(PackResources packResources) {
        try {
            return packResources.getMetadataSection(ResourceFilterSection.TYPE);
        } catch (IOException iOException) {
            LOGGER.error("Failed to get filter section from pack {}", (Object)packResources.packId());
            return null;
        }
    }

    @Override
    public Set<String> getNamespaces() {
        return this.namespacedManagers.keySet();
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation resourceLocation) {
        ResourceManager resourceManager = this.namespacedManagers.get(resourceLocation.getNamespace());
        if (resourceManager != null) {
            return resourceManager.getResource(resourceLocation);
        }
        return Optional.empty();
    }

    @Override
    public List<Resource> getResourceStack(ResourceLocation resourceLocation) {
        ResourceManager resourceManager = this.namespacedManagers.get(resourceLocation.getNamespace());
        if (resourceManager != null) {
            return resourceManager.getResourceStack(resourceLocation);
        }
        return List.of();
    }

    @Override
    public Map<ResourceLocation, Resource> listResources(String string, Predicate<ResourceLocation> predicate) {
        MultiPackResourceManager.checkTrailingDirectoryPath(string);
        TreeMap<ResourceLocation, Resource> map = new TreeMap<ResourceLocation, Resource>();
        for (FallbackResourceManager fallbackResourceManager : this.namespacedManagers.values()) {
            map.putAll(fallbackResourceManager.listResources(string, predicate));
        }
        return map;
    }

    @Override
    public Map<ResourceLocation, List<Resource>> listResourceStacks(String string, Predicate<ResourceLocation> predicate) {
        MultiPackResourceManager.checkTrailingDirectoryPath(string);
        TreeMap<ResourceLocation, List<Resource>> map = new TreeMap<ResourceLocation, List<Resource>>();
        for (FallbackResourceManager fallbackResourceManager : this.namespacedManagers.values()) {
            map.putAll(fallbackResourceManager.listResourceStacks(string, predicate));
        }
        return map;
    }

    private static void checkTrailingDirectoryPath(String string) {
        if (string.endsWith("/")) {
            throw new IllegalArgumentException("Trailing slash in path " + string);
        }
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.packs.stream();
    }

    @Override
    public void close() {
        this.packs.forEach(PackResources::close);
    }
}

