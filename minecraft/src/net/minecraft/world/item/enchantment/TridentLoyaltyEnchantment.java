package net.minecraft.world.item.enchantment;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;

public class TridentLoyaltyEnchantment extends Enchantment {
	public TridentLoyaltyEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, ItemTags.TRIDENT_ENCHANTABLE, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 5 + i * 7;
	}

	@Override
	public int getMaxCost(int i) {
		return 50;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}
}
