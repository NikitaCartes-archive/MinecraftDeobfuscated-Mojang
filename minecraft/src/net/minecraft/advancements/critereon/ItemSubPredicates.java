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
	public static final ItemSubPredicate.Type<ItemContainerPredicate> CONTAINER = register("container", ItemContainerPredicate.CODEC);
	public static final ItemSubPredicate.Type<ItemBundlePredicate> BUNDLE_CONTENTS = register("bundle_contents", ItemBundlePredicate.CODEC);
	public static final ItemSubPredicate.Type<ItemFireworkExplosionPredicate> FIREWORK_EXPLOSION = register(
		"firework_explosion", ItemFireworkExplosionPredicate.CODEC
	);
	public static final ItemSubPredicate.Type<ItemFireworksPredicate> FIREWORKS = register("fireworks", ItemFireworksPredicate.CODEC);
	public static final ItemSubPredicate.Type<ItemWritableBookPredicate> WRITABLE_BOOK = register("writable_book_content", ItemWritableBookPredicate.CODEC);
	public static final ItemSubPredicate.Type<ItemWrittenBookPredicate> WRITTEN_BOOK = register("written_book_content", ItemWrittenBookPredicate.CODEC);
	public static final ItemSubPredicate.Type<ItemAttributeModifiersPredicate> ATTRIBUTE_MODIFIERS = register(
		"attribute_modifiers", ItemAttributeModifiersPredicate.CODEC
	);
	public static final ItemSubPredicate.Type<ItemTrimPredicate> ARMOR_TRIM = register("trim", ItemTrimPredicate.CODEC);
	public static final ItemSubPredicate.Type<ItemJukeboxPlayablePredicate> JUKEBOX_PLAYABLE = register("jukebox_playable", ItemJukeboxPlayablePredicate.CODEC);

	private static <T extends ItemSubPredicate> ItemSubPredicate.Type<T> register(String string, Codec<T> codec) {
		return Registry.register(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE, string, new ItemSubPredicate.Type<>(codec));
	}

	public static ItemSubPredicate.Type<?> bootstrap(Registry<ItemSubPredicate.Type<?>> registry) {
		return DAMAGE;
	}
}
