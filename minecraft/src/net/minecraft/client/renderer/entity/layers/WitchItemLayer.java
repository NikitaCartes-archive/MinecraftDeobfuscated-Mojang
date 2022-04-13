package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class WitchItemLayer<T extends LivingEntity> extends CrossedArmsItemLayer<T, WitchModel<T>> {
	public WitchItemLayer(RenderLayerParent<T, WitchModel<T>> renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
		super(renderLayerParent, itemInHandRenderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		ItemStack itemStack = livingEntity.getMainHandItem();
		poseStack.pushPose();
		if (itemStack.is(Items.POTION)) {
			this.getParentModel().getHead().translateAndRotate(poseStack);
			this.getParentModel().getNose().translateAndRotate(poseStack);
			poseStack.translate(0.0625, 0.25, 0.0);
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(140.0F));
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(10.0F));
			poseStack.translate(0.0, -0.4F, 0.4F);
		}

		super.render(poseStack, multiBufferSource, i, livingEntity, f, g, h, j, k, l);
		poseStack.popPose();
	}
}
