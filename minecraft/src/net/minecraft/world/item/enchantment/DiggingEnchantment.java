package net.minecraft.world.item.enchantment;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;

public class DiggingEnchantment extends Enchantment {
	protected DiggingEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, ItemTags.MINING_ENCHANTABLE, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 1 + 10 * (i - 1);
	}

	@Override
	public int getMaxCost(int i) {
		return super.getMinCost(i) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}
}
