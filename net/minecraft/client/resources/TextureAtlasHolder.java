/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(value=EnvType.CLIENT)
public abstract class TextureAtlasHolder
implements PreparableReloadListener,
AutoCloseable {
    private final TextureAtlas textureAtlas;
    private final ResourceLocation atlasInfoLocation;

    public TextureAtlasHolder(TextureManager textureManager, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        this.atlasInfoLocation = resourceLocation2;
        this.textureAtlas = new TextureAtlas(resourceLocation);
        textureManager.register(this.textureAtlas.location(), this.textureAtlas);
    }

    protected TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
        return this.textureAtlas.getSprite(resourceLocation);
    }

    @Override
    public final CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        return ((CompletableFuture)((CompletableFuture)SpriteLoader.create(this.textureAtlas).loadAndStitch(resourceManager, this.atlasInfoLocation, 0, executor).thenCompose(SpriteLoader.Preparations::waitForUpload)).thenCompose(preparationBarrier::wait)).thenAcceptAsync(preparations -> this.apply((SpriteLoader.Preparations)preparations, profilerFiller2), executor2);
    }

    private void apply(SpriteLoader.Preparations preparations, ProfilerFiller profilerFiller) {
        profilerFiller.startTick();
        profilerFiller.push("upload");
        this.textureAtlas.upload(preparations);
        profilerFiller.pop();
        profilerFiller.endTick();
    }

    @Override
    public void close() {
        this.textureAtlas.clearTextureData();
    }
}

