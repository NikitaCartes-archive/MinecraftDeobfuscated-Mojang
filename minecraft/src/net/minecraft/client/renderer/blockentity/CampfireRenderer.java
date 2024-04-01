package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
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
		boolean bl = campfireBlockEntity.isFryingTable;
		Direction direction = campfireBlockEntity.getBlockState().getValue(CampfireBlock.FACING);
		NonNullList<ItemStack> nonNullList = campfireBlockEntity.getItems();
		int k = (int)campfireBlockEntity.getBlockPos().asLong();
		float g = bl ? -2.5F : -5.0F;
		float h = bl ? 11.0F : 7.0F;

		for (int l = 0; l < nonNullList.size(); l++) {
			ItemStack itemStack = nonNullList.get(l);
			if (itemStack != ItemStack.EMPTY) {
				poseStack.pushPose();
				poseStack.translate(0.5F, (h + 0.1875F) / 16.0F, 0.5F);
				Direction direction2 = Direction.from2DDataValue((l + direction.get2DDataValue()) % 4);
				float m = -direction2.toYRot();
				poseStack.mulPose(Axis.YP.rotationDegrees(m));
				poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
				poseStack.translate(g / 16.0F, g / 16.0F, 0.0F);
				poseStack.scale(0.375F, 0.375F, 0.375F);
				this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, i, j, poseStack, multiBufferSource, campfireBlockEntity.getLevel(), k + l);
				poseStack.popPose();
			}
		}
	}
}
