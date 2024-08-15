package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AbstractEquineModel;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

@Environment(EnvType.CLIENT)
public class UndeadHorseRenderer extends AbstractHorseRenderer<AbstractHorse, EquineRenderState, AbstractEquineModel<EquineRenderState>> {
	private static final ResourceLocation ZOMBIE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_zombie.png");
	private static final ResourceLocation SKELETON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_skeleton.png");
	private final ResourceLocation texture;

	public UndeadHorseRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, boolean bl) {
		super(context, new HorseModel(context.bakeLayer(modelLayerLocation)), new HorseModel(context.bakeLayer(modelLayerLocation2)), 1.0F);
		this.texture = bl ? SKELETON_TEXTURE : ZOMBIE_TEXTURE;
	}

	public ResourceLocation getTextureLocation(EquineRenderState equineRenderState) {
		return this.texture;
	}

	public EquineRenderState createRenderState() {
		return new EquineRenderState();
	}
}
