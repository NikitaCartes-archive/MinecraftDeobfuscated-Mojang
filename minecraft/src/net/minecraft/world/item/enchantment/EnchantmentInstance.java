package net.minecraft.world.item.enchantment;

import net.minecraft.util.random.WeightedEntry;

public class EnchantmentInstance extends WeightedEntry.IntrusiveBase {
	public final Enchantment enchantment;
	public final int level;

	public EnchantmentInstance(Enchantment enchantment, int i) {
		super(enchantment.getRarity().getWeight());
		this.enchantment = enchantment;
		this.level = i;
	}
}
