package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface CraftingContainer extends Container, StackedContentsCompatible {
	int getWidth();

	int getHeight();

	List<ItemStack> getItems();
}
