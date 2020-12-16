package net.minecraft.tags;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagManager implements PreparableReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private final RegistryAccess registryAccess;
	private TagContainer tags = TagContainer.EMPTY;

	public TagManager(RegistryAccess registryAccess) {
		this.registryAccess = registryAccess;
	}

	public TagContainer getTags() {
		return this.tags;
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
		List<TagManager.LoaderInfo<?>> list = Lists.<TagManager.LoaderInfo<?>>newArrayList();
		StaticTags.visitHelpers(staticTagHelper -> {
			TagManager.LoaderInfo<?> loaderInfo = this.createLoader(resourceManager, executor, staticTagHelper);
			if (loaderInfo != null) {
				list.add(loaderInfo);
			}
		});
		return CompletableFuture.allOf((CompletableFuture[])list.stream().map(loaderInfo -> loaderInfo.pendingLoad).toArray(CompletableFuture[]::new))
			.thenCompose(preparationBarrier::wait)
			.thenAcceptAsync(
				void_ -> {
					TagContainer.Builder builder = new TagContainer.Builder();
					list.forEach(loaderInfo -> loaderInfo.addToBuilder(builder));
					TagContainer tagContainer = builder.build();
					Multimap<ResourceKey<? extends Registry<?>>, ResourceLocation> multimap = StaticTags.getAllMissingTags(tagContainer);
					if (!multimap.isEmpty()) {
						throw new IllegalStateException(
							"Missing required tags: "
								+ (String)multimap.entries().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).sorted().collect(Collectors.joining(","))
						);
					} else {
						SerializationTags.bind(tagContainer);
						this.tags = tagContainer;
					}
				},
				executor2
			);
	}

	@Nullable
	private <T> TagManager.LoaderInfo<T> createLoader(ResourceManager resourceManager, Executor executor, StaticTagHelper<T> staticTagHelper) {
		Optional<? extends Registry<T>> optional = this.registryAccess.registry(staticTagHelper.getKey());
		if (optional.isPresent()) {
			Registry<T> registry = (Registry<T>)optional.get();
			TagLoader<T> tagLoader = new TagLoader<>(registry::getOptional, staticTagHelper.getDirectory());
			CompletableFuture<? extends TagCollection<T>> completableFuture = CompletableFuture.supplyAsync(() -> tagLoader.loadAndBuild(resourceManager), executor);
			return new TagManager.LoaderInfo<>(staticTagHelper, completableFuture);
		} else {
			LOGGER.warn("Can't find registry for {}", staticTagHelper.getKey());
			return null;
		}
	}

	static class LoaderInfo<T> {
		private final StaticTagHelper<T> helper;
		private final CompletableFuture<? extends TagCollection<T>> pendingLoad;

		private LoaderInfo(StaticTagHelper<T> staticTagHelper, CompletableFuture<? extends TagCollection<T>> completableFuture) {
			this.helper = staticTagHelper;
			this.pendingLoad = completableFuture;
		}

		public void addToBuilder(TagContainer.Builder builder) {
			builder.add(this.helper.getKey(), (TagCollection<T>)this.pendingLoad.join());
		}
	}
}
