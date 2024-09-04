package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.animal.Wolf;

@Environment(EnvType.CLIENT)
public class WolfRenderer extends AgeableMobRenderer<Wolf, WolfRenderState, WolfModel> {
	public WolfRenderer(EntityRendererProvider.Context context) {
		super(context, new WolfModel(context.bakeLayer(ModelLayers.WOLF)), new WolfModel(context.bakeLayer(ModelLayers.WOLF_BABY)), 0.5F);
		this.addLayer(new WolfArmorLayer(this, context.getModelSet(), context.getEquipmentRenderer()));
		this.addLayer(new WolfCollarLayer(this));
	}

	protected int getModelTint(WolfRenderState wolfRenderState) {
		float f = wolfRenderState.wetShade;
		return f == 1.0F ? -1 : ARGB.colorFromFloat(1.0F, f, f, f);
	}

	public ResourceLocation getTextureLocation(WolfRenderState wolfRenderState) {
		return wolfRenderState.texture;
	}

	public WolfRenderState createRenderState() {
		return new WolfRenderState();
	}

	public void extractRenderState(Wolf wolf, WolfRenderState wolfRenderState, float f) {
		super.extractRenderState(wolf, wolfRenderState, f);
		wolfRenderState.isAngry = wolf.isAngry();
		wolfRenderState.isSitting = wolf.isInSittingPose();
		wolfRenderState.tailAngle = wolf.getTailAngle();
		wolfRenderState.headRollAngle = wolf.getHeadRollAngle(f);
		wolfRenderState.shakeAnim = wolf.getShakeAnim(f);
		wolfRenderState.texture = wolf.getTexture();
		wolfRenderState.wetShade = wolf.getWetShade(f);
		wolfRenderState.collarColor = wolf.isTame() ? wolf.getCollarColor() : null;
		wolfRenderState.bodyArmorItem = wolf.getBodyArmorItem().copy();
	}
}
