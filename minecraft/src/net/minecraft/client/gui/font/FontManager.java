package net.minecraft.client.gui.font;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class FontManager implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final ResourceLocation MISSING_FONT = new ResourceLocation("minecraft", "missing");
	private final FontSet missingFontSet;
	private final Map<ResourceLocation, FontSet> fontSets = Maps.<ResourceLocation, FontSet>newHashMap();
	private final TextureManager textureManager;
	private Map<ResourceLocation, ResourceLocation> renames = ImmutableMap.of();
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
											profilerFiller.push(string2);
											GlyphProvider glyphProvider = glyphProviderBuilderType.create(jsonObject).create(resourceManager);
											if (glyphProvider != null) {
												list.add(glyphProvider);
											}

											profilerFiller.pop();
										} catch (RuntimeException var49) {
											FontManager.LOGGER
												.warn("Unable to read definition '{}' in fonts.json in resourcepack: '{}': {}", resourceLocation2, resource.getSourceName(), var49.getMessage());
										}
									}

									profilerFiller.pop();
								} catch (Throwable var50) {
									var15 = var50;
									throw var50;
								} finally {
									if (reader != null) {
										if (var15 != null) {
											try {
												reader.close();
											} catch (Throwable var48) {
												var15.addSuppressed(var48);
											}
										} else {
											reader.close();
										}
									}
								}
							} catch (Throwable var52) {
								var13 = var52;
								throw var52;
							} finally {
								if (inputStream != null) {
									if (var13 != null) {
										try {
											inputStream.close();
										} catch (Throwable var47) {
											var13.addSuppressed(var47);
										}
									} else {
										inputStream.close();
									}
								}
							}
						} catch (RuntimeException var54) {
							FontManager.LOGGER
								.warn("Unable to load font '{}' in fonts.json in resourcepack: '{}': {}", resourceLocation2, resource.getSourceName(), var54.getMessage());
						}

						profilerFiller.pop();
					}
				} catch (IOException var55) {
					FontManager.LOGGER.warn("Unable to load font '{}' in fonts.json: {}", resourceLocation2, var55.getMessage());
				}

				profilerFiller.push("caching");
				IntSet intSet = new IntOpenHashSet();

				for (GlyphProvider glyphProvider2 : list) {
					intSet.addAll(glyphProvider2.getSupportedGlyphs());
				}

				intSet.forEach(ix -> {
					if (ix != 32) {
						for (GlyphProvider glyphProviderx : Lists.reverse(list)) {
							if (glyphProviderx.getGlyph(ix) != null) {
								break;
							}
						}
					}
				});
				profilerFiller.pop();
				profilerFiller.pop();
			}

			profilerFiller.endTick();
			return map;
		}

		protected void apply(Map<ResourceLocation, List<GlyphProvider>> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
			profilerFiller.startTick();
			profilerFiller.push("closing");
			FontManager.this.fontSets.values().forEach(FontSet::close);
			FontManager.this.fontSets.clear();
			profilerFiller.popPush("reloading");
			map.forEach((resourceLocation, list) -> {
				FontSet fontSet = new FontSet(FontManager.this.textureManager, resourceLocation);
				fontSet.reload(Lists.reverse(list));
				FontManager.this.fontSets.put(resourceLocation, fontSet);
			});
			profilerFiller.pop();
			profilerFiller.endTick();
		}

		@Override
		public String getName() {
			return "FontManager";
		}
	};

	public FontManager(TextureManager textureManager) {
		this.textureManager = textureManager;
		this.missingFontSet = Util.make(
			new FontSet(textureManager, MISSING_FONT), fontSet -> fontSet.reload(Lists.<GlyphProvider>newArrayList(new AllMissingGlyphProvider()))
		);
	}

	public void setRenames(Map<ResourceLocation, ResourceLocation> map) {
		this.renames = map;
	}

	public Font createFont() {
		return new Font(resourceLocation -> (FontSet)this.fontSets.getOrDefault(this.renames.getOrDefault(resourceLocation, resourceLocation), this.missingFontSet));
	}

	public PreparableReloadListener getReloadListener() {
		return this.reloadListener;
	}

	public void close() {
		this.fontSets.values().forEach(FontSet::close);
		this.missingFontSet.close();
	}
}
