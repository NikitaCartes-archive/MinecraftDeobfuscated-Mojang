package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.ShulkerBulletRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;

@Environment(EnvType.CLIENT)
public class ShulkerBulletRenderer extends EntityRenderer<ShulkerBullet, ShulkerBulletRenderState> {
	private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/shulker/spark.png");
	private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE_LOCATION);
	private final ShulkerBulletModel model;

	public ShulkerBulletRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new ShulkerBulletModel(context.bakeLayer(ModelLayers.SHULKER_BULLET));
	}

	protected int getBlockLightLevel(ShulkerBullet shulkerBullet, BlockPos blockPos) {
		return 15;
	}

	public void render(ShulkerBulletRenderState shulkerBulletRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		float f = shulkerBulletRenderState.ageInTicks;
		poseStack.translate(0.0F, 0.15F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(f * 0.1F) * 180.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(Mth.cos(f * 0.1F) * 180.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f * 0.15F) * 360.0F));
		poseStack.scale(-0.5F, -0.5F, 0.5F);
		this.model.setupAnim(shulkerBulletRenderState);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(TEXTURE_LOCATION));
		this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
		poseStack.scale(1.5F, 1.5F, 1.5F);
		VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RENDER_TYPE);
		this.model.renderToBuffer(poseStack, vertexConsumer2, i, OverlayTexture.NO_OVERLAY, 654311423);
		poseStack.popPose();
		super.render(shulkerBulletRenderState, poseStack, multiBufferSource, i);
	}

	public ShulkerBulletRenderState createRenderState() {
		return new ShulkerBulletRenderState();
	}

	public void extractRenderState(ShulkerBullet shulkerBullet, ShulkerBulletRenderState shulkerBulletRenderState, float f) {
		super.extractRenderState(shulkerBullet, shulkerBulletRenderState, f);
		shulkerBulletRenderState.yRot = shulkerBullet.getYRot(f);
		shulkerBulletRenderState.xRot = shulkerBullet.getXRot(f);
	}
}
