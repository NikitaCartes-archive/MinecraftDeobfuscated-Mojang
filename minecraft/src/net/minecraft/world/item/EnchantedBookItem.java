package net.minecraft.world.item;

import net.minecraft.world.item.enchantment.EnchantmentInstance;

public class EnchantedBookItem extends Item {
	public EnchantedBookItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isEnchantable(ItemStack itemStack) {
		return false;
	}

	public static ItemStack createForEnchantment(EnchantmentInstance enchantmentInstance) {
		ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
		itemStack.enchant(enchantmentInstance.enchantment, enchantmentInstance.level);
		return itemStack;
	}
}
