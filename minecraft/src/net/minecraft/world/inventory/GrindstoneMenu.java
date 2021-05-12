package net.minecraft.world.inventory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
				return itemStack.isDamageableItem() || itemStack.is(Items.ENCHANTED_BOOK) || itemStack.isEnchanted();
			}
		});
		this.addSlot(new Slot(this.repairSlots, 1, 49, 40) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return itemStack.isDamageableItem() || itemStack.is(Items.ENCHANTED_BOOK) || itemStack.isEnchanted();
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
				Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemStack);

				for (Entry<Enchantment, Integer> entry : map.entrySet()) {
					Enchantment enchantment = (Enchantment)entry.getKey();
					Integer integer = (Integer)entry.getValue();
					if (!enchantment.isCurse()) {
						i += enchantment.getMinCost(integer);
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
		ItemStack itemStack = this.repairSlots.getItem(0);
		ItemStack itemStack2 = this.repairSlots.getItem(1);
		boolean bl = !itemStack.isEmpty() || !itemStack2.isEmpty();
		boolean bl2 = !itemStack.isEmpty() && !itemStack2.isEmpty();
		if (!bl) {
			this.resultSlots.setItem(0, ItemStack.EMPTY);
		} else {
			boolean bl3 = !itemStack.isEmpty() && !itemStack.is(Items.ENCHANTED_BOOK) && !itemStack.isEnchanted()
				|| !itemStack2.isEmpty() && !itemStack2.is(Items.ENCHANTED_BOOK) && !itemStack2.isEnchanted();
			if (itemStack.getCount() > 1 || itemStack2.getCount() > 1 || !bl2 && bl3) {
				this.resultSlots.setItem(0, ItemStack.EMPTY);
				this.broadcastChanges();
				return;
			}

			int i = 1;
			int m;
			ItemStack itemStack3;
			if (bl2) {
				if (!itemStack.is(itemStack2.getItem())) {
					this.resultSlots.setItem(0, ItemStack.EMPTY);
					this.broadcastChanges();
					return;
				}

				Item item = itemStack.getItem();
				int j = item.getMaxDamage() - itemStack.getDamageValue();
				int k = item.getMaxDamage() - itemStack2.getDamageValue();
				int l = j + k + item.getMaxDamage() * 5 / 100;
				m = Math.max(item.getMaxDamage() - l, 0);
				itemStack3 = this.mergeEnchants(itemStack, itemStack2);
				if (!itemStack3.isDamageableItem()) {
					if (!ItemStack.matches(itemStack, itemStack2)) {
						this.resultSlots.setItem(0, ItemStack.EMPTY);
						this.broadcastChanges();
						return;
					}

					i = 2;
				}
			} else {
				boolean bl4 = !itemStack.isEmpty();
				m = bl4 ? itemStack.getDamageValue() : itemStack2.getDamageValue();
				itemStack3 = bl4 ? itemStack : itemStack2;
			}

			this.resultSlots.setItem(0, this.removeNonCurses(itemStack3, m, i));
		}

		this.broadcastChanges();
	}

	private ItemStack mergeEnchants(ItemStack itemStack, ItemStack itemStack2) {
		ItemStack itemStack3 = itemStack.copy();
		Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemStack2);

		for (Entry<Enchantment, Integer> entry : map.entrySet()) {
			Enchantment enchantment = (Enchantment)entry.getKey();
			if (!enchantment.isCurse() || EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack3) == 0) {
				itemStack3.enchant(enchantment, (Integer)entry.getValue());
			}
		}

		return itemStack3;
	}

	private ItemStack removeNonCurses(ItemStack itemStack, int i, int j) {
		ItemStack itemStack2 = itemStack.copy();
		itemStack2.removeTagKey("Enchantments");
		itemStack2.removeTagKey("StoredEnchantments");
		if (i > 0) {
			itemStack2.setDamageValue(i);
		} else {
			itemStack2.removeTagKey("Damage");
		}

		itemStack2.setCount(j);
		Map<Enchantment, Integer> map = (Map<Enchantment, Integer>)EnchantmentHelper.getEnchantments(itemStack)
			.entrySet()
			.stream()
			.filter(entry -> ((Enchantment)entry.getKey()).isCurse())
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		EnchantmentHelper.setEnchantments(map, itemStack2);
		itemStack2.setRepairCost(0);
		if (itemStack2.is(Items.ENCHANTED_BOOK) && map.size() == 0) {
			itemStack2 = new ItemStack(Items.BOOK);
			if (itemStack.hasCustomHoverName()) {
				itemStack2.setHoverName(itemStack.getHoverName());
			}
		}

		for (int k = 0; k < map.size(); k++) {
			itemStack2.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(itemStack2.getBaseRepairCost()));
		}

		return itemStack2;
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
}
