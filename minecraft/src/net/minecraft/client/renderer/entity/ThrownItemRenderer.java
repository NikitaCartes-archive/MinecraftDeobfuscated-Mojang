package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemDisplayContext;

@Environment(EnvType.CLIENT)
public class ThrownItemRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T> {
	private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;
	private final ItemRenderer itemRenderer;
	private final float scale;
	private final boolean fullBright;

	public ThrownItemRenderer(EntityRendererProvider.Context context, float f, boolean bl) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
		this.scale = f;
		this.fullBright = bl;
	}

	public ThrownItemRenderer(EntityRendererProvider.Context context) {
		this(context, 1.0F, false);
	}

	@Override
	protected int getBlockLightLevel(T entity, BlockPos blockPos) {
		return this.fullBright ? 15 : super.getBlockLightLevel(entity, blockPos);
	}

	@Override
	public void render(T entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25)) {
			poseStack.pushPose();
			poseStack.scale(this.scale, this.scale, this.scale);
			poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
			this.itemRenderer
				.renderStatic(entity.getItem(), ItemDisplayContext.GROUND, i, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, entity.level(), entity.getId());
			poseStack.popPose();
			super.render(entity, f, g, poseStack, multiBufferSource, i);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(Entity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
