package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public class IronGolemFlowerLayer extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
	public IronGolemFlowerLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, IronGolem ironGolem, float f, float g, float h, float j, float k, float l, float m
	) {
		if (ironGolem.getOfferFlowerTick() != 0) {
			poseStack.pushPose();
			poseStack.scale(-1.0F, -1.0F, 1.0F);
			poseStack.mulPose(Vector3f.XP.rotationDegrees(5.0F + 180.0F * this.getParentModel().getFlowerHoldingArm().xRot / (float) Math.PI));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
			poseStack.translate(0.6875, -0.3125, 1.0625);
			float n = 0.5F;
			poseStack.scale(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
			poseStack.translate(-0.5, -0.5, 0.5);
			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.POPPY.defaultBlockState(), poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}
}
