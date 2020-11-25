package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.WanderingTrader;

@Environment(EnvType.CLIENT)
public class WanderingTraderRenderer extends MobRenderer<WanderingTrader, VillagerModel<WanderingTrader>> {
	private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/wandering_trader.png");

	public WanderingTraderRenderer(EntityRendererProvider.Context context) {
		super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.WANDERING_TRADER)), 0.5F);
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet()));
		this.addLayer(new CrossedArmsItemLayer<>(this));
	}

	public ResourceLocation getTextureLocation(WanderingTrader wanderingTrader) {
		return VILLAGER_BASE_SKIN;
	}

	protected void scale(WanderingTrader wanderingTrader, PoseStack poseStack, float f) {
		float g = 0.9375F;
		poseStack.scale(0.9375F, 0.9375F, 0.9375F);
	}
}
