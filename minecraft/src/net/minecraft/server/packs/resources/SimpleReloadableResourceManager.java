package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
import net.minecraft.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleReloadableResourceManager implements ReloadableResourceManager {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Map<String, FallbackResourceManager> namespacedPacks = Maps.<String, FallbackResourceManager>newHashMap();
	private final List<PreparableReloadListener> listeners = Lists.<PreparableReloadListener>newArrayList();
	private final List<PreparableReloadListener> recentlyRegistered = Lists.<PreparableReloadListener>newArrayList();
	private final Set<String> namespaces = Sets.<String>newLinkedHashSet();
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
			FallbackResourceManager fallbackResourceManager = (FallbackResourceManager)this.namespacedPacks.get(string);
			if (fallbackResourceManager == null) {
				fallbackResourceManager = new FallbackResourceManager(this.type);
				this.namespacedPacks.put(string, fallbackResourceManager);
			}

			fallbackResourceManager.add(pack);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Set<String> getNamespaces() {
		return this.namespaces;
	}

	@Override
	public Resource getResource(ResourceLocation resourceLocation) throws IOException {
		ResourceManager resourceManager = (ResourceManager)this.namespacedPacks.get(resourceLocation.getNamespace());
		if (resourceManager != null) {
			return resourceManager.getResource(resourceLocation);
		} else {
			throw new FileNotFoundException(resourceLocation.toString());
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean hasResource(ResourceLocation resourceLocation) {
		ResourceManager resourceManager = (ResourceManager)this.namespacedPacks.get(resourceLocation.getNamespace());
		return resourceManager != null ? resourceManager.hasResource(resourceLocation) : false;
	}

	@Override
	public List<Resource> getResources(ResourceLocation resourceLocation) throws IOException {
		ResourceManager resourceManager = (ResourceManager)this.namespacedPacks.get(resourceLocation.getNamespace());
		if (resourceManager != null) {
			return resourceManager.getResources(resourceLocation);
		} else {
			throw new FileNotFoundException(resourceLocation.toString());
		}
	}

	@Override
	public Collection<ResourceLocation> listResources(String string, Predicate<String> predicate) {
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();

		for (FallbackResourceManager fallbackResourceManager : this.namespacedPacks.values()) {
			set.addAll(fallbackResourceManager.listResources(string, predicate));
		}

		List<ResourceLocation> list = Lists.<ResourceLocation>newArrayList(set);
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
		ReloadInstance reloadInstance;
		if (LOGGER.isDebugEnabled()) {
			reloadInstance = new ProfiledReloadInstance(this, Lists.<PreparableReloadListener>newArrayList(list), executor, executor2, completableFuture);
		} else {
			reloadInstance = SimpleReloadInstance.of(this, Lists.<PreparableReloadListener>newArrayList(list), executor, executor2, completableFuture);
		}

		this.recentlyRegistered.clear();
		return reloadInstance;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ReloadInstance createQueuedReload(Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture) {
		return this.createReload(executor, executor2, this.recentlyRegistered, completableFuture);
	}

	@Override
	public ReloadInstance createFullReload(Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture, List<Pack> list) {
		this.clear();
		LOGGER.info("Reloading ResourceManager: {}", list.stream().map(Pack::getName).collect(Collectors.joining(", ")));

		for (Pack pack : list) {
			this.add(pack);
		}

		return this.createReload(executor, executor2, this.listeners, completableFuture);
	}
}
