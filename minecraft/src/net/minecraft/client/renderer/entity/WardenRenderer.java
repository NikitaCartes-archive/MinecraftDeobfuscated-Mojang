package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.WardenEmissiveLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.warden.Warden;

@Environment(EnvType.CLIENT)
public class WardenRenderer extends MobRenderer<Warden, WardenModel<Warden>> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/warden/warden.png");
	private static final ResourceLocation BIOLUMINESCENT_LAYER_TEXTURE = new ResourceLocation("textures/entity/warden/warden_bioluminescent_layer.png");
	private static final ResourceLocation EARS_TEXTURE = new ResourceLocation("textures/entity/warden/warden_ears.png");
	private static final ResourceLocation HEART_TEXTURE = new ResourceLocation("textures/entity/warden/warden_heart.png");
	private static final ResourceLocation PULSATING_SPOTS_TEXTURE_1 = new ResourceLocation("textures/entity/warden/warden_pulsating_spots_1.png");
	private static final ResourceLocation PULSATING_SPOTS_TEXTURE_2 = new ResourceLocation("textures/entity/warden/warden_pulsating_spots_2.png");

	public WardenRenderer(EntityRendererProvider.Context context) {
		super(context, new WardenModel<>(context.bakeLayer(ModelLayers.WARDEN)), 0.5F);
		this.addLayer(new WardenEmissiveLayer<>(this, BIOLUMINESCENT_LAYER_TEXTURE, (warden, f, g) -> 1.0F));
		this.addLayer(new WardenEmissiveLayer<>(this, PULSATING_SPOTS_TEXTURE_1, Warden::getPrimaryPulsatingSpotsAlpha));
		this.addLayer(new WardenEmissiveLayer<>(this, PULSATING_SPOTS_TEXTURE_2, Warden::getSecondaryPulsatingSpotsAlpha));
		this.addLayer(new WardenEmissiveLayer<>(this, EARS_TEXTURE, Warden::getEarEmissiveAlpha));
		this.addLayer(new WardenEmissiveLayer<>(this, HEART_TEXTURE, Warden::getHeartEmissiveAlpha));
	}

	public void render(Warden warden, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		if (warden.tickCount > 2) {
			super.render(warden, f, g, poseStack, multiBufferSource, i);
		}
	}

	public ResourceLocation getTextureLocation(Warden warden) {
		return TEXTURE;
	}
}
