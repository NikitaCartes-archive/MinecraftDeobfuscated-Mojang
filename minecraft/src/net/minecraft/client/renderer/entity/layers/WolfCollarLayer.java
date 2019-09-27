package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

@Environment(EnvType.CLIENT)
public class WolfCollarLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
	private static final ResourceLocation WOLF_COLLAR_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_collar.png");

	public WolfCollarLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Wolf wolf, float f, float g, float h, float j, float k, float l, float m) {
		if (wolf.isTame() && !wolf.isInvisible()) {
			float[] fs = wolf.getCollarColor().getTextureDiffuseColors();
			renderColoredModel(this.getParentModel(), WOLF_COLLAR_LOCATION, poseStack, multiBufferSource, i, wolf, fs[0], fs[1], fs[2]);
		}
	}
}
