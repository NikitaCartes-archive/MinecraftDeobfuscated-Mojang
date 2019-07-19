package net.minecraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class GuiComponent {
	public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
	public static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
	public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
	protected int blitOffset;

	protected void hLine(int i, int j, int k, int l) {
		if (j < i) {
			int m = i;
			i = j;
			j = m;
		}

		fill(i, k, j + 1, k + 1, l);
	}

	protected void vLine(int i, int j, int k, int l) {
		if (k < j) {
			int m = j;
			j = k;
			k = m;
		}

		fill(i, j + 1, i + 1, k, l);
	}

	public static void fill(int i, int j, int k, int l, int m) {
		if (i < k) {
			int n = i;
			i = k;
			k = n;
		}

		if (j < l) {
			int n = j;
			j = l;
			l = n;
		}

		float f = (float)(m >> 24 & 0xFF) / 255.0F;
		float g = (float)(m >> 16 & 0xFF) / 255.0F;
		float h = (float)(m >> 8 & 0xFF) / 255.0F;
		float o = (float)(m & 0xFF) / 255.0F;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		GlStateManager.color4f(g, h, o, f);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
		bufferBuilder.vertex((double)i, (double)l, 0.0).endVertex();
		bufferBuilder.vertex((double)k, (double)l, 0.0).endVertex();
		bufferBuilder.vertex((double)k, (double)j, 0.0).endVertex();
		bufferBuilder.vertex((double)i, (double)j, 0.0).endVertex();
		tesselator.end();
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
	}

	protected void fillGradient(int i, int j, int k, int l, int m, int n) {
		float f = (float)(m >> 24 & 0xFF) / 255.0F;
		float g = (float)(m >> 16 & 0xFF) / 255.0F;
		float h = (float)(m >> 8 & 0xFF) / 255.0F;
		float o = (float)(m & 0xFF) / 255.0F;
		float p = (float)(n >> 24 & 0xFF) / 255.0F;
		float q = (float)(n >> 16 & 0xFF) / 255.0F;
		float r = (float)(n >> 8 & 0xFF) / 255.0F;
		float s = (float)(n & 0xFF) / 255.0F;
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		GlStateManager.shadeModel(7425);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex((double)k, (double)j, (double)this.blitOffset).color(g, h, o, f).endVertex();
		bufferBuilder.vertex((double)i, (double)j, (double)this.blitOffset).color(g, h, o, f).endVertex();
		bufferBuilder.vertex((double)i, (double)l, (double)this.blitOffset).color(q, r, s, p).endVertex();
		bufferBuilder.vertex((double)k, (double)l, (double)this.blitOffset).color(q, r, s, p).endVertex();
		tesselator.end();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableTexture();
	}

	public void drawCenteredString(Font font, String string, int i, int j, int k) {
		font.drawShadow(string, (float)(i - font.width(string) / 2), (float)j, k);
	}

	public void drawRightAlignedString(Font font, String string, int i, int j, int k) {
		font.drawShadow(string, (float)(i - font.width(string)), (float)j, k);
	}

	public void drawString(Font font, String string, int i, int j, int k) {
		font.drawShadow(string, (float)i, (float)j, k);
	}

	public static void blit(int i, int j, int k, int l, int m, TextureAtlasSprite textureAtlasSprite) {
		innerBlit(i, i + l, j, j + m, k, textureAtlasSprite.getU0(), textureAtlasSprite.getU1(), textureAtlasSprite.getV0(), textureAtlasSprite.getV1());
	}

	public void blit(int i, int j, int k, int l, int m, int n) {
		blit(i, j, this.blitOffset, (float)k, (float)l, m, n, 256, 256);
	}

	public static void blit(int i, int j, int k, float f, float g, int l, int m, int n, int o) {
		innerBlit(i, i + l, j, j + m, k, l, m, f, g, o, n);
	}

	public static void blit(int i, int j, int k, int l, float f, float g, int m, int n, int o, int p) {
		innerBlit(i, i + k, j, j + l, 0, m, n, f, g, o, p);
	}

	public static void blit(int i, int j, float f, float g, int k, int l, int m, int n) {
		blit(i, j, k, l, f, g, k, l, m, n);
	}

	private static void innerBlit(int i, int j, int k, int l, int m, int n, int o, float f, float g, int p, int q) {
		innerBlit(i, j, k, l, m, (f + 0.0F) / (float)p, (f + (float)n) / (float)p, (g + 0.0F) / (float)q, (g + (float)o) / (float)q);
	}

	protected static void innerBlit(int i, int j, int k, int l, int m, float f, float g, float h, float n) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex((double)i, (double)l, (double)m).uv((double)f, (double)n).endVertex();
		bufferBuilder.vertex((double)j, (double)l, (double)m).uv((double)g, (double)n).endVertex();
		bufferBuilder.vertex((double)j, (double)k, (double)m).uv((double)g, (double)h).endVertex();
		bufferBuilder.vertex((double)i, (double)k, (double)m).uv((double)f, (double)h).endVertex();
		tesselator.end();
	}
}
