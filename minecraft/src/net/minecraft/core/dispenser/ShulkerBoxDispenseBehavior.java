package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DirectionalPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

public class ShulkerBoxDispenseBehavior extends OptionalDispenseItemBehavior {
	@Override
	protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
		this.setSuccess(false);
		Item item = itemStack.getItem();
		if (item instanceof BlockItem) {
			Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
			BlockPos blockPos = blockSource.getPos().relative(direction);
			Direction direction2 = blockSource.getLevel().isEmptyBlock(blockPos.below()) ? direction : Direction.UP;
			this.setSuccess(
				((BlockItem)item).place(new DirectionalPlaceContext(blockSource.getLevel(), blockPos, direction, itemStack, direction2)) == InteractionResult.SUCCESS
			);
		}

		return itemStack;
	}
}
