/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ProfiledReloadInstance;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleReloadableResourceManager
implements ReloadableResourceManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<String, FallbackResourceManager> namespacedPacks = Maps.newHashMap();
    private final List<PreparableReloadListener> listeners = Lists.newArrayList();
    private final List<PreparableReloadListener> recentlyRegistered = Lists.newArrayList();
    private final Set<String> namespaces = Sets.newLinkedHashSet();
    private final PackType type;
    private final Thread mainThread;

    public SimpleReloadableResourceManager(PackType packType, Thread thread) {
        this.type = packType;
        this.mainThread = thread;
    }

    @Override
    public void add(Pack pack) {
        for (String string : pack.getNamespaces(this.type)) {
            this.namespaces.add(string);
            FallbackResourceManager fallbackResourceManager = this.namespacedPacks.get(string);
            if (fallbackResourceManager == null) {
                fallbackResourceManager = new FallbackResourceManager(this.type);
                this.namespacedPacks.put(string, fallbackResourceManager);
            }
            fallbackResourceManager.add(pack);
        }
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public Set<String> getNamespaces() {
        return this.namespaces;
    }

    @Override
    public Resource getResource(ResourceLocation resourceLocation) throws IOException {
        ResourceManager resourceManager = this.namespacedPacks.get(resourceLocation.getNamespace());
        if (resourceManager != null) {
            return resourceManager.getResource(resourceLocation);
        }
        throw new FileNotFoundException(resourceLocation.toString());
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean hasResource(ResourceLocation resourceLocation) {
        ResourceManager resourceManager = this.namespacedPacks.get(resourceLocation.getNamespace());
        if (resourceManager != null) {
            return resourceManager.hasResource(resourceLocation);
        }
        return false;
    }

    @Override
    public List<Resource> getResources(ResourceLocation resourceLocation) throws IOException {
        ResourceManager resourceManager = this.namespacedPacks.get(resourceLocation.getNamespace());
        if (resourceManager != null) {
            return resourceManager.getResources(resourceLocation);
        }
        throw new FileNotFoundException(resourceLocation.toString());
    }

    @Override
    public Collection<ResourceLocation> listResources(String string, Predicate<String> predicate) {
        HashSet<ResourceLocation> set = Sets.newHashSet();
        for (FallbackResourceManager fallbackResourceManager : this.namespacedPacks.values()) {
            set.addAll(fallbackResourceManager.listResources(string, predicate));
        }
        ArrayList<ResourceLocation> list = Lists.newArrayList(set);
        Collections.sort(list);
        return list;
    }

    private void clear() {
        this.namespacedPacks.clear();
        this.namespaces.clear();
    }

    @Override
    public CompletableFuture<Unit> reload(Executor executor, Executor executor2, List<Pack> list, CompletableFuture<Unit> completableFuture) {
        ReloadInstance reloadInstance = this.createFullReload(executor, executor2, completableFuture, list);
        return reloadInstance.done();
    }

    @Override
    public void registerReloadListener(PreparableReloadListener preparableReloadListener) {
        this.listeners.add(preparableReloadListener);
        this.recentlyRegistered.add(preparableReloadListener);
    }

    protected ReloadInstance createReload(Executor executor, Executor executor2, List<PreparableReloadListener> list, CompletableFuture<Unit> completableFuture) {
        ProfiledReloadInstance reloadInstance = LOGGER.isDebugEnabled() ? new ProfiledReloadInstance(this, Lists.newArrayList(list), executor, executor2, completableFuture) : SimpleReloadInstance.of(this, Lists.newArrayList(list), executor, executor2, completableFuture);
        this.recentlyRegistered.clear();
        return reloadInstance;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public ReloadInstance createQueuedReload(Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture) {
        return this.createReload(executor, executor2, this.recentlyRegistered, completableFuture);
    }

    @Override
    public ReloadInstance createFullReload(Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture, List<Pack> list) {
        this.clear();
        LOGGER.info("Reloading ResourceManager: {}", (Object)list.stream().map(Pack::getName).collect(Collectors.joining(", ")));
        for (Pack pack : list) {
            this.add(pack);
        }
        return this.createReload(executor, executor2, this.listeners, completableFuture);
    }
}

