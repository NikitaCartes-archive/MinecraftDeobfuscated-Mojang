package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ravager;

@Environment(EnvType.CLIENT)
public class RavagerRenderer extends MobRenderer<Ravager, RavagerModel> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/ravager.png");

	public RavagerRenderer(EntityRendererProvider.Context context) {
		super(context, new RavagerModel(context.bakeLayer(ModelLayers.RAVAGER)), 1.1F);
	}

	public ResourceLocation getTextureLocation(Ravager ravager) {
		return TEXTURE_LOCATION;
	}
}
