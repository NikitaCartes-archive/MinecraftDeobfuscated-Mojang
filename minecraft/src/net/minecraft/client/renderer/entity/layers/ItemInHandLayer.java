package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemInHandLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
	private final ItemInHandRenderer itemInHandRenderer;

	public ItemInHandLayer(RenderLayerParent<T, M> renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
		super(renderLayerParent);
		this.itemInHandRenderer = itemInHandRenderer;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		boolean bl = livingEntity.getMainArm() == HumanoidArm.RIGHT;
		ItemStack itemStack = bl ? livingEntity.getOffhandItem() : livingEntity.getMainHandItem();
		ItemStack itemStack2 = bl ? livingEntity.getMainHandItem() : livingEntity.getOffhandItem();
		if (!itemStack.isEmpty() || !itemStack2.isEmpty()) {
			poseStack.pushPose();
			if (this.getParentModel().young) {
				float m = 0.5F;
				poseStack.translate(0.0F, 0.75F, 0.0F);
				poseStack.scale(0.5F, 0.5F, 0.5F);
			}

			this.renderArmWithItem(livingEntity, itemStack2, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, poseStack, multiBufferSource, i);
			this.renderArmWithItem(livingEntity, itemStack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, poseStack, multiBufferSource, i);
			poseStack.popPose();
		}
	}

	protected void renderArmWithItem(
		LivingEntity livingEntity,
		ItemStack itemStack,
		ItemDisplayContext itemDisplayContext,
		HumanoidArm humanoidArm,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i
	) {
		if (!itemStack.isEmpty()) {
			poseStack.pushPose();
			this.getParentModel().translateToHand(humanoidArm, poseStack);
			poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			boolean bl = humanoidArm == HumanoidArm.LEFT;
			poseStack.translate((float)(bl ? -1 : 1) / 16.0F, 0.125F, -0.625F);
			this.itemInHandRenderer.renderItem(livingEntity, itemStack, itemDisplayContext, bl, poseStack, multiBufferSource, i);
			poseStack.popPose();
		}
	}
}
