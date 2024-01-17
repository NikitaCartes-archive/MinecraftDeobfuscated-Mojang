package net.minecraft.world.item.enchantment;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;

public class ArrowFireEnchantment extends Enchantment {
	public ArrowFireEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, ItemTags.BOW_ENCHANTABLE, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 20;
	}

	@Override
	public int getMaxCost(int i) {
		return 50;
	}
}
