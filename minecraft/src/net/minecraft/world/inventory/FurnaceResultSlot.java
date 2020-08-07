package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class FurnaceResultSlot extends Slot {
	private final Player player;
	private int removeCount;

	public FurnaceResultSlot(Player player, Container container, int i, int j, int k) {
		super(container, i, j, k);
		this.player = player;
	}

	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return false;
	}

	@Override
	public ItemStack remove(int i) {
		if (this.hasItem()) {
			this.removeCount = this.removeCount + Math.min(i, this.getItem().getCount());
		}

		return super.remove(i);
	}

	@Override
	public ItemStack onTake(Player player, ItemStack itemStack) {
		this.checkTakeAchievements(itemStack);
		super.onTake(player, itemStack);
		return itemStack;
	}

	@Override
	protected void onQuickCraft(ItemStack itemStack, int i) {
		this.removeCount += i;
		this.checkTakeAchievements(itemStack);
	}

	@Override
	protected void checkTakeAchievements(ItemStack itemStack) {
		itemStack.onCraftedBy(this.player.level, this.player, this.removeCount);
		if (!this.player.level.isClientSide && this.container instanceof AbstractFurnaceBlockEntity) {
			((AbstractFurnaceBlockEntity)this.container).awardUsedRecipesAndPopExperience(this.player);
		}

		this.removeCount = 0;
	}
}
