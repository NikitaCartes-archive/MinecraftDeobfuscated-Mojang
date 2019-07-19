package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.BufferBuilder;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class BakedGlyph {
	private final ResourceLocation texture;
	private final float u0;
	private final float u1;
	private final float v0;
	private final float v1;
	private final float left;
	private final float right;
	private final float up;
	private final float down;

	public BakedGlyph(ResourceLocation resourceLocation, float f, float g, float h, float i, float j, float k, float l, float m) {
		this.texture = resourceLocation;
		this.u0 = f;
		this.u1 = g;
		this.v0 = h;
		this.v1 = i;
		this.left = j;
		this.right = k;
		this.up = l;
		this.down = m;
	}

	public void render(TextureManager textureManager, boolean bl, float f, float g, BufferBuilder bufferBuilder, float h, float i, float j, float k) {
		int l = 3;
		float m = f + this.left;
		float n = f + this.right;
		float o = this.up - 3.0F;
		float p = this.down - 3.0F;
		float q = g + o;
		float r = g + p;
		float s = bl ? 1.0F - 0.25F * o : 0.0F;
		float t = bl ? 1.0F - 0.25F * p : 0.0F;
		bufferBuilder.vertex((double)(m + s), (double)q, 0.0).uv((double)this.u0, (double)this.v0).color(h, i, j, k).endVertex();
		bufferBuilder.vertex((double)(m + t), (double)r, 0.0).uv((double)this.u0, (double)this.v1).color(h, i, j, k).endVertex();
		bufferBuilder.vertex((double)(n + t), (double)r, 0.0).uv((double)this.u1, (double)this.v1).color(h, i, j, k).endVertex();
		bufferBuilder.vertex((double)(n + s), (double)q, 0.0).uv((double)this.u1, (double)this.v0).color(h, i, j, k).endVertex();
	}

	@Nullable
	public ResourceLocation getTexture() {
		return this.texture;
	}
}
