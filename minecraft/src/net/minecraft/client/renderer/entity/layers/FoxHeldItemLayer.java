package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.FoxRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class FoxHeldItemLayer extends RenderLayer<FoxRenderState, FoxModel> {
	private final ItemRenderer itemRenderer;

	public FoxHeldItemLayer(RenderLayerParent<FoxRenderState, FoxModel> renderLayerParent, ItemRenderer itemRenderer) {
		super(renderLayerParent);
		this.itemRenderer = itemRenderer;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, FoxRenderState foxRenderState, float f, float g) {
		BakedModel bakedModel = foxRenderState.getMainHandItemModel();
		ItemStack itemStack = foxRenderState.getMainHandItem();
		if (bakedModel != null && !itemStack.isEmpty()) {
			boolean bl = foxRenderState.isSleeping;
			boolean bl2 = foxRenderState.isBaby;
			poseStack.pushPose();
			poseStack.translate(this.getParentModel().head.x / 16.0F, this.getParentModel().head.y / 16.0F, this.getParentModel().head.z / 16.0F);
			if (bl2) {
				float h = 0.75F;
				poseStack.scale(0.75F, 0.75F, 0.75F);
			}

			poseStack.mulPose(Axis.ZP.rotation(foxRenderState.headRollAngle));
			poseStack.mulPose(Axis.YP.rotationDegrees(f));
			poseStack.mulPose(Axis.XP.rotationDegrees(g));
			if (foxRenderState.isBaby) {
				if (bl) {
					poseStack.translate(0.4F, 0.26F, 0.15F);
				} else {
					poseStack.translate(0.06F, 0.26F, -0.5F);
				}
			} else if (bl) {
				poseStack.translate(0.46F, 0.26F, 0.22F);
			} else {
				poseStack.translate(0.06F, 0.27F, -0.5F);
			}

			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			if (bl) {
				poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
			}

			this.itemRenderer.render(itemStack, ItemDisplayContext.GROUND, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, bakedModel);
			poseStack.popPose();
		}
	}
}
