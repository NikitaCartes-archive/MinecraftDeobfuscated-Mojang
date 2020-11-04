package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;

@Environment(EnvType.CLIENT)
public class WitchRenderer extends MobRenderer<Witch, WitchModel<Witch>> {
	private static final ResourceLocation WITCH_LOCATION = new ResourceLocation("textures/entity/witch.png");

	public WitchRenderer(EntityRendererProvider.Context context) {
		super(context, new WitchModel<>(context.getLayer(ModelLayers.WITCH)), 0.5F);
		this.addLayer(new WitchItemLayer<>(this));
	}

	public void render(Witch witch, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		this.model.setHoldingItem(!witch.getMainHandItem().isEmpty());
		super.render(witch, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(Witch witch) {
		return WITCH_LOCATION;
	}

	protected void scale(Witch witch, PoseStack poseStack, float f) {
		float g = 0.9375F;
		poseStack.scale(0.9375F, 0.9375F, 0.9375F);
	}
}
