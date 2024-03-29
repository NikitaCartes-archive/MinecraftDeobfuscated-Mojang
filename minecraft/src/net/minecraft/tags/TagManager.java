package net.minecraft.tags;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public class TagManager implements PreparableReloadListener {
	private static final Map<ResourceKey<? extends Registry<?>>, String> CUSTOM_REGISTRY_DIRECTORIES = Map.of(
		Registries.BLOCK,
		"tags/blocks",
		Registries.ENTITY_TYPE,
		"tags/entity_types",
		Registries.FLUID,
		"tags/fluids",
		Registries.GAME_EVENT,
		"tags/game_events",
		Registries.ITEM,
		"tags/items"
	);
	private final RegistryAccess registryAccess;
	private List<TagManager.LoadResult<?>> results = List.of();

	public TagManager(RegistryAccess registryAccess) {
		this.registryAccess = registryAccess;
	}

	public List<TagManager.LoadResult<?>> getResult() {
		return this.results;
	}

	public static String getTagDir(ResourceKey<? extends Registry<?>> resourceKey) {
		String string = (String)CUSTOM_REGISTRY_DIRECTORIES.get(resourceKey);
		return string != null ? string : "tags/" + resourceKey.location().getPath();
	}

	@Override
	public CompletableFuture<Void> reload(
		PreparableReloadListener.PreparationBarrier preparationBarrier,
		ResourceManager resourceManager,
		ProfilerFiller profilerFiller,
		ProfilerFiller profilerFiller2,
		Executor executor,
		Executor executor2
	) {
		List<? extends CompletableFuture<? extends TagManager.LoadResult<?>>> list = this.registryAccess
			.registries()
			.map(registryEntry -> this.createLoader(resourceManager, executor, registryEntry))
			.toList();
		return CompletableFuture.allOf((CompletableFuture[])list.toArray(i -> new CompletableFuture[i]))
			.thenCompose(preparationBarrier::wait)
			.thenAcceptAsync(void_ -> this.results = (List)list.stream().map(CompletableFuture::join).collect(Collectors.toUnmodifiableList()), executor2);
	}

	private <T> CompletableFuture<TagManager.LoadResult<T>> createLoader(
		ResourceManager resourceManager, Executor executor, RegistryAccess.RegistryEntry<T> registryEntry
	) {
		ResourceKey<? extends Registry<T>> resourceKey = registryEntry.key();
		Registry<T> registry = registryEntry.value();
		TagLoader<Holder<T>> tagLoader = new TagLoader<>(registry::getHolder, getTagDir(resourceKey));
		return CompletableFuture.supplyAsync(() -> new TagManager.LoadResult<>(resourceKey, tagLoader.loadAndBuild(resourceManager)), executor);
	}

	public static record LoadResult<T>(ResourceKey<? extends Registry<T>> key, Map<ResourceLocation, Collection<Holder<T>>> tags) {
	}
}
