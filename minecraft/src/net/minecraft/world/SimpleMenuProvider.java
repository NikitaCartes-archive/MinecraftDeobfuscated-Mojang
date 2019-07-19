package net.minecraft.world;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;

public final class SimpleMenuProvider implements MenuProvider {
	private final Component title;
	private final MenuConstructor menuConstructor;

	public SimpleMenuProvider(MenuConstructor menuConstructor, Component component) {
		this.menuConstructor = menuConstructor;
		this.title = component;
	}

	@Override
	public Component getDisplayName() {
		return this.title;
	}

	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		return this.menuConstructor.createMenu(i, inventory, player);
	}
}
