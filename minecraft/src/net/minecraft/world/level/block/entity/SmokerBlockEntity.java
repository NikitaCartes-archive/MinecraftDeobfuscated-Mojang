package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

public class SmokerBlockEntity extends AbstractFurnaceBlockEntity {
	public SmokerBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SMOKER, blockPos, blockState, RecipeType.SMOKING);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.smoker");
	}

	@Override
	protected int getBurnDuration(FuelValues fuelValues, ItemStack itemStack) {
		return super.getBurnDuration(fuelValues, itemStack) / 2;
	}

	@Override
	protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return new SmokerMenu(i, inventory, this, this.dataAccess);
	}
}
