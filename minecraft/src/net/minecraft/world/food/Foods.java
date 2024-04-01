package net.minecraft.world.food;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class Foods {
	public static final FoodProperties APPLE = new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build();
	public static final FoodProperties BAKED_POTATO = new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build();
	public static final FoodProperties HOT_POTATO = new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).alwaysEdible().build();
	public static final FoodProperties POISONOUS_POTATO_STICKS = new FoodProperties.Builder()
		.nutrition(1)
		.saturationModifier(0.6F)
		.eatSound(SoundEvents.ENTITY_POTATO_CHIPS)
		.build();
	public static final FoodProperties POISONOUS_POTATO_SLICES = new FoodProperties.Builder()
		.nutrition(1)
		.saturationModifier(0.6F)
		.eatSound(SoundEvents.ENTITY_POTATO_CHIPS)
		.build();
	public static final FoodProperties POISONOUS_POTATO_FRIES = new FoodProperties.Builder()
		.nutrition(10)
		.saturationModifier(0.6F)
		.eatSound(SoundEvents.ENTITY_POTATO_CHIPS)
		.build();
	public static final FoodProperties POISONOUS_POTATO_CHIPS = new FoodProperties.Builder()
		.nutrition(8)
		.saturationModifier(0.6F)
		.eatSound(SoundEvents.ENTITY_POTATO_CHIPS)
		.build();
	public static final FoodProperties BEEF = new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build();
	public static final FoodProperties BEETROOT = new FoodProperties.Builder().nutrition(1).saturationModifier(0.6F).build();
	public static final FoodProperties BEETROOT_SOUP = stew(6).build();
	public static final FoodProperties BREAD = new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build();
	public static final FoodProperties CARROT = new FoodProperties.Builder().nutrition(3).saturationModifier(0.6F).build();
	public static final FoodProperties CHICKEN = new FoodProperties.Builder()
		.nutrition(2)
		.saturationModifier(0.3F)
		.effect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F)
		.build();
	public static final FoodProperties CHORUS_FRUIT = new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).alwaysEdible().build();
	public static final FoodProperties COD = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build();
	public static final FoodProperties COOKED_BEEF = new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build();
	public static final FoodProperties COOKED_CHICKEN = new FoodProperties.Builder().nutrition(6).saturationModifier(0.6F).build();
	public static final FoodProperties COOKED_COD = new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build();
	public static final FoodProperties COOKED_MUTTON = new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).build();
	public static final FoodProperties COOKED_PORKCHOP = new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build();
	public static final FoodProperties COOKED_RABBIT = new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build();
	public static final FoodProperties COOKED_SALMON = new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).build();
	public static final FoodProperties COOKIE = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build();
	public static final FoodProperties DRIED_KELP = new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).fast().build();
	public static final FoodProperties ENCHANTED_GOLDEN_APPLE = new FoodProperties.Builder()
		.nutrition(4)
		.saturationModifier(1.2F)
		.effect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1), 1.0F)
		.effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0), 1.0F)
		.effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0), 1.0F)
		.effect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3), 1.0F)
		.alwaysEdible()
		.build();
	public static final FoodProperties GOLDEN_APPLE = new FoodProperties.Builder()
		.nutrition(4)
		.saturationModifier(1.2F)
		.effect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1), 1.0F)
		.effect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0), 1.0F)
		.alwaysEdible()
		.build();
	public static final FoodProperties GOLDEN_POISONOUS_POTATO = new FoodProperties.Builder()
		.nutrition(2)
		.saturationModifier(1.2F)
		.effect(new MobEffectInstance(MobEffects.POISON, 100, 1), 1.0F)
		.effect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0), 1.0F)
		.alwaysEdible()
		.build();
	public static final FoodProperties ENCHANTED_GOLDEN_POISONOUS_POTATO = new FoodProperties.Builder()
		.nutrition(2)
		.saturationModifier(1.2F)
		.effect(new MobEffectInstance(MobEffects.POISON, 400, 7), 1.0F)
		.effect(new MobEffectInstance(MobEffects.LUCK, 6000, 4), 1.0F)
		.effect(new MobEffectInstance(MobEffects.UNLUCK, 6000, 4), 1.0F)
		.effect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3), 1.0F)
		.alwaysEdible()
		.build();
	public static final FoodProperties GOLDEN_CARROT = new FoodProperties.Builder().nutrition(6).saturationModifier(1.2F).build();
	public static final FoodProperties HONEY_BOTTLE = new FoodProperties.Builder().nutrition(6).saturationModifier(0.1F).eatSound(SoundEvents.HONEY_DRINK).build();
	public static final FoodProperties MELON_SLICE = new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build();
	public static final FoodProperties MUSHROOM_STEW = stew(6).build();
	public static final FoodProperties MUTTON = new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build();
	public static final FoodProperties POISONOUS_POTATO = new FoodProperties.Builder()
		.nutrition(2)
		.saturationModifier(0.3F)
		.effect(new MobEffectInstance(MobEffects.POISON, 100, 0), 0.6F)
		.build();
	public static final FoodProperties POTATO_FRUIT = new FoodProperties.Builder()
		.nutrition(6)
		.saturationModifier(1.2F)
		.effect(new MobEffectInstance(MobEffects.POISON, 40, 0), 1.0F)
		.build();
	public static final FoodProperties PORKCHOP = new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build();
	public static final FoodProperties POTATO = new FoodProperties.Builder().nutrition(1).saturationModifier(0.3F).build();
	public static final FoodProperties PUFFERFISH = new FoodProperties.Builder()
		.nutrition(1)
		.saturationModifier(0.1F)
		.effect(new MobEffectInstance(MobEffects.POISON, 1200, 1), 1.0F)
		.effect(new MobEffectInstance(MobEffects.HUNGER, 300, 2), 1.0F)
		.effect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0), 1.0F)
		.build();
	public static final FoodProperties PUMPKIN_PIE = new FoodProperties.Builder().nutrition(8).saturationModifier(0.3F).build();
	public static final FoodProperties RABBIT = new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build();
	public static final FoodProperties RABBIT_STEW = stew(10).build();
	public static final FoodProperties ROTTEN_FLESH = new FoodProperties.Builder()
		.nutrition(4)
		.saturationModifier(0.1F)
		.effect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.8F)
		.build();
	public static final FoodProperties SALMON = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build();
	public static final FoodProperties SPIDER_EYE = new FoodProperties.Builder()
		.nutrition(2)
		.saturationModifier(0.8F)
		.effect(new MobEffectInstance(MobEffects.POISON, 100, 0), 1.0F)
		.build();
	public static final FoodProperties SUSPICIOUS_STEW = stew(6).alwaysEdible().build();
	public static final FoodProperties SWEET_BERRIES = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build();
	public static final FoodProperties GLOW_BERRIES = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build();
	public static final FoodProperties TROPICAL_FISH = new FoodProperties.Builder().nutrition(1).saturationModifier(0.1F).build();
	public static final FoodProperties HASH_BROWNS = new FoodProperties.Builder().nutrition(2).saturationModifier(0.6F).build();

	private static FoodProperties.Builder stew(int i) {
		return new FoodProperties.Builder().nutrition(i).saturationModifier(0.6F);
	}
}
