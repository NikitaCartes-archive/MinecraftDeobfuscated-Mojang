package net.minecraft.world.item.enchantment;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

public class LootBonusEnchantment extends Enchantment {
	protected LootBonusEnchantment(Enchantment.Rarity rarity, TagKey<Item> tagKey, EquipmentSlot... equipmentSlots) {
		super(rarity, tagKey, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 15 + (i - 1) * 9;
	}

	@Override
	public int getMaxCost(int i) {
		return super.getMinCost(i) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && enchantment != Enchantments.SILK_TOUCH;
	}
}
