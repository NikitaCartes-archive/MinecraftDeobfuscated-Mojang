package net.minecraft.world.inventory;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilMenu extends AbstractContainerMenu {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Container resultSlots = new ResultContainer();
	private final Container repairSlots = new SimpleContainer(2) {
		@Override
		public void setChanged() {
			super.setChanged();
			AnvilMenu.this.slotsChanged(this);
		}
	};
	private final DataSlot cost = DataSlot.standalone();
	private final ContainerLevelAccess access;
	private int repairItemCountCost;
	private String itemName;
	private final Player player;

	public AnvilMenu(int i, Inventory inventory) {
		this(i, inventory, ContainerLevelAccess.NULL);
	}

	public AnvilMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(MenuType.ANVIL, i);
		this.access = containerLevelAccess;
		this.player = inventory.player;
		this.addDataSlot(this.cost);
		this.addSlot(new Slot(this.repairSlots, 0, 27, 47));
		this.addSlot(new Slot(this.repairSlots, 1, 76, 47));
		this.addSlot(new Slot(this.resultSlots, 2, 134, 47) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return false;
			}

			@Override
			public boolean mayPickup(Player player) {
				return (player.abilities.instabuild || player.experienceLevel >= AnvilMenu.this.cost.get()) && AnvilMenu.this.cost.get() > 0 && this.hasItem();
			}

			@Override
			public ItemStack onTake(Player player, ItemStack itemStack) {
				if (!player.abilities.instabuild) {
					player.giveExperienceLevels(-AnvilMenu.this.cost.get());
				}

				AnvilMenu.this.repairSlots.setItem(0, ItemStack.EMPTY);
				if (AnvilMenu.this.repairItemCountCost > 0) {
					ItemStack itemStack2 = AnvilMenu.this.repairSlots.getItem(1);
					if (!itemStack2.isEmpty() && itemStack2.getCount() > AnvilMenu.this.repairItemCountCost) {
						itemStack2.shrink(AnvilMenu.this.repairItemCountCost);
						AnvilMenu.this.repairSlots.setItem(1, itemStack2);
					} else {
						AnvilMenu.this.repairSlots.setItem(1, ItemStack.EMPTY);
					}
				} else {
					AnvilMenu.this.repairSlots.setItem(1, ItemStack.EMPTY);
				}

				AnvilMenu.this.cost.set(0);
				containerLevelAccess.execute((level, blockPos) -> {
					BlockState blockState = level.getBlockState(blockPos);
					if (!player.abilities.instabuild && blockState.is(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
						BlockState blockState2 = AnvilBlock.damage(blockState);
						if (blockState2 == null) {
							level.removeBlock(blockPos, false);
							level.levelEvent(1029, blockPos, 0);
						} else {
							level.setBlock(blockPos, blockState2, 2);
							level.levelEvent(1030, blockPos, 0);
						}
					} else {
						level.levelEvent(1030, blockPos, 0);
					}
				});
				return itemStack;
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

	public void createResult() {
		ItemStack itemStack = this.repairSlots.getItem(0);
		this.cost.set(1);
		int i = 0;
		int j = 0;
		int k = 0;
		if (itemStack.isEmpty()) {
			this.resultSlots.setItem(0, ItemStack.EMPTY);
			this.cost.set(0);
		} else {
			ItemStack itemStack2 = itemStack.copy();
			ItemStack itemStack3 = this.repairSlots.getItem(1);
			Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemStack2);
			j += itemStack.getBaseRepairCost() + (itemStack3.isEmpty() ? 0 : itemStack3.getBaseRepairCost());
			this.repairItemCountCost = 0;
			if (!itemStack3.isEmpty()) {
				boolean bl = itemStack3.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(itemStack3).isEmpty();
				if (itemStack2.isDamageableItem() && itemStack2.getItem().isValidRepairItem(itemStack, itemStack3)) {
					int l = Math.min(itemStack2.getDamageValue(), itemStack2.getMaxDamage() / 4);
					if (l <= 0) {
						this.resultSlots.setItem(0, ItemStack.EMPTY);
						this.cost.set(0);
						return;
					}

					int m;
					for (m = 0; l > 0 && m < itemStack3.getCount(); m++) {
						int n = itemStack2.getDamageValue() - l;
						itemStack2.setDamageValue(n);
						i++;
						l = Math.min(itemStack2.getDamageValue(), itemStack2.getMaxDamage() / 4);
					}

					this.repairItemCountCost = m;
				} else {
					if (!bl && (itemStack2.getItem() != itemStack3.getItem() || !itemStack2.isDamageableItem())) {
						this.resultSlots.setItem(0, ItemStack.EMPTY);
						this.cost.set(0);
						return;
					}

					if (itemStack2.isDamageableItem() && !bl) {
						int lx = itemStack.getMaxDamage() - itemStack.getDamageValue();
						int m = itemStack3.getMaxDamage() - itemStack3.getDamageValue();
						int n = m + itemStack2.getMaxDamage() * 12 / 100;
						int o = lx + n;
						int p = itemStack2.getMaxDamage() - o;
						if (p < 0) {
							p = 0;
						}

						if (p < itemStack2.getDamageValue()) {
							itemStack2.setDamageValue(p);
							i += 2;
						}
					}

					Map<Enchantment, Integer> map2 = EnchantmentHelper.getEnchantments(itemStack3);
					boolean bl2 = false;
					boolean bl3 = false;

					for (Enchantment enchantment : map2.keySet()) {
						if (enchantment != null) {
							int q = map.containsKey(enchantment) ? (Integer)map.get(enchantment) : 0;
							int r = (Integer)map2.get(enchantment);
							r = q == r ? r + 1 : Math.max(r, q);
							boolean bl4 = enchantment.canEnchant(itemStack);
							if (this.player.abilities.instabuild || itemStack.getItem() == Items.ENCHANTED_BOOK) {
								bl4 = true;
							}

							for (Enchantment enchantment2 : map.keySet()) {
								if (enchantment2 != enchantment && !enchantment.isCompatibleWith(enchantment2)) {
									bl4 = false;
									i++;
								}
							}

							if (!bl4) {
								bl3 = true;
							} else {
								bl2 = true;
								if (r > enchantment.getMaxLevel()) {
									r = enchantment.getMaxLevel();
								}

								map.put(enchantment, r);
								int s = 0;
								switch (enchantment.getRarity()) {
									case COMMON:
										s = 1;
										break;
									case UNCOMMON:
										s = 2;
										break;
									case RARE:
										s = 4;
										break;
									case VERY_RARE:
										s = 8;
								}

								if (bl) {
									s = Math.max(1, s / 2);
								}

								i += s * r;
								if (itemStack.getCount() > 1) {
									i = 40;
								}
							}
						}
					}

					if (bl3 && !bl2) {
						this.resultSlots.setItem(0, ItemStack.EMPTY);
						this.cost.set(0);
						return;
					}
				}
			}

			if (StringUtils.isBlank(this.itemName)) {
				if (itemStack.hasCustomHoverName()) {
					k = 1;
					i += k;
					itemStack2.resetHoverName();
				}
			} else if (!this.itemName.equals(itemStack.getHoverName().getString())) {
				k = 1;
				i += k;
				itemStack2.setHoverName(new TextComponent(this.itemName));
			}

			this.cost.set(j + i);
			if (i <= 0) {
				itemStack2 = ItemStack.EMPTY;
			}

			if (k == i && k > 0 && this.cost.get() >= 40) {
				this.cost.set(39);
			}

			if (this.cost.get() >= 40 && !this.player.abilities.instabuild) {
				itemStack2 = ItemStack.EMPTY;
			}

			if (!itemStack2.isEmpty()) {
				int t = itemStack2.getBaseRepairCost();
				if (!itemStack3.isEmpty() && t < itemStack3.getBaseRepairCost()) {
					t = itemStack3.getBaseRepairCost();
				}

				if (k != i || k == 0) {
					t = calculateIncreasedRepairCost(t);
				}

				itemStack2.setRepairCost(t);
				EnchantmentHelper.setEnchantments(map, itemStack2);
			}

			this.resultSlots.setItem(0, itemStack2);
			this.broadcastChanges();
		}
	}

	public static int calculateIncreasedRepairCost(int i) {
		return i * 2 + 1;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.access.execute((level, blockPos) -> this.clearContainer(player, level, this.repairSlots));
	}

	@Override
	public boolean stillValid(Player player) {
		return this.access
			.evaluate(
				(level, blockPos) -> !level.getBlockState(blockPos).is(BlockTags.ANVIL)
						? false
						: player.distanceToSqr((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5) <= 64.0,
				true
			);
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
			} else if (i != 0 && i != 1) {
				if (i >= 3 && i < 39 && !this.moveItemStackTo(itemStack2, 0, 2, false)) {
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

	public void setItemName(String string) {
		this.itemName = string;
		if (this.getSlot(2).hasItem()) {
			ItemStack itemStack = this.getSlot(2).getItem();
			if (StringUtils.isBlank(string)) {
				itemStack.resetHoverName();
			} else {
				itemStack.setHoverName(new TextComponent(this.itemName));
			}
		}

		this.createResult();
	}

	@Environment(EnvType.CLIENT)
	public int getCost() {
		return this.cost.get();
	}
}
