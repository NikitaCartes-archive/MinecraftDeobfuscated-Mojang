package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArrowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.projectile.AbstractArrow;

@Environment(EnvType.CLIENT)
public abstract class ArrowRenderer<T extends AbstractArrow, S extends ArrowRenderState> extends EntityRenderer<T, S> {
	private final ArrowModel model;

	public ArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new ArrowModel(context.bakeLayer(ModelLayers.ARROW));
	}

	public void render(S arrowRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(arrowRenderState.yRot - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(arrowRenderState.xRot));
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(this.getTextureLocation(arrowRenderState)));
		this.model.setupAnim(arrowRenderState);
		this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
		super.render(arrowRenderState, poseStack, multiBufferSource, i);
	}

	public void extractRenderState(T abstractArrow, S arrowRenderState, float f) {
		super.extractRenderState(abstractArrow, arrowRenderState, f);
		arrowRenderState.xRot = abstractArrow.getXRot(f);
		arrowRenderState.yRot = abstractArrow.getYRot(f);
		arrowRenderState.shake = (float)abstractArrow.shakeTime - f;
	}
}
