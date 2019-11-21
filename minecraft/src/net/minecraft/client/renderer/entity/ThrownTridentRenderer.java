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

	public void render(ThrownTrident thrownTrident, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(g, thrownTrident.yRotO, thrownTrident.yRot) - 90.0F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(g, thrownTrident.xRotO, thrownTrident.xRot) + 90.0F));
		VertexConsumer vertexConsumer = ItemRenderer.getFoilBuffer(
			multiBufferSource, this.model.renderType(this.getTextureLocation(thrownTrident)), false, thrownTrident.isFoil()
		);
		this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		poseStack.popPose();
		super.render(thrownTrident, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(ThrownTrident thrownTrident) {
		return TRIDENT_LOCATION;
	}
}
