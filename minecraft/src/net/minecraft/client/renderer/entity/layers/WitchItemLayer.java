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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;

@Environment(EnvType.CLIENT)
public class WitchItemLayer<T extends LivingEntity> extends RenderLayer<T, WitchModel<T>> {
	public WitchItemLayer(RenderLayerParent<T, WitchModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m
	) {
		ItemStack itemStack = livingEntity.getMainHandItem();
		if (!itemStack.isEmpty()) {
			poseStack.pushPose();
			if (this.getParentModel().young) {
				poseStack.translate(0.0, 0.625, 0.0);
				poseStack.mulPose(Vector3f.XP.rotation(20.0F, true));
				float n = 0.5F;
				poseStack.scale(0.5F, 0.5F, 0.5F);
			}

			this.getParentModel().getNose().translateAndRotate(poseStack, 0.0625F);
			poseStack.translate(-0.0625, 0.53125, 0.21875);
			Item item = itemStack.getItem();
			if (Block.byItem(item).defaultBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) {
				poseStack.translate(0.0, 0.0625, -0.25);
				poseStack.mulPose(Vector3f.XP.rotation(30.0F, true));
				poseStack.mulPose(Vector3f.YP.rotation(-5.0F, true));
				float o = 0.375F;
				poseStack.scale(0.375F, -0.375F, 0.375F);
			} else if (item == Items.BOW) {
				poseStack.translate(0.0, 0.125, -0.125);
				poseStack.mulPose(Vector3f.YP.rotation(-45.0F, true));
				float o = 0.625F;
				poseStack.scale(0.625F, -0.625F, 0.625F);
				poseStack.mulPose(Vector3f.XP.rotation(-100.0F, true));
				poseStack.mulPose(Vector3f.YP.rotation(-20.0F, true));
			} else {
				poseStack.translate(0.1875, 0.1875, 0.0);
				float o = 0.875F;
				poseStack.scale(0.875F, 0.875F, 0.875F);
				poseStack.mulPose(Vector3f.ZP.rotation(-20.0F, true));
				poseStack.mulPose(Vector3f.XP.rotation(-60.0F, true));
				poseStack.mulPose(Vector3f.ZP.rotation(-30.0F, true));
			}

			poseStack.mulPose(Vector3f.XP.rotation(-15.0F, true));
			poseStack.mulPose(Vector3f.ZP.rotation(40.0F, true));
			Minecraft.getInstance()
				.getItemInHandRenderer()
				.renderItem(livingEntity, itemStack, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false, poseStack, multiBufferSource);
			poseStack.popPose();
		}
	}
}
