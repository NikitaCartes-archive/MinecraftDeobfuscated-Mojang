/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.alchemy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;

public class Potions {
    public static final Potion EMPTY = Potions.register("empty", new Potion(new MobEffectInstance[0]));
    public static final Potion WATER = Potions.register("water", new Potion(new MobEffectInstance[0]));
    public static final Potion MUNDANE = Potions.register("mundane", new Potion(new MobEffectInstance[0]));
    public static final Potion THICK = Potions.register("thick", new Potion(new MobEffectInstance[0]));
    public static final Potion AWKWARD = Potions.register("awkward", new Potion(new MobEffectInstance[0]));
    public static final Potion NIGHT_VISION = Potions.register("night_vision", new Potion(new MobEffectInstance(MobEffects.NIGHT_VISION, 3600)));
    public static final Potion LONG_NIGHT_VISION = Potions.register("long_night_vision", new Potion("night_vision", new MobEffectInstance(MobEffects.NIGHT_VISION, 9600)));
    public static final Potion INVISIBILITY = Potions.register("invisibility", new Potion(new MobEffectInstance(MobEffects.INVISIBILITY, 3600)));
    public static final Potion LONG_INVISIBILITY = Potions.register("long_invisibility", new Potion("invisibility", new MobEffectInstance(MobEffects.INVISIBILITY, 9600)));
    public static final Potion LEAPING = Potions.register("leaping", new Potion(new MobEffectInstance(MobEffects.JUMP, 3600)));
    public static final Potion LONG_LEAPING = Potions.register("long_leaping", new Potion("leaping", new MobEffectInstance(MobEffects.JUMP, 9600)));
    public static final Potion STRONG_LEAPING = Potions.register("strong_leaping", new Potion("leaping", new MobEffectInstance(MobEffects.JUMP, 1800, 1)));
    public static final Potion FIRE_RESISTANCE = Potions.register("fire_resistance", new Potion(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600)));
    public static final Potion LONG_FIRE_RESISTANCE = Potions.register("long_fire_resistance", new Potion("fire_resistance", new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600)));
    public static final Potion SWIFTNESS = Potions.register("swiftness", new Potion(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600)));
    public static final Potion LONG_SWIFTNESS = Potions.register("long_swiftness", new Potion("swiftness", new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 9600)));
    public static final Potion STRONG_SWIFTNESS = Potions.register("strong_swiftness", new Potion("swiftness", new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1800, 1)));
    public static final Potion SLOWNESS = Potions.register("slowness", new Potion(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1800)));
    public static final Potion LONG_SLOWNESS = Potions.register("long_slowness", new Potion("slowness", new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 4800)));
    public static final Potion STRONG_SLOWNESS = Potions.register("strong_slowness", new Potion("slowness", new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 3)));
    public static final Potion TURTLE_MASTER = Potions.register("turtle_master", new Potion("turtle_master", new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 3), new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2)));
    public static final Potion LONG_TURTLE_MASTER = Potions.register("long_turtle_master", new Potion("turtle_master", new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 800, 3), new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 800, 2)));
    public static final Potion STRONG_TURTLE_MASTER = Potions.register("strong_turtle_master", new Potion("turtle_master", new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 5), new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 3)));
    public static final Potion WATER_BREATHING = Potions.register("water_breathing", new Potion(new MobEffectInstance(MobEffects.WATER_BREATHING, 3600)));
    public static final Potion LONG_WATER_BREATHING = Potions.register("long_water_breathing", new Potion("water_breathing", new MobEffectInstance(MobEffects.WATER_BREATHING, 9600)));
    public static final Potion HEALING = Potions.register("healing", new Potion(new MobEffectInstance(MobEffects.HEAL, 1)));
    public static final Potion STRONG_HEALING = Potions.register("strong_healing", new Potion("healing", new MobEffectInstance(MobEffects.HEAL, 1, 1)));
    public static final Potion HARMING = Potions.register("harming", new Potion(new MobEffectInstance(MobEffects.HARM, 1)));
    public static final Potion STRONG_HARMING = Potions.register("strong_harming", new Potion("harming", new MobEffectInstance(MobEffects.HARM, 1, 1)));
    public static final Potion POISON = Potions.register("poison", new Potion(new MobEffectInstance(MobEffects.POISON, 900)));
    public static final Potion LONG_POISON = Potions.register("long_poison", new Potion("poison", new MobEffectInstance(MobEffects.POISON, 1800)));
    public static final Potion STRONG_POISON = Potions.register("strong_poison", new Potion("poison", new MobEffectInstance(MobEffects.POISON, 432, 1)));
    public static final Potion REGENERATION = Potions.register("regeneration", new Potion(new MobEffectInstance(MobEffects.REGENERATION, 900)));
    public static final Potion LONG_REGENERATION = Potions.register("long_regeneration", new Potion("regeneration", new MobEffectInstance(MobEffects.REGENERATION, 1800)));
    public static final Potion STRONG_REGENERATION = Potions.register("strong_regeneration", new Potion("regeneration", new MobEffectInstance(MobEffects.REGENERATION, 450, 1)));
    public static final Potion STRENGTH = Potions.register("strength", new Potion(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600)));
    public static final Potion LONG_STRENGTH = Potions.register("long_strength", new Potion("strength", new MobEffectInstance(MobEffects.DAMAGE_BOOST, 9600)));
    public static final Potion STRONG_STRENGTH = Potions.register("strong_strength", new Potion("strength", new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1800, 1)));
    public static final Potion WEAKNESS = Potions.register("weakness", new Potion(new MobEffectInstance(MobEffects.WEAKNESS, 1800)));
    public static final Potion LONG_WEAKNESS = Potions.register("long_weakness", new Potion("weakness", new MobEffectInstance(MobEffects.WEAKNESS, 4800)));
    public static final Potion LUCK = Potions.register("luck", new Potion("luck", new MobEffectInstance(MobEffects.LUCK, 6000)));
    public static final Potion SLOW_FALLING = Potions.register("slow_falling", new Potion(new MobEffectInstance(MobEffects.SLOW_FALLING, 1800)));
    public static final Potion LONG_SLOW_FALLING = Potions.register("long_slow_falling", new Potion("slow_falling", new MobEffectInstance(MobEffects.SLOW_FALLING, 4800)));

    private static Potion register(String string, Potion potion) {
        return Registry.register(BuiltInRegistries.POTION, string, potion);
    }
}

