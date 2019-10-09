package net.minecraft.world.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class MerchantMenu extends AbstractContainerMenu {
	private final Merchant trader;
	private final MerchantContainer tradeContainer;
	@Environment(EnvType.CLIENT)
	private int merchantLevel;
	@Environment(EnvType.CLIENT)
	private boolean showProgressBar;
	@Environment(EnvType.CLIENT)
	private boolean canRestock;

	public MerchantMenu(int i, Inventory inventory) {
		this(i, inventory, new ClientSideMerchant(inventory.player));
	}

	public MerchantMenu(int i, Inventory inventory, Merchant merchant) {
		super(MenuType.MERCHANT, i);
		this.trader = merchant;
		this.tradeContainer = new MerchantContainer(merchant);
		this.addSlot(new Slot(this.tradeContainer, 0, 136, 37));
		this.addSlot(new Slot(this.tradeContainer, 1, 162, 37));
		this.addSlot(new MerchantResultSlot(inventory.player, merchant, this.tradeContainer, 2, 220, 37));

		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 9; k++) {
				this.addSlot(new Slot(inventory, k + j * 9 + 9, 108 + k * 18, 84 + j * 18));
			}
		}

		for (int j = 0; j < 9; j++) {
			this.addSlot(new Slot(inventory, j, 108 + j * 18, 142));
		}
	}

	@Environment(EnvType.CLIENT)
	public void setShowProgressBar(boolean bl) {
		this.showProgressBar = bl;
	}

	@Override
	public void slotsChanged(Container container) {
		this.tradeContainer.updateSellItem();
		super.slotsChanged(container);
	}

	public void setSelectionHint(int i) {
		this.tradeContainer.setSelectionHint(i);
	}

	@Override
	public boolean stillValid(Player player) {
		return this.trader.getTradingPlayer() == player;
	}

	@Environment(EnvType.CLIENT)
	public int getTraderXp() {
		return this.trader.getVillagerXp();
	}

	@Environment(EnvType.CLIENT)
	public int getFutureTraderXp() {
		return this.tradeContainer.getFutureXp();
	}

	@Environment(EnvType.CLIENT)
	public void setXp(int i) {
		this.trader.overrideXp(i);
	}

	@Environment(EnvType.CLIENT)
	public int getTraderLevel() {
		return this.merchantLevel;
	}

	@Environment(EnvType.CLIENT)
	public void setMerchantLevel(int i) {
		this.merchantLevel = i;
	}

	@Environment(EnvType.CLIENT)
	public void setCanRestock(boolean bl) {
		this.canRestock = bl;
	}

	@Environment(EnvType.CLIENT)
	public boolean canRestock() {
		return this.canRestock;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
		return false;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = (Slot)this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i == 2) {
				if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
				this.playTradeSound();
			} else if (i != 0 && i != 1) {
				if (i >= 3 && i < 30) {
					if (!this.moveItemStackTo(itemStack2, 30, 39, false)) {
						return ItemStack.EMPTY;
					}
				} else if (i >= 30 && i < 39 && !this.moveItemStackTo(itemStack2, 3, 30, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 3, 39, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemStack2.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemStack2);
		}

		return itemStack;
	}

	private void playTradeSound() {
		if (!this.trader.getLevel().isClientSide) {
			Entity entity = (Entity)this.trader;
			this.trader
				.getLevel()
				.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.trader.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
		}
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.trader.setTradingPlayer(null);
		if (!this.trader.getLevel().isClientSide) {
			if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
				ItemStack itemStack = this.tradeContainer.removeItemNoUpdate(0);
				if (!itemStack.isEmpty()) {
					player.drop(itemStack, false);
				}

				itemStack = this.tradeContainer.removeItemNoUpdate(1);
				if (!itemStack.isEmpty()) {
					player.drop(itemStack, false);
				}
			} else {
				player.inventory.placeItemBackInInventory(player.level, this.tradeContainer.removeItemNoUpdate(0));
				player.inventory.placeItemBackInInventory(player.level, this.tradeContainer.removeItemNoUpdate(1));
			}
		}
	}

	public void tryMoveItems(int i) {
		if (this.getOffers().size() > i) {
			ItemStack itemStack = this.tradeContainer.getItem(0);
			if (!itemStack.isEmpty()) {
				if (!this.moveItemStackTo(itemStack, 3, 39, true)) {
					return;
				}

				this.tradeContainer.setItem(0, itemStack);
			}

			ItemStack itemStack2 = this.tradeContainer.getItem(1);
			if (!itemStack2.isEmpty()) {
				if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
					return;
				}

				this.tradeContainer.setItem(1, itemStack2);
			}

			if (this.tradeContainer.getItem(0).isEmpty() && this.tradeContainer.getItem(1).isEmpty()) {
				ItemStack itemStack3 = ((MerchantOffer)this.getOffers().get(i)).getCostA();
				this.moveFromInventoryToPaymentSlot(0, itemStack3);
				ItemStack itemStack4 = ((MerchantOffer)this.getOffers().get(i)).getCostB();
				this.moveFromInventoryToPaymentSlot(1, itemStack4);
			}
		}
	}

	private void moveFromInventoryToPaymentSlot(int i, ItemStack itemStack) {
		if (!itemStack.isEmpty()) {
			for (int j = 3; j < 39; j++) {
				ItemStack itemStack2 = ((Slot)this.slots.get(j)).getItem();
				if (!itemStack2.isEmpty() && this.isSameItem(itemStack, itemStack2)) {
					ItemStack itemStack3 = this.tradeContainer.getItem(i);
					int k = itemStack3.isEmpty() ? 0 : itemStack3.getCount();
					int l = Math.min(itemStack.getMaxStackSize() - k, itemStack2.getCount());
					ItemStack itemStack4 = itemStack2.copy();
					int m = k + l;
					itemStack2.shrink(l);
					itemStack4.setCount(m);
					this.tradeContainer.setItem(i, itemStack4);
					if (m >= itemStack.getMaxStackSize()) {
						break;
					}
				}
			}
		}
	}

	private boolean isSameItem(ItemStack itemStack, ItemStack itemStack2) {
		return itemStack.getItem() == itemStack2.getItem() && ItemStack.tagMatches(itemStack, itemStack2);
	}

	@Environment(EnvType.CLIENT)
	public void setOffers(MerchantOffers merchantOffers) {
		this.trader.overrideOffers(merchantOffers);
	}

	public MerchantOffers getOffers() {
		return this.trader.getOffers();
	}

	@Environment(EnvType.CLIENT)
	public boolean showProgressBar() {
		return this.showProgressBar;
	}
}
