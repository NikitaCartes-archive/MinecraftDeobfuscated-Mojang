/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class FontManager
implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<ResourceLocation, Font> fonts = Maps.newHashMap();
    private final Set<GlyphProvider> providers = Sets.newHashSet();
    private final TextureManager textureManager;
    private boolean forceUnicode;
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
                            for (int i = jsonArray.size() - 1; i >= 0; --i) {
                                JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonArray.get(i), "providers[" + i + "]");
                                try {
                                    String string22 = GsonHelper.getAsString(jsonObject, "type");
                                    GlyphProviderBuilderType glyphProviderBuilderType = GlyphProviderBuilderType.byName(string22);
                                    if (FontManager.this.forceUnicode && glyphProviderBuilderType != GlyphProviderBuilderType.LEGACY_UNICODE && resourceLocation22.equals(Minecraft.DEFAULT_FONT)) continue;
                                    profilerFiller.push(string22);
                                    list.add(glyphProviderBuilderType.create(jsonObject).create(resourceManager));
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
                for (char c = '\u0000'; c < '\uffff'; c = (char)((char)(c + 1))) {
                    GlyphProvider glyphProvider;
                    if (c == ' ') continue;
                    Iterator iterator = Lists.reverse(list).iterator();
                    while (iterator.hasNext() && (glyphProvider = (GlyphProvider)iterator.next()).getGlyph(c) == null) {
                    }
                }
                profilerFiller.pop();
                profilerFiller.pop();
            }
            profilerFiller.endTick();
            return map;
        }

        @Override
        protected void apply(Map<ResourceLocation, List<GlyphProvider>> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            profilerFiller.startTick();
            profilerFiller.push("reloading");
            Stream.concat(FontManager.this.fonts.keySet().stream(), map.keySet().stream()).distinct().forEach(resourceLocation2 -> {
                List<GlyphProvider> list = map.getOrDefault(resourceLocation2, Collections.emptyList());
                Collections.reverse(list);
                FontManager.this.fonts.computeIfAbsent(resourceLocation2, resourceLocation -> new Font(FontManager.this.textureManager, new FontSet(FontManager.this.textureManager, (ResourceLocation)resourceLocation))).reload(list);
            });
            map.values().forEach(FontManager.this.providers::addAll);
            profilerFiller.pop();
            profilerFiller.endTick();
        }

        @Override
        protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            return this.prepare(resourceManager, profilerFiller);
        }
    };

    public FontManager(TextureManager textureManager, boolean bl) {
        this.textureManager = textureManager;
        this.forceUnicode = bl;
    }

    @Nullable
    public Font get(ResourceLocation resourceLocation2) {
        return this.fonts.computeIfAbsent(resourceLocation2, resourceLocation -> {
            Font font = new Font(this.textureManager, new FontSet(this.textureManager, (ResourceLocation)resourceLocation));
            font.reload(Lists.newArrayList(new AllMissingGlyphProvider()));
            return font;
        });
    }

    public void setForceUnicode(boolean bl, Executor executor, Executor executor2) {
        if (bl == this.forceUnicode) {
            return;
        }
        this.forceUnicode = bl;
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        PreparableReloadListener.PreparationBarrier preparationBarrier = new PreparableReloadListener.PreparationBarrier(){

            @Override
            public <T> CompletableFuture<T> wait(T object) {
                return CompletableFuture.completedFuture(object);
            }
        };
        this.reloadListener.reload(preparationBarrier, resourceManager, InactiveProfiler.INACTIVE, InactiveProfiler.INACTIVE, executor, executor2);
    }

    public PreparableReloadListener getReloadListener() {
        return this.reloadListener;
    }

    @Override
    public void close() {
        this.fonts.values().forEach(Font::close);
        this.providers.forEach(GlyphProvider::close);
    }
}

