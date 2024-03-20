package net.minecraft.world.inventory;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class GrindstoneMenu extends AbstractContainerMenu {
	public static final int MAX_NAME_LENGTH = 35;
	public static final int INPUT_SLOT = 0;
	public static final int ADDITIONAL_SLOT = 1;
	public static final int RESULT_SLOT = 2;
	private static final int INV_SLOT_START = 3;
	private static final int INV_SLOT_END = 30;
	private static final int USE_ROW_SLOT_START = 30;
	private static final int USE_ROW_SLOT_END = 39;
	private final Container resultSlots = new ResultContainer();
	final Container repairSlots = new SimpleContainer(2) {
		@Override
		public void setChanged() {
			super.setChanged();
			GrindstoneMenu.this.slotsChanged(this);
		}
	};
	private final ContainerLevelAccess access;

	public GrindstoneMenu(int i, Inventory inventory) {
		this(i, inventory, ContainerLevelAccess.NULL);
	}

	public GrindstoneMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(MenuType.GRINDSTONE, i);
		this.access = containerLevelAccess;
		this.addSlot(new Slot(this.repairSlots, 0, 49, 19) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return itemStack.isDamageableItem() || EnchantmentHelper.hasAnyEnchantments(itemStack);
			}
		});
		this.addSlot(new Slot(this.repairSlots, 1, 49, 40) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return itemStack.isDamageableItem() || EnchantmentHelper.hasAnyEnchantments(itemStack);
			}
		});
		this.addSlot(new Slot(this.resultSlots, 2, 129, 34) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return false;
			}

			@Override
			public void onTake(Player player, ItemStack itemStack) {
				containerLevelAccess.execute((level, blockPos) -> {
					if (level instanceof ServerLevel) {
						ExperienceOrb.award((ServerLevel)level, Vec3.atCenterOf(blockPos), this.getExperienceAmount(level));
					}

					level.levelEvent(1042, blockPos, 0);
				});
				GrindstoneMenu.this.repairSlots.setItem(0, ItemStack.EMPTY);
				GrindstoneMenu.this.repairSlots.setItem(1, ItemStack.EMPTY);
			}

			private int getExperienceAmount(Level level) {
				int i = 0;
				i += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(0));
				i += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(1));
				if (i > 0) {
					int j = (int)Math.ceil((double)i / 2.0);
					return j + level.random.nextInt(j);
				} else {
					return 0;
				}
			}

			private int getExperienceFromItem(ItemStack itemStack) {
				int i = 0;
				ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);

				for (Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
					Enchantment enchantment = (Enchantment)((Holder)entry.getKey()).value();
					int j = entry.getIntValue();
					if (!enchantment.isCurse()) {
						i += enchantment.getMinCost(j);
					}
				}

				return i;
			}
		});

		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 9; k++) {
				this.addSlot(new Slot(inventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
			}
		}

		for (int j = 0; j < 9; j++) {
			this.addSlot(new Slot(inventory, j, 8 + j * 18, 142));
		}
	}

	@Override
	public void slotsChanged(Container container) {
		super.slotsChanged(container);
		if (container == this.repairSlots) {
			this.createResult();
		}
	}

	private void createResult() {
		this.resultSlots.setItem(0, this.computeResult(this.repairSlots.getItem(0), this.repairSlots.getItem(1)));
		this.broadcastChanges();
	}

	private ItemStack computeResult(ItemStack itemStack, ItemStack itemStack2) {
		boolean bl = !itemStack.isEmpty() || !itemStack2.isEmpty();
		if (!bl) {
			return ItemStack.EMPTY;
		} else if (itemStack.getCount() <= 1 && itemStack2.getCount() <= 1) {
			boolean bl2 = !itemStack.isEmpty() && !itemStack2.isEmpty();
			if (!bl2) {
				ItemStack itemStack3 = !itemStack.isEmpty() ? itemStack : itemStack2;
				return !EnchantmentHelper.hasAnyEnchantments(itemStack3) ? ItemStack.EMPTY : this.removeNonCursesFrom(itemStack3.copy());
			} else {
				return this.mergeItems(itemStack, itemStack2);
			}
		} else {
			return ItemStack.EMPTY;
		}
	}

	private ItemStack mergeItems(ItemStack itemStack, ItemStack itemStack2) {
		if (!itemStack.is(itemStack2.getItem())) {
			return ItemStack.EMPTY;
		} else {
			int i = Math.max(itemStack.getMaxDamage(), itemStack2.getMaxDamage());
			int j = itemStack.getMaxDamage() - itemStack.getDamageValue();
			int k = itemStack2.getMaxDamage() - itemStack2.getDamageValue();
			int l = j + k + i * 5 / 100;
			int m = 1;
			if (!itemStack.isDamageableItem()) {
				if (itemStack.getMaxStackSize() < 2 || !ItemStack.matches(itemStack, itemStack2)) {
					return ItemStack.EMPTY;
				}

				m = 2;
			}

			ItemStack itemStack3 = itemStack.copyWithCount(m);
			if (itemStack3.isDamageableItem()) {
				itemStack3.set(DataComponents.MAX_DAMAGE, i);
				itemStack3.setDamageValue(Math.max(i - l, 0));
			}

			this.mergeEnchantsFrom(itemStack3, itemStack2);
			return this.removeNonCursesFrom(itemStack3);
		}
	}

	private void mergeEnchantsFrom(ItemStack itemStack, ItemStack itemStack2) {
		EnchantmentHelper.updateEnchantments(itemStack, mutable -> {
			ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack2);

			for (Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
				Enchantment enchantment = (Enchantment)((Holder)entry.getKey()).value();
				if (!enchantment.isCurse() || mutable.getLevel(enchantment) == 0) {
					mutable.upgrade(enchantment, entry.getIntValue());
				}
			}
		});
	}

	private ItemStack removeNonCursesFrom(ItemStack itemStack) {
		ItemEnchantments itemEnchantments = EnchantmentHelper.updateEnchantments(
			itemStack, mutable -> mutable.removeIf(holder -> !((Enchantment)holder.value()).isCurse())
		);
		if (itemStack.is(Items.ENCHANTED_BOOK) && itemEnchantments.isEmpty()) {
			itemStack = itemStack.transmuteCopy(Items.BOOK, itemStack.getCount());
		}

		int i = 0;

		for (int j = 0; j < itemEnchantments.size(); j++) {
			i = AnvilMenu.calculateIncreasedRepairCost(i);
		}

		itemStack.set(DataComponents.REPAIR_COST, i);
		return itemStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.access.execute((level, blockPos) -> this.clearContainer(player, this.repairSlots));
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, Blocks.GRINDSTONE);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			ItemStack itemStack3 = this.repairSlots.getItem(0);
			ItemStack itemStack4 = this.repairSlots.getItem(1);
			if (i == 2) {
				if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
			} else if (i != 0 && i != 1) {
				if (!itemStack3.isEmpty() && !itemStack4.isEmpty()) {
					if (i >= 3 && i < 30) {
						if (!this.moveItemStackTo(itemStack2, 30, 39, false)) {
							return ItemStack.EMPTY;
						}
					} else if (i >= 30 && i < 39 && !this.moveItemStackTo(itemStack2, 3, 30, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.moveItemStackTo(itemStack2, 0, 2, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 3, 39, false)) {
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
}
