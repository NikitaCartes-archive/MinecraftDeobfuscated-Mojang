package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Display;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public abstract class DisplayRenderer<T extends Display> extends EntityRenderer<T> {
	private static final float MAX_SHADOW_RADIUS = 64.0F;
	private final EntityRenderDispatcher entityRenderDispatcher;

	protected DisplayRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.entityRenderDispatcher = context.getEntityRenderDispatcher();
	}

	public ResourceLocation getTextureLocation(T display) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	public void render(T display, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		float h = display.calculateInterpolationProgress(g);
		this.shadowRadius = Math.min(display.getShadowRadius(h), 64.0F);
		this.shadowStrength = display.getShadowStrength(h);
		int j = display.getPackedBrightnessOverride();
		int k = j != -1 ? j : i;
		super.render(display, f, g, poseStack, multiBufferSource, k);
		poseStack.pushPose();
		poseStack.mulPose(this.calculateOrientation(display));
		poseStack.mulPoseMatrix(display.transformation(h).getMatrix());
		this.renderInner(display, poseStack, multiBufferSource, k, h);
		poseStack.popPose();
	}

	private Quaternionf calculateOrientation(T display) {
		Camera camera = this.entityRenderDispatcher.camera;

		return switch (display.getBillboardConstraints()) {
			case FIXED -> display.orientation();
			case HORIZONTAL -> new Quaternionf().rotationYXZ((float) (-Math.PI / 180.0) * display.getYRot(), (float) (-Math.PI / 180.0) * camera.getXRot(), 0.0F);
			case VERTICAL -> new Quaternionf()
			.rotationYXZ((float) Math.PI - (float) (Math.PI / 180.0) * camera.getYRot(), (float) (Math.PI / 180.0) * display.getXRot(), 0.0F);
			case CENTER -> new Quaternionf()
			.rotationYXZ((float) Math.PI - (float) (Math.PI / 180.0) * camera.getYRot(), (float) (-Math.PI / 180.0) * camera.getXRot(), 0.0F);
		};
	}

	protected abstract void renderInner(T display, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f);

	@Environment(EnvType.CLIENT)
	public static class BlockDisplayRenderer extends DisplayRenderer<Display.BlockDisplay> {
		private final BlockRenderDispatcher blockRenderer;

		protected BlockDisplayRenderer(EntityRendererProvider.Context context) {
			super(context);
			this.blockRenderer = context.getBlockRenderDispatcher();
		}

		public void renderInner(Display.BlockDisplay blockDisplay, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f) {
			this.blockRenderer.renderSingleBlock(blockDisplay.getBlockState(), poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class ItemDisplayRenderer extends DisplayRenderer<Display.ItemDisplay> {
		private final ItemRenderer itemRenderer;

		protected ItemDisplayRenderer(EntityRendererProvider.Context context) {
			super(context);
			this.itemRenderer = context.getItemRenderer();
		}

		public void renderInner(Display.ItemDisplay itemDisplay, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f) {
			this.itemRenderer
				.renderStatic(
					itemDisplay.getItemStack(),
					itemDisplay.getItemTransform(),
					i,
					OverlayTexture.NO_OVERLAY,
					poseStack,
					multiBufferSource,
					itemDisplay.getLevel(),
					itemDisplay.getId()
				);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class TextDisplayRenderer extends DisplayRenderer<Display.TextDisplay> {
		private final Font font;

		protected TextDisplayRenderer(EntityRendererProvider.Context context) {
			super(context);
			this.font = context.getFont();
		}

		private Display.TextDisplay.CachedInfo splitLines(Component component, int i) {
			List<FormattedCharSequence> list = this.font.split(component, i);
			List<Display.TextDisplay.CachedLine> list2 = new ArrayList(list.size());
			int j = 0;

			for (FormattedCharSequence formattedCharSequence : list) {
				int k = this.font.width(formattedCharSequence);
				j = Math.max(j, k);
				list2.add(new Display.TextDisplay.CachedLine(formattedCharSequence, k));
			}

			return new Display.TextDisplay.CachedInfo(list2, j);
		}

		public void renderInner(Display.TextDisplay textDisplay, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f) {
			byte b = textDisplay.getFlags();
			boolean bl = (b & 2) != 0;
			boolean bl2 = (b & 4) != 0;
			boolean bl3 = (b & 1) != 0;
			Display.TextDisplay.Align align = Display.TextDisplay.getAlign(b);
			byte c = textDisplay.getTextOpacity(f);
			int j;
			if (bl2) {
				float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
				j = (int)(g * 255.0F) << 24;
			} else {
				j = textDisplay.getBackgroundColor(f);
			}

			float g = 0.0F;
			Matrix4f matrix4f = poseStack.last().pose();
			matrix4f.rotate((float) Math.PI, 0.0F, 1.0F, 0.0F);
			matrix4f.scale(-0.025F, -0.025F, -0.025F);
			Display.TextDisplay.CachedInfo cachedInfo = textDisplay.cacheDisplay(this::splitLines);
			int k = 9 + 1;
			int l = cachedInfo.width();
			int m = cachedInfo.lines().size() * k;
			matrix4f.translate(1.0F - (float)l / 2.0F, (float)(-m), 0.0F);
			if (j != 0) {
				VertexConsumer vertexConsumer = multiBufferSource.getBuffer(bl ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
				vertexConsumer.vertex(matrix4f, -1.0F, -1.0F, 0.0F).color(j).uv2(i).endVertex();
				vertexConsumer.vertex(matrix4f, -1.0F, (float)m, 0.0F).color(j).uv2(i).endVertex();
				vertexConsumer.vertex(matrix4f, (float)l, (float)m, 0.0F).color(j).uv2(i).endVertex();
				vertexConsumer.vertex(matrix4f, (float)l, -1.0F, 0.0F).color(j).uv2(i).endVertex();
			}

			for (Display.TextDisplay.CachedLine cachedLine : cachedInfo.lines()) {
				float h = switch (align) {
					case LEFT -> 0.0F;
					case RIGHT -> (float)(l - cachedLine.width());
					case CENTER -> (float)l / 2.0F - (float)cachedLine.width() / 2.0F;
				};
				this.font
					.drawInBatch(
						cachedLine.contents(),
						h,
						g,
						c << 24 | 16777215,
						bl3,
						matrix4f,
						multiBufferSource,
						bl ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
						0,
						i
					);
				g += (float)k;
			}
		}
	}
}
