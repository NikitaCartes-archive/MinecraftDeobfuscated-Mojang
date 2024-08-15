package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ravager;

@Environment(EnvType.CLIENT)
public class RavagerRenderer extends MobRenderer<Ravager, RavagerRenderState, RavagerModel> {
	private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/ravager.png");

	public RavagerRenderer(EntityRendererProvider.Context context) {
		super(context, new RavagerModel(context.bakeLayer(ModelLayers.RAVAGER)), 1.1F);
	}

	public ResourceLocation getTextureLocation(RavagerRenderState ravagerRenderState) {
		return TEXTURE_LOCATION;
	}

	public RavagerRenderState createRenderState() {
		return new RavagerRenderState();
	}

	public void extractRenderState(Ravager ravager, RavagerRenderState ravagerRenderState, float f) {
		super.extractRenderState(ravager, ravagerRenderState, f);
		ravagerRenderState.stunnedTicksRemaining = (float)ravager.getStunnedTick() > 0.0F ? (float)ravager.getStunnedTick() - f : 0.0F;
		ravagerRenderState.attackTicksRemaining = (float)ravager.getAttackTick() > 0.0F ? (float)ravager.getAttackTick() - f : 0.0F;
		if (ravager.getRoarTick() > 0) {
			ravagerRenderState.roarAnimation = ((float)(20 - ravager.getRoarTick()) + f) / 20.0F;
		} else {
			ravagerRenderState.roarAnimation = 0.0F;
		}
	}
}
