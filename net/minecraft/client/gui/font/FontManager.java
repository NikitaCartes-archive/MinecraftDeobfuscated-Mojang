/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.AllMissingGlyphProvider;
import net.minecraft.client.gui.font.FontSet;
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

@Environment(value=EnvType.CLIENT)
public class FontManager
implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation MISSING_FONT = new ResourceLocation("minecraft", "missing");
    private final FontSet missingFontSet;
    private final Map<ResourceLocation, FontSet> fontSets = Maps.newHashMap();
    private final TextureManager textureManager;
    private Map<ResourceLocation, ResourceLocation> renames = ImmutableMap.of();
    private final PreparableReloadListener reloadListener = new SimplePreparableReloadListener<Map<ResourceLocation, List<GlyphProvider>>>(){

        @Override
        protected Map<ResourceLocation, List<GlyphProvider>> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            profilerFiller.startTick();
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            HashMap<ResourceLocation, List<GlyphProvider>> map = Maps.newHashMap();
            for (ResourceLocation resourceLocation2 : resourceManager.listResources("font", string -> string.endsWith(".json"))) {
                String string2 = resourceLocation2.getPath();
                ResourceLocation resourceLocation22 = new ResourceLocation(resourceLocation2.getNamespace(), string2.substring("font/".length(), string2.length() - ".json".length()));
                List list = map.computeIfAbsent(resourceLocation22, resourceLocation -> Lists.newArrayList(new AllMissingGlyphProvider()));
                profilerFiller.push(resourceLocation22::toString);
                try {
                    for (Resource resource : resourceManager.getResources(resourceLocation2)) {
                        profilerFiller.push(resource::getSourceName);
                        try (InputStream inputStream = resource.getInputStream();
                             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
                            profilerFiller.push("reading");
                            JsonArray jsonArray = GsonHelper.getAsJsonArray(GsonHelper.fromJson(gson, (Reader)reader, JsonObject.class), "providers");
                            profilerFiller.popPush("parsing");
                            for (int i2 = jsonArray.size() - 1; i2 >= 0; --i2) {
                                JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonArray.get(i2), "providers[" + i2 + "]");
                                try {
                                    String string22 = GsonHelper.getAsString(jsonObject, "type");
                                    GlyphProviderBuilderType glyphProviderBuilderType = GlyphProviderBuilderType.byName(string22);
                                    profilerFiller.push(string22);
                                    GlyphProvider glyphProvider = glyphProviderBuilderType.create(jsonObject).create(resourceManager);
                                    if (glyphProvider != null) {
                                        list.add(glyphProvider);
                                    }
                                    profilerFiller.pop();
                                    continue;
                                } catch (RuntimeException runtimeException) {
                                    LOGGER.warn("Unable to read definition '{}' in fonts.json in resourcepack: '{}': {}", (Object)resourceLocation22, (Object)resource.getSourceName(), (Object)runtimeException.getMessage());
                                }
                            }
                            profilerFiller.pop();
                        } catch (RuntimeException runtimeException2) {
                            LOGGER.warn("Unable to load font '{}' in fonts.json in resourcepack: '{}': {}", (Object)resourceLocation22, (Object)resource.getSourceName(), (Object)runtimeException2.getMessage());
                        }
                        profilerFiller.pop();
                    }
                } catch (IOException iOException) {
                    LOGGER.warn("Unable to load font '{}' in fonts.json: {}", (Object)resourceLocation22, (Object)iOException.getMessage());
                }
                profilerFiller.push("caching");
                IntOpenHashSet intSet = new IntOpenHashSet();
                for (GlyphProvider glyphProvider2 : list) {
                    intSet.addAll(glyphProvider2.getSupportedGlyphs());
                }
                intSet.forEach(i -> {
                    GlyphProvider glyphProvider;
                    if (i == 32) {
                        return;
                    }
                    Iterator iterator = Lists.reverse(list).iterator();
                    while (iterator.hasNext() && (glyphProvider = (GlyphProvider)iterator.next()).getGlyph(i) == null) {
                    }
                });
                profilerFiller.pop();
                profilerFiller.pop();
            }
            profilerFiller.endTick();
            return map;
        }

        @Override
        protected void apply(Map<ResourceLocation, List<GlyphProvider>> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            profilerFiller.startTick();
            profilerFiller.push("closing");
            FontManager.this.fontSets.values().forEach(FontSet::close);
            FontManager.this.fontSets.clear();
            profilerFiller.popPush("reloading");
            map.forEach((resourceLocation, list) -> {
                FontSet fontSet = new FontSet(FontManager.this.textureManager, (ResourceLocation)resourceLocation);
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

        @Override
        protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            return this.prepare(resourceManager, profilerFiller);
        }
    };

    public FontManager(TextureManager textureManager) {
        this.textureManager = textureManager;
        this.missingFontSet = Util.make(new FontSet(textureManager, MISSING_FONT), fontSet -> fontSet.reload(Lists.newArrayList(new AllMissingGlyphProvider())));
    }

    public void setRenames(Map<ResourceLocation, ResourceLocation> map) {
        this.renames = map;
    }

    public Font createFont() {
        return new Font(resourceLocation -> this.fontSets.getOrDefault(this.renames.getOrDefault(resourceLocation, (ResourceLocation)resourceLocation), this.missingFontSet));
    }

    public PreparableReloadListener getReloadListener() {
        return this.reloadListener;
    }

    @Override
    public void close() {
        this.fontSets.values().forEach(FontSet::close);
        this.missingFontSet.close();
    }
}

