package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public abstract class AbstractBoatRenderer extends EntityRenderer<AbstractBoat, BoatRenderState> {
	public AbstractBoatRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.8F;
	}

	public void render(BoatRenderState boatRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.375F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - boatRenderState.yRot));
		float f = boatRenderState.hurtTime;
		if (f > 0.0F) {
			poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(f) * f * boatRenderState.damageTime / 10.0F * (float)boatRenderState.hurtDir));
		}

		if (!Mth.equal(boatRenderState.bubbleAngle, 0.0F)) {
			poseStack.mulPose(new Quaternionf().setAngleAxis(boatRenderState.bubbleAngle * (float) (Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
		}

		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
		EntityModel<BoatRenderState> entityModel = this.model();
		entityModel.setupAnim(boatRenderState);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.renderType());
		entityModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
		this.renderTypeAdditions(boatRenderState, poseStack, multiBufferSource, i);
		poseStack.popPose();
		super.render(boatRenderState, poseStack, multiBufferSource, i);
	}

	protected void renderTypeAdditions(BoatRenderState boatRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
	}

	protected abstract EntityModel<BoatRenderState> model();

	protected abstract RenderType renderType();

	public BoatRenderState createRenderState() {
		return new BoatRenderState();
	}

	public void extractRenderState(AbstractBoat abstractBoat, BoatRenderState boatRenderState, float f) {
		super.extractRenderState(abstractBoat, boatRenderState, f);
		boatRenderState.yRot = abstractBoat.getYRot(f);
		boatRenderState.hurtTime = (float)abstractBoat.getHurtTime() - f;
		boatRenderState.hurtDir = abstractBoat.getHurtDir();
		boatRenderState.damageTime = Math.max(abstractBoat.getDamage() - f, 0.0F);
		boatRenderState.bubbleAngle = abstractBoat.getBubbleAngle(f);
		boatRenderState.isUnderWater = abstractBoat.isUnderWater();
		boatRenderState.rowingTimeLeft = abstractBoat.getRowingTime(0, f);
		boatRenderState.rowingTimeRight = abstractBoat.getRowingTime(1, f);
	}
}
