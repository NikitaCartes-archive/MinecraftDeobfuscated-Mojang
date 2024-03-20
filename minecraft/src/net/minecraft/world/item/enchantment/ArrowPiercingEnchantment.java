package net.minecraft.world.item.enchantment;

public class ArrowPiercingEnchantment extends Enchantment {
	public ArrowPiercingEnchantment(Enchantment.EnchantmentDefinition enchantmentDefinition) {
		super(enchantmentDefinition);
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && enchantment != Enchantments.MULTISHOT;
	}
}
