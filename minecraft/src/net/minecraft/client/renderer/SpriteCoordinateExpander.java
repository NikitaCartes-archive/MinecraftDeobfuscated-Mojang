package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Environment(EnvType.CLIENT)
public class SpriteCoordinateExpander implements VertexConsumer {
	private final VertexConsumer delegate;
	private final TextureAtlasSprite sprite;

	public SpriteCoordinateExpander(VertexConsumer vertexConsumer, TextureAtlasSprite textureAtlasSprite) {
		this.delegate = vertexConsumer;
		this.sprite = textureAtlasSprite;
	}

	@Override
	public VertexConsumer vertex(double d, double e, double f) {
		return this.delegate.vertex(d, e, f);
	}

	@Override
	public VertexConsumer color(int i, int j, int k, int l) {
		return this.delegate.color(i, j, k, l);
	}

	@Override
	public VertexConsumer uv(float f, float g) {
		return this.delegate.uv(this.sprite.getU(f), this.sprite.getV(g));
	}

	@Override
	public VertexConsumer overlayCoords(int i, int j) {
		return this.delegate.overlayCoords(i, j);
	}

	@Override
	public VertexConsumer uv2(int i, int j) {
		return this.delegate.uv2(i, j);
	}

	@Override
	public VertexConsumer normal(float f, float g, float h) {
		return this.delegate.normal(f, g, h);
	}

	@Override
	public void endVertex() {
		this.delegate.endVertex();
	}

	@Override
	public void defaultColor(int i, int j, int k, int l) {
		this.delegate.defaultColor(i, j, k, l);
	}

	@Override
	public void unsetDefaultColor() {
		this.delegate.unsetDefaultColor();
	}

	@Override
	public void vertex(float f, float g, float h, float i, float j, float k, float l, float m, float n, int o, int p, float q, float r, float s) {
		this.delegate.vertex(f, g, h, i, j, k, l, this.sprite.getU(m), this.sprite.getV(n), o, p, q, r, s);
	}
}
