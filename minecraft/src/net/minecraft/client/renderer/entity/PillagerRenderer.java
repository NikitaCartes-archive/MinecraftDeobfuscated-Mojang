package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Pillager;

@Environment(EnvType.CLIENT)
public class PillagerRenderer extends IllagerRenderer<Pillager> {
	private static final ResourceLocation PILLAGER = new ResourceLocation("textures/entity/illager/pillager.png");

	public PillagerRenderer(EntityRendererProvider.Context context) {
		super(context, new IllagerModel<>(context.getLayer(ModelLayers.PILLAGER)), 0.5F);
		this.addLayer(new ItemInHandLayer<>(this));
	}

	public ResourceLocation getTextureLocation(Pillager pillager) {
		return PILLAGER;
	}
}
