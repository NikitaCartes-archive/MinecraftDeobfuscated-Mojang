package net.minecraft.world.inventory;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class BeaconMenu extends AbstractContainerMenu {
	private static final int PAYMENT_SLOT = 0;
	private static final int SLOT_COUNT = 1;
	private static final int DATA_COUNT = 3;
	private static final int INV_SLOT_START = 1;
	private static final int INV_SLOT_END = 28;
	private static final int USE_ROW_SLOT_START = 28;
	private static final int USE_ROW_SLOT_END = 37;
	private static final int NO_EFFECT = 0;
	private final Container beacon = new SimpleContainer(1) {
		@Override
		public boolean canPlaceItem(int i, ItemStack itemStack) {
			return itemStack.is(ItemTags.BEACON_PAYMENT_ITEMS);
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	};
	private final BeaconMenu.PaymentSlot paymentSlot;
	private final ContainerLevelAccess access;
	private final ContainerData beaconData;

	public BeaconMenu(int i, Container container) {
		this(i, container, new SimpleContainerData(3), ContainerLevelAccess.NULL);
	}

	public BeaconMenu(int i, Container container, ContainerData containerData, ContainerLevelAccess containerLevelAccess) {
		super(MenuType.BEACON, i);
		checkContainerDataCount(containerData, 3);
		this.beaconData = containerData;
		this.access = containerLevelAccess;
		this.paymentSlot = new BeaconMenu.PaymentSlot(this.beacon, 0, 136, 110);
		this.addSlot(this.paymentSlot);
		this.addDataSlots(containerData);
		int j = 36;
		int k = 137;

		for (int l = 0; l < 3; l++) {
			for (int m = 0; m < 9; m++) {
				this.addSlot(new Slot(container, m + l * 9 + 9, 36 + m * 18, 137 + l * 18));
			}
		}

		for (int l = 0; l < 9; l++) {
			this.addSlot(new Slot(container, l, 36 + l * 18, 195));
		}
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (!player.level().isClientSide) {
			ItemStack itemStack = this.paymentSlot.remove(this.paymentSlot.getMaxStackSize());
			if (!itemStack.isEmpty()) {
				player.drop(itemStack, false);
			}
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, Blocks.BEACON);
	}

	@Override
	public void setData(int i, int j) {
		super.setData(i, j);
		this.broadcastChanges();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i == 0) {
				if (!this.moveItemStackTo(itemStack2, 1, 37, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
			} else if (!this.paymentSlot.hasItem() && this.paymentSlot.mayPlace(itemStack2) && itemStack2.getCount() == 1) {
				if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (i >= 1 && i < 28) {
				if (!this.moveItemStackTo(itemStack2, 28, 37, false)) {
					return ItemStack.EMPTY;
				}
			} else if (i >= 28 && i < 37) {
				if (!this.moveItemStackTo(itemStack2, 1, 28, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 1, 37, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
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

	public int getLevels() {
		return this.beaconData.get(0);
	}

	public static int encodeEffect(@Nullable Holder<MobEffect> holder) {
		return holder == null ? 0 : BuiltInRegistries.MOB_EFFECT.asHolderIdMap().getId(holder) + 1;
	}

	@Nullable
	public static Holder<MobEffect> decodeEffect(int i) {
		return i == 0 ? null : BuiltInRegistries.MOB_EFFECT.asHolderIdMap().byId(i - 1);
	}

	@Nullable
	public Holder<MobEffect> getPrimaryEffect() {
		return decodeEffect(this.beaconData.get(1));
	}

	@Nullable
	public Holder<MobEffect> getSecondaryEffect() {
		return decodeEffect(this.beaconData.get(2));
	}

	public void updateEffects(Optional<Holder<MobEffect>> optional, Optional<Holder<MobEffect>> optional2) {
		if (this.paymentSlot.hasItem()) {
			this.beaconData.set(1, encodeEffect((Holder<MobEffect>)optional.orElse(null)));
			this.beaconData.set(2, encodeEffect((Holder<MobEffect>)optional2.orElse(null)));
			this.paymentSlot.remove(1);
			this.access.execute(Level::blockEntityChanged);
		}
	}

	public boolean hasPayment() {
		return !this.beacon.getItem(0).isEmpty();
	}

	class PaymentSlot extends Slot {
		public PaymentSlot(Container container, int i, int j, int k) {
			super(container, i, j, k);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack) {
			return itemStack.is(ItemTags.BEACON_PAYMENT_ITEMS);
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	}
}
