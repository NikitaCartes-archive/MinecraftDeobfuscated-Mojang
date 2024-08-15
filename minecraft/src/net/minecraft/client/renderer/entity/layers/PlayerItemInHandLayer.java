package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class PlayerItemInHandLayer<S extends PlayerRenderState, M extends EntityModel<S> & ArmedModel & HeadedModel> extends ItemInHandLayer<S, M> {
	private final ItemRenderer itemRenderer;
	private static final float X_ROT_MIN = (float) (-Math.PI / 6);
	private static final float X_ROT_MAX = (float) (Math.PI / 2);

	public PlayerItemInHandLayer(RenderLayerParent<S, M> renderLayerParent, ItemRenderer itemRenderer) {
		super(renderLayerParent, itemRenderer);
		this.itemRenderer = itemRenderer;
	}

	protected void renderArmWithItem(
		S playerRenderState,
		@Nullable BakedModel bakedModel,
		ItemStack itemStack,
		ItemDisplayContext itemDisplayContext,
		HumanoidArm humanoidArm,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i
	) {
		if (bakedModel != null) {
			InteractionHand interactionHand = humanoidArm == playerRenderState.mainArm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			if (playerRenderState.isUsingItem
				&& playerRenderState.useItemHand == interactionHand
				&& playerRenderState.attackTime < 1.0E-5F
				&& itemStack.is(Items.SPYGLASS)) {
				this.renderArmWithSpyglass(bakedModel, itemStack, humanoidArm, poseStack, multiBufferSource, i);
			} else {
				super.renderArmWithItem(playerRenderState, bakedModel, itemStack, itemDisplayContext, humanoidArm, poseStack, multiBufferSource, i);
			}
		}
	}

	private void renderArmWithSpyglass(
		BakedModel bakedModel, ItemStack itemStack, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int i
	) {
		poseStack.pushPose();
		this.getParentModel().root().translateAndRotate(poseStack);
		ModelPart modelPart = this.getParentModel().getHead();
		float f = modelPart.xRot;
		modelPart.xRot = Mth.clamp(modelPart.xRot, (float) (-Math.PI / 6), (float) (Math.PI / 2));
		modelPart.translateAndRotate(poseStack);
		modelPart.xRot = f;
		CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);
		boolean bl = humanoidArm == HumanoidArm.LEFT;
		poseStack.translate((bl ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);
		this.itemRenderer.render(itemStack, ItemDisplayContext.HEAD, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, bakedModel);
		poseStack.popPose();
	}
}
