package net.minecraft.world.item;

import java.util.EnumMap;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.Ingredient;

public enum ArmorMaterials implements StringRepresentable, ArmorMaterial {
	LEATHER("leather", 5, Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 1);
		enumMap.put(ArmorItem.Type.LEGGINGS, 2);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 3);
		enumMap.put(ArmorItem.Type.HELMET, 1);
	}), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(Items.LEATHER), false),
	CHAIN("chainmail", 15, Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 1);
		enumMap.put(ArmorItem.Type.LEGGINGS, 4);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 5);
		enumMap.put(ArmorItem.Type.HELMET, 2);
	}), 12, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT), true),
	IRON("iron", 15, Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 2);
		enumMap.put(ArmorItem.Type.LEGGINGS, 5);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 6);
		enumMap.put(ArmorItem.Type.HELMET, 2);
	}), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT), true),
	GOLD("gold", 7, Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 1);
		enumMap.put(ArmorItem.Type.LEGGINGS, 3);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 5);
		enumMap.put(ArmorItem.Type.HELMET, 2);
	}), 25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, () -> Ingredient.of(Items.GOLD_INGOT), true),
	DIAMOND("diamond", 33, Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 3);
		enumMap.put(ArmorItem.Type.LEGGINGS, 6);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 8);
		enumMap.put(ArmorItem.Type.HELMET, 3);
	}), 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, () -> Ingredient.of(Items.DIAMOND), true),
	TURTLE("turtle", 25, Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 2);
		enumMap.put(ArmorItem.Type.LEGGINGS, 5);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 6);
		enumMap.put(ArmorItem.Type.HELMET, 2);
	}), 9, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, () -> Ingredient.of(Items.SCUTE), true),
	NETHERITE("netherite", 37, Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 3);
		enumMap.put(ArmorItem.Type.LEGGINGS, 6);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 8);
		enumMap.put(ArmorItem.Type.HELMET, 3);
	}), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> Ingredient.of(Items.NETHERITE_INGOT), true);

	public static final StringRepresentable.EnumCodec<ArmorMaterials> CODEC = StringRepresentable.fromEnum(ArmorMaterials::values);
	private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
		enumMap.put(ArmorItem.Type.BOOTS, 13);
		enumMap.put(ArmorItem.Type.LEGGINGS, 15);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 16);
		enumMap.put(ArmorItem.Type.HELMET, 11);
	});
	private final String name;
	private final int durabilityMultiplier;
	private final EnumMap<ArmorItem.Type, Integer> protectionFunctionForType;
	private final int enchantmentValue;
	private final SoundEvent sound;
	private final float toughness;
	private final float knockbackResistance;
	private final LazyLoadedValue<Ingredient> repairIngredient;
	private final boolean canHaveTrims;

	private ArmorMaterials(
		String string2, int j, EnumMap<ArmorItem.Type, Integer> enumMap, int k, SoundEvent soundEvent, float f, float g, Supplier<Ingredient> supplier, boolean bl
	) {
		this.name = string2;
		this.durabilityMultiplier = j;
		this.protectionFunctionForType = enumMap;
		this.enchantmentValue = k;
		this.sound = soundEvent;
		this.toughness = f;
		this.knockbackResistance = g;
		this.repairIngredient = new LazyLoadedValue<>(supplier);
		this.canHaveTrims = bl;
	}

	@Override
	public int getDurabilityForType(ArmorItem.Type type) {
		return (Integer)HEALTH_FUNCTION_FOR_TYPE.get(type) * this.durabilityMultiplier;
	}

	@Override
	public int getDefenseForType(ArmorItem.Type type) {
		return (Integer)this.protectionFunctionForType.get(type);
	}

	@Override
	public int getEnchantmentValue() {
		return this.enchantmentValue;
	}

	@Override
	public SoundEvent getEquipSound() {
		return this.sound;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return this.repairIngredient.get();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public float getToughness() {
		return this.toughness;
	}

	@Override
	public float getKnockbackResistance() {
		return this.knockbackResistance;
	}

	@Override
	public boolean canHaveTrims() {
		return this.canHaveTrims;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
