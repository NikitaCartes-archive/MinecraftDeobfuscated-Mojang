package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class BakedGlyph {
	private final GlyphRenderTypes renderTypes;
	private final float u0;
	private final float u1;
	private final float v0;
	private final float v1;
	private final float left;
	private final float right;
	private final float up;
	private final float down;

	public BakedGlyph(GlyphRenderTypes glyphRenderTypes, float f, float g, float h, float i, float j, float k, float l, float m) {
		this.renderTypes = glyphRenderTypes;
		this.u0 = f;
		this.u1 = g;
		this.v0 = h;
		this.v1 = i;
		this.left = j;
		this.right = k;
		this.up = l;
		this.down = m;
	}

	public void render(boolean bl, float f, float g, Matrix4f matrix4f, VertexConsumer vertexConsumer, float h, float i, float j, float k, int l) {
		float m = f + this.left;
		float n = f + this.right;
		float o = g + this.up;
		float p = g + this.down;
		float q = bl ? 1.0F - 0.25F * this.up : 0.0F;
		float r = bl ? 1.0F - 0.25F * this.down : 0.0F;
		vertexConsumer.addVertex(matrix4f, m + q, o, 0.0F).setColor(h, i, j, k).setUv(this.u0, this.v0).setLight(l);
		vertexConsumer.addVertex(matrix4f, m + r, p, 0.0F).setColor(h, i, j, k).setUv(this.u0, this.v1).setLight(l);
		vertexConsumer.addVertex(matrix4f, n + r, p, 0.0F).setColor(h, i, j, k).setUv(this.u1, this.v1).setLight(l);
		vertexConsumer.addVertex(matrix4f, n + q, o, 0.0F).setColor(h, i, j, k).setUv(this.u1, this.v0).setLight(l);
	}

	public void renderEffect(BakedGlyph.Effect effect, Matrix4f matrix4f, VertexConsumer vertexConsumer, int i) {
		vertexConsumer.addVertex(matrix4f, effect.x0, effect.y0, effect.depth).setColor(effect.r, effect.g, effect.b, effect.a).setUv(this.u0, this.v0).setLight(i);
		vertexConsumer.addVertex(matrix4f, effect.x1, effect.y0, effect.depth).setColor(effect.r, effect.g, effect.b, effect.a).setUv(this.u0, this.v1).setLight(i);
		vertexConsumer.addVertex(matrix4f, effect.x1, effect.y1, effect.depth).setColor(effect.r, effect.g, effect.b, effect.a).setUv(this.u1, this.v1).setLight(i);
		vertexConsumer.addVertex(matrix4f, effect.x0, effect.y1, effect.depth).setColor(effect.r, effect.g, effect.b, effect.a).setUv(this.u1, this.v0).setLight(i);
	}

	public RenderType renderType(Font.DisplayMode displayMode) {
		return this.renderTypes.select(displayMode);
	}

	@Environment(EnvType.CLIENT)
	public static class Effect {
		protected final float x0;
		protected final float y0;
		protected final float x1;
		protected final float y1;
		protected final float depth;
		protected final float r;
		protected final float g;
		protected final float b;
		protected final float a;

		public Effect(float f, float g, float h, float i, float j, float k, float l, float m, float n) {
			this.x0 = f;
			this.y0 = g;
			this.x1 = h;
			this.y1 = i;
			this.depth = j;
			this.r = k;
			this.g = l;
			this.b = m;
			this.a = n;
		}
	}
}
