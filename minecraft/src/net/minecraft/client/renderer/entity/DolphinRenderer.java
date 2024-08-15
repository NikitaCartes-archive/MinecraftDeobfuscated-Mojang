package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.DolphinCarryingItemLayer;
import net.minecraft.client.renderer.entity.state.DolphinRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Dolphin;

@Environment(EnvType.CLIENT)
public class DolphinRenderer extends AgeableMobRenderer<Dolphin, DolphinRenderState, DolphinModel> {
	private static final ResourceLocation DOLPHIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/dolphin.png");

	public DolphinRenderer(EntityRendererProvider.Context context) {
		super(context, new DolphinModel(context.bakeLayer(ModelLayers.DOLPHIN)), new DolphinModel(context.bakeLayer(ModelLayers.DOLPHIN_BABY)), 0.7F);
		this.addLayer(new DolphinCarryingItemLayer(this, context.getItemRenderer()));
	}

	public ResourceLocation getTextureLocation(DolphinRenderState dolphinRenderState) {
		return DOLPHIN_LOCATION;
	}

	public DolphinRenderState createRenderState() {
		return new DolphinRenderState();
	}

	public void extractRenderState(Dolphin dolphin, DolphinRenderState dolphinRenderState, float f) {
		super.extractRenderState(dolphin, dolphinRenderState, f);
		dolphinRenderState.isMoving = dolphin.getDeltaMovement().horizontalDistanceSqr() > 1.0E-7;
	}
}
