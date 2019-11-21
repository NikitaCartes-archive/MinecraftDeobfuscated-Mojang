/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Material {
    private final ResourceLocation atlasLocation;
    private final ResourceLocation texture;
    @Nullable
    private RenderType renderType;

    public Material(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        this.atlasLocation = resourceLocation;
        this.texture = resourceLocation2;
    }

    public ResourceLocation atlasLocation() {
        return this.atlasLocation;
    }

    public ResourceLocation texture() {
        return this.texture;
    }

    public TextureAtlasSprite sprite() {
        return Minecraft.getInstance().getTextureAtlas(this.atlasLocation()).apply(this.texture());
    }

    public RenderType renderType(Function<ResourceLocation, RenderType> function) {
        if (this.renderType == null) {
            this.renderType = function.apply(this.atlasLocation);
        }
        return this.renderType;
    }

    public VertexConsumer buffer(MultiBufferSource multiBufferSource, Function<ResourceLocation, RenderType> function) {
        return this.sprite().wrap(multiBufferSource.getBuffer(this.renderType(function)));
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Material material = (Material)object;
        return this.atlasLocation.equals(material.atlasLocation) && this.texture.equals(material.texture);
    }

    public int hashCode() {
        return Objects.hash(this.atlasLocation, this.texture);
    }

    public String toString() {
        return "Material{atlasLocation=" + this.atlasLocation + ", texture=" + this.texture + '}';
    }
}

