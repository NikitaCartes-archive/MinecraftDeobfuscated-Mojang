package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemInHandLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
	public ItemInHandLayer(RenderLayerParent<T, M> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		boolean bl = livingEntity.getMainArm() == HumanoidArm.RIGHT;
		ItemStack itemStack = bl ? livingEntity.getOffhandItem() : livingEntity.getMainHandItem();
		ItemStack itemStack2 = bl ? livingEntity.getMainHandItem() : livingEntity.getOffhandItem();
		if (!itemStack.isEmpty() || !itemStack2.isEmpty()) {
			poseStack.pushPose();
			if (this.getParentModel().young) {
				float m = 0.5F;
				poseStack.translate(0.0, 0.75, 0.0);
				poseStack.scale(0.5F, 0.5F, 0.5F);
			}

			this.renderArmWithItem(livingEntity, itemStack2, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, poseStack, multiBufferSource, i);
			this.renderArmWithItem(livingEntity, itemStack, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, poseStack, multiBufferSource, i);
			poseStack.popPose();
		}
	}

	private void renderArmWithItem(
		LivingEntity livingEntity,
		ItemStack itemStack,
		ItemTransforms.TransformType transformType,
		HumanoidArm humanoidArm,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i
	) {
		if (!itemStack.isEmpty()) {
			poseStack.pushPose();
			this.getParentModel().translateToHand(humanoidArm, poseStack);
			poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
			boolean bl = humanoidArm == HumanoidArm.LEFT;
			poseStack.translate((double)((float)(bl ? -1 : 1) / 16.0F), 0.125, -0.625);
			Minecraft.getInstance().getItemInHandRenderer().renderItem(livingEntity, itemStack, transformType, bl, poseStack, multiBufferSource, i);
			poseStack.popPose();
		}
	}
}
