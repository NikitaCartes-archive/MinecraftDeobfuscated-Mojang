package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemInHandLayer<S extends LivingEntityRenderState, M extends EntityModel<S> & ArmedModel> extends RenderLayer<S, M> {
	private final ItemRenderer itemRenderer;

	public ItemInHandLayer(RenderLayerParent<S, M> renderLayerParent, ItemRenderer itemRenderer) {
		super(renderLayerParent);
		this.itemRenderer = itemRenderer;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S livingEntityRenderState, float f, float g) {
		this.renderArmWithItem(
			livingEntityRenderState,
			livingEntityRenderState.rightHandItemModel,
			livingEntityRenderState.rightHandItem,
			ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
			HumanoidArm.RIGHT,
			poseStack,
			multiBufferSource,
			i
		);
		this.renderArmWithItem(
			livingEntityRenderState,
			livingEntityRenderState.leftHandItemModel,
			livingEntityRenderState.leftHandItem,
			ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
			HumanoidArm.LEFT,
			poseStack,
			multiBufferSource,
			i
		);
	}

	protected void renderArmWithItem(
		S livingEntityRenderState,
		@Nullable BakedModel bakedModel,
		ItemStack itemStack,
		ItemDisplayContext itemDisplayContext,
		HumanoidArm humanoidArm,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i
	) {
		if (bakedModel != null && !itemStack.isEmpty()) {
			poseStack.pushPose();
			this.getParentModel().translateToHand(humanoidArm, poseStack);
			poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			boolean bl = humanoidArm == HumanoidArm.LEFT;
			poseStack.translate((float)(bl ? -1 : 1) / 16.0F, 0.125F, -0.625F);
			this.itemRenderer.render(itemStack, itemDisplayContext, bl, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, bakedModel);
			poseStack.popPose();
		}
	}
}
