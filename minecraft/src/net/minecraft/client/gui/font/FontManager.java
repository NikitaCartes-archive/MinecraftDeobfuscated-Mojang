package net.minecraft.client.gui.font;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class FontManager implements PreparableReloadListener, AutoCloseable {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final String FONTS_PATH = "fonts.json";
	public static final ResourceLocation MISSING_FONT = new ResourceLocation("minecraft", "missing");
	private static final FileToIdConverter FONT_DEFINITIONS = FileToIdConverter.json("font");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private final FontSet missingFontSet;
	private final List<GlyphProvider> providersToClose = new ArrayList();
	private final Map<ResourceLocation, FontSet> fontSets = new HashMap();
	private final TextureManager textureManager;
	private Map<ResourceLocation, ResourceLocation> renames = ImmutableMap.of();

	public FontManager(TextureManager textureManager) {
		this.textureManager = textureManager;
		this.missingFontSet = Util.make(
			new FontSet(textureManager, MISSING_FONT), fontSet -> fontSet.reload(Lists.<GlyphProvider>newArrayList(new AllMissingGlyphProvider()))
		);
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
		profilerFiller.startTick();
		profilerFiller.endTick();
		return this.prepare(resourceManager, executor)
			.thenCompose(preparationBarrier::wait)
			.thenAcceptAsync(preparation -> this.apply(preparation, profilerFiller2), executor2);
	}

	private CompletableFuture<FontManager.Preparation> prepare(ResourceManager resourceManager, Executor executor) {
		List<CompletableFuture<FontManager.UnresolvedBuilderBundle>> list = new ArrayList();

		for (Entry<ResourceLocation, List<Resource>> entry : FONT_DEFINITIONS.listMatchingResourceStacks(resourceManager).entrySet()) {
			ResourceLocation resourceLocation = FONT_DEFINITIONS.fileToId((ResourceLocation)entry.getKey());
			list.add(CompletableFuture.supplyAsync(() -> {
				List<Pair<FontManager.BuilderId, GlyphProviderBuilder>> listx = loadResourceStack((List<Resource>)entry.getValue(), resourceLocation);
				FontManager.UnresolvedBuilderBundle unresolvedBuilderBundle = new FontManager.UnresolvedBuilderBundle(resourceLocation);

				for (Pair<FontManager.BuilderId, GlyphProviderBuilder> pair : listx) {
					FontManager.BuilderId builderId = pair.getFirst();
					pair.getSecond().build().ifLeft(loader -> {
						CompletableFuture<Optional<GlyphProvider>> completableFuture = this.safeLoad(builderId, loader, resourceManager, executor);
						unresolvedBuilderBundle.add(builderId, completableFuture);
					}).ifRight(reference -> unresolvedBuilderBundle.add(builderId, reference));
				}

				return unresolvedBuilderBundle;
			}, executor));
		}

		return Util.sequence(list)
			.thenCompose(
				listx -> {
					List<CompletableFuture<Optional<GlyphProvider>>> list2 = (List<CompletableFuture<Optional<GlyphProvider>>>)listx.stream()
						.flatMap(FontManager.UnresolvedBuilderBundle::listBuilders)
						.collect(Collectors.toCollection(ArrayList::new));
					GlyphProvider glyphProvider = new AllMissingGlyphProvider();
					list2.add(CompletableFuture.completedFuture(Optional.of(glyphProvider)));
					return Util.sequence(list2)
						.thenCompose(
							list2x -> {
								Map<ResourceLocation, List<GlyphProvider>> map = this.resolveProviders(listx);
								CompletableFuture<?>[] completableFutures = (CompletableFuture<?>[])map.values()
									.stream()
									.map(listxxx -> CompletableFuture.runAsync(() -> this.finalizeProviderLoading(listxxx, glyphProvider), executor))
									.toArray(CompletableFuture[]::new);
								return CompletableFuture.allOf(completableFutures).thenApply(void_ -> {
									List<GlyphProvider> list2xx = list2x.stream().flatMap(Optional::stream).toList();
									return new FontManager.Preparation(map, list2xx);
								});
							}
						);
				}
			);
	}

	private CompletableFuture<Optional<GlyphProvider>> safeLoad(
		FontManager.BuilderId builderId, GlyphProviderBuilder.Loader loader, ResourceManager resourceManager, Executor executor
	) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return Optional.of(loader.load(resourceManager));
			} catch (Exception var4) {
				LOGGER.warn("Failed to load builder {}, rejecting", builderId, var4);
				return Optional.empty();
			}
		}, executor);
	}

	private Map<ResourceLocation, List<GlyphProvider>> resolveProviders(List<FontManager.UnresolvedBuilderBundle> list) {
		Map<ResourceLocation, List<GlyphProvider>> map = new HashMap();
		DependencySorter<ResourceLocation, FontManager.UnresolvedBuilderBundle> dependencySorter = new DependencySorter<>();
		list.forEach(unresolvedBuilderBundle -> dependencySorter.addEntry(unresolvedBuilderBundle.fontId, unresolvedBuilderBundle));
		dependencySorter.orderByDependencies(
			(resourceLocation, unresolvedBuilderBundle) -> unresolvedBuilderBundle.resolve(map::get).ifPresent(listx -> map.put(resourceLocation, listx))
		);
		return map;
	}

	private void finalizeProviderLoading(List<GlyphProvider> list, GlyphProvider glyphProvider) {
		list.add(0, glyphProvider);
		IntSet intSet = new IntOpenHashSet();

		for (GlyphProvider glyphProvider2 : list) {
			intSet.addAll(glyphProvider2.getSupportedGlyphs());
		}

		intSet.forEach(i -> {
			if (i != 32) {
				for (GlyphProvider glyphProviderx : Lists.reverse(list)) {
					if (glyphProviderx.getGlyph(i) != null) {
						break;
					}
				}
			}
		});
	}

	private void apply(FontManager.Preparation preparation, ProfilerFiller profilerFiller) {
		profilerFiller.startTick();
		profilerFiller.push("closing");
		this.fontSets.values().forEach(FontSet::close);
		this.fontSets.clear();
		this.providersToClose.forEach(GlyphProvider::close);
		this.providersToClose.clear();
		profilerFiller.popPush("reloading");
		preparation.providers().forEach((resourceLocation, list) -> {
			FontSet fontSet = new FontSet(this.textureManager, resourceLocation);
			fontSet.reload(Lists.reverse(list));
			this.fontSets.put(resourceLocation, fontSet);
		});
		this.providersToClose.addAll(preparation.allProviders);
		profilerFiller.pop();
		profilerFiller.endTick();
		if (!this.fontSets.containsKey(this.getActualId(Minecraft.DEFAULT_FONT))) {
			throw new IllegalStateException("Default font failed to load");
		}
	}

	private static List<Pair<FontManager.BuilderId, GlyphProviderBuilder>> loadResourceStack(List<Resource> list, ResourceLocation resourceLocation) {
		List<Pair<FontManager.BuilderId, GlyphProviderBuilder>> list2 = new ArrayList();

		for (Resource resource : list) {
			try {
				Reader reader = resource.openAsReader();

				try {
					JsonArray jsonArray = GsonHelper.getAsJsonArray(GsonHelper.fromJson(GSON, reader, JsonObject.class), "providers");

					for (int i = jsonArray.size() - 1; i >= 0; i--) {
						JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonArray.get(i), "providers[" + i + "]");
						String string = GsonHelper.getAsString(jsonObject, "type");
						GlyphProviderBuilderType glyphProviderBuilderType = GlyphProviderBuilderType.byName(string);
						FontManager.BuilderId builderId = new FontManager.BuilderId(resourceLocation, resource.sourcePackId(), i);
						list2.add(Pair.of(builderId, glyphProviderBuilderType.create(jsonObject)));
					}
				} catch (Throwable var13) {
					if (reader != null) {
						try {
							reader.close();
						} catch (Throwable var12) {
							var13.addSuppressed(var12);
						}
					}

					throw var13;
				}

				if (reader != null) {
					reader.close();
				}
			} catch (Exception var14) {
				LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}'", resourceLocation, "fonts.json", resource.sourcePackId(), var14);
			}
		}

		return list2;
	}

	public void setRenames(Map<ResourceLocation, ResourceLocation> map) {
		this.renames = map;
	}

	private ResourceLocation getActualId(ResourceLocation resourceLocation) {
		return (ResourceLocation)this.renames.getOrDefault(resourceLocation, resourceLocation);
	}

	public Font createFont() {
		return new Font(resourceLocation -> (FontSet)this.fontSets.getOrDefault(this.getActualId(resourceLocation), this.missingFontSet), false);
	}

	public Font createFontFilterFishy() {
		return new Font(resourceLocation -> (FontSet)this.fontSets.getOrDefault(this.getActualId(resourceLocation), this.missingFontSet), true);
	}

	public void close() {
		this.fontSets.values().forEach(FontSet::close);
		this.providersToClose.forEach(GlyphProvider::close);
		this.missingFontSet.close();
	}

	@Environment(EnvType.CLIENT)
	static record BuilderId(ResourceLocation fontId, String pack, int index) {
		public String toString() {
			return "(" + this.fontId + ": builder #" + this.index + " from pack " + this.pack + ")";
		}
	}

	@Environment(EnvType.CLIENT)
	static record BuilderResult(FontManager.BuilderId id, Either<CompletableFuture<Optional<GlyphProvider>>, ResourceLocation> result) {

		public Optional<List<GlyphProvider>> resolve(Function<ResourceLocation, List<GlyphProvider>> function) {
			return this.result
				.map(
					completableFuture -> ((Optional)completableFuture.join()).map(List::of),
					resourceLocation -> {
						List<GlyphProvider> list = (List<GlyphProvider>)function.apply(resourceLocation);
						if (list == null) {
							FontManager.LOGGER
								.warn("Can't find font {} referenced by builder {}, either because it's missing, failed to load or is part of loading cycle", resourceLocation, this.id);
							return Optional.empty();
						} else {
							return Optional.of(list);
						}
					}
				);
		}
	}

	@Environment(EnvType.CLIENT)
	static record Preparation(Map<ResourceLocation, List<GlyphProvider>> providers, List<GlyphProvider> allProviders) {
	}

	@Environment(EnvType.CLIENT)
	static record UnresolvedBuilderBundle(ResourceLocation fontId, List<FontManager.BuilderResult> builders, Set<ResourceLocation> dependencies)
		implements DependencySorter.Entry<ResourceLocation> {

		public UnresolvedBuilderBundle(ResourceLocation resourceLocation) {
			this(resourceLocation, new ArrayList(), new HashSet());
		}

		public void add(FontManager.BuilderId builderId, GlyphProviderBuilder.Reference reference) {
			this.builders.add(new FontManager.BuilderResult(builderId, Either.right(reference.id())));
			this.dependencies.add(reference.id());
		}

		public void add(FontManager.BuilderId builderId, CompletableFuture<Optional<GlyphProvider>> completableFuture) {
			this.builders.add(new FontManager.BuilderResult(builderId, Either.left(completableFuture)));
		}

		private Stream<CompletableFuture<Optional<GlyphProvider>>> listBuilders() {
			return this.builders.stream().flatMap(builderResult -> builderResult.result.left().stream());
		}

		public Optional<List<GlyphProvider>> resolve(Function<ResourceLocation, List<GlyphProvider>> function) {
			List<GlyphProvider> list = new ArrayList();

			for (FontManager.BuilderResult builderResult : this.builders) {
				Optional<List<GlyphProvider>> optional = builderResult.resolve(function);
				if (!optional.isPresent()) {
					return Optional.empty();
				}

				list.addAll((Collection)optional.get());
			}

			return Optional.of(list);
		}

		@Override
		public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
			this.dependencies.forEach(consumer);
		}

		@Override
		public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
		}
	}
}
