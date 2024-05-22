package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmadilloModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

@Environment(EnvType.CLIENT)
public class ArmadilloRenderer extends MobRenderer<Armadillo, ArmadilloModel> {
	private static final ResourceLocation ARMADILLO_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/armadillo.png");

	public ArmadilloRenderer(EntityRendererProvider.Context context) {
		super(context, new ArmadilloModel(context.bakeLayer(ModelLayers.ARMADILLO)), 0.4F);
	}

	public ResourceLocation getTextureLocation(Armadillo armadillo) {
		return ARMADILLO_LOCATION;
	}
}
