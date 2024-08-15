package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;

@Environment(EnvType.CLIENT)
public class CowRenderer extends AgeableMobRenderer<Cow, LivingEntityRenderState, CowModel> {
	private static final ResourceLocation COW_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/cow/cow.png");

	public CowRenderer(EntityRendererProvider.Context context) {
		super(context, new CowModel(context.bakeLayer(ModelLayers.COW)), new CowModel(context.bakeLayer(ModelLayers.COW_BABY)), 0.7F);
	}

	public ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
		return COW_LOCATION;
	}

	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	public void extractRenderState(Cow cow, LivingEntityRenderState livingEntityRenderState, float f) {
		super.extractRenderState(cow, livingEntityRenderState, f);
	}
}
