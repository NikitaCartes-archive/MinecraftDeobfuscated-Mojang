/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(value=EnvType.CLIENT)
public abstract class TextureAtlasHolder
extends SimplePreparableReloadListener<TextureAtlas.Preparations>
implements AutoCloseable {
    private final TextureAtlas textureAtlas;

    public TextureAtlasHolder(TextureManager textureManager, ResourceLocation resourceLocation, String string) {
        this.textureAtlas = new TextureAtlas(string);
        textureManager.register(resourceLocation, this.textureAtlas);
    }

    protected abstract Iterable<ResourceLocation> getResourcesToLoad();

    protected TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
        return this.textureAtlas.getSprite(resourceLocation);
    }

    @Override
    protected TextureAtlas.Preparations prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        profilerFiller.startTick();
        profilerFiller.push("stitching");
        TextureAtlas.Preparations preparations = this.textureAtlas.prepareToStitch(resourceManager, this.getResourcesToLoad(), profilerFiller);
        profilerFiller.pop();
        profilerFiller.endTick();
        return preparations;
    }

    @Override
    protected void apply(TextureAtlas.Preparations preparations, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        profilerFiller.startTick();
        profilerFiller.push("upload");
        this.textureAtlas.reload(preparations);
        profilerFiller.pop();
        profilerFiller.endTick();
    }

    @Override
    public void close() {
        this.textureAtlas.clearTextureData();
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }
}

