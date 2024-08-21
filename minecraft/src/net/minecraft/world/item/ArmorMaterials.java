package net.minecraft.world.item;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class ArmorMaterials {
	static int LEATHER_ENCHANTMENT_VALUE = 15;
	static int CHAIN_ENCHANTMENT_VALUE = 12;
	static int IRON_ENCHANTMENT_VALUE = 9;
	static int GOLD_ENCHANTMENT_VALUE = 25;
	static int DIAMOND_ENCHANTMENT_VALUE = 10;
	static int TURTLE_ENCHANTMENT_VALUE = 9;
	static int NETHERITE_ENCHANTMENT_VALUE = 15;
	public static final Holder<ArmorMaterial> LEATHER = register(
		"leather",
		Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
			enumMap.put(ArmorItem.Type.BOOTS, 1);
			enumMap.put(ArmorItem.Type.LEGGINGS, 2);
			enumMap.put(ArmorItem.Type.CHESTPLATE, 3);
			enumMap.put(ArmorItem.Type.HELMET, 1);
			enumMap.put(ArmorItem.Type.BODY, 3);
		}),
		SoundEvents.ARMOR_EQUIP_LEATHER,
		0.0F,
		0.0F,
		itemStack -> itemStack.is(Items.LEATHER),
		List.of(
			new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("leather"), "", true),
			new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("leather"), "_overlay", false)
		)
	);
	public static final Holder<ArmorMaterial> CHAIN = register("chainmail", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 1);
		enumMap.put(ArmorItem.Type.LEGGINGS, 4);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 5);
		enumMap.put(ArmorItem.Type.HELMET, 2);
		enumMap.put(ArmorItem.Type.BODY, 4);
	}), SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, itemStack -> itemStack.is(Items.IRON_INGOT));
	public static final Holder<ArmorMaterial> IRON = register("iron", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 2);
		enumMap.put(ArmorItem.Type.LEGGINGS, 5);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 6);
		enumMap.put(ArmorItem.Type.HELMET, 2);
		enumMap.put(ArmorItem.Type.BODY, 5);
	}), SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, itemStack -> itemStack.is(Items.IRON_INGOT));
	public static final Holder<ArmorMaterial> GOLD = register("gold", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 1);
		enumMap.put(ArmorItem.Type.LEGGINGS, 3);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 5);
		enumMap.put(ArmorItem.Type.HELMET, 2);
		enumMap.put(ArmorItem.Type.BODY, 7);
	}), SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, itemStack -> itemStack.is(Items.GOLD_INGOT));
	public static final Holder<ArmorMaterial> DIAMOND = register("diamond", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 3);
		enumMap.put(ArmorItem.Type.LEGGINGS, 6);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 8);
		enumMap.put(ArmorItem.Type.HELMET, 3);
		enumMap.put(ArmorItem.Type.BODY, 11);
	}), SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, itemStack -> itemStack.is(Items.DIAMOND));
	public static final Holder<ArmorMaterial> TURTLE = register("turtle", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 2);
		enumMap.put(ArmorItem.Type.LEGGINGS, 5);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 6);
		enumMap.put(ArmorItem.Type.HELMET, 2);
		enumMap.put(ArmorItem.Type.BODY, 5);
	}), SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, itemStack -> itemStack.is(Items.TURTLE_SCUTE));
	public static final Holder<ArmorMaterial> NETHERITE = register("netherite", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 3);
		enumMap.put(ArmorItem.Type.LEGGINGS, 6);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 8);
		enumMap.put(ArmorItem.Type.HELMET, 3);
		enumMap.put(ArmorItem.Type.BODY, 11);
	}), SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, itemStack -> itemStack.is(Items.NETHERITE_INGOT));
	public static final Holder<ArmorMaterial> ARMADILLO = register("armadillo", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 3);
		enumMap.put(ArmorItem.Type.LEGGINGS, 6);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 8);
		enumMap.put(ArmorItem.Type.HELMET, 3);
		enumMap.put(ArmorItem.Type.BODY, 11);
	}), SoundEvents.ARMOR_EQUIP_WOLF, 0.0F, 0.0F, itemStack -> itemStack.is(Items.ARMADILLO_SCUTE));

	public static Holder<ArmorMaterial> bootstrap(Registry<ArmorMaterial> registry) {
		return LEATHER;
	}

	private static Holder<ArmorMaterial> register(
		String string, EnumMap<ArmorItem.Type, Integer> enumMap, Holder<SoundEvent> holder, float f, float g, Predicate<ItemStack> predicate
	) {
		List<ArmorMaterial.Layer> list = List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace(string)));
		return register(string, enumMap, holder, f, g, predicate, list);
	}

	private static Holder<ArmorMaterial> register(
		String string,
		EnumMap<ArmorItem.Type, Integer> enumMap,
		Holder<SoundEvent> holder,
		float f,
		float g,
		Predicate<ItemStack> predicate,
		List<ArmorMaterial.Layer> list
	) {
		EnumMap<ArmorItem.Type, Integer> enumMap2 = new EnumMap(ArmorItem.Type.class);

		for (ArmorItem.Type type : ArmorItem.Type.values()) {
			enumMap2.put(type, (Integer)enumMap.get(type));
		}

		return Registry.registerForHolder(
			BuiltInRegistries.ARMOR_MATERIAL, ResourceLocation.withDefaultNamespace(string), new ArmorMaterial(enumMap2, holder, predicate, list, f, g)
		);
	}
}
