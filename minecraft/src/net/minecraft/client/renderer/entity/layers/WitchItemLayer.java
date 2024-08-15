package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class WitchItemLayer extends CrossedArmsItemLayer<WitchRenderState, WitchModel> {
	public WitchItemLayer(RenderLayerParent<WitchRenderState, WitchModel> renderLayerParent, ItemRenderer itemRenderer) {
		super(renderLayerParent, itemRenderer);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, WitchRenderState witchRenderState, float f, float g) {
		poseStack.pushPose();
		if (witchRenderState.rightHandItem.is(Items.POTION)) {
			this.getParentModel().root().translateAndRotate(poseStack);
			this.getParentModel().getHead().translateAndRotate(poseStack);
			this.getParentModel().getNose().translateAndRotate(poseStack);
			poseStack.translate(0.0625F, 0.25F, 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(140.0F));
			poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F));
			poseStack.translate(0.0F, -0.4F, 0.4F);
		}

		super.render(poseStack, multiBufferSource, i, witchRenderState, f, g);
		poseStack.popPose();
	}
}
