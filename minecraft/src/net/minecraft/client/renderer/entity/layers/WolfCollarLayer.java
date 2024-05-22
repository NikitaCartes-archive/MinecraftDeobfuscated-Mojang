package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

@Environment(EnvType.CLIENT)
public class WolfCollarLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
	private static final ResourceLocation WOLF_COLLAR_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_collar.png");

	public WolfCollarLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Wolf wolf, float f, float g, float h, float j, float k, float l) {
		if (wolf.isTame() && !wolf.isInvisible()) {
			int m = wolf.getCollarColor().getTextureDiffuseColor();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(WOLF_COLLAR_LOCATION));
			this.getParentModel().renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, m);
		}
	}
}
