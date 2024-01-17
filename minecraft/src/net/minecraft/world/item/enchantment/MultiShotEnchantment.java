package net.minecraft.world.item.enchantment;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;

public class MultiShotEnchantment extends Enchantment {
	public MultiShotEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, ItemTags.CROSSBOW_ENCHANTABLE, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 20;
	}

	@Override
	public int getMaxCost(int i) {
		return 50;
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && enchantment != Enchantments.PIERCING;
	}
}
