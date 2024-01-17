package net.minecraft.world.item.enchantment;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;

public class SwiftSneakEnchantment extends Enchantment {
	public SwiftSneakEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, ItemTags.LEG_ARMOR_ENCHANTABLE, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return i * 25;
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + 50;
	}

	@Override
	public boolean isTreasureOnly() {
		return true;
	}

	@Override
	public boolean isTradeable() {
		return false;
	}

	@Override
	public boolean isDiscoverable() {
		return false;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}
}
