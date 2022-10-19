/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.model;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AtlasSet
implements AutoCloseable {
    private final Map<ResourceLocation, AtlasEntry> atlases;

    public AtlasSet(Map<ResourceLocation, ResourceLister> map, TextureManager textureManager) {
        this.atlases = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            TextureAtlas textureAtlas = new TextureAtlas((ResourceLocation)entry.getKey());
            textureManager.register((ResourceLocation)entry.getKey(), textureAtlas);
            return new AtlasEntry(textureAtlas, (ResourceLister)entry.getValue());
        }));
    }

    public TextureAtlas getAtlas(ResourceLocation resourceLocation) {
        return this.atlases.get(resourceLocation).atlas();
    }

    @Override
    public void close() {
        this.atlases.values().forEach(AtlasEntry::close);
        this.atlases.clear();
    }

    public Map<ResourceLocation, CompletableFuture<StitchResult>> scheduleLoad(ResourceManager resourceManager, int i, Executor executor) {
        return this.atlases.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            AtlasEntry atlasEntry = (AtlasEntry)entry.getValue();
            return ((CompletableFuture)CompletableFuture.supplyAsync(() -> (Map)atlasEntry.resourceLister.apply(resourceManager), executor).thenCompose(map -> SpriteLoader.create(atlasEntry.atlas).stitch((Map<ResourceLocation, Resource>)map, i, executor))).thenApply(preparations -> new StitchResult(atlasEntry.atlas, (SpriteLoader.Preparations)preparations));
        }));
    }

    @Environment(value=EnvType.CLIENT)
    record AtlasEntry(TextureAtlas atlas, ResourceLister resourceLister) implements AutoCloseable
    {
        @Override
        public void close() {
            this.atlas.clearTextureData();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class StitchResult {
        private final TextureAtlas atlas;
        private final SpriteLoader.Preparations preparations;

        public StitchResult(TextureAtlas textureAtlas, SpriteLoader.Preparations preparations) {
            this.atlas = textureAtlas;
            this.preparations = preparations;
        }

        @Nullable
        public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
            return this.preparations.regions().get(resourceLocation);
        }

        public TextureAtlasSprite missing() {
            return this.preparations.missing();
        }

        public CompletableFuture<Void> readyForUpload() {
            return this.preparations.readyForUpload();
        }

        public void upload() {
            this.atlas.upload(this.preparations);
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface ResourceLister
    extends Function<ResourceManager, Map<ResourceLocation, Resource>> {
    }
}

