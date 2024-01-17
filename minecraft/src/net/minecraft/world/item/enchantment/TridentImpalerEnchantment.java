package net.minecraft.world.item.enchantment;

import javax.annotation.Nullable;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;

public class TridentImpalerEnchantment extends Enchantment {
	public TridentImpalerEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, ItemTags.TRIDENT_ENCHANTABLE, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 1 + (i - 1) * 8;
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + 20;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public float getDamageBonus(int i, @Nullable EntityType<?> entityType) {
		return entityType != null && entityType.is(EntityTypeTags.AQUATIC) ? (float)i * 2.5F : 0.0F;
	}
}
