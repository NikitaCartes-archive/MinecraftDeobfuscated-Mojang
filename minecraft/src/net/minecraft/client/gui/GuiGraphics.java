package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
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
	private boolean managed;

	private GuiGraphics(Minecraft minecraft, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
		this.minecraft = minecraft;
		this.pose = poseStack;
		this.bufferSource = bufferSource;
	}

	public GuiGraphics(Minecraft minecraft, MultiBufferSource.BufferSource bufferSource) {
		this(minecraft, new PoseStack(), bufferSource);
	}

	@Deprecated
	public void drawManaged(Runnable runnable) {
		this.flush();
		this.managed = true;
		runnable.run();
		this.managed = false;
		this.flush();
	}

	@Deprecated
	private void flushIfUnmanaged() {
		if (!this.managed) {
			this.flush();
		}
	}

	@Deprecated
	private void flushIfManaged() {
		if (this.managed) {
			this.flush();
		}
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

	public MultiBufferSource.BufferSource bufferSource() {
		return this.bufferSource;
	}

	public void flush() {
		RenderSystem.disableDepthTest();
		this.bufferSource.endBatch();
		RenderSystem.enableDepthTest();
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

	private void applyScissor(@Nullable ScreenRectangle screenRectangle) {
		this.flushIfManaged();
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

	public void setColor(float f, float g, float h, float i) {
		this.flushIfManaged();
		RenderSystem.setShaderColor(f, g, h, i);
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

		float f = (float)FastColor.ARGB32.alpha(n) / 255.0F;
		float g = (float)FastColor.ARGB32.red(n) / 255.0F;
		float h = (float)FastColor.ARGB32.green(n) / 255.0F;
		float p = (float)FastColor.ARGB32.blue(n) / 255.0F;
		VertexConsumer vertexConsumer = this.bufferSource.getBuffer(renderType);
		vertexConsumer.vertex(matrix4f, (float)i, (float)j, (float)m).color(g, h, p, f).endVertex();
		vertexConsumer.vertex(matrix4f, (float)i, (float)l, (float)m).color(g, h, p, f).endVertex();
		vertexConsumer.vertex(matrix4f, (float)k, (float)l, (float)m).color(g, h, p, f).endVertex();
		vertexConsumer.vertex(matrix4f, (float)k, (float)j, (float)m).color(g, h, p, f).endVertex();
		this.flushIfUnmanaged();
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
		this.flushIfUnmanaged();
	}

	private void fillGradient(VertexConsumer vertexConsumer, int i, int j, int k, int l, int m, int n, int o) {
		float f = (float)FastColor.ARGB32.alpha(n) / 255.0F;
		float g = (float)FastColor.ARGB32.red(n) / 255.0F;
		float h = (float)FastColor.ARGB32.green(n) / 255.0F;
		float p = (float)FastColor.ARGB32.blue(n) / 255.0F;
		float q = (float)FastColor.ARGB32.alpha(o) / 255.0F;
		float r = (float)FastColor.ARGB32.red(o) / 255.0F;
		float s = (float)FastColor.ARGB32.green(o) / 255.0F;
		float t = (float)FastColor.ARGB32.blue(o) / 255.0F;
		Matrix4f matrix4f = this.pose.last().pose();
		vertexConsumer.vertex(matrix4f, (float)i, (float)j, (float)m).color(g, h, p, f).endVertex();
		vertexConsumer.vertex(matrix4f, (float)i, (float)l, (float)m).color(r, s, t, q).endVertex();
		vertexConsumer.vertex(matrix4f, (float)k, (float)l, (float)m).color(r, s, t, q).endVertex();
		vertexConsumer.vertex(matrix4f, (float)k, (float)j, (float)m).color(g, h, p, f).endVertex();
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
		if (string == null) {
			return 0;
		} else {
			int l = font.drawInBatch(
				string, (float)i, (float)j, k, bl, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880, font.isBidirectional()
			);
			this.flushIfUnmanaged();
			return l;
		}
	}

	public int drawString(Font font, FormattedCharSequence formattedCharSequence, int i, int j, int k) {
		return this.drawString(font, formattedCharSequence, i, j, k, true);
	}

	public int drawString(Font font, FormattedCharSequence formattedCharSequence, int i, int j, int k, boolean bl) {
		int l = font.drawInBatch(formattedCharSequence, (float)i, (float)j, k, bl, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
		this.flushIfUnmanaged();
		return l;
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

	public void blit(int i, int j, int k, int l, int m, TextureAtlasSprite textureAtlasSprite) {
		this.innerBlit(
			textureAtlasSprite.atlasLocation(),
			i,
			i + l,
			j,
			j + m,
			k,
			textureAtlasSprite.getU0(),
			textureAtlasSprite.getU1(),
			textureAtlasSprite.getV0(),
			textureAtlasSprite.getV1()
		);
	}

	public void blit(int i, int j, int k, int l, int m, TextureAtlasSprite textureAtlasSprite, float f, float g, float h, float n) {
		this.innerBlit(
			textureAtlasSprite.atlasLocation(),
			i,
			i + l,
			j,
			j + m,
			k,
			textureAtlasSprite.getU0(),
			textureAtlasSprite.getU1(),
			textureAtlasSprite.getV0(),
			textureAtlasSprite.getV1(),
			f,
			g,
			h,
			n
		);
	}

	public void renderOutline(int i, int j, int k, int l, int m) {
		this.fill(i, j, i + k, j + 1, m);
		this.fill(i, j + l - 1, i + k, j + l, m);
		this.fill(i, j + 1, i + 1, j + l - 1, m);
		this.fill(i + k - 1, j + 1, i + k, j + l - 1, m);
	}

	public void blit(ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n) {
		this.blit(resourceLocation, i, j, 0, (float)k, (float)l, m, n, 256, 256);
	}

	public void blit(ResourceLocation resourceLocation, int i, int j, int k, float f, float g, int l, int m, int n, int o) {
		this.blit(resourceLocation, i, i + l, j, j + m, k, l, m, f, g, n, o);
	}

	public void blit(ResourceLocation resourceLocation, int i, int j, int k, int l, float f, float g, int m, int n, int o, int p) {
		this.blit(resourceLocation, i, i + k, j, j + l, 0, m, n, f, g, o, p);
	}

	public void blit(ResourceLocation resourceLocation, int i, int j, float f, float g, int k, int l, int m, int n) {
		this.blit(resourceLocation, i, j, k, l, f, g, k, l, m, n);
	}

	void blit(ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n, int o, float f, float g, int p, int q) {
		this.innerBlit(resourceLocation, i, j, k, l, m, (f + 0.0F) / (float)p, (f + (float)n) / (float)p, (g + 0.0F) / (float)q, (g + (float)o) / (float)q);
	}

	void innerBlit(ResourceLocation resourceLocation, int i, int j, int k, int l, int m, float f, float g, float h, float n) {
		RenderSystem.setShaderTexture(0, resourceLocation);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		Matrix4f matrix4f = this.pose.last().pose();
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(matrix4f, (float)i, (float)k, (float)m).uv(f, h).endVertex();
		bufferBuilder.vertex(matrix4f, (float)i, (float)l, (float)m).uv(f, n).endVertex();
		bufferBuilder.vertex(matrix4f, (float)j, (float)l, (float)m).uv(g, n).endVertex();
		bufferBuilder.vertex(matrix4f, (float)j, (float)k, (float)m).uv(g, h).endVertex();
		BufferUploader.drawWithShader(bufferBuilder.end());
	}

	void innerBlit(ResourceLocation resourceLocation, int i, int j, int k, int l, int m, float f, float g, float h, float n, float o, float p, float q, float r) {
		RenderSystem.setShaderTexture(0, resourceLocation);
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.enableBlend();
		Matrix4f matrix4f = this.pose.last().pose();
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		bufferBuilder.vertex(matrix4f, (float)i, (float)k, (float)m).color(o, p, q, r).uv(f, h).endVertex();
		bufferBuilder.vertex(matrix4f, (float)i, (float)l, (float)m).color(o, p, q, r).uv(f, n).endVertex();
		bufferBuilder.vertex(matrix4f, (float)j, (float)l, (float)m).color(o, p, q, r).uv(g, n).endVertex();
		bufferBuilder.vertex(matrix4f, (float)j, (float)k, (float)m).color(o, p, q, r).uv(g, h).endVertex();
		BufferUploader.drawWithShader(bufferBuilder.end());
		RenderSystem.disableBlend();
	}

	public void blitNineSliced(ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n, int o, int p, int q) {
		this.blitNineSliced(resourceLocation, i, j, k, l, m, m, m, m, n, o, p, q);
	}

	public void blitNineSliced(ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n, int o, int p, int q, int r) {
		this.blitNineSliced(resourceLocation, i, j, k, l, m, n, m, n, o, p, q, r);
	}

	public void blitNineSliced(ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n, int o, int p, int q, int r, int s, int t) {
		m = Math.min(m, k / 2);
		o = Math.min(o, k / 2);
		n = Math.min(n, l / 2);
		p = Math.min(p, l / 2);
		if (k == q && l == r) {
			this.blit(resourceLocation, i, j, s, t, k, l);
		} else if (l == r) {
			this.blit(resourceLocation, i, j, s, t, m, l);
			this.blitRepeating(resourceLocation, i + m, j, k - o - m, l, s + m, t, q - o - m, r);
			this.blit(resourceLocation, i + k - o, j, s + q - o, t, o, l);
		} else if (k == q) {
			this.blit(resourceLocation, i, j, s, t, k, n);
			this.blitRepeating(resourceLocation, i, j + n, k, l - p - n, s, t + n, q, r - p - n);
			this.blit(resourceLocation, i, j + l - p, s, t + r - p, k, p);
		} else {
			this.blit(resourceLocation, i, j, s, t, m, n);
			this.blitRepeating(resourceLocation, i + m, j, k - o - m, n, s + m, t, q - o - m, n);
			this.blit(resourceLocation, i + k - o, j, s + q - o, t, o, n);
			this.blit(resourceLocation, i, j + l - p, s, t + r - p, m, p);
			this.blitRepeating(resourceLocation, i + m, j + l - p, k - o - m, p, s + m, t + r - p, q - o - m, p);
			this.blit(resourceLocation, i + k - o, j + l - p, s + q - o, t + r - p, o, p);
			this.blitRepeating(resourceLocation, i, j + n, m, l - p - n, s, t + n, m, r - p - n);
			this.blitRepeating(resourceLocation, i + m, j + n, k - o - m, l - p - n, s + m, t + n, q - o - m, r - p - n);
			this.blitRepeating(resourceLocation, i + k - o, j + n, m, l - p - n, s + q - o, t + n, o, r - p - n);
		}
	}

	public void blitRepeating(ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n, int o, int p) {
		int q = i;
		IntIterator intIterator = slices(k, o);

		while (intIterator.hasNext()) {
			int r = intIterator.nextInt();
			int s = (o - r) / 2;
			int t = j;
			IntIterator intIterator2 = slices(l, p);

			while (intIterator2.hasNext()) {
				int u = intIterator2.nextInt();
				int v = (p - u) / 2;
				this.blit(resourceLocation, q, t, m + s, n + v, r, u);
				t += u;
			}

			q += r;
		}
	}

	private static IntIterator slices(int i, int j) {
		int k = Mth.positiveCeilDiv(i, j);
		return new Divisor(i, k);
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
		this.renderItem(null, this.minecraft.level, itemStack, i, j, 0);
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
				this.pose.mulPoseMatrix(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
				this.pose.scale(16.0F, 16.0F, 16.0F);
				boolean bl = !bakedModel.usesBlockLight();
				if (bl) {
					Lighting.setupForFlatItems();
				}

				this.minecraft
					.getItemRenderer()
					.render(itemStack, ItemDisplayContext.GUI, false, this.pose, this.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
				this.flush();
				if (bl) {
					Lighting.setupFor3DItems();
				}
			} catch (Throwable var12) {
				CrashReport crashReport = CrashReport.forThrowable(var12, "Rendering item");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
				crashReportCategory.setDetail("Item Type", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.getItem())));
				crashReportCategory.setDetail("Item Damage", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.getDamageValue())));
				crashReportCategory.setDetail("Item NBT", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.getTag())));
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
				this.fill(RenderType.guiOverlay(), m, n, m + k, n + 1, l | 0xFF000000);
			}

			LocalPlayer localPlayer = this.minecraft.player;
			float f = localPlayer == null ? 0.0F : localPlayer.getCooldowns().getCooldownPercent(itemStack.getItem(), this.minecraft.getFrameTime());
			if (f > 0.0F) {
				int m = j + Mth.floor(16.0F * (1.0F - f));
				int n = m + Mth.ceil(16.0F * f);
				this.fill(RenderType.guiOverlay(), i, m, i + 16, n, Integer.MAX_VALUE);
			}

			this.pose.popPose();
		}
	}

	public void renderTooltip(Font font, ItemStack itemStack, int i, int j) {
		this.renderTooltip(font, Screen.getTooltipFromItem(this.minecraft, itemStack), itemStack.getTooltipImage(), i, j);
	}

	public void renderTooltip(Font font, List<Component> list, Optional<TooltipComponent> optional, int i, int j) {
		List<ClientTooltipComponent> list2 = (List<ClientTooltipComponent>)list.stream()
			.map(Component::getVisualOrderText)
			.map(ClientTooltipComponent::create)
			.collect(Collectors.toList());
		optional.ifPresent(tooltipComponent -> list2.add(1, ClientTooltipComponent.create(tooltipComponent)));
		this.renderTooltipInternal(font, list2, i, j, DefaultTooltipPositioner.INSTANCE);
	}

	public void renderTooltip(Font font, Component component, int i, int j) {
		this.renderTooltip(font, List.of(component.getVisualOrderText()), i, j);
	}

	public void renderComponentTooltip(Font font, List<Component> list, int i, int j) {
		this.renderTooltip(font, Lists.transform(list, Component::getVisualOrderText), i, j);
	}

	public void renderTooltip(Font font, List<? extends FormattedCharSequence> list, int i, int j) {
		this.renderTooltipInternal(
			font, (List<ClientTooltipComponent>)list.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), i, j, DefaultTooltipPositioner.INSTANCE
		);
	}

	public void renderTooltip(Font font, List<FormattedCharSequence> list, ClientTooltipPositioner clientTooltipPositioner, int i, int j) {
		this.renderTooltipInternal(
			font, (List<ClientTooltipComponent>)list.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), i, j, clientTooltipPositioner
		);
	}

	private void renderTooltipInternal(Font font, List<ClientTooltipComponent> list, int i, int j, ClientTooltipPositioner clientTooltipPositioner) {
		if (!list.isEmpty()) {
			int k = 0;
			int l = list.size() == 1 ? -2 : 0;

			for (ClientTooltipComponent clientTooltipComponent : list) {
				int m = clientTooltipComponent.getWidth(font);
				if (m > k) {
					k = m;
				}

				l += clientTooltipComponent.getHeight();
			}

			int n = k;
			int o = l;
			Vector2ic vector2ic = clientTooltipPositioner.positionTooltip(this.guiWidth(), this.guiHeight(), i, j, n, o);
			int p = vector2ic.x();
			int q = vector2ic.y();
			this.pose.pushPose();
			int r = 400;
			this.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(this, p, q, n, o, 400));
			this.pose.translate(0.0F, 0.0F, 400.0F);
			int s = q;

			for (int t = 0; t < list.size(); t++) {
				ClientTooltipComponent clientTooltipComponent2 = (ClientTooltipComponent)list.get(t);
				clientTooltipComponent2.renderText(font, p, s, this.pose.last().pose(), this.bufferSource);
				s += clientTooltipComponent2.getHeight() + (t == 0 ? 2 : 0);
			}

			s = q;

			for (int t = 0; t < list.size(); t++) {
				ClientTooltipComponent clientTooltipComponent2 = (ClientTooltipComponent)list.get(t);
				clientTooltipComponent2.renderImage(font, p, s, this);
				s += clientTooltipComponent2.getHeight() + (t == 0 ? 2 : 0);
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
	}
}
