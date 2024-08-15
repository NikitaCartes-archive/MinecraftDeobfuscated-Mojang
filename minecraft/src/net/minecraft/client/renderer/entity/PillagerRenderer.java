package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Pillager;

@Environment(EnvType.CLIENT)
public class PillagerRenderer extends IllagerRenderer<Pillager, IllagerRenderState> {
	private static final ResourceLocation PILLAGER = ResourceLocation.withDefaultNamespace("textures/entity/illager/pillager.png");

	public PillagerRenderer(EntityRendererProvider.Context context) {
		super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.PILLAGER)), 0.5F);
		this.addLayer(new ItemInHandLayer<>(this, context.getItemRenderer()));
	}

	public ResourceLocation getTextureLocation(IllagerRenderState illagerRenderState) {
		return PILLAGER;
	}

	public IllagerRenderState createRenderState() {
		return new IllagerRenderState();
	}
}
