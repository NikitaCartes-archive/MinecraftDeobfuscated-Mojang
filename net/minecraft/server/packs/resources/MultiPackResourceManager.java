/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class MultiPackResourceManager
implements CloseableResourceManager {
    private final Map<String, FallbackResourceManager> namespacedManagers;
    private final List<PackResources> packs;

    public MultiPackResourceManager(PackType packType, List<PackResources> list) {
        this.packs = List.copyOf(list);
        HashMap<String, FallbackResourceManager> map = new HashMap<String, FallbackResourceManager>();
        for (PackResources packResources : list) {
            for (String string2 : packResources.getNamespaces(packType)) {
                map.computeIfAbsent(string2, string -> new FallbackResourceManager(packType, (String)string)).add(packResources);
            }
        }
        this.namespacedManagers = map;
    }

    @Override
    public Set<String> getNamespaces() {
        return this.namespacedManagers.keySet();
    }

    @Override
    public Resource getResource(ResourceLocation resourceLocation) throws IOException {
        ResourceManager resourceManager = this.namespacedManagers.get(resourceLocation.getNamespace());
        if (resourceManager != null) {
            return resourceManager.getResource(resourceLocation);
        }
        throw new FileNotFoundException(resourceLocation.toString());
    }

    @Override
    public boolean hasResource(ResourceLocation resourceLocation) {
        ResourceManager resourceManager = this.namespacedManagers.get(resourceLocation.getNamespace());
        if (resourceManager != null) {
            return resourceManager.hasResource(resourceLocation);
        }
        return false;
    }

    @Override
    public List<Resource> getResources(ResourceLocation resourceLocation) throws IOException {
        ResourceManager resourceManager = this.namespacedManagers.get(resourceLocation.getNamespace());
        if (resourceManager != null) {
            return resourceManager.getResources(resourceLocation);
        }
        throw new FileNotFoundException(resourceLocation.toString());
    }

    @Override
    public Collection<ResourceLocation> listResources(String string, Predicate<String> predicate) {
        HashSet<ResourceLocation> set = Sets.newHashSet();
        for (FallbackResourceManager fallbackResourceManager : this.namespacedManagers.values()) {
            set.addAll(fallbackResourceManager.listResources(string, predicate));
        }
        ArrayList<ResourceLocation> list = Lists.newArrayList(set);
        Collections.sort(list);
        return list;
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

