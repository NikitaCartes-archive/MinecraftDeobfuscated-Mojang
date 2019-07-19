package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface MenuConstructor {
	@Nullable
	AbstractContainerMenu createMenu(int i, Inventory inventory, Player player);
}
