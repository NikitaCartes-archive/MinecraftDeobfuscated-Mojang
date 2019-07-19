package net.minecraft.world.item.enchantment;

import java.util.Random;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class DigDurabilityEnchantment extends Enchantment {
	protected DigDurabilityEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.BREAKABLE, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 5 + (i - 1) * 8;
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
	public boolean canEnchant(ItemStack itemStack) {
		return itemStack.isDamageableItem() ? true : super.canEnchant(itemStack);
	}

	public static boolean shouldIgnoreDurabilityDrop(ItemStack itemStack, int i, Random random) {
		return itemStack.getItem() instanceof ArmorItem && random.nextFloat() < 0.6F ? false : random.nextInt(i + 1) > 0;
	}
}
