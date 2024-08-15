package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ghast;

@Environment(EnvType.CLIENT)
public class GhastRenderer extends MobRenderer<Ghast, GhastRenderState, GhastModel> {
	private static final ResourceLocation GHAST_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/ghast/ghast.png");
	private static final ResourceLocation GHAST_SHOOTING_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/ghast/ghast_shooting.png");

	public GhastRenderer(EntityRendererProvider.Context context) {
		super(context, new GhastModel(context.bakeLayer(ModelLayers.GHAST)), 1.5F);
	}

	public ResourceLocation getTextureLocation(GhastRenderState ghastRenderState) {
		return ghastRenderState.isCharging ? GHAST_SHOOTING_LOCATION : GHAST_LOCATION;
	}

	public GhastRenderState createRenderState() {
		return new GhastRenderState();
	}

	public void extractRenderState(Ghast ghast, GhastRenderState ghastRenderState, float f) {
		super.extractRenderState(ghast, ghastRenderState, f);
		ghastRenderState.isCharging = ghast.isCharging();
	}
}
