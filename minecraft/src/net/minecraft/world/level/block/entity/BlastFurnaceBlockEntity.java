package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

public class BlastFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
	public BlastFurnaceBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.BLAST_FURNACE, blockPos, blockState, RecipeType.BLASTING);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.blast_furnace");
	}

	@Override
	protected int getBurnDuration(FuelValues fuelValues, ItemStack itemStack) {
		return super.getBurnDuration(fuelValues, itemStack) / 2;
	}

	@Override
	protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return new BlastFurnaceMenu(i, inventory, this, this.dataAccess);
	}
}
