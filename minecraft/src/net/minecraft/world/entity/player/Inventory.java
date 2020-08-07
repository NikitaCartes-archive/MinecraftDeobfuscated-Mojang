package net.minecraft.world.entity.player;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Inventory implements Container, Nameable {
	public final NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
	public final NonNullList<ItemStack> armor = NonNullList.withSize(4, ItemStack.EMPTY);
	public final NonNullList<ItemStack> offhand = NonNullList.withSize(1, ItemStack.EMPTY);
	private final List<NonNullList<ItemStack>> compartments = ImmutableList.of(this.items, this.armor, this.offhand);
	public int selected;
	public final Player player;
	private ItemStack carried = ItemStack.EMPTY;
	private int timesChanged;

	public Inventory(Player player) {
		this.player = player;
	}

	public ItemStack getSelected() {
		return isHotbarSlot(this.selected) ? this.items.get(this.selected) : ItemStack.EMPTY;
	}

	public static int getSelectionSize() {
		return 9;
	}

	private boolean hasRemainingSpaceForItem(ItemStack itemStack, ItemStack itemStack2) {
		return !itemStack.isEmpty()
			&& this.isSameItem(itemStack, itemStack2)
			&& itemStack.isStackable()
			&& itemStack.getCount() < itemStack.getMaxStackSize()
			&& itemStack.getCount() < this.getMaxStackSize();
	}

	private boolean isSameItem(ItemStack itemStack, ItemStack itemStack2) {
		return itemStack.getItem() == itemStack2.getItem() && ItemStack.tagMatches(itemStack, itemStack2);
	}

	public int getFreeSlot() {
		for (int i = 0; i < this.items.size(); i++) {
			if (this.items.get(i).isEmpty()) {
				return i;
			}
		}

		return -1;
	}

	@Environment(EnvType.CLIENT)
	public void setPickedItem(ItemStack itemStack) {
		int i = this.findSlotMatchingItem(itemStack);
		if (isHotbarSlot(i)) {
			this.selected = i;
		} else {
			if (i == -1) {
				this.selected = this.getSuitableHotbarSlot();
				if (!this.items.get(this.selected).isEmpty()) {
					int j = this.getFreeSlot();
					if (j != -1) {
						this.items.set(j, this.items.get(this.selected));
					}
				}

				this.items.set(this.selected, itemStack);
			} else {
				this.pickSlot(i);
			}
		}
	}

	public void pickSlot(int i) {
		this.selected = this.getSuitableHotbarSlot();
		ItemStack itemStack = this.items.get(this.selected);
		this.items.set(this.selected, this.items.get(i));
		this.items.set(i, itemStack);
	}

	public static boolean isHotbarSlot(int i) {
		return i >= 0 && i < 9;
	}

	@Environment(EnvType.CLIENT)
	public int findSlotMatchingItem(ItemStack itemStack) {
		for (int i = 0; i < this.items.size(); i++) {
			if (!this.items.get(i).isEmpty() && this.isSameItem(itemStack, this.items.get(i))) {
				return i;
			}
		}

		return -1;
	}

	public int findSlotMatchingUnusedItem(ItemStack itemStack) {
		for (int i = 0; i < this.items.size(); i++) {
			ItemStack itemStack2 = this.items.get(i);
			if (!this.items.get(i).isEmpty()
				&& this.isSameItem(itemStack, this.items.get(i))
				&& !this.items.get(i).isDamaged()
				&& !itemStack2.isEnchanted()
				&& !itemStack2.hasCustomHoverName()) {
				return i;
			}
		}

		return -1;
	}

	public int getSuitableHotbarSlot() {
		for (int i = 0; i < 9; i++) {
			int j = (this.selected + i) % 9;
			if (this.items.get(j).isEmpty()) {
				return j;
			}
		}

		for (int ix = 0; ix < 9; ix++) {
			int j = (this.selected + ix) % 9;
			if (!this.items.get(j).isEnchanted()) {
				return j;
			}
		}

		return this.selected;
	}

	@Environment(EnvType.CLIENT)
	public void swapPaint(double d) {
		if (d > 0.0) {
			d = 1.0;
		}

		if (d < 0.0) {
			d = -1.0;
		}

		this.selected = (int)((double)this.selected - d);

		while (this.selected < 0) {
			this.selected += 9;
		}

		while (this.selected >= 9) {
			this.selected -= 9;
		}
	}

	public int clearOrCountMatchingItems(Predicate<ItemStack> predicate, int i, Container container) {
		int j = 0;
		boolean bl = i == 0;
		j += ContainerHelper.clearOrCountMatchingItems(this, predicate, i - j, bl);
		j += ContainerHelper.clearOrCountMatchingItems(container, predicate, i - j, bl);
		j += ContainerHelper.clearOrCountMatchingItems(this.carried, predicate, i - j, bl);
		if (this.carried.isEmpty()) {
			this.carried = ItemStack.EMPTY;
		}

		return j;
	}

	private int addResource(ItemStack itemStack) {
		int i = this.getSlotWithRemainingSpace(itemStack);
		if (i == -1) {
			i = this.getFreeSlot();
		}

		return i == -1 ? itemStack.getCount() : this.addResource(i, itemStack);
	}

	private int addResource(int i, ItemStack itemStack) {
		Item item = itemStack.getItem();
		int j = itemStack.getCount();
		ItemStack itemStack2 = this.getItem(i);
		if (itemStack2.isEmpty()) {
			itemStack2 = new ItemStack(item, 0);
			if (itemStack.hasTag()) {
				itemStack2.setTag(itemStack.getTag().copy());
			}

			this.setItem(i, itemStack2);
		}

		int k = j;
		if (j > itemStack2.getMaxStackSize() - itemStack2.getCount()) {
			k = itemStack2.getMaxStackSize() - itemStack2.getCount();
		}

		if (k > this.getMaxStackSize() - itemStack2.getCount()) {
			k = this.getMaxStackSize() - itemStack2.getCount();
		}

		if (k == 0) {
			return j;
		} else {
			j -= k;
			itemStack2.grow(k);
			itemStack2.setPopTime(5);
			return j;
		}
	}

	public int getSlotWithRemainingSpace(ItemStack itemStack) {
		if (this.hasRemainingSpaceForItem(this.getItem(this.selected), itemStack)) {
			return this.selected;
		} else if (this.hasRemainingSpaceForItem(this.getItem(40), itemStack)) {
			return 40;
		} else {
			for (int i = 0; i < this.items.size(); i++) {
				if (this.hasRemainingSpaceForItem(this.items.get(i), itemStack)) {
					return i;
				}
			}

			return -1;
		}
	}

	public void tick() {
		for (NonNullList<ItemStack> nonNullList : this.compartments) {
			for (int i = 0; i < nonNullList.size(); i++) {
				if (!nonNullList.get(i).isEmpty()) {
					nonNullList.get(i).inventoryTick(this.player.level, this.player, i, this.selected == i);
				}
			}
		}
	}

	public boolean add(ItemStack itemStack) {
		return this.add(-1, itemStack);
	}

	public boolean add(int i, ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return false;
		} else {
			try {
				if (itemStack.isDamaged()) {
					if (i == -1) {
						i = this.getFreeSlot();
					}

					if (i >= 0) {
						this.items.set(i, itemStack.copy());
						this.items.get(i).setPopTime(5);
						itemStack.setCount(0);
						return true;
					} else if (this.player.abilities.instabuild) {
						itemStack.setCount(0);
						return true;
					} else {
						return false;
					}
				} else {
					int j;
					do {
						j = itemStack.getCount();
						if (i == -1) {
							itemStack.setCount(this.addResource(itemStack));
						} else {
							itemStack.setCount(this.addResource(i, itemStack));
						}
					} while (!itemStack.isEmpty() && itemStack.getCount() < j);

					if (itemStack.getCount() == j && this.player.abilities.instabuild) {
						itemStack.setCount(0);
						return true;
					} else {
						return itemStack.getCount() < j;
					}
				}
			} catch (Throwable var6) {
				CrashReport crashReport = CrashReport.forThrowable(var6, "Adding item to inventory");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Item being added");
				crashReportCategory.setDetail("Item ID", Item.getId(itemStack.getItem()));
				crashReportCategory.setDetail("Item data", itemStack.getDamageValue());
				crashReportCategory.setDetail("Item name", (CrashReportDetail<String>)(() -> itemStack.getHoverName().getString()));
				throw new ReportedException(crashReport);
			}
		}
	}

	public void placeItemBackInInventory(Level level, ItemStack itemStack) {
		if (!level.isClientSide) {
			while (!itemStack.isEmpty()) {
				int i = this.getSlotWithRemainingSpace(itemStack);
				if (i == -1) {
					i = this.getFreeSlot();
				}

				if (i == -1) {
					this.player.drop(itemStack, false);
					break;
				}

				int j = itemStack.getMaxStackSize() - this.getItem(i).getCount();
				if (this.add(i, itemStack.split(j))) {
					((ServerPlayer)this.player).connection.send(new ClientboundContainerSetSlotPacket(-2, i, this.getItem(i)));
				}
			}
		}
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		List<ItemStack> list = null;

		for (NonNullList<ItemStack> nonNullList : this.compartments) {
			if (i < nonNullList.size()) {
				list = nonNullList;
				break;
			}

			i -= nonNullList.size();
		}

		return list != null && !((ItemStack)list.get(i)).isEmpty() ? ContainerHelper.removeItem(list, i, j) : ItemStack.EMPTY;
	}

	public void removeItem(ItemStack itemStack) {
		for (NonNullList<ItemStack> nonNullList : this.compartments) {
			for (int i = 0; i < nonNullList.size(); i++) {
				if (nonNullList.get(i) == itemStack) {
					nonNullList.set(i, ItemStack.EMPTY);
					break;
				}
			}
		}
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		NonNullList<ItemStack> nonNullList = null;

		for (NonNullList<ItemStack> nonNullList2 : this.compartments) {
			if (i < nonNullList2.size()) {
				nonNullList = nonNullList2;
				break;
			}

			i -= nonNullList2.size();
		}

		if (nonNullList != null && !nonNullList.get(i).isEmpty()) {
			ItemStack itemStack = nonNullList.get(i);
			nonNullList.set(i, ItemStack.EMPTY);
			return itemStack;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		NonNullList<ItemStack> nonNullList = null;

		for (NonNullList<ItemStack> nonNullList2 : this.compartments) {
			if (i < nonNullList2.size()) {
				nonNullList = nonNullList2;
				break;
			}

			i -= nonNullList2.size();
		}

		if (nonNullList != null) {
			nonNullList.set(i, itemStack);
		}
	}

	public float getDestroySpeed(BlockState blockState) {
		return this.items.get(this.selected).getDestroySpeed(blockState);
	}

	public ListTag save(ListTag listTag) {
		for (int i = 0; i < this.items.size(); i++) {
			if (!this.items.get(i).isEmpty()) {
				CompoundTag compoundTag = new CompoundTag();
				compoundTag.putByte("Slot", (byte)i);
				this.items.get(i).save(compoundTag);
				listTag.add(compoundTag);
			}
		}

		for (int ix = 0; ix < this.armor.size(); ix++) {
			if (!this.armor.get(ix).isEmpty()) {
				CompoundTag compoundTag = new CompoundTag();
				compoundTag.putByte("Slot", (byte)(ix + 100));
				this.armor.get(ix).save(compoundTag);
				listTag.add(compoundTag);
			}
		}

		for (int ixx = 0; ixx < this.offhand.size(); ixx++) {
			if (!this.offhand.get(ixx).isEmpty()) {
				CompoundTag compoundTag = new CompoundTag();
				compoundTag.putByte("Slot", (byte)(ixx + 150));
				this.offhand.get(ixx).save(compoundTag);
				listTag.add(compoundTag);
			}
		}

		return listTag;
	}

	public void load(ListTag listTag) {
		this.items.clear();
		this.armor.clear();
		this.offhand.clear();

		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			int j = compoundTag.getByte("Slot") & 255;
			ItemStack itemStack = ItemStack.of(compoundTag);
			if (!itemStack.isEmpty()) {
				if (j >= 0 && j < this.items.size()) {
					this.items.set(j, itemStack);
				} else if (j >= 100 && j < this.armor.size() + 100) {
					this.armor.set(j - 100, itemStack);
				} else if (j >= 150 && j < this.offhand.size() + 150) {
					this.offhand.set(j - 150, itemStack);
				}
			}
		}
	}

	@Override
	public int getContainerSize() {
		return this.items.size() + this.armor.size() + this.offhand.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemStack : this.items) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}

		for (ItemStack itemStackx : this.armor) {
			if (!itemStackx.isEmpty()) {
				return false;
			}
		}

		for (ItemStack itemStackxx : this.offhand) {
			if (!itemStackxx.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack getItem(int i) {
		List<ItemStack> list = null;

		for (NonNullList<ItemStack> nonNullList : this.compartments) {
			if (i < nonNullList.size()) {
				list = nonNullList;
				break;
			}

			i -= nonNullList.size();
		}

		return list == null ? ItemStack.EMPTY : (ItemStack)list.get(i);
	}

	@Override
	public Component getName() {
		return new TranslatableComponent("container.inventory");
	}

	@Environment(EnvType.CLIENT)
	public ItemStack getArmor(int i) {
		return this.armor.get(i);
	}

	public void hurtArmor(DamageSource damageSource, float f) {
		if (!(f <= 0.0F)) {
			f /= 4.0F;
			if (f < 1.0F) {
				f = 1.0F;
			}

			for (int i = 0; i < this.armor.size(); i++) {
				ItemStack itemStack = this.armor.get(i);
				if ((!damageSource.isFire() || !itemStack.getItem().isFireResistant()) && itemStack.getItem() instanceof ArmorItem) {
					int j = i;
					itemStack.hurtAndBreak((int)f, this.player, player -> player.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, j)));
				}
			}
		}
	}

	public void dropAll() {
		for (List<ItemStack> list : this.compartments) {
			for (int i = 0; i < list.size(); i++) {
				ItemStack itemStack = (ItemStack)list.get(i);
				if (!itemStack.isEmpty()) {
					this.player.drop(itemStack, true, false);
					list.set(i, ItemStack.EMPTY);
				}
			}
		}
	}

	@Override
	public void setChanged() {
		this.timesChanged++;
	}

	@Environment(EnvType.CLIENT)
	public int getTimesChanged() {
		return this.timesChanged;
	}

	public void setCarried(ItemStack itemStack) {
		this.carried = itemStack;
	}

	public ItemStack getCarried() {
		return this.carried;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.player.removed ? false : !(player.distanceToSqr(this.player) > 64.0);
	}

	public boolean contains(ItemStack itemStack) {
		for (List<ItemStack> list : this.compartments) {
			for (ItemStack itemStack2 : list) {
				if (!itemStack2.isEmpty() && itemStack2.sameItem(itemStack)) {
					return true;
				}
			}
		}

		return false;
	}

	@Environment(EnvType.CLIENT)
	public boolean contains(Tag<Item> tag) {
		for (List<ItemStack> list : this.compartments) {
			for (ItemStack itemStack : list) {
				if (!itemStack.isEmpty() && tag.contains(itemStack.getItem())) {
					return true;
				}
			}
		}

		return false;
	}

	public void replaceWith(Inventory inventory) {
		for (int i = 0; i < this.getContainerSize(); i++) {
			this.setItem(i, inventory.getItem(i));
		}

		this.selected = inventory.selected;
	}

	@Override
	public void clearContent() {
		for (List<ItemStack> list : this.compartments) {
			list.clear();
		}
	}

	public void fillStackedContents(StackedContents stackedContents) {
		for (ItemStack itemStack : this.items) {
			stackedContents.accountSimpleStack(itemStack);
		}
	}
}
