package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

@Environment(EnvType.CLIENT)
public class CampfireRenderer extends BlockEntityRenderer<CampfireBlockEntity> {
	public CampfireRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(CampfireBlockEntity campfireBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		Direction direction = campfireBlockEntity.getBlockState().getValue(CampfireBlock.FACING);
		NonNullList<ItemStack> nonNullList = campfireBlockEntity.getItems();

		for (int k = 0; k < nonNullList.size(); k++) {
			ItemStack itemStack = nonNullList.get(k);
			if (itemStack != ItemStack.EMPTY) {
				poseStack.pushPose();
				poseStack.translate(0.5, 0.44921875, 0.5);
				Direction direction2 = Direction.from2DDataValue((k + direction.get2DDataValue()) % 4);
				float g = -direction2.toYRot();
				poseStack.mulPose(Vector3f.YP.rotationDegrees(g));
				poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
				poseStack.translate(-0.3125, -0.3125, 0.0);
				poseStack.scale(0.375F, 0.375F, 0.375F);
				Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemTransforms.TransformType.FIXED, i, j, poseStack, multiBufferSource);
				poseStack.popPose();
			}
		}
	}
}
