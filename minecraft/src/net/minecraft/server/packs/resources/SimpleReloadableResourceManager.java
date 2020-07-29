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
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
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
	private final List<PackResources> packs = Lists.<PackResources>newArrayList();
	private final PackType type;

	public SimpleReloadableResourceManager(PackType packType) {
		this.type = packType;
	}

	public void add(PackResources packResources) {
		this.packs.add(packResources);

		for (String string : packResources.getNamespaces(this.type)) {
			this.namespaces.add(string);
			FallbackResourceManager fallbackResourceManager = (FallbackResourceManager)this.namespacedPacks.get(string);
			if (fallbackResourceManager == null) {
				fallbackResourceManager = new FallbackResourceManager(this.type, string);
				this.namespacedPacks.put(string, fallbackResourceManager);
			}

			fallbackResourceManager.add(packResources);
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

	@Override
	public ReloadInstance createFullReload(Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture, List<PackResources> list) {
		this.clear();
		LOGGER.info("Reloading ResourceManager: {}", () -> (String)list.stream().map(PackResources::getName).collect(Collectors.joining(", ")));

		for (PackResources packResources : list) {
			try {
				this.add(packResources);
			} catch (Exception var8) {
				LOGGER.error("Failed to add resource pack {}", packResources.getName(), var8);
				return new SimpleReloadableResourceManager.FailingReloadInstance(new SimpleReloadableResourceManager.ResourcePackLoadingFailure(packResources, var8));
			}
		}

		return this.createReload(executor, executor2, this.listeners, completableFuture);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Stream<PackResources> listPacks() {
		return this.packs.stream();
	}

	static class FailingReloadInstance implements ReloadInstance {
		private final SimpleReloadableResourceManager.ResourcePackLoadingFailure exception;
		private final CompletableFuture<Unit> failedFuture;

		public FailingReloadInstance(SimpleReloadableResourceManager.ResourcePackLoadingFailure resourcePackLoadingFailure) {
			this.exception = resourcePackLoadingFailure;
			this.failedFuture = new CompletableFuture();
			this.failedFuture.completeExceptionally(resourcePackLoadingFailure);
		}

		@Override
		public CompletableFuture<Unit> done() {
			return this.failedFuture;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public float getActualProgress() {
			return 0.0F;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public boolean isApplying() {
			return false;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public boolean isDone() {
			return true;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public void checkExceptions() {
			throw this.exception;
		}
	}

	public static class ResourcePackLoadingFailure extends RuntimeException {
		private final PackResources pack;

		public ResourcePackLoadingFailure(PackResources packResources, Throwable throwable) {
			super(packResources.getName(), throwable);
			this.pack = packResources;
		}

		@Environment(EnvType.CLIENT)
		public PackResources getPack() {
			return this.pack;
		}
	}
}
