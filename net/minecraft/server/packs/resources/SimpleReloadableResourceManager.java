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
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
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
    private final Set<String> namespaces = Sets.newLinkedHashSet();
    private final List<PackResources> packs = Lists.newArrayList();
    private final PackType type;

    public SimpleReloadableResourceManager(PackType packType) {
        this.type = packType;
    }

    public void add(PackResources packResources) {
        this.packs.add(packResources);
        for (String string : packResources.getNamespaces(this.type)) {
            this.namespaces.add(string);
            FallbackResourceManager fallbackResourceManager = this.namespacedPacks.get(string);
            if (fallbackResourceManager == null) {
                fallbackResourceManager = new FallbackResourceManager(this.type, string);
                this.namespacedPacks.put(string, fallbackResourceManager);
            }
            fallbackResourceManager.add(packResources);
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
        this.packs.forEach(PackResources::close);
        this.packs.clear();
    }

    @Override
    public void close() {
        this.clear();
    }

    @Override
    public void registerReloadListener(PreparableReloadListener preparableReloadListener) {
        this.listeners.add(preparableReloadListener);
    }

    @Override
    public ReloadInstance createReload(Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture, List<PackResources> list) {
        LOGGER.info("Reloading ResourceManager: {}", () -> list.stream().map(PackResources::getName).collect(Collectors.joining(", ")));
        this.clear();
        for (PackResources packResources : list) {
            try {
                this.add(packResources);
            } catch (Exception exception) {
                LOGGER.error("Failed to add resource pack {}", (Object)packResources.getName(), (Object)exception);
                return new FailingReloadInstance(new ResourcePackLoadingFailure(packResources, (Throwable)exception));
            }
        }
        if (LOGGER.isDebugEnabled()) {
            return new ProfiledReloadInstance(this, Lists.newArrayList(this.listeners), executor, executor2, completableFuture);
        }
        return SimpleReloadInstance.of(this, Lists.newArrayList(this.listeners), executor, executor2, completableFuture);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public Stream<PackResources> listPacks() {
        return this.packs.stream();
    }

    static class FailingReloadInstance
    implements ReloadInstance {
        private final ResourcePackLoadingFailure exception;
        private final CompletableFuture<Unit> failedFuture;

        public FailingReloadInstance(ResourcePackLoadingFailure resourcePackLoadingFailure) {
            this.exception = resourcePackLoadingFailure;
            this.failedFuture = new CompletableFuture();
            this.failedFuture.completeExceptionally(resourcePackLoadingFailure);
        }

        @Override
        public CompletableFuture<Unit> done() {
            return this.failedFuture;
        }

        @Override
        @Environment(value=EnvType.CLIENT)
        public float getActualProgress() {
            return 0.0f;
        }

        @Override
        @Environment(value=EnvType.CLIENT)
        public boolean isDone() {
            return true;
        }

        @Override
        @Environment(value=EnvType.CLIENT)
        public void checkExceptions() {
            throw this.exception;
        }
    }

    public static class ResourcePackLoadingFailure
    extends RuntimeException {
        private final PackResources pack;

        public ResourcePackLoadingFailure(PackResources packResources, Throwable throwable) {
            super(packResources.getName(), throwable);
            this.pack = packResources;
        }

        @Environment(value=EnvType.CLIENT)
        public PackResources getPack() {
            return this.pack;
        }
    }
}

