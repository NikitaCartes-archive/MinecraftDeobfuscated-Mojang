package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

@Environment(EnvType.CLIENT)
public class CampfireRenderer implements BlockEntityRenderer<CampfireBlockEntity> {
	private static final float SIZE = 0.375F;
	private final ItemRenderer itemRenderer;

	public CampfireRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	public void render(CampfireBlockEntity campfireBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		Direction direction = campfireBlockEntity.getBlockState().getValue(CampfireBlock.FACING);
		NonNullList<ItemStack> nonNullList = campfireBlockEntity.getItems();
		int k = (int)campfireBlockEntity.getBlockPos().asLong();

		for (int l = 0; l < nonNullList.size(); l++) {
			ItemStack itemStack = nonNullList.get(l);
			if (itemStack != ItemStack.EMPTY) {
				poseStack.pushPose();
				poseStack.translate(0.5F, 0.44921875F, 0.5F);
				Direction direction2 = Direction.from2DDataValue((l + direction.get2DDataValue()) % 4);
				float g = -direction2.toYRot();
				poseStack.mulPose(Axis.YP.rotationDegrees(g));
				poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
				poseStack.translate(-0.3125F, -0.3125F, 0.0F);
				poseStack.scale(0.375F, 0.375F, 0.375F);
				this.itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.FIXED, i, j, poseStack, multiBufferSource, campfireBlockEntity.getLevel(), k + l);
				poseStack.popPose();
			}
		}
	}
}
