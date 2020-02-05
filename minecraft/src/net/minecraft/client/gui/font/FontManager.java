package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class FontManager implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Map<ResourceLocation, Font> fonts = Maps.<ResourceLocation, Font>newHashMap();
	private final TextureManager textureManager;
	private boolean forceUnicode;
	private final PreparableReloadListener reloadListener = new SimplePreparableReloadListener<Map<ResourceLocation, List<GlyphProvider>>>() {
		protected Map<ResourceLocation, List<GlyphProvider>> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
			profilerFiller.startTick();
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
			Map<ResourceLocation, List<GlyphProvider>> map = Maps.<ResourceLocation, List<GlyphProvider>>newHashMap();

			for (ResourceLocation resourceLocation : resourceManager.listResources("font", stringx -> stringx.endsWith(".json"))) {
				String string = resourceLocation.getPath();
				ResourceLocation resourceLocation2 = new ResourceLocation(
					resourceLocation.getNamespace(), string.substring("font/".length(), string.length() - ".json".length())
				);
				List<GlyphProvider> list = (List<GlyphProvider>)map.computeIfAbsent(
					resourceLocation2, resourceLocationx -> Lists.<GlyphProvider>newArrayList(new AllMissingGlyphProvider())
				);
				profilerFiller.push(resourceLocation2::toString);

				try {
					for (Resource resource : resourceManager.getResources(resourceLocation)) {
						profilerFiller.push(resource::getSourceName);

						try {
							InputStream inputStream = resource.getInputStream();
							Throwable var13 = null;

							try {
								Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
								Throwable var15 = null;

								try {
									profilerFiller.push("reading");
									JsonArray jsonArray = GsonHelper.getAsJsonArray(GsonHelper.fromJson(gson, reader, JsonObject.class), "providers");
									profilerFiller.popPush("parsing");

									for (int i = jsonArray.size() - 1; i >= 0; i--) {
										JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonArray.get(i), "providers[" + i + "]");

										try {
											String string2 = GsonHelper.getAsString(jsonObject, "type");
											GlyphProviderBuilderType glyphProviderBuilderType = GlyphProviderBuilderType.byName(string2);
											if (!FontManager.this.forceUnicode
												|| glyphProviderBuilderType == GlyphProviderBuilderType.LEGACY_UNICODE
												|| !resourceLocation2.equals(Minecraft.DEFAULT_FONT)) {
												profilerFiller.push(string2);
												list.add(glyphProviderBuilderType.create(jsonObject).create(resourceManager));
												profilerFiller.pop();
											}
										} catch (RuntimeException var48) {
											FontManager.LOGGER
												.warn("Unable to read definition '{}' in fonts.json in resourcepack: '{}': {}", resourceLocation2, resource.getSourceName(), var48.getMessage());
										}
									}

									profilerFiller.pop();
								} catch (Throwable var49) {
									var15 = var49;
									throw var49;
								} finally {
									if (reader != null) {
										if (var15 != null) {
											try {
												reader.close();
											} catch (Throwable var47) {
												var15.addSuppressed(var47);
											}
										} else {
											reader.close();
										}
									}
								}
							} catch (Throwable var51) {
								var13 = var51;
								throw var51;
							} finally {
								if (inputStream != null) {
									if (var13 != null) {
										try {
											inputStream.close();
										} catch (Throwable var46) {
											var13.addSuppressed(var46);
										}
									} else {
										inputStream.close();
									}
								}
							}
						} catch (RuntimeException var53) {
							FontManager.LOGGER
								.warn("Unable to load font '{}' in fonts.json in resourcepack: '{}': {}", resourceLocation2, resource.getSourceName(), var53.getMessage());
						}

						profilerFiller.pop();
					}
				} catch (IOException var54) {
					FontManager.LOGGER.warn("Unable to load font '{}' in fonts.json: {}", resourceLocation2, var54.getMessage());
				}

				profilerFiller.push("caching");

				for (char c = 0; c < '\uffff'; c++) {
					if (c != ' ') {
						for (GlyphProvider glyphProvider : Lists.reverse(list)) {
							if (glyphProvider.getGlyph(c) != null) {
								break;
							}
						}
					}
				}

				profilerFiller.pop();
				profilerFiller.pop();
			}

			profilerFiller.endTick();
			return map;
		}

		protected void apply(Map<ResourceLocation, List<GlyphProvider>> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
			profilerFiller.startTick();
			profilerFiller.push("reloading");
			Stream.concat(FontManager.this.fonts.keySet().stream(), map.keySet().stream())
				.distinct()
				.forEach(
					resourceLocation -> {
						List<GlyphProvider> list = (List<GlyphProvider>)map.getOrDefault(resourceLocation, Collections.emptyList());
						Collections.reverse(list);
						((Font)FontManager.this.fonts
								.computeIfAbsent(
									resourceLocation, resourceLocationx -> new Font(FontManager.this.textureManager, new FontSet(FontManager.this.textureManager, resourceLocationx))
								))
							.reload(list);
					}
				);
			profilerFiller.pop();
			profilerFiller.endTick();
		}

		@Override
		public String getName() {
			return "FontManager";
		}
	};

	public FontManager(TextureManager textureManager, boolean bl) {
		this.textureManager = textureManager;
		this.forceUnicode = bl;
	}

	@Nullable
	public Font get(ResourceLocation resourceLocation) {
		return (Font)this.fonts.computeIfAbsent(resourceLocation, resourceLocationx -> {
			Font font = new Font(this.textureManager, new FontSet(this.textureManager, resourceLocationx));
			font.reload(Lists.<GlyphProvider>newArrayList(new AllMissingGlyphProvider()));
			return font;
		});
	}

	public void setForceUnicode(boolean bl, Executor executor, Executor executor2) {
		if (bl != this.forceUnicode) {
			this.forceUnicode = bl;
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			PreparableReloadListener.PreparationBarrier preparationBarrier = new PreparableReloadListener.PreparationBarrier() {
				@Override
				public <T> CompletableFuture<T> wait(T object) {
					return CompletableFuture.completedFuture(object);
				}
			};
			this.reloadListener.reload(preparationBarrier, resourceManager, InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, executor, executor2);
		}
	}

	public PreparableReloadListener getReloadListener() {
		return this.reloadListener;
	}

	public void close() {
		this.fonts.values().forEach(Font::close);
	}
}
