package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
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
import net.minecraft.client.renderer.entity.state.BlockDisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.DisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemDisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public abstract class DisplayRenderer<T extends Display, S, ST extends DisplayEntityRenderState> extends EntityRenderer<T, ST> {
	private final EntityRenderDispatcher entityRenderDispatcher;

	protected DisplayRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.entityRenderDispatcher = context.getEntityRenderDispatcher();
	}

	protected AABB getBoundingBoxForCulling(T display) {
		return display.getBoundingBoxForCulling();
	}

	protected boolean affectedByCulling(T display) {
		return display.affectedByCulling();
	}

	public ResourceLocation getTextureLocation(DisplayEntityRenderState displayEntityRenderState) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	public void render(ST displayEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		Display.RenderState renderState = displayEntityRenderState.renderState;
		if (renderState != null) {
			if (displayEntityRenderState.hasSubState()) {
				float f = displayEntityRenderState.interpolationProgress;
				this.shadowRadius = renderState.shadowRadius().get(f);
				this.shadowStrength = renderState.shadowStrength().get(f);
				int j = renderState.brightnessOverride();
				int k = j != -1 ? j : i;
				super.render(displayEntityRenderState, poseStack, multiBufferSource, k);
				poseStack.pushPose();
				poseStack.mulPose(this.calculateOrientation(renderState, displayEntityRenderState, new Quaternionf()));
				Transformation transformation = renderState.transformation().get(f);
				poseStack.mulPose(transformation.getMatrix());
				this.renderInner(displayEntityRenderState, poseStack, multiBufferSource, k, f);
				poseStack.popPose();
			}
		}
	}

	private Quaternionf calculateOrientation(Display.RenderState renderState, ST displayEntityRenderState, Quaternionf quaternionf) {
		Camera camera = this.entityRenderDispatcher.camera;

		return switch (renderState.billboardConstraints()) {
			case FIXED -> quaternionf.rotationYXZ(
			(float) (-Math.PI / 180.0) * displayEntityRenderState.entityYRot, (float) (Math.PI / 180.0) * displayEntityRenderState.entityXRot, 0.0F
		);
			case HORIZONTAL -> quaternionf.rotationYXZ(
			(float) (-Math.PI / 180.0) * displayEntityRenderState.entityYRot, (float) (Math.PI / 180.0) * cameraXRot(camera), 0.0F
		);
			case VERTICAL -> quaternionf.rotationYXZ(
			(float) (-Math.PI / 180.0) * cameraYrot(camera), (float) (Math.PI / 180.0) * displayEntityRenderState.entityXRot, 0.0F
		);
			case CENTER -> quaternionf.rotationYXZ((float) (-Math.PI / 180.0) * cameraYrot(camera), (float) (Math.PI / 180.0) * cameraXRot(camera), 0.0F);
		};
	}

	private static float cameraYrot(Camera camera) {
		return camera.getYRot() - 180.0F;
	}

	private static float cameraXRot(Camera camera) {
		return -camera.getXRot();
	}

	private static <T extends Display> float entityYRot(T display, float f) {
		return display.getYRot(f);
	}

	private static <T extends Display> float entityXRot(T display, float f) {
		return display.getXRot(f);
	}

	protected abstract void renderInner(ST displayEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f);

	public void extractRenderState(T display, ST displayEntityRenderState, float f) {
		super.extractRenderState(display, displayEntityRenderState, f);
		displayEntityRenderState.renderState = display.renderState();
		displayEntityRenderState.interpolationProgress = display.calculateInterpolationProgress(f);
		displayEntityRenderState.entityYRot = entityYRot(display, f);
		displayEntityRenderState.entityXRot = entityXRot(display, f);
	}

	@Environment(EnvType.CLIENT)
	public static class BlockDisplayRenderer extends DisplayRenderer<Display.BlockDisplay, Display.BlockDisplay.BlockRenderState, BlockDisplayEntityRenderState> {
		private final BlockRenderDispatcher blockRenderer;

		protected BlockDisplayRenderer(EntityRendererProvider.Context context) {
			super(context);
			this.blockRenderer = context.getBlockRenderDispatcher();
		}

		public BlockDisplayEntityRenderState createRenderState() {
			return new BlockDisplayEntityRenderState();
		}

		public void extractRenderState(Display.BlockDisplay blockDisplay, BlockDisplayEntityRenderState blockDisplayEntityRenderState, float f) {
			super.extractRenderState(blockDisplay, blockDisplayEntityRenderState, f);
			blockDisplayEntityRenderState.blockRenderState = blockDisplay.blockRenderState();
		}

		public void renderInner(BlockDisplayEntityRenderState blockDisplayEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f) {
			this.blockRenderer
				.renderSingleBlock(blockDisplayEntityRenderState.blockRenderState.blockState(), poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class ItemDisplayRenderer extends DisplayRenderer<Display.ItemDisplay, Display.ItemDisplay.ItemRenderState, ItemDisplayEntityRenderState> {
		private final ItemRenderer itemRenderer;

		protected ItemDisplayRenderer(EntityRendererProvider.Context context) {
			super(context);
			this.itemRenderer = context.getItemRenderer();
		}

		public ItemDisplayEntityRenderState createRenderState() {
			return new ItemDisplayEntityRenderState();
		}

		public void extractRenderState(Display.ItemDisplay itemDisplay, ItemDisplayEntityRenderState itemDisplayEntityRenderState, float f) {
			super.extractRenderState(itemDisplay, itemDisplayEntityRenderState, f);
			Display.ItemDisplay.ItemRenderState itemRenderState = itemDisplay.itemRenderState();
			if (itemRenderState != null) {
				itemDisplayEntityRenderState.itemRenderState = itemRenderState;
				itemDisplayEntityRenderState.itemModel = this.itemRenderer
					.getModel(itemDisplayEntityRenderState.itemRenderState.itemStack(), itemDisplay.level(), null, itemDisplay.getId());
			} else {
				itemDisplayEntityRenderState.itemRenderState = null;
				itemDisplayEntityRenderState.itemModel = null;
			}
		}

		public void renderInner(ItemDisplayEntityRenderState itemDisplayEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f) {
			Display.ItemDisplay.ItemRenderState itemRenderState = itemDisplayEntityRenderState.itemRenderState;
			BakedModel bakedModel = itemDisplayEntityRenderState.itemModel;
			if (itemRenderState != null && bakedModel != null) {
				poseStack.mulPose(Axis.YP.rotation((float) Math.PI));
				this.itemRenderer
					.render(itemRenderState.itemStack(), itemRenderState.itemTransform(), false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, bakedModel);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class TextDisplayRenderer extends DisplayRenderer<Display.TextDisplay, Display.TextDisplay.TextRenderState, TextDisplayEntityRenderState> {
		private final Font font;

		protected TextDisplayRenderer(EntityRendererProvider.Context context) {
			super(context);
			this.font = context.getFont();
		}

		public TextDisplayEntityRenderState createRenderState() {
			return new TextDisplayEntityRenderState();
		}

		public void extractRenderState(Display.TextDisplay textDisplay, TextDisplayEntityRenderState textDisplayEntityRenderState, float f) {
			super.extractRenderState(textDisplay, textDisplayEntityRenderState, f);
			textDisplayEntityRenderState.textRenderState = textDisplay.textRenderState();
			textDisplayEntityRenderState.cachedInfo = textDisplay.cacheDisplay(this::splitLines);
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

		public void renderInner(TextDisplayEntityRenderState textDisplayEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f) {
			Display.TextDisplay.TextRenderState textRenderState = textDisplayEntityRenderState.textRenderState;
			byte b = textRenderState.flags();
			boolean bl = (b & 2) != 0;
			boolean bl2 = (b & 4) != 0;
			boolean bl3 = (b & 1) != 0;
			Display.TextDisplay.Align align = Display.TextDisplay.getAlign(b);
			byte c = (byte)textRenderState.textOpacity().get(f);
			int j;
			if (bl2) {
				float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
				j = (int)(g * 255.0F) << 24;
			} else {
				j = textRenderState.backgroundColor().get(f);
			}

			float g = 0.0F;
			Matrix4f matrix4f = poseStack.last().pose();
			matrix4f.rotate((float) Math.PI, 0.0F, 1.0F, 0.0F);
			matrix4f.scale(-0.025F, -0.025F, -0.025F);
			Display.TextDisplay.CachedInfo cachedInfo = textDisplayEntityRenderState.cachedInfo;
			int k = 1;
			int l = 9 + 1;
			int m = cachedInfo.width();
			int n = cachedInfo.lines().size() * l - 1;
			matrix4f.translate(1.0F - (float)m / 2.0F, (float)(-n), 0.0F);
			if (j != 0) {
				VertexConsumer vertexConsumer = multiBufferSource.getBuffer(bl ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
				vertexConsumer.addVertex(matrix4f, -1.0F, -1.0F, 0.0F).setColor(j).setLight(i);
				vertexConsumer.addVertex(matrix4f, -1.0F, (float)n, 0.0F).setColor(j).setLight(i);
				vertexConsumer.addVertex(matrix4f, (float)m, (float)n, 0.0F).setColor(j).setLight(i);
				vertexConsumer.addVertex(matrix4f, (float)m, -1.0F, 0.0F).setColor(j).setLight(i);
			}

			for (Display.TextDisplay.CachedLine cachedLine : cachedInfo.lines()) {
				float h = switch (align) {
					case LEFT -> 0.0F;
					case RIGHT -> (float)(m - cachedLine.width());
					case CENTER -> (float)m / 2.0F - (float)cachedLine.width() / 2.0F;
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
				g += (float)l;
			}
		}
	}
}
