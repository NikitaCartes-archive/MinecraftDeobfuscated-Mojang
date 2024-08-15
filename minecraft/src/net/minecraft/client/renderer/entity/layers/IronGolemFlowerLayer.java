package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public class IronGolemFlowerLayer extends RenderLayer<IronGolemRenderState, IronGolemModel> {
	private final BlockRenderDispatcher blockRenderer;

	public IronGolemFlowerLayer(RenderLayerParent<IronGolemRenderState, IronGolemModel> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher) {
		super(renderLayerParent);
		this.blockRenderer = blockRenderDispatcher;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, IronGolemRenderState ironGolemRenderState, float f, float g) {
		if (ironGolemRenderState.offerFlowerTick != 0) {
			poseStack.pushPose();
			ModelPart modelPart = this.getParentModel().getFlowerHoldingArm();
			modelPart.translateAndRotate(poseStack);
			poseStack.translate(-1.1875F, 1.0625F, -0.9375F);
			poseStack.translate(0.5F, 0.5F, 0.5F);
			float h = 0.5F;
			poseStack.scale(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			poseStack.translate(-0.5F, -0.5F, -0.5F);
			this.blockRenderer.renderSingleBlock(Blocks.POPPY.defaultBlockState(), poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}
}
