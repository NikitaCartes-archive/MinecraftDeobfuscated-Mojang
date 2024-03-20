package net.minecraft.world.item.enchantment;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class DigDurabilityEnchantment extends Enchantment {
	protected DigDurabilityEnchantment(Enchantment.EnchantmentDefinition enchantmentDefinition) {
		super(enchantmentDefinition);
	}

	public static boolean shouldIgnoreDurabilityDrop(ItemStack itemStack, int i, RandomSource randomSource) {
		if (itemStack.getItem() instanceof ArmorItem && randomSource.nextFloat() < 0.6F) {
			return false;
		} else {
			return randomSource.nextInt(i + 1) > 0;
		}
	}
}
