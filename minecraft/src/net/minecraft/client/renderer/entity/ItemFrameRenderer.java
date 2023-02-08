package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ItemFrameRenderer<T extends ItemFrame> extends EntityRenderer<T> {
	private static final ModelResourceLocation FRAME_LOCATION = ModelResourceLocation.vanilla("item_frame", "map=false");
	private static final ModelResourceLocation MAP_FRAME_LOCATION = ModelResourceLocation.vanilla("item_frame", "map=true");
	private static final ModelResourceLocation GLOW_FRAME_LOCATION = ModelResourceLocation.vanilla("glow_item_frame", "map=false");
	private static final ModelResourceLocation GLOW_MAP_FRAME_LOCATION = ModelResourceLocation.vanilla("glow_item_frame", "map=true");
	public static final int GLOW_FRAME_BRIGHTNESS = 5;
	public static final int BRIGHT_MAP_LIGHT_ADJUSTMENT = 30;
	private final ItemRenderer itemRenderer;
	private final BlockRenderDispatcher blockRenderer;

	public ItemFrameRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
		this.blockRenderer = context.getBlockRenderDispatcher();
	}

	protected int getBlockLightLevel(T itemFrame, BlockPos blockPos) {
		return itemFrame.getType() == EntityType.GLOW_ITEM_FRAME
			? Math.max(5, super.getBlockLightLevel(itemFrame, blockPos))
			: super.getBlockLightLevel(itemFrame, blockPos);
	}

	public void render(T itemFrame, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		super.render(itemFrame, f, g, poseStack, multiBufferSource, i);
		poseStack.pushPose();
		Direction direction = itemFrame.getDirection();
		Vec3 vec3 = this.getRenderOffset(itemFrame, g);
		poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
		double d = 0.46875;
		poseStack.translate((double)direction.getStepX() * 0.46875, (double)direction.getStepY() * 0.46875, (double)direction.getStepZ() * 0.46875);
		poseStack.mulPose(Axis.XP.rotationDegrees(itemFrame.getXRot()));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - itemFrame.getYRot()));
		boolean bl = itemFrame.isInvisible();
		ItemStack itemStack = itemFrame.getItem();
		if (!bl) {
			ModelManager modelManager = this.blockRenderer.getBlockModelShaper().getModelManager();
			ModelResourceLocation modelResourceLocation = this.getFrameModelResourceLoc(itemFrame, itemStack);
			poseStack.pushPose();
			poseStack.translate(-0.5F, -0.5F, -0.5F);
			this.blockRenderer
				.getModelRenderer()
				.renderModel(
					poseStack.last(),
					multiBufferSource.getBuffer(Sheets.solidBlockSheet()),
					null,
					modelManager.getModel(modelResourceLocation),
					1.0F,
					1.0F,
					1.0F,
					i,
					OverlayTexture.NO_OVERLAY
				);
			poseStack.popPose();
		}

		if (!itemStack.isEmpty()) {
			OptionalInt optionalInt = itemFrame.getFramedMapId();
			if (bl) {
				poseStack.translate(0.0F, 0.0F, 0.5F);
			} else {
				poseStack.translate(0.0F, 0.0F, 0.4375F);
			}

			int j = optionalInt.isPresent() ? itemFrame.getRotation() % 4 * 2 : itemFrame.getRotation();
			poseStack.mulPose(Axis.ZP.rotationDegrees((float)j * 360.0F / 8.0F));
			if (optionalInt.isPresent()) {
				poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
				float h = 0.0078125F;
				poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
				poseStack.translate(-64.0F, -64.0F, 0.0F);
				MapItemSavedData mapItemSavedData = MapItem.getSavedData(optionalInt.getAsInt(), itemFrame.level);
				poseStack.translate(0.0F, 0.0F, -1.0F);
				if (mapItemSavedData != null) {
					int k = this.getLightVal(itemFrame, 15728850, i);
					Minecraft.getInstance().gameRenderer.getMapRenderer().render(poseStack, multiBufferSource, optionalInt.getAsInt(), mapItemSavedData, true, k);
				}
			} else {
				int l = this.getLightVal(itemFrame, 15728880, i);
				poseStack.scale(0.5F, 0.5F, 0.5F);
				this.itemRenderer
					.renderStatic(itemStack, ItemDisplayContext.FIXED, l, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, itemFrame.level, itemFrame.getId());
			}
		}

		poseStack.popPose();
	}

	private int getLightVal(T itemFrame, int i, int j) {
		return itemFrame.getType() == EntityType.GLOW_ITEM_FRAME ? i : j;
	}

	private ModelResourceLocation getFrameModelResourceLoc(T itemFrame, ItemStack itemStack) {
		boolean bl = itemFrame.getType() == EntityType.GLOW_ITEM_FRAME;
		if (itemStack.is(Items.FILLED_MAP)) {
			return bl ? GLOW_MAP_FRAME_LOCATION : MAP_FRAME_LOCATION;
		} else {
			return bl ? GLOW_FRAME_LOCATION : FRAME_LOCATION;
		}
	}

	public Vec3 getRenderOffset(T itemFrame, float f) {
		return new Vec3((double)((float)itemFrame.getDirection().getStepX() * 0.3F), -0.25, (double)((float)itemFrame.getDirection().getStepZ() * 0.3F));
	}

	public ResourceLocation getTextureLocation(T itemFrame) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	protected boolean shouldShowName(T itemFrame) {
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

	protected void renderNameTag(T itemFrame, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		super.renderNameTag(itemFrame, itemFrame.getItem().getHoverName(), poseStack, multiBufferSource, i);
	}
}
