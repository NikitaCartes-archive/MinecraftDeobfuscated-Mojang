package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ItemFrameRenderer<T extends ItemFrame> extends EntityRenderer<T, ItemFrameRenderState> {
	public static final int GLOW_FRAME_BRIGHTNESS = 5;
	public static final int BRIGHT_MAP_LIGHT_ADJUSTMENT = 30;
	private final ItemRenderer itemRenderer;
	private final MapRenderer mapRenderer;
	private final BlockRenderDispatcher blockRenderer;

	public ItemFrameRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
		this.mapRenderer = context.getMapRenderer();
		this.blockRenderer = context.getBlockRenderDispatcher();
	}

	protected int getBlockLightLevel(T itemFrame, BlockPos blockPos) {
		return itemFrame.getType() == EntityType.GLOW_ITEM_FRAME
			? Math.max(5, super.getBlockLightLevel(itemFrame, blockPos))
			: super.getBlockLightLevel(itemFrame, blockPos);
	}

	public void render(ItemFrameRenderState itemFrameRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		super.render(itemFrameRenderState, poseStack, multiBufferSource, i);
		poseStack.pushPose();
		Direction direction = itemFrameRenderState.direction;
		Vec3 vec3 = this.getRenderOffset(itemFrameRenderState);
		poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
		double d = 0.46875;
		poseStack.translate((double)direction.getStepX() * 0.46875, (double)direction.getStepY() * 0.46875, (double)direction.getStepZ() * 0.46875);
		float f;
		float g;
		if (direction.getAxis().isHorizontal()) {
			f = 0.0F;
			g = 180.0F - direction.toYRot();
		} else {
			f = (float)(-90 * direction.getAxisDirection().getStep());
			g = 180.0F;
		}

		poseStack.mulPose(Axis.XP.rotationDegrees(f));
		poseStack.mulPose(Axis.YP.rotationDegrees(g));
		ItemStack itemStack = itemFrameRenderState.itemStack;
		if (!itemFrameRenderState.isInvisible) {
			ModelManager modelManager = this.blockRenderer.getBlockModelShaper().getModelManager();
			ModelResourceLocation modelResourceLocation = this.getFrameModelResourceLoc(itemFrameRenderState.isGlowFrame, itemStack);
			poseStack.pushPose();
			poseStack.translate(-0.5F, -0.5F, -0.5F);
			this.blockRenderer
				.getModelRenderer()
				.renderModel(
					poseStack.last(),
					multiBufferSource.getBuffer(RenderType.entitySolidZOffsetForward(TextureAtlas.LOCATION_BLOCKS)),
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
			MapId mapId = itemFrameRenderState.mapId;
			if (itemFrameRenderState.isInvisible) {
				poseStack.translate(0.0F, 0.0F, 0.5F);
			} else {
				poseStack.translate(0.0F, 0.0F, 0.4375F);
			}

			int j = mapId != null ? itemFrameRenderState.rotation % 4 * 2 : itemFrameRenderState.rotation;
			poseStack.mulPose(Axis.ZP.rotationDegrees((float)j * 360.0F / 8.0F));
			if (mapId != null) {
				poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
				float h = 0.0078125F;
				poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
				poseStack.translate(-64.0F, -64.0F, 0.0F);
				poseStack.translate(0.0F, 0.0F, -1.0F);
				int k = this.getLightVal(itemFrameRenderState.isGlowFrame, 15728850, i);
				this.mapRenderer.render(itemFrameRenderState.mapRenderState, poseStack, multiBufferSource, true, k);
			} else if (itemFrameRenderState.itemModel != null) {
				int l = this.getLightVal(itemFrameRenderState.isGlowFrame, 15728880, i);
				poseStack.scale(0.5F, 0.5F, 0.5F);
				this.itemRenderer
					.render(itemStack, ItemDisplayContext.FIXED, false, poseStack, multiBufferSource, l, OverlayTexture.NO_OVERLAY, itemFrameRenderState.itemModel);
			}
		}

		poseStack.popPose();
	}

	private int getLightVal(boolean bl, int i, int j) {
		return bl ? i : j;
	}

	private ModelResourceLocation getFrameModelResourceLoc(boolean bl, ItemStack itemStack) {
		if (itemStack.has(DataComponents.MAP_ID)) {
			return bl ? BlockStateModelLoader.GLOW_MAP_FRAME_LOCATION : BlockStateModelLoader.MAP_FRAME_LOCATION;
		} else {
			return bl ? BlockStateModelLoader.GLOW_FRAME_LOCATION : BlockStateModelLoader.FRAME_LOCATION;
		}
	}

	public Vec3 getRenderOffset(ItemFrameRenderState itemFrameRenderState) {
		return new Vec3((double)((float)itemFrameRenderState.direction.getStepX() * 0.3F), -0.25, (double)((float)itemFrameRenderState.direction.getStepZ() * 0.3F));
	}

	protected boolean shouldShowName(T itemFrame, double d) {
		return Minecraft.renderNames()
			&& !itemFrame.getItem().isEmpty()
			&& itemFrame.getItem().has(DataComponents.CUSTOM_NAME)
			&& this.entityRenderDispatcher.crosshairPickEntity == itemFrame;
	}

	protected Component getNameTag(T itemFrame) {
		return itemFrame.getItem().getHoverName();
	}

	public ItemFrameRenderState createRenderState() {
		return new ItemFrameRenderState();
	}

	public void extractRenderState(T itemFrame, ItemFrameRenderState itemFrameRenderState, float f) {
		super.extractRenderState(itemFrame, itemFrameRenderState, f);
		itemFrameRenderState.direction = itemFrame.getDirection();
		ItemStack itemStack = itemFrame.getItem();
		itemFrameRenderState.itemStack = itemStack.copy();
		itemFrameRenderState.rotation = itemFrame.getRotation();
		itemFrameRenderState.isGlowFrame = itemFrame.getType() == EntityType.GLOW_ITEM_FRAME;
		itemFrameRenderState.itemModel = null;
		itemFrameRenderState.mapId = null;
		if (!itemFrameRenderState.itemStack.isEmpty()) {
			MapId mapId = itemFrame.getFramedMapId(itemStack);
			if (mapId != null) {
				MapItemSavedData mapItemSavedData = itemFrame.level().getMapData(mapId);
				if (mapItemSavedData != null) {
					this.mapRenderer.extractRenderState(mapId, mapItemSavedData, itemFrameRenderState.mapRenderState);
					itemFrameRenderState.mapId = mapId;
				}
			} else {
				itemFrameRenderState.itemModel = this.itemRenderer.getModel(itemStack, itemFrame.level(), null, itemFrame.getId());
			}
		}
	}
}
