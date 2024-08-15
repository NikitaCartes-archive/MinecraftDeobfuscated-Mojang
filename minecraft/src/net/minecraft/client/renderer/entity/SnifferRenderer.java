package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SnifferModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.SnifferRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public class SnifferRenderer extends AgeableMobRenderer<Sniffer, SnifferRenderState, SnifferModel> {
	private static final ResourceLocation SNIFFER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/sniffer/sniffer.png");

	public SnifferRenderer(EntityRendererProvider.Context context) {
		super(context, new SnifferModel(context.bakeLayer(ModelLayers.SNIFFER)), new SnifferModel(context.bakeLayer(ModelLayers.SNIFFER_BABY)), 1.1F);
	}

	public ResourceLocation getTextureLocation(SnifferRenderState snifferRenderState) {
		return SNIFFER_LOCATION;
	}

	public SnifferRenderState createRenderState() {
		return new SnifferRenderState();
	}

	public void extractRenderState(Sniffer sniffer, SnifferRenderState snifferRenderState, float f) {
		super.extractRenderState(sniffer, snifferRenderState, f);
		snifferRenderState.isSearching = sniffer.isSearching();
		snifferRenderState.diggingAnimationState.copyFrom(sniffer.diggingAnimationState);
		snifferRenderState.sniffingAnimationState.copyFrom(sniffer.sniffingAnimationState);
		snifferRenderState.risingAnimationState.copyFrom(sniffer.risingAnimationState);
		snifferRenderState.feelingHappyAnimationState.copyFrom(sniffer.feelingHappyAnimationState);
		snifferRenderState.scentingAnimationState.copyFrom(sniffer.scentingAnimationState);
	}

	protected AABB getBoundingBoxForCulling(Sniffer sniffer) {
		return super.getBoundingBoxForCulling(sniffer).inflate(0.6F);
	}
}
