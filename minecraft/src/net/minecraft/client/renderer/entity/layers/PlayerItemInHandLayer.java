package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class PlayerItemInHandLayer<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {
	private final ItemInHandRenderer itemInHandRenderer;
	private static final float X_ROT_MIN = (float) (-Math.PI / 6);
	private static final float X_ROT_MAX = (float) (Math.PI / 2);

	public PlayerItemInHandLayer(RenderLayerParent<T, M> renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
		super(renderLayerParent, itemInHandRenderer);
		this.itemInHandRenderer = itemInHandRenderer;
	}

	@Override
	protected void renderArmWithItem(
		LivingEntity livingEntity,
		ItemStack itemStack,
		ItemTransforms.TransformType transformType,
		HumanoidArm humanoidArm,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i
	) {
		if (itemStack.is(Items.SPYGLASS) && livingEntity.getUseItem() == itemStack && livingEntity.swingTime == 0) {
			this.renderArmWithSpyglass(livingEntity, itemStack, humanoidArm, poseStack, multiBufferSource, i);
		} else {
			super.renderArmWithItem(livingEntity, itemStack, transformType, humanoidArm, poseStack, multiBufferSource, i);
		}
	}

	private void renderArmWithSpyglass(
		LivingEntity livingEntity, ItemStack itemStack, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int i
	) {
		poseStack.pushPose();
		ModelPart modelPart = this.getParentModel().getHead();
		float f = modelPart.xRot;
		modelPart.xRot = Mth.clamp(modelPart.xRot, (float) (-Math.PI / 6), (float) (Math.PI / 2));
		modelPart.translateAndRotate(poseStack);
		modelPart.xRot = f;
		CustomHeadLayer.translateToHead(poseStack, false);
		boolean bl = humanoidArm == HumanoidArm.LEFT;
		poseStack.translate((double)((bl ? -2.5F : 2.5F) / 16.0F), -0.0625, 0.0);
		this.itemInHandRenderer.renderItem(livingEntity, itemStack, ItemTransforms.TransformType.HEAD, false, poseStack, multiBufferSource, i);
		poseStack.popPose();
	}
}
