package net.minecraft.client.resources.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
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
		return (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(this.atlasLocation()).apply(this.texture());
	}

	public RenderType renderType(Function<ResourceLocation, RenderType> function) {
		if (this.renderType == null) {
			this.renderType = (RenderType)function.apply(this.atlasLocation);
		}

		return this.renderType;
	}

	public VertexConsumer buffer(MultiBufferSource multiBufferSource, Function<ResourceLocation, RenderType> function) {
		return this.sprite().wrap(multiBufferSource.getBuffer(this.renderType(function)));
	}

	public VertexConsumer buffer(MultiBufferSource multiBufferSource, Function<ResourceLocation, RenderType> function, boolean bl) {
		return this.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, this.renderType(function), false, bl));
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			Material material = (Material)object;
			return this.atlasLocation.equals(material.atlasLocation) && this.texture.equals(material.texture);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.atlasLocation, this.texture});
	}

	public String toString() {
		return "Material{atlasLocation=" + this.atlasLocation + ", texture=" + this.texture + '}';
	}
}
