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

	public void render(boolean bl, float f, float g, Matrix4f matrix4f, VertexConsumer vertexConsumer, int i, int j) {
		float h = f + this.left;
		float k = f + this.right;
		float l = g + this.up;
		float m = g + this.down;
		float n = bl ? 1.0F - 0.25F * this.up : 0.0F;
		float o = bl ? 1.0F - 0.25F * this.down : 0.0F;
		vertexConsumer.addVertex(matrix4f, h + n, l, 0.0F).setColor(i).setUv(this.u0, this.v0).setLight(j);
		vertexConsumer.addVertex(matrix4f, h + o, m, 0.0F).setColor(i).setUv(this.u0, this.v1).setLight(j);
		vertexConsumer.addVertex(matrix4f, k + o, m, 0.0F).setColor(i).setUv(this.u1, this.v1).setLight(j);
		vertexConsumer.addVertex(matrix4f, k + n, l, 0.0F).setColor(i).setUv(this.u1, this.v0).setLight(j);
	}

	public void renderEffect(BakedGlyph.Effect effect, Matrix4f matrix4f, VertexConsumer vertexConsumer, int i) {
		vertexConsumer.addVertex(matrix4f, effect.x0, effect.y0, effect.depth).setColor(effect.color).setUv(this.u0, this.v0).setLight(i);
		vertexConsumer.addVertex(matrix4f, effect.x1, effect.y0, effect.depth).setColor(effect.color).setUv(this.u0, this.v1).setLight(i);
		vertexConsumer.addVertex(matrix4f, effect.x1, effect.y1, effect.depth).setColor(effect.color).setUv(this.u1, this.v1).setLight(i);
		vertexConsumer.addVertex(matrix4f, effect.x0, effect.y1, effect.depth).setColor(effect.color).setUv(this.u1, this.v0).setLight(i);
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
		protected final int color;

		public Effect(float f, float g, float h, float i, float j, int k) {
			this.x0 = f;
			this.y0 = g;
			this.x1 = h;
			this.y1 = i;
			this.depth = j;
			this.color = k;
		}
	}
}
