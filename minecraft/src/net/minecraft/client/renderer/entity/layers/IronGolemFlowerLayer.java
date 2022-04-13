package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public class IronGolemFlowerLayer extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
	private final BlockRenderDispatcher blockRenderer;

	public IronGolemFlowerLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher) {
		super(renderLayerParent);
		this.blockRenderer = blockRenderDispatcher;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, IronGolem ironGolem, float f, float g, float h, float j, float k, float l) {
		if (ironGolem.getOfferFlowerTick() != 0) {
			poseStack.pushPose();
			ModelPart modelPart = this.getParentModel().getFlowerHoldingArm();
			modelPart.translateAndRotate(poseStack);
			poseStack.translate(-1.1875, 1.0625, -0.9375);
			poseStack.translate(0.5, 0.5, 0.5);
			float m = 0.5F;
			poseStack.scale(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
			poseStack.translate(-0.5, -0.5, -0.5);
			this.blockRenderer.renderSingleBlock(Blocks.POPPY.defaultBlockState(), poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}
}
