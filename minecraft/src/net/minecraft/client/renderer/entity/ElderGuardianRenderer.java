package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ElderGuardianRenderer extends GuardianRenderer {
	public static final ResourceLocation GUARDIAN_ELDER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/guardian_elder.png");

	public ElderGuardianRenderer(EntityRendererProvider.Context context) {
		super(context, 1.2F, ModelLayers.ELDER_GUARDIAN);
	}

	@Override
	public ResourceLocation getTextureLocation(GuardianRenderState guardianRenderState) {
		return GUARDIAN_ELDER_LOCATION;
	}
}
