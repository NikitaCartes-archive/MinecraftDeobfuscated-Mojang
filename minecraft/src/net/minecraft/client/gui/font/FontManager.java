package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
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
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class FontManager implements PreparableReloadListener, AutoCloseable {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final String FONTS_PATH = "fonts.json";
	public static final ResourceLocation MISSING_FONT = ResourceLocation.withDefaultNamespace("missing");
	private static final FileToIdConverter FONT_DEFINITIONS = FileToIdConverter.json("font");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private final FontSet missingFontSet;
	private final List<GlyphProvider> providersToClose = new ArrayList();
	private final Map<ResourceLocation, FontSet> fontSets = new HashMap();
	private final TextureManager textureManager;
	@Nullable
	private volatile FontSet lastFontSetCache;

	public FontManager(TextureManager textureManager) {
		this.textureManager = textureManager;
		this.missingFontSet = Util.make(new FontSet(textureManager, MISSING_FONT), fontSet -> fontSet.reload(List.of(createFallbackProvider()), Set.of()));
	}

	private static GlyphProvider.Conditional createFallbackProvider() {
		return new GlyphProvider.Conditional(new AllMissingGlyphProvider(), FontOption.Filter.ALWAYS_PASS);
	}

	@Override
	public CompletableFuture<Void> reload(
		PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor2
	) {
		return this.prepare(resourceManager, executor)
			.thenCompose(preparationBarrier::wait)
			.thenAcceptAsync(preparation -> this.apply(preparation, Profiler.get()), executor2);
	}

	private CompletableFuture<FontManager.Preparation> prepare(ResourceManager resourceManager, Executor executor) {
		List<CompletableFuture<FontManager.UnresolvedBuilderBundle>> list = new ArrayList();

		for (Entry<ResourceLocation, List<Resource>> entry : FONT_DEFINITIONS.listMatchingResourceStacks(resourceManager).entrySet()) {
			ResourceLocation resourceLocation = FONT_DEFINITIONS.fileToId((ResourceLocation)entry.getKey());
			list.add(CompletableFuture.supplyAsync(() -> {
				List<Pair<FontManager.BuilderId, GlyphProviderDefinition.Conditional>> listx = loadResourceStack((List<Resource>)entry.getValue(), resourceLocation);
				FontManager.UnresolvedBuilderBundle unresolvedBuilderBundle = new FontManager.UnresolvedBuilderBundle(resourceLocation);

				for (Pair<FontManager.BuilderId, GlyphProviderDefinition.Conditional> pair : listx) {
					FontManager.BuilderId builderId = pair.getFirst();
					FontOption.Filter filter = pair.getSecond().filter();
					pair.getSecond().definition().unpack().ifLeft(loader -> {
						CompletableFuture<Optional<GlyphProvider>> completableFuture = this.safeLoad(builderId, loader, resourceManager, executor);
						unresolvedBuilderBundle.add(builderId, filter, completableFuture);
					}).ifRight(reference -> unresolvedBuilderBundle.add(builderId, filter, reference));
				}

				return unresolvedBuilderBundle;
			}, executor));
		}

		return Util.sequence(list)
			.thenCompose(
				listx -> {
					List<CompletableFuture<Optional<GlyphProvider>>> list2 = (List<CompletableFuture<Optional<GlyphProvider>>>)listx.stream()
						.flatMap(FontManager.UnresolvedBuilderBundle::listBuilders)
						.collect(Util.toMutableList());
					GlyphProvider.Conditional conditional = createFallbackProvider();
					list2.add(CompletableFuture.completedFuture(Optional.of(conditional.provider())));
					return Util.sequence(list2)
						.thenCompose(
							list2x -> {
								Map<ResourceLocation, List<GlyphProvider.Conditional>> map = this.resolveProviders(listx);
								CompletableFuture<?>[] completableFutures = (CompletableFuture<?>[])map.values()
									.stream()
									.map(listxxx -> CompletableFuture.runAsync(() -> this.finalizeProviderLoading(listxxx, conditional), executor))
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
		FontManager.BuilderId builderId, GlyphProviderDefinition.Loader loader, ResourceManager resourceManager, Executor executor
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

	private Map<ResourceLocation, List<GlyphProvider.Conditional>> resolveProviders(List<FontManager.UnresolvedBuilderBundle> list) {
		Map<ResourceLocation, List<GlyphProvider.Conditional>> map = new HashMap();
		DependencySorter<ResourceLocation, FontManager.UnresolvedBuilderBundle> dependencySorter = new DependencySorter<>();
		list.forEach(unresolvedBuilderBundle -> dependencySorter.addEntry(unresolvedBuilderBundle.fontId, unresolvedBuilderBundle));
		dependencySorter.orderByDependencies(
			(resourceLocation, unresolvedBuilderBundle) -> unresolvedBuilderBundle.resolve(map::get).ifPresent(listx -> map.put(resourceLocation, listx))
		);
		return map;
	}

	private void finalizeProviderLoading(List<GlyphProvider.Conditional> list, GlyphProvider.Conditional conditional) {
		list.add(0, conditional);
		IntSet intSet = new IntOpenHashSet();

		for (GlyphProvider.Conditional conditional2 : list) {
			intSet.addAll(conditional2.provider().getSupportedGlyphs());
		}

		intSet.forEach(i -> {
			if (i != 32) {
				for (GlyphProvider.Conditional conditionalx : Lists.reverse(list)) {
					if (conditionalx.provider().getGlyph(i) != null) {
						break;
					}
				}
			}
		});
	}

	private static Set<FontOption> getFontOptions(Options options) {
		Set<FontOption> set = EnumSet.noneOf(FontOption.class);
		if (options.forceUnicodeFont().get()) {
			set.add(FontOption.UNIFORM);
		}

		if (options.japaneseGlyphVariants().get()) {
			set.add(FontOption.JAPANESE_VARIANTS);
		}

		return set;
	}

	private void apply(FontManager.Preparation preparation, ProfilerFiller profilerFiller) {
		profilerFiller.push("closing");
		this.lastFontSetCache = null;
		this.fontSets.values().forEach(FontSet::close);
		this.fontSets.clear();
		this.providersToClose.forEach(GlyphProvider::close);
		this.providersToClose.clear();
		Set<FontOption> set = getFontOptions(Minecraft.getInstance().options);
		profilerFiller.popPush("reloading");
		preparation.fontSets().forEach((resourceLocation, list) -> {
			FontSet fontSet = new FontSet(this.textureManager, resourceLocation);
			fontSet.reload(Lists.reverse(list), set);
			this.fontSets.put(resourceLocation, fontSet);
		});
		this.providersToClose.addAll(preparation.allProviders);
		profilerFiller.pop();
		if (!this.fontSets.containsKey(Minecraft.DEFAULT_FONT)) {
			throw new IllegalStateException("Default font failed to load");
		}
	}

	public void updateOptions(Options options) {
		Set<FontOption> set = getFontOptions(options);

		for (FontSet fontSet : this.fontSets.values()) {
			fontSet.reload(set);
		}
	}

	private static List<Pair<FontManager.BuilderId, GlyphProviderDefinition.Conditional>> loadResourceStack(List<Resource> list, ResourceLocation resourceLocation) {
		List<Pair<FontManager.BuilderId, GlyphProviderDefinition.Conditional>> list2 = new ArrayList();

		for (Resource resource : list) {
			try {
				Reader reader = resource.openAsReader();

				try {
					JsonElement jsonElement = GSON.fromJson(reader, JsonElement.class);
					FontManager.FontDefinitionFile fontDefinitionFile = FontManager.FontDefinitionFile.CODEC
						.parse(JsonOps.INSTANCE, jsonElement)
						.getOrThrow(JsonParseException::new);
					List<GlyphProviderDefinition.Conditional> list3 = fontDefinitionFile.providers;

					for (int i = list3.size() - 1; i >= 0; i--) {
						FontManager.BuilderId builderId = new FontManager.BuilderId(resourceLocation, resource.sourcePackId(), i);
						list2.add(Pair.of(builderId, (GlyphProviderDefinition.Conditional)list3.get(i)));
					}
				} catch (Throwable var12) {
					if (reader != null) {
						try {
							reader.close();
						} catch (Throwable var11) {
							var12.addSuppressed(var11);
						}
					}

					throw var12;
				}

				if (reader != null) {
					reader.close();
				}
			} catch (Exception var13) {
				LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}'", resourceLocation, "fonts.json", resource.sourcePackId(), var13);
			}
		}

		return list2;
	}

	public Font createFont() {
		return new Font(this::getFontSetCached, false);
	}

	public Font createFontFilterFishy() {
		return new Font(this::getFontSetCached, true);
	}

	private FontSet getFontSetRaw(ResourceLocation resourceLocation) {
		return (FontSet)this.fontSets.getOrDefault(resourceLocation, this.missingFontSet);
	}

	private FontSet getFontSetCached(ResourceLocation resourceLocation) {
		FontSet fontSet = this.lastFontSetCache;
		if (fontSet != null && resourceLocation.equals(fontSet.name())) {
			return fontSet;
		} else {
			FontSet fontSet2 = this.getFontSetRaw(resourceLocation);
			this.lastFontSetCache = fontSet2;
			return fontSet2;
		}
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
	static record BuilderResult(FontManager.BuilderId id, FontOption.Filter filter, Either<CompletableFuture<Optional<GlyphProvider>>, ResourceLocation> result) {

		public Optional<List<GlyphProvider.Conditional>> resolve(Function<ResourceLocation, List<GlyphProvider.Conditional>> function) {
			return this.result
				.map(
					completableFuture -> ((Optional)completableFuture.join()).map(glyphProvider -> List.of(new GlyphProvider.Conditional(glyphProvider, this.filter))),
					resourceLocation -> {
						List<GlyphProvider.Conditional> list = (List<GlyphProvider.Conditional>)function.apply(resourceLocation);
						if (list == null) {
							FontManager.LOGGER
								.warn("Can't find font {} referenced by builder {}, either because it's missing, failed to load or is part of loading cycle", resourceLocation, this.id);
							return Optional.empty();
						} else {
							return Optional.of(list.stream().map(this::mergeFilters).toList());
						}
					}
				);
		}

		private GlyphProvider.Conditional mergeFilters(GlyphProvider.Conditional conditional) {
			return new GlyphProvider.Conditional(conditional.provider(), this.filter.merge(conditional.filter()));
		}
	}

	@Environment(EnvType.CLIENT)
	static record FontDefinitionFile(List<GlyphProviderDefinition.Conditional> providers) {
		public static final Codec<FontManager.FontDefinitionFile> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(GlyphProviderDefinition.Conditional.CODEC.listOf().fieldOf("providers").forGetter(FontManager.FontDefinitionFile::providers))
					.apply(instance, FontManager.FontDefinitionFile::new)
		);
	}

	@Environment(EnvType.CLIENT)
	static record Preparation(Map<ResourceLocation, List<GlyphProvider.Conditional>> fontSets, List<GlyphProvider> allProviders) {
	}

	@Environment(EnvType.CLIENT)
	static record UnresolvedBuilderBundle(ResourceLocation fontId, List<FontManager.BuilderResult> builders, Set<ResourceLocation> dependencies)
		implements DependencySorter.Entry<ResourceLocation> {

		public UnresolvedBuilderBundle(ResourceLocation resourceLocation) {
			this(resourceLocation, new ArrayList(), new HashSet());
		}

		public void add(FontManager.BuilderId builderId, FontOption.Filter filter, GlyphProviderDefinition.Reference reference) {
			this.builders.add(new FontManager.BuilderResult(builderId, filter, Either.right(reference.id())));
			this.dependencies.add(reference.id());
		}

		public void add(FontManager.BuilderId builderId, FontOption.Filter filter, CompletableFuture<Optional<GlyphProvider>> completableFuture) {
			this.builders.add(new FontManager.BuilderResult(builderId, filter, Either.left(completableFuture)));
		}

		private Stream<CompletableFuture<Optional<GlyphProvider>>> listBuilders() {
			return this.builders.stream().flatMap(builderResult -> builderResult.result.left().stream());
		}

		public Optional<List<GlyphProvider.Conditional>> resolve(Function<ResourceLocation, List<GlyphProvider.Conditional>> function) {
			List<GlyphProvider.Conditional> list = new ArrayList();

			for (FontManager.BuilderResult builderResult : this.builders) {
				Optional<List<GlyphProvider.Conditional>> optional = builderResult.resolve(function);
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
