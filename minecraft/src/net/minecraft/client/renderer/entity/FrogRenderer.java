package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Frog;

@Environment(EnvType.CLIENT)
public class FrogRenderer extends MobRenderer<Frog, FrogModel<Frog>> {
	public FrogRenderer(EntityRendererProvider.Context context) {
		super(context, new FrogModel<>(context.bakeLayer(ModelLayers.FROG)), 0.3F);
	}

	public ResourceLocation getTextureLocation(Frog frog) {
		return frog.getVariant().texture();
	}
}
