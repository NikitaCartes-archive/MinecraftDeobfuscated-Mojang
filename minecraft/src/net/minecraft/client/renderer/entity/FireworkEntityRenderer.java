package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemDisplayContext;

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
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		if (fireworkRocketEntity.isShotAtAngle()) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		}

		this.itemRenderer
			.renderStatic(
				fireworkRocketEntity.getItem(),
				ItemDisplayContext.GROUND,
				i,
				OverlayTexture.NO_OVERLAY,
				poseStack,
				multiBufferSource,
				fireworkRocketEntity.level(),
				fireworkRocketEntity.getId()
			);
		poseStack.popPose();
		super.render(fireworkRocketEntity, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(FireworkRocketEntity fireworkRocketEntity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
