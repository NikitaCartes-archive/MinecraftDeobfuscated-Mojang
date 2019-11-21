/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public class AtlasSet
implements AutoCloseable {
    private final Map<ResourceLocation, TextureAtlas> atlases;

    public AtlasSet(Collection<TextureAtlas> collection) {
        this.atlases = collection.stream().collect(Collectors.toMap(TextureAtlas::location, Function.identity()));
    }

    public TextureAtlas getAtlas(ResourceLocation resourceLocation) {
        return this.atlases.get(resourceLocation);
    }

    public TextureAtlasSprite getSprite(Material material) {
        return this.atlases.get(material.atlasLocation()).getSprite(material.texture());
    }

    @Override
    public void close() {
        this.atlases.values().forEach(TextureAtlas::clearTextureData);
        this.atlases.clear();
    }

    public void updateMaxMipLevel(TextureManager textureManager, int i) {
        this.atlases.values().forEach(textureAtlas -> {
            textureAtlas.setMaxMipLevel(i);
            textureManager.bind(textureAtlas.location());
            textureAtlas.setFilter(false, i > 0);
        });
    }
}

