package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Bee;

@Environment(EnvType.CLIENT)
public class BeeRenderer extends MobRenderer<Bee, BeeModel<Bee>> {
	private static final ResourceLocation ANGRY_BEE_TEXTURE = new ResourceLocation("textures/entity/bee/bee_angry.png");
	private static final ResourceLocation ANGRY_NECTAR_BEE_TEXTURE = new ResourceLocation("textures/entity/bee/bee_angry_nectar.png");
	private static final ResourceLocation BEE_TEXTURE = new ResourceLocation("textures/entity/bee/bee.png");
	private static final ResourceLocation NECTAR_BEE_TEXTURE = new ResourceLocation("textures/entity/bee/bee_nectar.png");

	public BeeRenderer(EntityRendererProvider.Context context) {
		super(context, new BeeModel<>(context.bakeLayer(ModelLayers.BEE)), 0.4F);
	}

	public ResourceLocation getTextureLocation(Bee bee) {
		if (bee.isAngry()) {
			return bee.hasNectar() ? ANGRY_NECTAR_BEE_TEXTURE : ANGRY_BEE_TEXTURE;
		} else {
			return bee.hasNectar() ? NECTAR_BEE_TEXTURE : BEE_TEXTURE;
		}
	}
}
