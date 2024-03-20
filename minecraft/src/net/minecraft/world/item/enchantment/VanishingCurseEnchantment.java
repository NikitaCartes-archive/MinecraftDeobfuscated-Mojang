package net.minecraft.world.item.enchantment;

public class VanishingCurseEnchantment extends Enchantment {
	public VanishingCurseEnchantment(Enchantment.EnchantmentDefinition enchantmentDefinition) {
		super(enchantmentDefinition);
	}

	@Override
	public boolean isTreasureOnly() {
		return true;
	}

	@Override
	public boolean isCurse() {
		return true;
	}
}
