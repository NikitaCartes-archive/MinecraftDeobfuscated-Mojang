package net.minecraft.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector2ic;

@Environment(EnvType.CLIENT)
public class GuiGraphics {
	public static final float MAX_GUI_Z = 10000.0F;
	public static final float MIN_GUI_Z = -10000.0F;
	private static final int EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE = 2;
	private final Minecraft minecraft;
	private final PoseStack pose;
	private final MultiBufferSource.BufferSource bufferSource;
	private final GuiGraphics.ScissorStack scissorStack = new GuiGraphics.ScissorStack();
	private final GuiSpriteManager sprites;

	private GuiGraphics(Minecraft minecraft, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
		this.minecraft = minecraft;
		this.pose = poseStack;
		this.bufferSource = bufferSource;
		this.sprites = minecraft.getGuiSprites();
	}

	public GuiGraphics(Minecraft minecraft, MultiBufferSource.BufferSource bufferSource) {
		this(minecraft, new PoseStack(), bufferSource);
	}

	public int guiWidth() {
		return this.minecraft.getWindow().getGuiScaledWidth();
	}

	public int guiHeight() {
		return this.minecraft.getWindow().getGuiScaledHeight();
	}

	public PoseStack pose() {
		return this.pose;
	}

	public void flush() {
		this.bufferSource.endBatch();
	}

	public void hLine(int i, int j, int k, int l) {
		this.hLine(RenderType.gui(), i, j, k, l);
	}

	public void hLine(RenderType renderType, int i, int j, int k, int l) {
		if (j < i) {
			int m = i;
			i = j;
			j = m;
		}

		this.fill(renderType, i, k, j + 1, k + 1, l);
	}

	public void vLine(int i, int j, int k, int l) {
		this.vLine(RenderType.gui(), i, j, k, l);
	}

	public void vLine(RenderType renderType, int i, int j, int k, int l) {
		if (k < j) {
			int m = j;
			j = k;
			k = m;
		}

		this.fill(renderType, i, j + 1, i + 1, k, l);
	}

	public void enableScissor(int i, int j, int k, int l) {
		this.applyScissor(this.scissorStack.push(new ScreenRectangle(i, j, k - i, l - j)));
	}

	public void disableScissor() {
		this.applyScissor(this.scissorStack.pop());
	}

	public boolean containsPointInScissor(int i, int j) {
		return this.scissorStack.containsPoint(i, j);
	}

	private void applyScissor(@Nullable ScreenRectangle screenRectangle) {
		this.flush();
		if (screenRectangle != null) {
			Window window = Minecraft.getInstance().getWindow();
			int i = window.getHeight();
			double d = window.getGuiScale();
			double e = (double)screenRectangle.left() * d;
			double f = (double)i - (double)screenRectangle.bottom() * d;
			double g = (double)screenRectangle.width() * d;
			double h = (double)screenRectangle.height() * d;
			RenderSystem.enableScissor((int)e, (int)f, Math.max(0, (int)g), Math.max(0, (int)h));
		} else {
			RenderSystem.disableScissor();
		}
	}

	public void fill(int i, int j, int k, int l, int m) {
		this.fill(i, j, k, l, 0, m);
	}

	public void fill(int i, int j, int k, int l, int m, int n) {
		this.fill(RenderType.gui(), i, j, k, l, m, n);
	}

	public void fill(RenderType renderType, int i, int j, int k, int l, int m) {
		this.fill(renderType, i, j, k, l, 0, m);
	}

	public void fill(RenderType renderType, int i, int j, int k, int l, int m, int n) {
		Matrix4f matrix4f = this.pose.last().pose();
		if (i < k) {
			int o = i;
			i = k;
			k = o;
		}

		if (j < l) {
			int o = j;
			j = l;
			l = o;
		}

		VertexConsumer vertexConsumer = this.bufferSource.getBuffer(renderType);
		vertexConsumer.addVertex(matrix4f, (float)i, (float)j, (float)m).setColor(n);
		vertexConsumer.addVertex(matrix4f, (float)i, (float)l, (float)m).setColor(n);
		vertexConsumer.addVertex(matrix4f, (float)k, (float)l, (float)m).setColor(n);
		vertexConsumer.addVertex(matrix4f, (float)k, (float)j, (float)m).setColor(n);
	}

	public void fillGradient(int i, int j, int k, int l, int m, int n) {
		this.fillGradient(i, j, k, l, 0, m, n);
	}

	public void fillGradient(int i, int j, int k, int l, int m, int n, int o) {
		this.fillGradient(RenderType.gui(), i, j, k, l, n, o, m);
	}

	public void fillGradient(RenderType renderType, int i, int j, int k, int l, int m, int n, int o) {
		VertexConsumer vertexConsumer = this.bufferSource.getBuffer(renderType);
		this.fillGradient(vertexConsumer, i, j, k, l, o, m, n);
	}

	private void fillGradient(VertexConsumer vertexConsumer, int i, int j, int k, int l, int m, int n, int o) {
		Matrix4f matrix4f = this.pose.last().pose();
		vertexConsumer.addVertex(matrix4f, (float)i, (float)j, (float)m).setColor(n);
		vertexConsumer.addVertex(matrix4f, (float)i, (float)l, (float)m).setColor(o);
		vertexConsumer.addVertex(matrix4f, (float)k, (float)l, (float)m).setColor(o);
		vertexConsumer.addVertex(matrix4f, (float)k, (float)j, (float)m).setColor(n);
	}

	public void fillRenderType(RenderType renderType, int i, int j, int k, int l, int m) {
		Matrix4f matrix4f = this.pose.last().pose();
		VertexConsumer vertexConsumer = this.bufferSource.getBuffer(renderType);
		vertexConsumer.addVertex(matrix4f, (float)i, (float)j, (float)m);
		vertexConsumer.addVertex(matrix4f, (float)i, (float)l, (float)m);
		vertexConsumer.addVertex(matrix4f, (float)k, (float)l, (float)m);
		vertexConsumer.addVertex(matrix4f, (float)k, (float)j, (float)m);
	}

	public void drawCenteredString(Font font, String string, int i, int j, int k) {
		this.drawString(font, string, i - font.width(string) / 2, j, k);
	}

	public void drawCenteredString(Font font, Component component, int i, int j, int k) {
		FormattedCharSequence formattedCharSequence = component.getVisualOrderText();
		this.drawString(font, formattedCharSequence, i - font.width(formattedCharSequence) / 2, j, k);
	}

	public void drawCenteredString(Font font, FormattedCharSequence formattedCharSequence, int i, int j, int k) {
		this.drawString(font, formattedCharSequence, i - font.width(formattedCharSequence) / 2, j, k);
	}

	public int drawString(Font font, @Nullable String string, int i, int j, int k) {
		return this.drawString(font, string, i, j, k, true);
	}

	public int drawString(Font font, @Nullable String string, int i, int j, int k, boolean bl) {
		return string == null
			? 0
			: font.drawInBatch(
				string, (float)i, (float)j, k, bl, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880, font.isBidirectional()
			);
	}

	public int drawString(Font font, FormattedCharSequence formattedCharSequence, int i, int j, int k) {
		return this.drawString(font, formattedCharSequence, i, j, k, true);
	}

	public int drawString(Font font, FormattedCharSequence formattedCharSequence, int i, int j, int k, boolean bl) {
		return font.drawInBatch(formattedCharSequence, (float)i, (float)j, k, bl, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
	}

	public int drawString(Font font, Component component, int i, int j, int k) {
		return this.drawString(font, component, i, j, k, true);
	}

	public int drawString(Font font, Component component, int i, int j, int k, boolean bl) {
		return this.drawString(font, component.getVisualOrderText(), i, j, k, bl);
	}

	public void drawWordWrap(Font font, FormattedText formattedText, int i, int j, int k, int l) {
		for (FormattedCharSequence formattedCharSequence : font.split(formattedText, k)) {
			this.drawString(font, formattedCharSequence, i, j, l, false);
			j += 9;
		}
	}

	public int drawStringWithBackdrop(Font font, Component component, int i, int j, int k, int l) {
		int m = this.minecraft.options.getBackgroundColor(0.0F);
		if (m != 0) {
			int n = 2;
			this.fill(i - 2, j - 2, i + k + 2, j + 9 + 2, ARGB.multiply(m, l));
		}

		return this.drawString(font, component, i, j, l, true);
	}

	public void renderOutline(int i, int j, int k, int l, int m) {
		this.fill(i, j, i + k, j + 1, m);
		this.fill(i, j + l - 1, i + k, j + l, m);
		this.fill(i, j + 1, i + 1, j + l - 1, m);
		this.fill(i + k - 1, j + 1, i + k, j + l - 1, m);
	}

	public void blitSprite(Function<ResourceLocation, RenderType> function, ResourceLocation resourceLocation, int i, int j, int k, int l) {
		this.blitSprite(function, resourceLocation, i, j, k, l, -1);
	}

	public void blitSprite(Function<ResourceLocation, RenderType> function, ResourceLocation resourceLocation, int i, int j, int k, int l, int m) {
		TextureAtlasSprite textureAtlasSprite = this.sprites.getSprite(resourceLocation);
		GuiSpriteScaling guiSpriteScaling = this.sprites.getSpriteScaling(textureAtlasSprite);
		if (guiSpriteScaling instanceof GuiSpriteScaling.Stretch) {
			this.blitSprite(function, textureAtlasSprite, i, j, k, l, m);
		} else if (guiSpriteScaling instanceof GuiSpriteScaling.Tile tile) {
			this.blitTiledSprite(function, textureAtlasSprite, i, j, k, l, 0, 0, tile.width(), tile.height(), tile.width(), tile.height(), m);
		} else if (guiSpriteScaling instanceof GuiSpriteScaling.NineSlice nineSlice) {
			this.blitNineSlicedSprite(function, textureAtlasSprite, nineSlice, i, j, k, l, m);
		}
	}

	public void blitSprite(
		Function<ResourceLocation, RenderType> function, ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n, int o, int p
	) {
		TextureAtlasSprite textureAtlasSprite = this.sprites.getSprite(resourceLocation);
		GuiSpriteScaling guiSpriteScaling = this.sprites.getSpriteScaling(textureAtlasSprite);
		if (guiSpriteScaling instanceof GuiSpriteScaling.Stretch) {
			this.blitSprite(function, textureAtlasSprite, i, j, k, l, m, n, o, p, -1);
		} else {
			this.blitSprite(function, textureAtlasSprite, m, n, o, p);
		}
	}

	public void blitSprite(Function<ResourceLocation, RenderType> function, TextureAtlasSprite textureAtlasSprite, int i, int j, int k, int l) {
		this.blitSprite(function, textureAtlasSprite, i, j, k, l, -1);
	}

	public void blitSprite(Function<ResourceLocation, RenderType> function, TextureAtlasSprite textureAtlasSprite, int i, int j, int k, int l, int m) {
		if (k != 0 && l != 0) {
			this.innerBlit(
				function,
				textureAtlasSprite.atlasLocation(),
				i,
				i + k,
				j,
				j + l,
				textureAtlasSprite.getU0(),
				textureAtlasSprite.getU1(),
				textureAtlasSprite.getV0(),
				textureAtlasSprite.getV1(),
				m
			);
		}
	}

	private void blitSprite(
		Function<ResourceLocation, RenderType> function, TextureAtlasSprite textureAtlasSprite, int i, int j, int k, int l, int m, int n, int o, int p, int q
	) {
		if (o != 0 && p != 0) {
			this.innerBlit(
				function,
				textureAtlasSprite.atlasLocation(),
				m,
				m + o,
				n,
				n + p,
				textureAtlasSprite.getU((float)k / (float)i),
				textureAtlasSprite.getU((float)(k + o) / (float)i),
				textureAtlasSprite.getV((float)l / (float)j),
				textureAtlasSprite.getV((float)(l + p) / (float)j),
				q
			);
		}
	}

	private void blitNineSlicedSprite(
		Function<ResourceLocation, RenderType> function,
		TextureAtlasSprite textureAtlasSprite,
		GuiSpriteScaling.NineSlice nineSlice,
		int i,
		int j,
		int k,
		int l,
		int m
	) {
		GuiSpriteScaling.NineSlice.Border border = nineSlice.border();
		int n = Math.min(border.left(), k / 2);
		int o = Math.min(border.right(), k / 2);
		int p = Math.min(border.top(), l / 2);
		int q = Math.min(border.bottom(), l / 2);
		if (k == nineSlice.width() && l == nineSlice.height()) {
			this.blitSprite(function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, 0, i, j, k, l, m);
		} else if (l == nineSlice.height()) {
			this.blitSprite(function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, 0, i, j, n, l, m);
			this.blitNineSliceInnerSegment(
				function,
				nineSlice,
				textureAtlasSprite,
				i + n,
				j,
				k - o - n,
				l,
				n,
				0,
				nineSlice.width() - o - n,
				nineSlice.height(),
				nineSlice.width(),
				nineSlice.height(),
				m
			);
			this.blitSprite(function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - o, 0, i + k - o, j, o, l, m);
		} else if (k == nineSlice.width()) {
			this.blitSprite(function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, 0, i, j, k, p, m);
			this.blitNineSliceInnerSegment(
				function,
				nineSlice,
				textureAtlasSprite,
				i,
				j + p,
				k,
				l - q - p,
				0,
				p,
				nineSlice.width(),
				nineSlice.height() - q - p,
				nineSlice.width(),
				nineSlice.height(),
				m
			);
			this.blitSprite(function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - q, i, j + l - q, k, q, m);
		} else {
			this.blitSprite(function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, 0, i, j, n, p, m);
			this.blitNineSliceInnerSegment(
				function, nineSlice, textureAtlasSprite, i + n, j, k - o - n, p, n, 0, nineSlice.width() - o - n, p, nineSlice.width(), nineSlice.height(), m
			);
			this.blitSprite(function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - o, 0, i + k - o, j, o, p, m);
			this.blitSprite(function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - q, i, j + l - q, n, q, m);
			this.blitNineSliceInnerSegment(
				function,
				nineSlice,
				textureAtlasSprite,
				i + n,
				j + l - q,
				k - o - n,
				q,
				n,
				nineSlice.height() - q,
				nineSlice.width() - o - n,
				q,
				nineSlice.width(),
				nineSlice.height(),
				m
			);
			this.blitSprite(
				function, textureAtlasSprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - o, nineSlice.height() - q, i + k - o, j + l - q, o, q, m
			);
			this.blitNineSliceInnerSegment(
				function, nineSlice, textureAtlasSprite, i, j + p, n, l - q - p, 0, p, n, nineSlice.height() - q - p, nineSlice.width(), nineSlice.height(), m
			);
			this.blitNineSliceInnerSegment(
				function,
				nineSlice,
				textureAtlasSprite,
				i + n,
				j + p,
				k - o - n,
				l - q - p,
				n,
				p,
				nineSlice.width() - o - n,
				nineSlice.height() - q - p,
				nineSlice.width(),
				nineSlice.height(),
				m
			);
			this.blitNineSliceInnerSegment(
				function,
				nineSlice,
				textureAtlasSprite,
				i + k - o,
				j + p,
				n,
				l - q - p,
				nineSlice.width() - o,
				p,
				o,
				nineSlice.height() - q - p,
				nineSlice.width(),
				nineSlice.height(),
				m
			);
		}
	}

	private void blitNineSliceInnerSegment(
		Function<ResourceLocation, RenderType> function,
		GuiSpriteScaling.NineSlice nineSlice,
		TextureAtlasSprite textureAtlasSprite,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		int o,
		int p,
		int q,
		int r,
		int s
	) {
		if (k > 0 && l > 0) {
			if (nineSlice.stretchInner()) {
				this.innerBlit(
					function,
					textureAtlasSprite.atlasLocation(),
					i,
					i + k,
					j,
					j + l,
					textureAtlasSprite.getU((float)m / (float)q),
					textureAtlasSprite.getU((float)(m + o) / (float)q),
					textureAtlasSprite.getV((float)n / (float)r),
					textureAtlasSprite.getV((float)(n + p) / (float)r),
					s
				);
			} else {
				this.blitTiledSprite(function, textureAtlasSprite, i, j, k, l, m, n, o, p, q, r, s);
			}
		}
	}

	private void blitTiledSprite(
		Function<ResourceLocation, RenderType> function,
		TextureAtlasSprite textureAtlasSprite,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		int o,
		int p,
		int q,
		int r,
		int s
	) {
		if (k > 0 && l > 0) {
			if (o > 0 && p > 0) {
				for (int t = 0; t < k; t += o) {
					int u = Math.min(o, k - t);

					for (int v = 0; v < l; v += p) {
						int w = Math.min(p, l - v);
						this.blitSprite(function, textureAtlasSprite, q, r, m, n, i + t, j + v, u, w, s);
					}
				}
			} else {
				throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + o + "x" + p);
			}
		}
	}

	public void blit(
		Function<ResourceLocation, RenderType> function, ResourceLocation resourceLocation, int i, int j, float f, float g, int k, int l, int m, int n, int o
	) {
		this.blit(function, resourceLocation, i, j, f, g, k, l, k, l, m, n, o);
	}

	public void blit(
		Function<ResourceLocation, RenderType> function, ResourceLocation resourceLocation, int i, int j, float f, float g, int k, int l, int m, int n
	) {
		this.blit(function, resourceLocation, i, j, f, g, k, l, k, l, m, n);
	}

	public void blit(
		Function<ResourceLocation, RenderType> function, ResourceLocation resourceLocation, int i, int j, float f, float g, int k, int l, int m, int n, int o, int p
	) {
		this.blit(function, resourceLocation, i, j, f, g, k, l, m, n, o, p, -1);
	}

	public void blit(
		Function<ResourceLocation, RenderType> function,
		ResourceLocation resourceLocation,
		int i,
		int j,
		float f,
		float g,
		int k,
		int l,
		int m,
		int n,
		int o,
		int p,
		int q
	) {
		this.innerBlit(
			function, resourceLocation, i, i + k, j, j + l, (f + 0.0F) / (float)o, (f + (float)m) / (float)o, (g + 0.0F) / (float)p, (g + (float)n) / (float)p, q
		);
	}

	private void innerBlit(
		Function<ResourceLocation, RenderType> function, ResourceLocation resourceLocation, int i, int j, int k, int l, float f, float g, float h, float m, int n
	) {
		RenderType renderType = (RenderType)function.apply(resourceLocation);
		Matrix4f matrix4f = this.pose.last().pose();
		VertexConsumer vertexConsumer = this.bufferSource.getBuffer(renderType);
		vertexConsumer.addVertex(matrix4f, (float)i, (float)k, 0.0F).setUv(f, h).setColor(n);
		vertexConsumer.addVertex(matrix4f, (float)i, (float)l, 0.0F).setUv(f, m).setColor(n);
		vertexConsumer.addVertex(matrix4f, (float)j, (float)l, 0.0F).setUv(g, m).setColor(n);
		vertexConsumer.addVertex(matrix4f, (float)j, (float)k, 0.0F).setUv(g, h).setColor(n);
	}

	public void renderItem(ItemStack itemStack, int i, int j) {
		this.renderItem(this.minecraft.player, this.minecraft.level, itemStack, i, j, 0);
	}

	public void renderItem(ItemStack itemStack, int i, int j, int k) {
		this.renderItem(this.minecraft.player, this.minecraft.level, itemStack, i, j, k);
	}

	public void renderItem(ItemStack itemStack, int i, int j, int k, int l) {
		this.renderItem(this.minecraft.player, this.minecraft.level, itemStack, i, j, k, l);
	}

	public void renderFakeItem(ItemStack itemStack, int i, int j) {
		this.renderFakeItem(itemStack, i, j, 0);
	}

	public void renderFakeItem(ItemStack itemStack, int i, int j, int k) {
		this.renderItem(null, this.minecraft.level, itemStack, i, j, k);
	}

	public void renderItem(LivingEntity livingEntity, ItemStack itemStack, int i, int j, int k) {
		this.renderItem(livingEntity, livingEntity.level(), itemStack, i, j, k);
	}

	private void renderItem(@Nullable LivingEntity livingEntity, @Nullable Level level, ItemStack itemStack, int i, int j, int k) {
		this.renderItem(livingEntity, level, itemStack, i, j, k, 0);
	}

	private void renderItem(@Nullable LivingEntity livingEntity, @Nullable Level level, ItemStack itemStack, int i, int j, int k, int l) {
		if (!itemStack.isEmpty()) {
			BakedModel bakedModel = this.minecraft.getItemRenderer().getModel(itemStack, level, livingEntity, k);
			this.pose.pushPose();
			this.pose.translate((float)(i + 8), (float)(j + 8), (float)(150 + (bakedModel.isGui3d() ? l : 0)));

			try {
				this.pose.scale(16.0F, -16.0F, 16.0F);
				boolean bl = !bakedModel.usesBlockLight();
				if (bl) {
					this.flush();
					Lighting.setupForFlatItems();
				}

				if (itemStack.is(Items.BUNDLE)) {
					this.minecraft
						.getItemRenderer()
						.renderBundleItem(
							itemStack, ItemDisplayContext.GUI, false, this.pose, this.bufferSource, 15728880, OverlayTexture.NO_OVERLAY, bakedModel, level, livingEntity, k
						);
				} else {
					this.minecraft
						.getItemRenderer()
						.render(itemStack, ItemDisplayContext.GUI, false, this.pose, this.bufferSource, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
				}

				this.flush();
				if (bl) {
					Lighting.setupFor3DItems();
				}
			} catch (Throwable var12) {
				CrashReport crashReport = CrashReport.forThrowable(var12, "Rendering item");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
				crashReportCategory.setDetail("Item Type", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.getItem())));
				crashReportCategory.setDetail("Item Components", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.getComponents())));
				crashReportCategory.setDetail("Item Foil", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.hasFoil())));
				throw new ReportedException(crashReport);
			}

			this.pose.popPose();
		}
	}

	public void renderItemDecorations(Font font, ItemStack itemStack, int i, int j) {
		this.renderItemDecorations(font, itemStack, i, j, null);
	}

	public void renderItemDecorations(Font font, ItemStack itemStack, int i, int j, @Nullable String string) {
		if (!itemStack.isEmpty()) {
			this.pose.pushPose();
			if (itemStack.getCount() != 1 || string != null) {
				String string2 = string == null ? String.valueOf(itemStack.getCount()) : string;
				this.pose.translate(0.0F, 0.0F, 200.0F);
				this.drawString(font, string2, i + 19 - 2 - font.width(string2), j + 6 + 3, 16777215, true);
			}

			if (itemStack.isBarVisible()) {
				int k = itemStack.getBarWidth();
				int l = itemStack.getBarColor();
				int m = i + 2;
				int n = j + 13;
				this.fill(RenderType.guiOverlay(), m, n, m + 13, n + 2, -16777216);
				this.fill(RenderType.guiOverlay(), m, n, m + k, n + 1, ARGB.opaque(l));
			}

			LocalPlayer localPlayer = this.minecraft.player;
			float f = localPlayer == null
				? 0.0F
				: localPlayer.getCooldowns().getCooldownPercent(itemStack, this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true));
			if (f > 0.0F) {
				int m = j + Mth.floor(16.0F * (1.0F - f));
				int n = m + Mth.ceil(16.0F * f);
				this.fill(RenderType.guiOverlay(), i, m, i + 16, n, Integer.MAX_VALUE);
			}

			this.pose.popPose();
		}
	}

	public void renderTooltip(Font font, ItemStack itemStack, int i, int j) {
		this.renderTooltip(font, Screen.getTooltipFromItem(this.minecraft, itemStack), itemStack.getTooltipImage(), i, j, itemStack.get(DataComponents.TOOLTIP_STYLE));
	}

	public void renderTooltip(Font font, List<Component> list, Optional<TooltipComponent> optional, int i, int j) {
		this.renderTooltip(font, list, optional, i, j, null);
	}

	public void renderTooltip(Font font, List<Component> list, Optional<TooltipComponent> optional, int i, int j, @Nullable ResourceLocation resourceLocation) {
		List<ClientTooltipComponent> list2 = (List<ClientTooltipComponent>)list.stream()
			.map(Component::getVisualOrderText)
			.map(ClientTooltipComponent::create)
			.collect(Util.toMutableList());
		optional.ifPresent(tooltipComponent -> list2.add(list2.isEmpty() ? 0 : 1, ClientTooltipComponent.create(tooltipComponent)));
		this.renderTooltipInternal(font, list2, i, j, DefaultTooltipPositioner.INSTANCE, resourceLocation);
	}

	public void renderTooltip(Font font, Component component, int i, int j) {
		this.renderTooltip(font, component, i, j, null);
	}

	public void renderTooltip(Font font, Component component, int i, int j, @Nullable ResourceLocation resourceLocation) {
		this.renderTooltip(font, List.of(component.getVisualOrderText()), i, j, resourceLocation);
	}

	public void renderComponentTooltip(Font font, List<Component> list, int i, int j) {
		this.renderComponentTooltip(font, list, i, j, null);
	}

	public void renderComponentTooltip(Font font, List<Component> list, int i, int j, @Nullable ResourceLocation resourceLocation) {
		this.renderTooltipInternal(
			font,
			list.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(),
			i,
			j,
			DefaultTooltipPositioner.INSTANCE,
			resourceLocation
		);
	}

	public void renderTooltip(Font font, List<? extends FormattedCharSequence> list, int i, int j) {
		this.renderTooltip(font, list, i, j, null);
	}

	public void renderTooltip(Font font, List<? extends FormattedCharSequence> list, int i, int j, @Nullable ResourceLocation resourceLocation) {
		this.renderTooltipInternal(
			font,
			(List<ClientTooltipComponent>)list.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()),
			i,
			j,
			DefaultTooltipPositioner.INSTANCE,
			resourceLocation
		);
	}

	public void renderTooltip(Font font, List<FormattedCharSequence> list, ClientTooltipPositioner clientTooltipPositioner, int i, int j) {
		this.renderTooltipInternal(
			font, (List<ClientTooltipComponent>)list.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), i, j, clientTooltipPositioner, null
		);
	}

	private void renderTooltipInternal(
		Font font, List<ClientTooltipComponent> list, int i, int j, ClientTooltipPositioner clientTooltipPositioner, @Nullable ResourceLocation resourceLocation
	) {
		if (!list.isEmpty()) {
			int k = 0;
			int l = list.size() == 1 ? -2 : 0;

			for (ClientTooltipComponent clientTooltipComponent : list) {
				int m = clientTooltipComponent.getWidth(font);
				if (m > k) {
					k = m;
				}

				l += clientTooltipComponent.getHeight(font);
			}

			int n = k;
			int o = l;
			Vector2ic vector2ic = clientTooltipPositioner.positionTooltip(this.guiWidth(), this.guiHeight(), i, j, k, l);
			int p = vector2ic.x();
			int q = vector2ic.y();
			this.pose.pushPose();
			int r = 400;
			TooltipRenderUtil.renderTooltipBackground(this, p, q, k, l, 400, resourceLocation);
			this.pose.translate(0.0F, 0.0F, 400.0F);
			int s = q;

			for (int t = 0; t < list.size(); t++) {
				ClientTooltipComponent clientTooltipComponent2 = (ClientTooltipComponent)list.get(t);
				clientTooltipComponent2.renderText(font, p, s, this.pose.last().pose(), this.bufferSource);
				s += clientTooltipComponent2.getHeight(font) + (t == 0 ? 2 : 0);
			}

			s = q;

			for (int t = 0; t < list.size(); t++) {
				ClientTooltipComponent clientTooltipComponent2 = (ClientTooltipComponent)list.get(t);
				clientTooltipComponent2.renderImage(font, p, s, n, o, this);
				s += clientTooltipComponent2.getHeight(font) + (t == 0 ? 2 : 0);
			}

			this.pose.popPose();
		}
	}

	public void renderComponentHoverEffect(Font font, @Nullable Style style, int i, int j) {
		if (style != null && style.getHoverEvent() != null) {
			HoverEvent hoverEvent = style.getHoverEvent();
			HoverEvent.ItemStackInfo itemStackInfo = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
			if (itemStackInfo != null) {
				this.renderTooltip(font, itemStackInfo.getItemStack(), i, j);
			} else {
				HoverEvent.EntityTooltipInfo entityTooltipInfo = hoverEvent.getValue(HoverEvent.Action.SHOW_ENTITY);
				if (entityTooltipInfo != null) {
					if (this.minecraft.options.advancedItemTooltips) {
						this.renderComponentTooltip(font, entityTooltipInfo.getTooltipLines(), i, j);
					}
				} else {
					Component component = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
					if (component != null) {
						this.renderTooltip(font, font.split(component, Math.max(this.guiWidth() / 2, 200)), i, j);
					}
				}
			}
		}
	}

	public void drawSpecial(Consumer<MultiBufferSource> consumer) {
		consumer.accept(this.bufferSource);
		this.bufferSource.endBatch();
	}

	@Environment(EnvType.CLIENT)
	static class ScissorStack {
		private final Deque<ScreenRectangle> stack = new ArrayDeque();

		public ScreenRectangle push(ScreenRectangle screenRectangle) {
			ScreenRectangle screenRectangle2 = (ScreenRectangle)this.stack.peekLast();
			if (screenRectangle2 != null) {
				ScreenRectangle screenRectangle3 = (ScreenRectangle)Objects.requireNonNullElse(screenRectangle.intersection(screenRectangle2), ScreenRectangle.empty());
				this.stack.addLast(screenRectangle3);
				return screenRectangle3;
			} else {
				this.stack.addLast(screenRectangle);
				return screenRectangle;
			}
		}

		@Nullable
		public ScreenRectangle pop() {
			if (this.stack.isEmpty()) {
				throw new IllegalStateException("Scissor stack underflow");
			} else {
				this.stack.removeLast();
				return (ScreenRectangle)this.stack.peekLast();
			}
		}

		public boolean containsPoint(int i, int j) {
			return this.stack.isEmpty() ? true : ((ScreenRectangle)this.stack.peek()).containsPoint(i, j);
		}
	}
}
