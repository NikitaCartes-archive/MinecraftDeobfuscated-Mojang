package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class CarriedBlockLayer extends RenderLayer<EnderMan, EndermanModel<EnderMan>> {
	public CarriedBlockLayer(RenderLayerParent<EnderMan, EndermanModel<EnderMan>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, EnderMan enderMan, float f, float g, float h, float j, float k, float l) {
		BlockState blockState = enderMan.getCarriedBlock();
		if (blockState != null) {
			poseStack.pushPose();
			poseStack.translate(0.0, 0.6875, -0.75);
			poseStack.mulPose(Vector3f.XP.rotationDegrees(20.0F));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
			poseStack.translate(0.25, 0.1875, 0.25);
			float m = 0.5F;
			poseStack.scale(-0.5F, -0.5F, 0.5F);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}
}
