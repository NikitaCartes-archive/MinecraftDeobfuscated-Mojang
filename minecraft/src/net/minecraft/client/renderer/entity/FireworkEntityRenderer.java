package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;

@Environment(EnvType.CLIENT)
public class FireworkEntityRenderer extends EntityRenderer<FireworkRocketEntity> {
	private final ItemRenderer itemRenderer;

	public FireworkEntityRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
		super(entityRenderDispatcher);
		this.itemRenderer = itemRenderer;
	}

	public void render(
		FireworkRocketEntity fireworkRocketEntity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource
	) {
		poseStack.pushPose();
		poseStack.mulPose(Vector3f.YP.rotation(-this.entityRenderDispatcher.playerRotY, true));
		poseStack.mulPose(
			Vector3f.XP.rotation((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * this.entityRenderDispatcher.playerRotX, true)
		);
		if (fireworkRocketEntity.isShotAtAngle()) {
			poseStack.mulPose(Vector3f.XP.rotation(90.0F, true));
		} else {
			poseStack.mulPose(Vector3f.YP.rotation(180.0F, true));
		}

		this.itemRenderer
			.renderStatic(fireworkRocketEntity.getItem(), ItemTransforms.TransformType.GROUND, fireworkRocketEntity.getLightColor(), poseStack, multiBufferSource);
		poseStack.popPose();
		super.render(fireworkRocketEntity, d, e, f, g, h, poseStack, multiBufferSource);
	}

	public ResourceLocation getTextureLocation(FireworkRocketEntity fireworkRocketEntity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
