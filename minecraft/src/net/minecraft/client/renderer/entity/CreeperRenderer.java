package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;

@Environment(EnvType.CLIENT)
public class CreeperRenderer extends MobRenderer<Creeper, CreeperRenderState, CreeperModel> {
	private static final ResourceLocation CREEPER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper.png");

	public CreeperRenderer(EntityRendererProvider.Context context) {
		super(context, new CreeperModel(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
		this.addLayer(new CreeperPowerLayer(this, context.getModelSet()));
	}

	protected void scale(CreeperRenderState creeperRenderState, PoseStack poseStack) {
		float f = creeperRenderState.swelling;
		float g = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
		f = Mth.clamp(f, 0.0F, 1.0F);
		f *= f;
		f *= f;
		float h = (1.0F + f * 0.4F) * g;
		float i = (1.0F + f * 0.1F) / g;
		poseStack.scale(h, i, h);
	}

	protected float getWhiteOverlayProgress(CreeperRenderState creeperRenderState) {
		float f = creeperRenderState.swelling;
		return (int)(f * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(f, 0.5F, 1.0F);
	}

	public ResourceLocation getTextureLocation(CreeperRenderState creeperRenderState) {
		return CREEPER_LOCATION;
	}

	public CreeperRenderState createRenderState() {
		return new CreeperRenderState();
	}

	public void extractRenderState(Creeper creeper, CreeperRenderState creeperRenderState, float f) {
		super.extractRenderState(creeper, creeperRenderState, f);
		creeperRenderState.swelling = creeper.getSwelling(f);
		creeperRenderState.isPowered = creeper.isPowered();
	}
}
