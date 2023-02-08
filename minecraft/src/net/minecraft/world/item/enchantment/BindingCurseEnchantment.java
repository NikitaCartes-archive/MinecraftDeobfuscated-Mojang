package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BindingCurseEnchantment extends Enchantment {
	public BindingCurseEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.WEARABLE, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 25;
	}

	@Override
	public int getMaxCost(int i) {
		return 50;
	}

	@Override
	public boolean isTreasureOnly() {
		return true;
	}

	@Override
	public boolean isCurse() {
		return true;
	}

	@Override
	public boolean canEnchant(ItemStack itemStack) {
		return !itemStack.is(Items.SHIELD) && super.canEnchant(itemStack);
	}
}
