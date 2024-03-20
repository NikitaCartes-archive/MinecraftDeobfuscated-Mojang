package net.minecraft.world.item.enchantment;

public class LootBonusEnchantment extends Enchantment {
	protected LootBonusEnchantment(Enchantment.EnchantmentDefinition enchantmentDefinition) {
		super(enchantmentDefinition);
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && enchantment != Enchantments.SILK_TOUCH;
	}
}
