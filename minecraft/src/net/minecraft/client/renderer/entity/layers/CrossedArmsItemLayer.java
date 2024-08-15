package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class CrossedArmsItemLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
	private final ItemRenderer itemRenderer;

	public CrossedArmsItemLayer(RenderLayerParent<S, M> renderLayerParent, ItemRenderer itemRenderer) {
		super(renderLayerParent);
		this.itemRenderer = itemRenderer;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S livingEntityRenderState, float f, float g) {
		BakedModel bakedModel = livingEntityRenderState.getMainHandItemModel();
		if (bakedModel != null) {
			poseStack.pushPose();
			poseStack.translate(0.0F, 0.4F, -0.4F);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			ItemStack itemStack = livingEntityRenderState.getMainHandItem();
			this.itemRenderer.render(itemStack, ItemDisplayContext.GROUND, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, bakedModel);
			poseStack.popPose();
		}
	}
}
