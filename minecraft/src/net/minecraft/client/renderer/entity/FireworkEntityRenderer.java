package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;

@Environment(EnvType.CLIENT)
public class FireworkEntityRenderer extends EntityRenderer<FireworkRocketEntity> {
	private final ItemRenderer itemRenderer;

	public FireworkEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
	}

	public void render(FireworkRocketEntity fireworkRocketEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
		if (fireworkRocketEntity.isShotAtAngle()) {
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
		}

		this.itemRenderer
			.renderStatic(fireworkRocketEntity.getItem(), ItemTransforms.TransformType.GROUND, i, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource);
		poseStack.popPose();
		super.render(fireworkRocketEntity, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(FireworkRocketEntity fireworkRocketEntity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
