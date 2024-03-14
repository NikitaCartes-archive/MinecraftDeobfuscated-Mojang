package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ItemSubPredicates {
	public static final ItemSubPredicate.Type<ItemDamagePredicate> DAMAGE = register("damage", ItemDamagePredicate.CODEC);
	public static final ItemSubPredicate.Type<ItemEnchantmentsPredicate.Enchantments> ENCHANTMENTS = register(
		"enchantments", ItemEnchantmentsPredicate.Enchantments.CODEC
	);
	public static final ItemSubPredicate.Type<ItemEnchantmentsPredicate.StoredEnchantments> STORED_ENCHANTMENTS = register(
		"stored_enchantments", ItemEnchantmentsPredicate.StoredEnchantments.CODEC
	);
	public static final ItemSubPredicate.Type<ItemPotionsPredicate> POTIONS = register("potion_contents", ItemPotionsPredicate.CODEC);
	public static final ItemSubPredicate.Type<ItemCustomDataPredicate> CUSTOM_DATA = register("custom_data", ItemCustomDataPredicate.CODEC);

	private static <T extends ItemSubPredicate> ItemSubPredicate.Type<T> register(String string, Codec<T> codec) {
		return Registry.register(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE, string, new ItemSubPredicate.Type<>(codec));
	}

	public static ItemSubPredicate.Type<?> bootstrap(Registry<ItemSubPredicate.Type<?>> registry) {
		return DAMAGE;
	}
}
