package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

@Environment(EnvType.CLIENT)
public class CampfireRenderer extends BlockEntityRenderer<CampfireBlockEntity> {
	public void render(CampfireBlockEntity campfireBlockEntity, double d, double e, double f, float g, int i, RenderType renderType) {
		Direction direction = campfireBlockEntity.getBlockState().getValue(CampfireBlock.FACING);
		NonNullList<ItemStack> nonNullList = campfireBlockEntity.getItems();

		for (int j = 0; j < nonNullList.size(); j++) {
			ItemStack itemStack = nonNullList.get(j);
			if (itemStack != ItemStack.EMPTY) {
				RenderSystem.pushMatrix();
				RenderSystem.translatef((float)d + 0.5F, (float)e + 0.44921875F, (float)f + 0.5F);
				Direction direction2 = Direction.from2DDataValue((j + direction.get2DDataValue()) % 4);
				RenderSystem.rotatef(-direction2.toYRot(), 0.0F, 1.0F, 0.0F);
				RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				RenderSystem.translatef(-0.3125F, -0.3125F, 0.0F);
				RenderSystem.scalef(0.375F, 0.375F, 0.375F);
				Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemTransforms.TransformType.FIXED);
				RenderSystem.popMatrix();
			}
		}
	}
}
