package net.minecraft.world.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public class EnchantedBookItem extends Item {
	public static final String TAG_STORED_ENCHANTMENTS = "StoredEnchantments";

	public EnchantedBookItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return true;
	}

	@Override
	public boolean isEnchantable(ItemStack itemStack) {
		return false;
	}

	public static ListTag getEnchantments(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null ? compoundTag.getList("StoredEnchantments", 10) : new ListTag();
	}

	public static void addEnchantment(ItemStack itemStack, EnchantmentInstance enchantmentInstance) {
		ListTag listTag = getEnchantments(itemStack);
		boolean bl = true;
		ResourceLocation resourceLocation = EnchantmentHelper.getEnchantmentId(enchantmentInstance.enchantment);

		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			ResourceLocation resourceLocation2 = EnchantmentHelper.getEnchantmentId(compoundTag);
			if (resourceLocation2 != null && resourceLocation2.equals(resourceLocation)) {
				if (EnchantmentHelper.getEnchantmentLevel(compoundTag) < enchantmentInstance.level) {
					EnchantmentHelper.setEnchantmentLevel(compoundTag, enchantmentInstance.level);
				}

				bl = false;
				break;
			}
		}

		if (bl) {
			listTag.add(EnchantmentHelper.storeEnchantment(resourceLocation, enchantmentInstance.level));
		}

		itemStack.getOrCreateTag().put("StoredEnchantments", listTag);
	}

	public static ItemStack createForEnchantment(EnchantmentInstance enchantmentInstance) {
		ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
		itemStack.enchant(enchantmentInstance.enchantment, enchantmentInstance.level);
		return itemStack;
	}
}
