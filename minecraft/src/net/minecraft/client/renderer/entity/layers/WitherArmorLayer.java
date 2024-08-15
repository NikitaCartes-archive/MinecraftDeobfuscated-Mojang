package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class WitherArmorLayer extends EnergySwirlLayer<WitherRenderState, WitherBossModel> {
	private static final ResourceLocation WITHER_ARMOR_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wither/wither_armor.png");
	private final WitherBossModel model;

	public WitherArmorLayer(RenderLayerParent<WitherRenderState, WitherBossModel> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new WitherBossModel(entityModelSet.bakeLayer(ModelLayers.WITHER_ARMOR));
	}

	protected boolean isPowered(WitherRenderState witherRenderState) {
		return witherRenderState.isPowered;
	}

	@Override
	protected float xOffset(float f) {
		return Mth.cos(f * 0.02F) * 3.0F;
	}

	@Override
	protected ResourceLocation getTextureLocation() {
		return WITHER_ARMOR_LOCATION;
	}

	protected WitherBossModel model() {
		return this.model;
	}
}
