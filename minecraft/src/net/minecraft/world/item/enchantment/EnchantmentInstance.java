package net.minecraft.world.item.enchantment;

import net.minecraft.util.WeighedRandom;

public class EnchantmentInstance extends WeighedRandom.WeighedRandomItem {
	public final Enchantment enchantment;
	public final int level;

	public EnchantmentInstance(Enchantment enchantment, int i) {
		super(enchantment.getRarity().getWeight());
		this.enchantment = enchantment;
		this.level = i;
	}
}
