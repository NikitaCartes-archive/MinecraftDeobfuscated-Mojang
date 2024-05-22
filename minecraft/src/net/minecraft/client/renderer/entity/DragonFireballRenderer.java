package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.DragonFireball;

@Environment(EnvType.CLIENT)
public class DragonFireballRenderer extends EntityRenderer<DragonFireball> {
	private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_fireball.png");
	private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);

	public DragonFireballRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	protected int getBlockLightLevel(DragonFireball dragonFireball, BlockPos blockPos) {
		return 15;
	}

	public void render(DragonFireball dragonFireball, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.scale(2.0F, 2.0F, 2.0F);
		poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
		PoseStack.Pose pose = poseStack.last();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
		vertex(vertexConsumer, pose, i, 0.0F, 0, 0, 1);
		vertex(vertexConsumer, pose, i, 1.0F, 0, 1, 1);
		vertex(vertexConsumer, pose, i, 1.0F, 1, 1, 0);
		vertex(vertexConsumer, pose, i, 0.0F, 1, 0, 0);
		poseStack.popPose();
		super.render(dragonFireball, f, g, poseStack, multiBufferSource, i);
	}

	private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, int i, float f, int j, int k, int l) {
		vertexConsumer.addVertex(pose, f - 0.5F, (float)j - 0.25F, 0.0F)
			.setColor(-1)
			.setUv((float)k, (float)l)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(i)
			.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	public ResourceLocation getTextureLocation(DragonFireball dragonFireball) {
		return TEXTURE_LOCATION;
	}
}
