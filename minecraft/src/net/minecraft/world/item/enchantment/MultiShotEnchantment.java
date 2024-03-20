package net.minecraft.world.item.enchantment;

public class MultiShotEnchantment extends Enchantment {
	public MultiShotEnchantment(Enchantment.EnchantmentDefinition enchantmentDefinition) {
		super(enchantmentDefinition);
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && enchantment != Enchantments.PIERCING;
	}
}
