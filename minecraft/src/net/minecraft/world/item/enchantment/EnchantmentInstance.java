package net.minecraft.world.item.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.util.random.WeightedEntry;

public class EnchantmentInstance extends WeightedEntry.IntrusiveBase {
	public final Holder<Enchantment> enchantment;
	public final int level;

	public EnchantmentInstance(Holder<Enchantment> holder, int i) {
		super(holder.value().getWeight());
		this.enchantment = holder;
		this.level = i;
	}
}
