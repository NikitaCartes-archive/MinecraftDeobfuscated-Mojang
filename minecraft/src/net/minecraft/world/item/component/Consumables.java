package net.minecraft.world.item.component;

import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;

public class Consumables {
	public static final Consumable DEFAULT_FOOD = defaultFood().build();
	public static final Consumable DEFAULT_DRINK = defaultDrink().build();
	public static final Consumable HONEY_BOTTLE = defaultDrink()
		.consumeSeconds(2.0F)
		.sound(SoundEvents.HONEY_DRINK)
		.onConsume(new RemoveStatusEffectsConsumeEffect(MobEffects.POISON))
		.build();
	public static final Consumable OMINOUS_BOTTLE = defaultDrink().soundAfterConsume(SoundEvents.OMINOUS_BOTTLE_DISPOSE).build();
	public static final Consumable DRIED_KELP = defaultFood().consumeSeconds(0.8F).build();
	public static final Consumable CHICKEN = defaultFood()
		.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F))
		.build();
	public static final Consumable ENCHANTED_GOLDEN_APPLE = defaultFood()
		.onConsume(
			new ApplyStatusEffectsConsumeEffect(
				List.of(
					new MobEffectInstance(MobEffects.REGENERATION, 400, 1),
					new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0),
					new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0),
					new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3)
				)
			)
		)
		.build();
	public static final Consumable GOLDEN_APPLE = defaultFood()
		.onConsume(
			new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(MobEffects.REGENERATION, 100, 1), new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0)))
		)
		.build();
	public static final Consumable POISONOUS_POTATO = defaultFood()
		.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), 0.6F))
		.build();
	public static final Consumable PUFFERFISH = defaultFood()
		.onConsume(
			new ApplyStatusEffectsConsumeEffect(
				List.of(
					new MobEffectInstance(MobEffects.POISON, 1200, 1), new MobEffectInstance(MobEffects.HUNGER, 300, 2), new MobEffectInstance(MobEffects.CONFUSION, 300, 0)
				)
			)
		)
		.build();
	public static final Consumable ROTTEN_FLESH = defaultFood()
		.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.8F))
		.build();
	public static final Consumable SPIDER_EYE = defaultFood()
		.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 100, 0)))
		.build();
	public static final Consumable MILK_BUCKET = defaultDrink().onConsume(ClearAllStatusEffectsConsumeEffect.INSTANCE).build();
	public static final Consumable CHORUS_FRUIT = defaultFood().onConsume(new TeleportRandomlyConsumeEffect()).build();

	public static Consumable.Builder defaultFood() {
		return Consumable.builder().consumeSeconds(1.6F).animation(ItemUseAnimation.EAT).sound(SoundEvents.GENERIC_EAT).hasConsumeParticles(true);
	}

	public static Consumable.Builder defaultDrink() {
		return Consumable.builder().consumeSeconds(1.6F).animation(ItemUseAnimation.DRINK).sound(SoundEvents.GENERIC_DRINK).hasConsumeParticles(false);
	}
}
