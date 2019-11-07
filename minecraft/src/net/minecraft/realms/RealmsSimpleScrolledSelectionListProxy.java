package net.minecraft.realms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ScrolledSelectionList;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class RealmsSimpleScrolledSelectionListProxy extends ScrolledSelectionList {
	private final RealmsSimpleScrolledSelectionList realmsSimpleScrolledSelectionList;

	public RealmsSimpleScrolledSelectionListProxy(RealmsSimpleScrolledSelectionList realmsSimpleScrolledSelectionList, int i, int j, int k, int l, int m) {
		super(Minecraft.getInstance(), i, j, k, l, m);
		this.realmsSimpleScrolledSelectionList = realmsSimpleScrolledSelectionList;
	}

	@Override
	public int getItemCount() {
		return this.realmsSimpleScrolledSelectionList.getItemCount();
	}

	@Override
	public boolean selectItem(int i, int j, double d, double e) {
		return this.realmsSimpleScrolledSelectionList.selectItem(i, j, d, e);
	}

	@Override
	public boolean isSelectedItem(int i) {
		return this.realmsSimpleScrolledSelectionList.isSelectedItem(i);
	}

	@Override
	public void renderBackground() {
		this.realmsSimpleScrolledSelectionList.renderBackground();
	}

	@Override
	public void renderItem(int i, int j, int k, int l, int m, int n, float f) {
		this.realmsSimpleScrolledSelectionList.renderItem(i, j, k, l, m, n);
	}

	public int getWidth() {
		return this.width;
	}

	@Override
	public int getMaxPosition() {
		return this.realmsSimpleScrolledSelectionList.getMaxPosition();
	}

	@Override
	public int getScrollbarPosition() {
		return this.realmsSimpleScrolledSelectionList.getScrollbarPosition();
	}

	@Override
	public void render(int i, int j, float f) {
		if (this.visible) {
			this.renderBackground();
			int k = this.getScrollbarPosition();
			int l = k + 6;
			this.capYPosition();
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			int m = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
			int n = this.y0 + 4 - (int)this.yo;
			if (this.renderHeader) {
				this.renderHeader(m, n, tesselator);
			}

			this.renderList(m, n, i, j, f);
			RenderSystem.disableDepthTest();
			this.renderHoleBackground(0, this.y0, 255, 255);
			this.renderHoleBackground(this.y1, this.height, 255, 255);
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
			);
			RenderSystem.disableAlphaTest();
			RenderSystem.shadeModel(7425);
			RenderSystem.disableTexture();
			int o = this.getMaxScroll();
			if (o > 0) {
				int p = (this.y1 - this.y0) * (this.y1 - this.y0) / this.getMaxPosition();
				p = Mth.clamp(p, 32, this.y1 - this.y0 - 8);
				int q = (int)this.yo * (this.y1 - this.y0 - p) / o + this.y0;
				if (q < this.y0) {
					q = this.y0;
				}

				bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
				bufferBuilder.vertex((double)k, (double)this.y1, 0.0).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
				bufferBuilder.vertex((double)l, (double)this.y1, 0.0).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
				bufferBuilder.vertex((double)l, (double)this.y0, 0.0).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
				bufferBuilder.vertex((double)k, (double)this.y0, 0.0).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
				tesselator.end();
				bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
				bufferBuilder.vertex((double)k, (double)(q + p), 0.0).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
				bufferBuilder.vertex((double)l, (double)(q + p), 0.0).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
				bufferBuilder.vertex((double)l, (double)q, 0.0).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
				bufferBuilder.vertex((double)k, (double)q, 0.0).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
				tesselator.end();
				bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
				bufferBuilder.vertex((double)k, (double)(q + p - 1), 0.0).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
				bufferBuilder.vertex((double)(l - 1), (double)(q + p - 1), 0.0).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
				bufferBuilder.vertex((double)(l - 1), (double)q, 0.0).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
				bufferBuilder.vertex((double)k, (double)q, 0.0).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
				tesselator.end();
			}

			this.renderDecorations(i, j);
			RenderSystem.enableTexture();
			RenderSystem.shadeModel(7424);
			RenderSystem.enableAlphaTest();
			RenderSystem.disableBlend();
		}
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		return this.realmsSimpleScrolledSelectionList.mouseScrolled(d, e, f) ? true : super.mouseScrolled(d, e, f);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		return this.realmsSimpleScrolledSelectionList.mouseClicked(d, e, i) ? true : super.mouseClicked(d, e, i);
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		return this.realmsSimpleScrolledSelectionList.mouseReleased(d, e, i);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		return this.realmsSimpleScrolledSelectionList.mouseDragged(d, e, i, f, g);
	}
}
