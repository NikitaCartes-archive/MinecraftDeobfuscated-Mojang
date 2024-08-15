package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;

@Environment(EnvType.CLIENT)
public class WitchRenderer extends MobRenderer<Witch, WitchRenderState, WitchModel> {
	private static final ResourceLocation WITCH_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/witch.png");

	public WitchRenderer(EntityRendererProvider.Context context) {
		super(context, new WitchModel(context.bakeLayer(ModelLayers.WITCH)), 0.5F);
		this.addLayer(new WitchItemLayer(this, context.getItemRenderer()));
	}

	public ResourceLocation getTextureLocation(WitchRenderState witchRenderState) {
		return WITCH_LOCATION;
	}

	public WitchRenderState createRenderState() {
		return new WitchRenderState();
	}

	public void extractRenderState(Witch witch, WitchRenderState witchRenderState, float f) {
		super.extractRenderState(witch, witchRenderState, f);
		witchRenderState.entityId = witch.getId();
		witchRenderState.isHoldingItem = !witch.getMainHandItem().isEmpty();
	}
}
