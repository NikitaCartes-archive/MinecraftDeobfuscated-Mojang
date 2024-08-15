package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Bee;

@Environment(EnvType.CLIENT)
public class BeeRenderer extends AgeableMobRenderer<Bee, BeeRenderState, BeeModel> {
	private static final ResourceLocation ANGRY_BEE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_angry.png");
	private static final ResourceLocation ANGRY_NECTAR_BEE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_angry_nectar.png");
	private static final ResourceLocation BEE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee.png");
	private static final ResourceLocation NECTAR_BEE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_nectar.png");

	public BeeRenderer(EntityRendererProvider.Context context) {
		super(context, new BeeModel(context.bakeLayer(ModelLayers.BEE)), new BeeModel(context.bakeLayer(ModelLayers.BEE_BABY)), 0.4F);
	}

	public ResourceLocation getTextureLocation(BeeRenderState beeRenderState) {
		if (beeRenderState.isAngry) {
			return beeRenderState.hasNectar ? ANGRY_NECTAR_BEE_TEXTURE : ANGRY_BEE_TEXTURE;
		} else {
			return beeRenderState.hasNectar ? NECTAR_BEE_TEXTURE : BEE_TEXTURE;
		}
	}

	public BeeRenderState createRenderState() {
		return new BeeRenderState();
	}

	public void extractRenderState(Bee bee, BeeRenderState beeRenderState, float f) {
		super.extractRenderState(bee, beeRenderState, f);
		beeRenderState.rollAmount = bee.getRollAmount(f);
		beeRenderState.hasStinger = !bee.hasStung();
		beeRenderState.isOnGround = bee.onGround() && bee.getDeltaMovement().lengthSqr() < 1.0E-7;
		beeRenderState.isAngry = bee.isAngry();
		beeRenderState.hasNectar = bee.hasNectar();
	}
}
