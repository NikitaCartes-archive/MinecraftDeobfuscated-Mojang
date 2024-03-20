package net.minecraft.world.item.enchantment;

public class MendingEnchantment extends Enchantment {
	public MendingEnchantment(Enchantment.EnchantmentDefinition enchantmentDefinition) {
		super(enchantmentDefinition);
	}

	@Override
	public boolean isTreasureOnly() {
		return true;
	}
}
