package net.minecraft.world.item.crafting;

import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public class RepairItemRecipe extends CustomRecipe {
	public RepairItemRecipe(CraftingBookCategory craftingBookCategory) {
		super(craftingBookCategory);
	}

	@Nullable
	private Pair<ItemStack, ItemStack> getItemsToCombine(CraftingInput craftingInput) {
		ItemStack itemStack = null;
		ItemStack itemStack2 = null;

		for (int i = 0; i < craftingInput.size(); i++) {
			ItemStack itemStack3 = craftingInput.getItem(i);
			if (!itemStack3.isEmpty()) {
				if (itemStack == null) {
					itemStack = itemStack3;
				} else {
					if (itemStack2 != null) {
						return null;
					}

					itemStack2 = itemStack3;
				}
			}
		}

		return itemStack != null && itemStack2 != null && canCombine(itemStack, itemStack2) ? Pair.of(itemStack, itemStack2) : null;
	}

	private static boolean canCombine(ItemStack itemStack, ItemStack itemStack2) {
		return itemStack2.is(itemStack.getItem())
			&& itemStack.getCount() == 1
			&& itemStack2.getCount() == 1
			&& itemStack.has(DataComponents.MAX_DAMAGE)
			&& itemStack2.has(DataComponents.MAX_DAMAGE)
			&& itemStack.has(DataComponents.DAMAGE)
			&& itemStack2.has(DataComponents.DAMAGE);
	}

	public boolean matches(CraftingInput craftingInput, Level level) {
		return this.getItemsToCombine(craftingInput) != null;
	}

	public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
		Pair<ItemStack, ItemStack> pair = this.getItemsToCombine(craftingInput);
		if (pair == null) {
			return ItemStack.EMPTY;
		} else {
			ItemStack itemStack = pair.getFirst();
			ItemStack itemStack2 = pair.getSecond();
			int i = Math.max(itemStack.getMaxDamage(), itemStack2.getMaxDamage());
			int j = itemStack.getMaxDamage() - itemStack.getDamageValue();
			int k = itemStack2.getMaxDamage() - itemStack2.getDamageValue();
			int l = j + k + i * 5 / 100;
			ItemStack itemStack3 = new ItemStack(itemStack.getItem());
			itemStack3.set(DataComponents.MAX_DAMAGE, i);
			itemStack3.setDamageValue(Math.max(i - l, 0));
			ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
			ItemEnchantments itemEnchantments2 = EnchantmentHelper.getEnchantmentsForCrafting(itemStack2);
			EnchantmentHelper.updateEnchantments(
				itemStack3,
				mutable -> provider.lookupOrThrow(Registries.ENCHANTMENT).listElements().filter(reference -> reference.is(EnchantmentTags.CURSE)).forEach(reference -> {
						int ix = Math.max(itemEnchantments.getLevel(reference), itemEnchantments2.getLevel(reference));
						if (ix > 0) {
							mutable.upgrade(reference, ix);
						}
					})
			);
			return itemStack3;
		}
	}

	@Override
	public RecipeSerializer<RepairItemRecipe> getSerializer() {
		return RecipeSerializer.REPAIR_ITEM;
	}
}
