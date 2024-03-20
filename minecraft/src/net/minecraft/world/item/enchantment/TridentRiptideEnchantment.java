package net.minecraft.world.item.enchantment;

public class TridentRiptideEnchantment extends Enchantment {
	public TridentRiptideEnchantment(Enchantment.EnchantmentDefinition enchantmentDefinition) {
		super(enchantmentDefinition);
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && enchantment != Enchantments.LOYALTY && enchantment != Enchantments.CHANNELING;
	}
}
