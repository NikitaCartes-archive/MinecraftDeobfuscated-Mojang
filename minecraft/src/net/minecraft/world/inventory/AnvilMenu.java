package net.minecraft.world.inventory;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.tags.BlockTags;
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

public class AnvilMenu extends ItemCombinerMenu {
	private static final Logger LOGGER = LogManager.getLogger();
	private int repairItemCountCost;
	private String itemName;
	private final DataSlot cost = DataSlot.standalone();

	public AnvilMenu(int i, Inventory inventory) {
		this(i, inventory, ContainerLevelAccess.NULL);
	}

	public AnvilMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(MenuType.ANVIL, i, inventory, containerLevelAccess);
		this.addDataSlot(this.cost);
	}

	@Override
	protected boolean isValidBlock(BlockState blockState) {
		return blockState.is(BlockTags.ANVIL);
	}

	@Override
	protected boolean mayPickup(Player player, boolean bl) {
		return (player.abilities.instabuild || player.experienceLevel >= this.cost.get()) && this.cost.get() > 0;
	}

	@Override
	protected ItemStack onTake(Player player, ItemStack itemStack) {
		if (!player.abilities.instabuild) {
			player.giveExperienceLevels(-this.cost.get());
		}

		this.inputSlots.setItem(0, ItemStack.EMPTY);
		if (this.repairItemCountCost > 0) {
			ItemStack itemStack2 = this.inputSlots.getItem(1);
			if (!itemStack2.isEmpty() && itemStack2.getCount() > this.repairItemCountCost) {
				itemStack2.shrink(this.repairItemCountCost);
				this.inputSlots.setItem(1, itemStack2);
			} else {
				this.inputSlots.setItem(1, ItemStack.EMPTY);
			}
		} else {
			this.inputSlots.setItem(1, ItemStack.EMPTY);
		}

		this.cost.set(0);
		this.access.execute((level, blockPos) -> {
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

	@Override
	public void createResult() {
		ItemStack itemStack = this.inputSlots.getItem(0);
		this.cost.set(1);
		int i = 0;
		int j = 0;
		int k = 0;
		if (itemStack.isEmpty()) {
			this.resultSlots.setItem(0, ItemStack.EMPTY);
			this.cost.set(0);
		} else {
			ItemStack itemStack2 = itemStack.copy();
			ItemStack itemStack3 = this.inputSlots.getItem(1);
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
							int q = (Integer)map.getOrDefault(enchantment, 0);
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
