package net.minecraft.world.inventory;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class AnvilMenu extends ItemCombinerMenu {
	public static final int INPUT_SLOT = 0;
	public static final int ADDITIONAL_SLOT = 1;
	public static final int RESULT_SLOT = 2;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final boolean DEBUG_COST = false;
	public static final int MAX_NAME_LENGTH = 50;
	private int repairItemCountCost;
	@Nullable
	private String itemName;
	private final DataSlot cost = DataSlot.standalone();
	private boolean onlyRenaming = false;
	private static final int COST_FAIL = 0;
	private static final int COST_BASE = 1;
	private static final int COST_ADDED_BASE = 1;
	private static final int COST_REPAIR_MATERIAL = 1;
	private static final int COST_REPAIR_SACRIFICE = 2;
	private static final int COST_INCOMPATIBLE_PENALTY = 1;
	private static final int COST_RENAME = 1;
	private static final int INPUT_SLOT_X_PLACEMENT = 27;
	private static final int ADDITIONAL_SLOT_X_PLACEMENT = 76;
	private static final int RESULT_SLOT_X_PLACEMENT = 134;
	private static final int SLOT_Y_PLACEMENT = 47;

	public AnvilMenu(int i, Inventory inventory) {
		this(i, inventory, ContainerLevelAccess.NULL);
	}

	public AnvilMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(MenuType.ANVIL, i, inventory, containerLevelAccess);
		this.addDataSlot(this.cost);
	}

	@Override
	protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
		return ItemCombinerMenuSlotDefinition.create()
			.withSlot(0, 27, 47, itemStack -> true)
			.withSlot(1, 76, 47, itemStack -> true)
			.withResultSlot(2, 134, 47)
			.build();
	}

	@Override
	protected boolean isValidBlock(BlockState blockState) {
		return blockState.is(BlockTags.ANVIL);
	}

	@Override
	protected boolean mayPickup(Player player, boolean bl) {
		return (player.hasInfiniteMaterials() || player.experienceLevel >= this.cost.get()) && this.cost.get() > 0;
	}

	@Override
	protected void onTake(Player player, ItemStack itemStack) {
		if (!player.getAbilities().instabuild) {
			player.giveExperienceLevels(-this.cost.get());
		}

		if (this.repairItemCountCost > 0) {
			ItemStack itemStack2 = this.inputSlots.getItem(1);
			if (!itemStack2.isEmpty() && itemStack2.getCount() > this.repairItemCountCost) {
				itemStack2.shrink(this.repairItemCountCost);
				this.inputSlots.setItem(1, itemStack2);
			} else {
				this.inputSlots.setItem(1, ItemStack.EMPTY);
			}
		} else if (!this.onlyRenaming) {
			this.inputSlots.setItem(1, ItemStack.EMPTY);
		}

		this.cost.set(0);
		this.inputSlots.setItem(0, ItemStack.EMPTY);
		this.access.execute((level, blockPos) -> {
			BlockState blockState = level.getBlockState(blockPos);
			if (!player.hasInfiniteMaterials() && blockState.is(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
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
	}

	@Override
	public void createResult() {
		ItemStack itemStack = this.inputSlots.getItem(0);
		this.onlyRenaming = false;
		this.cost.set(1);
		int i = 0;
		long l = 0L;
		int j = 0;
		if (!itemStack.isEmpty() && EnchantmentHelper.canStoreEnchantments(itemStack)) {
			ItemStack itemStack2 = itemStack.copy();
			ItemStack itemStack3 = this.inputSlots.getItem(1);
			ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(itemStack2));
			l += (long)itemStack.getOrDefault(DataComponents.REPAIR_COST, Integer.valueOf(0)).intValue()
				+ (long)itemStack3.getOrDefault(DataComponents.REPAIR_COST, Integer.valueOf(0)).intValue();
			this.repairItemCountCost = 0;
			if (!itemStack3.isEmpty()) {
				boolean bl = itemStack3.has(DataComponents.STORED_ENCHANTMENTS);
				if (itemStack2.isDamageableItem() && itemStack.isValidRepairItem(itemStack3)) {
					int k = Math.min(itemStack2.getDamageValue(), itemStack2.getMaxDamage() / 4);
					if (k <= 0) {
						this.resultSlots.setItem(0, ItemStack.EMPTY);
						this.cost.set(0);
						return;
					}

					int m;
					for (m = 0; k > 0 && m < itemStack3.getCount(); m++) {
						int n = itemStack2.getDamageValue() - k;
						itemStack2.setDamageValue(n);
						i++;
						k = Math.min(itemStack2.getDamageValue(), itemStack2.getMaxDamage() / 4);
					}

					this.repairItemCountCost = m;
				} else {
					if (!bl && (!itemStack2.is(itemStack3.getItem()) || !itemStack2.isDamageableItem())) {
						this.resultSlots.setItem(0, ItemStack.EMPTY);
						this.cost.set(0);
						return;
					}

					if (itemStack2.isDamageableItem() && !bl) {
						int kx = itemStack.getMaxDamage() - itemStack.getDamageValue();
						int m = itemStack3.getMaxDamage() - itemStack3.getDamageValue();
						int n = m + itemStack2.getMaxDamage() * 12 / 100;
						int o = kx + n;
						int p = itemStack2.getMaxDamage() - o;
						if (p < 0) {
							p = 0;
						}

						if (p < itemStack2.getDamageValue()) {
							itemStack2.setDamageValue(p);
							i += 2;
						}
					}

					ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack3);
					boolean bl2 = false;
					boolean bl3 = false;

					for (Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
						Holder<Enchantment> holder = (Holder<Enchantment>)entry.getKey();
						int q = mutable.getLevel(holder);
						int r = entry.getIntValue();
						r = q == r ? r + 1 : Math.max(r, q);
						Enchantment enchantment = holder.value();
						boolean bl4 = enchantment.canEnchant(itemStack);
						if (this.player.getAbilities().instabuild || itemStack.is(Items.ENCHANTED_BOOK)) {
							bl4 = true;
						}

						for (Holder<Enchantment> holder2 : mutable.keySet()) {
							if (!holder2.equals(holder) && !Enchantment.areCompatible(holder, holder2)) {
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

							mutable.set(holder, r);
							int s = enchantment.getAnvilCost();
							if (bl) {
								s = Math.max(1, s / 2);
							}

							i += s * r;
							if (itemStack.getCount() > 1) {
								i = 40;
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

			if (this.itemName != null && !StringUtil.isBlank(this.itemName)) {
				if (!this.itemName.equals(itemStack.getHoverName().getString())) {
					j = 1;
					i += j;
					itemStack2.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
				}
			} else if (itemStack.has(DataComponents.CUSTOM_NAME)) {
				j = 1;
				i += j;
				itemStack2.remove(DataComponents.CUSTOM_NAME);
			}

			int t = (int)Mth.clamp(l + (long)i, 0L, 2147483647L);
			this.cost.set(t);
			if (i <= 0) {
				itemStack2 = ItemStack.EMPTY;
			}

			if (j == i && j > 0) {
				if (this.cost.get() >= 40) {
					this.cost.set(39);
				}

				this.onlyRenaming = true;
			}

			if (this.cost.get() >= 40 && !this.player.getAbilities().instabuild) {
				itemStack2 = ItemStack.EMPTY;
			}

			if (!itemStack2.isEmpty()) {
				int kxx = itemStack2.getOrDefault(DataComponents.REPAIR_COST, Integer.valueOf(0));
				if (kxx < itemStack3.getOrDefault(DataComponents.REPAIR_COST, Integer.valueOf(0))) {
					kxx = itemStack3.getOrDefault(DataComponents.REPAIR_COST, Integer.valueOf(0));
				}

				if (j != i || j == 0) {
					kxx = calculateIncreasedRepairCost(kxx);
				}

				itemStack2.set(DataComponents.REPAIR_COST, kxx);
				EnchantmentHelper.setEnchantments(itemStack2, mutable.toImmutable());
			}

			this.resultSlots.setItem(0, itemStack2);
			this.broadcastChanges();
		} else {
			this.resultSlots.setItem(0, ItemStack.EMPTY);
			this.cost.set(0);
		}
	}

	public static int calculateIncreasedRepairCost(int i) {
		return (int)Math.min((long)i * 2L + 1L, 2147483647L);
	}

	public boolean setItemName(String string) {
		String string2 = validateName(string);
		if (string2 != null && !string2.equals(this.itemName)) {
			this.itemName = string2;
			if (this.getSlot(2).hasItem()) {
				ItemStack itemStack = this.getSlot(2).getItem();
				if (StringUtil.isBlank(string2)) {
					itemStack.remove(DataComponents.CUSTOM_NAME);
				} else {
					itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(string2));
				}
			}

			this.createResult();
			return true;
		} else {
			return false;
		}
	}

	@Nullable
	private static String validateName(String string) {
		String string2 = StringUtil.filterText(string);
		return string2.length() <= 50 ? string2 : null;
	}

	public int getCost() {
		return this.cost.get();
	}
}
