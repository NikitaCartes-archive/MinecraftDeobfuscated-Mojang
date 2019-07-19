package net.minecraft.world.inventory;

import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;

public class MerchantResultSlot extends Slot {
	private final MerchantContainer slots;
	private final Player player;
	private int removeCount;
	private final Merchant merchant;

	public MerchantResultSlot(Player player, Merchant merchant, MerchantContainer merchantContainer, int i, int j, int k) {
		super(merchantContainer, i, j, k);
		this.player = player;
		this.merchant = merchant;
		this.slots = merchantContainer;
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
	protected void onQuickCraft(ItemStack itemStack, int i) {
		this.removeCount += i;
		this.checkTakeAchievements(itemStack);
	}

	@Override
	protected void checkTakeAchievements(ItemStack itemStack) {
		itemStack.onCraftedBy(this.player.level, this.player, this.removeCount);
		this.removeCount = 0;
	}

	@Override
	public ItemStack onTake(Player player, ItemStack itemStack) {
		this.checkTakeAchievements(itemStack);
		MerchantOffer merchantOffer = this.slots.getActiveOffer();
		if (merchantOffer != null) {
			ItemStack itemStack2 = this.slots.getItem(0);
			ItemStack itemStack3 = this.slots.getItem(1);
			if (merchantOffer.take(itemStack2, itemStack3) || merchantOffer.take(itemStack3, itemStack2)) {
				this.merchant.notifyTrade(merchantOffer);
				player.awardStat(Stats.TRADED_WITH_VILLAGER);
				this.slots.setItem(0, itemStack2);
				this.slots.setItem(1, itemStack3);
			}

			this.merchant.overrideXp(this.merchant.getVillagerXp() + merchantOffer.getXp());
		}

		return itemStack;
	}
}
