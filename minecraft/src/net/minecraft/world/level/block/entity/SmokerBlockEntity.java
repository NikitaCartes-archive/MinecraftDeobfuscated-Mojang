package net.minecraft.world.level.block.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class SmokerBlockEntity extends AbstractFurnaceBlockEntity {
	public SmokerBlockEntity() {
		super(BlockEntityType.SMOKER, RecipeType.SMOKING);
	}

	@Override
	protected Component getDefaultName() {
		return new TranslatableComponent("container.smoker");
	}

	@Override
	protected int getBurnDuration(ItemStack itemStack) {
		return super.getBurnDuration(itemStack) / 2;
	}

	@Override
	protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return new SmokerMenu(i, inventory, this, this.dataAccess);
	}
}
