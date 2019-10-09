package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ThrownTrident;

@Environment(EnvType.CLIENT)
public class ThrownTridentRenderer extends EntityRenderer<ThrownTrident> {
	public static final ResourceLocation TRIDENT_LOCATION = new ResourceLocation("textures/entity/trident.png");
	private final TridentModel model = new TridentModel();

	public ThrownTridentRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(ThrownTrident thrownTrident, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		poseStack.pushPose();
		poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(h, thrownTrident.yRotO, thrownTrident.yRot) - 90.0F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(h, thrownTrident.xRotO, thrownTrident.xRot) + 90.0F));
		int i = thrownTrident.getLightColor();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(this.getTextureLocation(thrownTrident)));
		this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F);
		poseStack.popPose();
		super.render(thrownTrident, d, e, f, g, h, poseStack, multiBufferSource);
	}

	public ResourceLocation getTextureLocation(ThrownTrident thrownTrident) {
		return TRIDENT_LOCATION;
	}
}
