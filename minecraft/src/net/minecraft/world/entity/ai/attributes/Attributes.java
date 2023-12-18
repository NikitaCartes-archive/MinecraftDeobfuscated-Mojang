package net.minecraft.world.entity.ai.attributes;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class Attributes {
	public static final Holder<Attribute> ARMOR = register("generic.armor", new RangedAttribute("attribute.name.generic.armor", 0.0, 0.0, 30.0).setSyncable(true));
	public static final Holder<Attribute> ARMOR_TOUGHNESS = register(
		"generic.armor_toughness", new RangedAttribute("attribute.name.generic.armor_toughness", 0.0, 0.0, 20.0).setSyncable(true)
	);
	public static final Holder<Attribute> ATTACK_DAMAGE = register(
		"generic.attack_damage", new RangedAttribute("attribute.name.generic.attack_damage", 2.0, 0.0, 2048.0)
	);
	public static final Holder<Attribute> ATTACK_KNOCKBACK = register(
		"generic.attack_knockback", new RangedAttribute("attribute.name.generic.attack_knockback", 0.0, 0.0, 5.0)
	);
	public static final Holder<Attribute> ATTACK_SPEED = register(
		"generic.attack_speed", new RangedAttribute("attribute.name.generic.attack_speed", 4.0, 0.0, 1024.0).setSyncable(true)
	);
	public static final Holder<Attribute> BLOCK_INTERACTION_RANGE = register(
		"generic.block_interaction_range", new RangedAttribute("attribute.name.generic.block_interaction_range", 4.5, 0.0, 64.0).setSyncable(true)
	);
	public static final Holder<Attribute> ENTITY_INTERACTION_RANGE = register(
		"generic.entity_interaction_range", new RangedAttribute("attribute.name.generic.entity_interaction_range", 3.0, 0.0, 64.0).setSyncable(true)
	);
	public static final Holder<Attribute> FLYING_SPEED = register(
		"generic.flying_speed", new RangedAttribute("attribute.name.generic.flying_speed", 0.4F, 0.0, 1024.0).setSyncable(true)
	);
	public static final Holder<Attribute> FOLLOW_RANGE = register(
		"generic.follow_range", new RangedAttribute("attribute.name.generic.follow_range", 32.0, 0.0, 2048.0)
	);
	public static final Holder<Attribute> JUMP_STRENGTH = register(
		"horse.jump_strength", new RangedAttribute("attribute.name.horse.jump_strength", 0.7, 0.0, 2.0).setSyncable(true)
	);
	public static final Holder<Attribute> KNOCKBACK_RESISTANCE = register(
		"generic.knockback_resistance", new RangedAttribute("attribute.name.generic.knockback_resistance", 0.0, 0.0, 1.0)
	);
	public static final Holder<Attribute> LUCK = register(
		"generic.luck", new RangedAttribute("attribute.name.generic.luck", 0.0, -1024.0, 1024.0).setSyncable(true)
	);
	public static final Holder<Attribute> MAX_ABSORPTION = register(
		"generic.max_absorption", new RangedAttribute("attribute.name.generic.max_absorption", 0.0, 0.0, 2048.0).setSyncable(true)
	);
	public static final Holder<Attribute> MAX_HEALTH = register(
		"generic.max_health", new RangedAttribute("attribute.name.generic.max_health", 20.0, 1.0, 1024.0).setSyncable(true)
	);
	public static final Holder<Attribute> MOVEMENT_SPEED = register(
		"generic.movement_speed", new RangedAttribute("attribute.name.generic.movement_speed", 0.7F, 0.0, 1024.0).setSyncable(true)
	);
	public static final Holder<Attribute> SCALE = register(
		"generic.scale", new RangedAttribute("attribute.name.generic.scale", 1.0, 0.0625, 16.0).setSyncable(true)
	);
	public static final Holder<Attribute> SPAWN_REINFORCEMENTS_CHANCE = register(
		"zombie.spawn_reinforcements", new RangedAttribute("attribute.name.zombie.spawn_reinforcements", 0.0, 0.0, 1.0)
	);
	public static final Holder<Attribute> STEP_HEIGHT = register(
		"generic.step_height", new RangedAttribute("attribute.name.generic.step_height", 0.6, 0.0, 10.0).setSyncable(true)
	);

	private static Holder<Attribute> register(String string, Attribute attribute) {
		return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, new ResourceLocation(string), attribute);
	}

	public static Holder<Attribute> bootstrap(Registry<Attribute> registry) {
		return MAX_HEALTH;
	}
}
