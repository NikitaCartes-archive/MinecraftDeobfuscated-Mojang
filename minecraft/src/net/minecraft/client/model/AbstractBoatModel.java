package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractBoatModel extends EntityModel<BoatRenderState> {
	private final ModelPart leftPaddle;
	private final ModelPart rightPaddle;

	public AbstractBoatModel(ModelPart modelPart) {
		super(modelPart);
		this.leftPaddle = modelPart.getChild("left_paddle");
		this.rightPaddle = modelPart.getChild("right_paddle");
	}

	public void setupAnim(BoatRenderState boatRenderState) {
		super.setupAnim(boatRenderState);
		animatePaddle(boatRenderState.rowingTimeLeft, 0, this.leftPaddle);
		animatePaddle(boatRenderState.rowingTimeRight, 1, this.rightPaddle);
	}

	private static void animatePaddle(float f, int i, ModelPart modelPart) {
		modelPart.xRot = Mth.clampedLerp((float) (-Math.PI / 3), (float) (-Math.PI / 12), (Mth.sin(-f) + 1.0F) / 2.0F);
		modelPart.yRot = Mth.clampedLerp((float) (-Math.PI / 4), (float) (Math.PI / 4), (Mth.sin(-f + 1.0F) + 1.0F) / 2.0F);
		if (i == 1) {
			modelPart.yRot = (float) Math.PI - modelPart.yRot;
		}
	}
}
