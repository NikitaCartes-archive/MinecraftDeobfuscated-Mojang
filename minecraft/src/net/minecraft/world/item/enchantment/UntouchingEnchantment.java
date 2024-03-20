package net.minecraft.world.item.enchantment;

public class UntouchingEnchantment extends Enchantment {
	protected UntouchingEnchantment(Enchantment.EnchantmentDefinition enchantmentDefinition) {
		super(enchantmentDefinition);
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && enchantment != Enchantments.FORTUNE;
	}
}
