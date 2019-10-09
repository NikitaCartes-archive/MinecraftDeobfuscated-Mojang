package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ItemFrameRenderer extends EntityRenderer<ItemFrame> {
	private static final ModelResourceLocation FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=false");
	private static final ModelResourceLocation MAP_FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=true");
	private final Minecraft minecraft = Minecraft.getInstance();
	private final ItemRenderer itemRenderer;

	public ItemFrameRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
		super(entityRenderDispatcher);
		this.itemRenderer = itemRenderer;
	}

	public void render(ItemFrame itemFrame, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		super.render(itemFrame, d, e, f, g, h, poseStack, multiBufferSource);
		poseStack.pushPose();
		Direction direction = itemFrame.getDirection();
		Vec3 vec3 = this.getRenderOffset(itemFrame, d, e, f, h);
		poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
		double i = 0.46875;
		poseStack.translate((double)direction.getStepX() * 0.46875, (double)direction.getStepY() * 0.46875, (double)direction.getStepZ() * 0.46875);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(itemFrame.xRot));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - itemFrame.yRot));
		BlockRenderDispatcher blockRenderDispatcher = this.minecraft.getBlockRenderer();
		ModelManager modelManager = blockRenderDispatcher.getBlockModelShaper().getModelManager();
		ModelResourceLocation modelResourceLocation = itemFrame.getItem().getItem() == Items.FILLED_MAP ? MAP_FRAME_LOCATION : FRAME_LOCATION;
		poseStack.pushPose();
		poseStack.translate(-0.5, -0.5, -0.5);
		int j = itemFrame.getLightColor();
		blockRenderDispatcher.getModelRenderer()
			.renderModel(
				poseStack.getPose(),
				poseStack.getNormal(),
				multiBufferSource.getBuffer(RenderType.solid()),
				null,
				modelManager.getModel(modelResourceLocation),
				1.0F,
				1.0F,
				1.0F,
				j,
				OverlayTexture.NO_OVERLAY
			);
		poseStack.popPose();
		ItemStack itemStack = itemFrame.getItem();
		if (!itemStack.isEmpty()) {
			boolean bl = itemStack.getItem() == Items.FILLED_MAP;
			poseStack.translate(0.0, 0.0, 0.4375);
			int k = bl ? itemFrame.getRotation() % 4 * 2 : itemFrame.getRotation();
			poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)k * 360.0F / 8.0F));
			if (bl) {
				this.entityRenderDispatcher.textureManager.bind(MapRenderer.MAP_BACKGROUND_LOCATION);
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
				float l = 0.0078125F;
				poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
				poseStack.translate(-64.0, -64.0, 0.0);
				MapItemSavedData mapItemSavedData = MapItem.getOrCreateSavedData(itemStack, itemFrame.level);
				poseStack.translate(0.0, 0.0, -1.0);
				if (mapItemSavedData != null) {
					this.minecraft.gameRenderer.getMapRenderer().render(poseStack, multiBufferSource, mapItemSavedData, true, j);
				}
			} else {
				poseStack.scale(0.5F, 0.5F, 0.5F);
				this.itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.FIXED, j, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource);
			}
		}

		poseStack.popPose();
	}

	public Vec3 getRenderOffset(ItemFrame itemFrame, double d, double e, double f, float g) {
		return new Vec3((double)((float)itemFrame.getDirection().getStepX() * 0.3F), -0.25, (double)((float)itemFrame.getDirection().getStepZ() * 0.3F));
	}

	public ResourceLocation getTextureLocation(ItemFrame itemFrame) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	protected boolean shouldShowName(ItemFrame itemFrame) {
		if (Minecraft.renderNames()
			&& !itemFrame.getItem().isEmpty()
			&& itemFrame.getItem().hasCustomHoverName()
			&& this.entityRenderDispatcher.crosshairPickEntity == itemFrame) {
			double d = this.entityRenderDispatcher.distanceToSqr(itemFrame);
			float f = itemFrame.isDiscrete() ? 32.0F : 64.0F;
			return d < (double)(f * f);
		} else {
			return false;
		}
	}

	protected void renderNameTag(ItemFrame itemFrame, String string, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		super.renderNameTag(itemFrame, itemFrame.getItem().getHoverName().getColoredString(), poseStack, multiBufferSource);
	}
}
