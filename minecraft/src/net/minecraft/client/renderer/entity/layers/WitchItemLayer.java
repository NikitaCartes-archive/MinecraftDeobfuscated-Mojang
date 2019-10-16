package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class WitchItemLayer<T extends LivingEntity> extends RenderLayer<T, WitchModel<T>> {
	public WitchItemLayer(RenderLayerParent<T, WitchModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m
	) {
		ItemStack itemStack = livingEntity.getMainHandItem();
		if (itemStack.getItem() == Items.POTION) {
			poseStack.pushPose();
			this.getParentModel().getHead().translateAndRotate(poseStack, 0.0625F);
			this.getParentModel().getNose().translateAndRotate(poseStack, 0.0625F);
			poseStack.translate(0.0, 0.375, -0.03125);
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(-35.0F));
			Minecraft.getInstance()
				.getItemInHandRenderer()
				.renderItem(livingEntity, itemStack, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false, poseStack, multiBufferSource);
			poseStack.popPose();
		}
	}
}
