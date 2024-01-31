package net.minecraft.world.item;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.Ingredient;

public class ArmorMaterials {
	public static final Holder<ArmorMaterial> LEATHER = register(
		"leather",
		Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
			enumMap.put(ArmorItem.Type.BOOTS, 1);
			enumMap.put(ArmorItem.Type.LEGGINGS, 2);
			enumMap.put(ArmorItem.Type.CHESTPLATE, 3);
			enumMap.put(ArmorItem.Type.HELMET, 1);
			enumMap.put(ArmorItem.Type.BODY, 3);
		}),
		15,
		SoundEvents.ARMOR_EQUIP_LEATHER,
		0.0F,
		0.0F,
		() -> Ingredient.of(Items.LEATHER),
		List.of(new ArmorMaterial.Layer(new ResourceLocation("leather"), "", true), new ArmorMaterial.Layer(new ResourceLocation("leather"), "_overlay", false))
	);
	public static final Holder<ArmorMaterial> CHAIN = register("chain", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 1);
		enumMap.put(ArmorItem.Type.LEGGINGS, 4);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 5);
		enumMap.put(ArmorItem.Type.HELMET, 2);
		enumMap.put(ArmorItem.Type.BODY, 4);
	}), 12, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));
	public static final Holder<ArmorMaterial> IRON = register("iron", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 2);
		enumMap.put(ArmorItem.Type.LEGGINGS, 5);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 6);
		enumMap.put(ArmorItem.Type.HELMET, 2);
		enumMap.put(ArmorItem.Type.BODY, 5);
	}), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));
	public static final Holder<ArmorMaterial> GOLD = register("gold", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 1);
		enumMap.put(ArmorItem.Type.LEGGINGS, 3);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 5);
		enumMap.put(ArmorItem.Type.HELMET, 2);
		enumMap.put(ArmorItem.Type.BODY, 7);
	}), 25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, () -> Ingredient.of(Items.GOLD_INGOT));
	public static final Holder<ArmorMaterial> DIAMOND = register("diamond", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 3);
		enumMap.put(ArmorItem.Type.LEGGINGS, 6);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 8);
		enumMap.put(ArmorItem.Type.HELMET, 3);
		enumMap.put(ArmorItem.Type.BODY, 11);
	}), 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, () -> Ingredient.of(Items.DIAMOND));
	public static final Holder<ArmorMaterial> TURTLE = register("turtle", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 2);
		enumMap.put(ArmorItem.Type.LEGGINGS, 5);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 6);
		enumMap.put(ArmorItem.Type.HELMET, 2);
		enumMap.put(ArmorItem.Type.BODY, 5);
	}), 9, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, () -> Ingredient.of(Items.TURTLE_SCUTE));
	public static final Holder<ArmorMaterial> NETHERITE = register("netherite", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 3);
		enumMap.put(ArmorItem.Type.LEGGINGS, 6);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 8);
		enumMap.put(ArmorItem.Type.HELMET, 3);
		enumMap.put(ArmorItem.Type.BODY, 11);
	}), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> Ingredient.of(Items.NETHERITE_INGOT));
	public static final Holder<ArmorMaterial> ARMADILLO = register("armadillo", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 3);
		enumMap.put(ArmorItem.Type.LEGGINGS, 6);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 8);
		enumMap.put(ArmorItem.Type.HELMET, 3);
		enumMap.put(ArmorItem.Type.BODY, 11);
	}), 10, SoundEvents.ARMOR_EQUIP_WOLF, 0.0F, 0.0F, () -> Ingredient.of(Items.ARMADILLO_SCUTE));

	public static Holder<ArmorMaterial> bootstrap(Registry<ArmorMaterial> registry) {
		return LEATHER;
	}

	private static Holder<ArmorMaterial> register(
		String string, EnumMap<ArmorItem.Type, Integer> enumMap, int i, Holder<SoundEvent> holder, float f, float g, Supplier<Ingredient> supplier
	) {
		List<ArmorMaterial.Layer> list = List.of(new ArmorMaterial.Layer(new ResourceLocation(string)));
		return register(string, enumMap, i, holder, f, g, supplier, list);
	}

	private static Holder<ArmorMaterial> register(
		String string,
		EnumMap<ArmorItem.Type, Integer> enumMap,
		int i,
		Holder<SoundEvent> holder,
		float f,
		float g,
		Supplier<Ingredient> supplier,
		List<ArmorMaterial.Layer> list
	) {
		EnumMap<ArmorItem.Type, Integer> enumMap2 = new EnumMap(ArmorItem.Type.class);

		for (ArmorItem.Type type : ArmorItem.Type.values()) {
			enumMap2.put(type, (Integer)enumMap.get(type));
		}

		return Registry.registerForHolder(
			BuiltInRegistries.ARMOR_MATERIAL, new ResourceLocation(string), new ArmorMaterial(enumMap2, i, holder, supplier, list, f, g)
		);
	}
}
