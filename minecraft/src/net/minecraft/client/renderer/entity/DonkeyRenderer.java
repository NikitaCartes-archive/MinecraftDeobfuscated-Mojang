package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DonkeyModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.state.DonkeyRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

@Environment(EnvType.CLIENT)
public class DonkeyRenderer<T extends AbstractChestedHorse> extends AbstractHorseRenderer<T, DonkeyRenderState, DonkeyModel> {
	public static final ResourceLocation DONKEY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/horse/donkey.png");
	public static final ResourceLocation MULE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/horse/mule.png");
	private final ResourceLocation texture;

	public DonkeyRenderer(
		EntityRendererProvider.Context context, float f, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, boolean bl
	) {
		super(context, new DonkeyModel(context.bakeLayer(modelLayerLocation)), new DonkeyModel(context.bakeLayer(modelLayerLocation2)), f);
		this.texture = bl ? MULE_TEXTURE : DONKEY_TEXTURE;
	}

	public ResourceLocation getTextureLocation(DonkeyRenderState donkeyRenderState) {
		return this.texture;
	}

	public DonkeyRenderState createRenderState() {
		return new DonkeyRenderState();
	}

	public void extractRenderState(T abstractChestedHorse, DonkeyRenderState donkeyRenderState, float f) {
		super.extractRenderState(abstractChestedHorse, donkeyRenderState, f);
		donkeyRenderState.hasChest = abstractChestedHorse.hasChest();
	}
}
