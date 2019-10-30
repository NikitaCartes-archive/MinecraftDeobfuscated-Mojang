package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class WitchItemLayer<T extends LivingEntity> extends CrossedArmsItemLayer<T, WitchModel<T>> {
	public WitchItemLayer(RenderLayerParent<T, WitchModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	public void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m
	) {
		ItemStack itemStack = livingEntity.getMainHandItem();
		poseStack.pushPose();
		if (itemStack.getItem() == Items.POTION) {
			this.getParentModel().getHead().translateAndRotate(poseStack, 0.0625F);
			this.getParentModel().getNose().translateAndRotate(poseStack, 0.0625F);
			poseStack.translate(0.0, 0.3125, -0.125);
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(165.0F));
			poseStack.translate(0.0, -0.4F, 0.4F);
		}

		super.render(poseStack, multiBufferSource, i, livingEntity, f, g, h, j, k, l, m);
		poseStack.popPose();
	}
}
