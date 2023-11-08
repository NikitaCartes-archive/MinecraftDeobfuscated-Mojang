package net.minecraft.world.inventory;

import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class NonInteractiveResultSlot extends Slot {
	public NonInteractiveResultSlot(Container container, int i, int j, int k) {
		super(container, i, j, k);
	}

	@Override
	public void onQuickCraft(ItemStack itemStack, ItemStack itemStack2) {
	}

	@Override
	public boolean mayPickup(Player player) {
		return false;
	}

	@Override
	public Optional<ItemStack> tryRemove(int i, int j, Player player) {
		return Optional.empty();
	}

	@Override
	public ItemStack safeTake(int i, int j, Player player) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack safeInsert(ItemStack itemStack) {
		return itemStack;
	}

	@Override
	public ItemStack safeInsert(ItemStack itemStack, int i) {
		return this.safeInsert(itemStack);
	}

	@Override
	public boolean allowModification(Player player) {
		return false;
	}

	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return false;
	}

	@Override
	public ItemStack remove(int i) {
		return ItemStack.EMPTY;
	}

	@Override
	public void onTake(Player player, ItemStack itemStack) {
	}

	@Override
	public boolean isHighlightable() {
		return false;
	}

	@Override
	public boolean isFake() {
		return true;
	}
}
