package net.minecraft.world.item.enchantment;

public class ArrowInfiniteEnchantment extends Enchantment {
	public ArrowInfiniteEnchantment(Enchantment.EnchantmentDefinition enchantmentDefinition) {
		super(enchantmentDefinition);
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return enchantment instanceof MendingEnchantment ? false : super.checkCompatibility(enchantment);
	}
}
