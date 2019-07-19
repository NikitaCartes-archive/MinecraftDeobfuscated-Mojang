package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeType;

public class SmokerMenu extends AbstractFurnaceMenu {
	public SmokerMenu(int i, Inventory inventory) {
		super(MenuType.SMOKER, RecipeType.SMOKING, i, inventory);
	}

	public SmokerMenu(int i, Inventory inventory, Container container, ContainerData containerData) {
		super(MenuType.SMOKER, RecipeType.SMOKING, i, inventory, container, containerData);
	}
}
