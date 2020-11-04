package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DiggingEnchantment extends Enchantment {
	protected DiggingEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.DIGGER, equipmentSlots);
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

	@Override
	public boolean canEnchant(ItemStack itemStack) {
		return itemStack.is(Items.SHEARS) ? true : super.canEnchant(itemStack);
	}
}
