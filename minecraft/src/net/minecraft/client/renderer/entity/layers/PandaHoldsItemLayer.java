package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PandaRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class PandaHoldsItemLayer extends RenderLayer<PandaRenderState, PandaModel> {
	private final ItemRenderer itemRenderer;

	public PandaHoldsItemLayer(RenderLayerParent<PandaRenderState, PandaModel> renderLayerParent, ItemRenderer itemRenderer) {
		super(renderLayerParent);
		this.itemRenderer = itemRenderer;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PandaRenderState pandaRenderState, float f, float g) {
		BakedModel bakedModel = pandaRenderState.getMainHandItemModel();
		if (bakedModel != null && pandaRenderState.isSitting && !pandaRenderState.isScared) {
			float h = -0.6F;
			float j = 1.4F;
			if (pandaRenderState.isEating) {
				h -= 0.2F * Mth.sin(pandaRenderState.ageInTicks * 0.6F) + 0.2F;
				j -= 0.09F * Mth.sin(pandaRenderState.ageInTicks * 0.6F);
			}

			poseStack.pushPose();
			poseStack.translate(0.1F, j, h);
			ItemStack itemStack = pandaRenderState.getMainHandItem();
			this.itemRenderer.render(itemStack, ItemDisplayContext.GROUND, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, bakedModel);
			poseStack.popPose();
		}
	}
}
