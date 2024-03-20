package net.minecraft.world.item.enchantment;

public class WaterWalkerEnchantment extends Enchantment {
	public WaterWalkerEnchantment(Enchantment.EnchantmentDefinition enchantmentDefinition) {
		super(enchantmentDefinition);
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && enchantment != Enchantments.FROST_WALKER;
	}
}
