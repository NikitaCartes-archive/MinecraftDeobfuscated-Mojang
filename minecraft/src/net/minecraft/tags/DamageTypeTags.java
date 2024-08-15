package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public interface DamageTypeTags {
	TagKey<DamageType> DAMAGES_HELMET = create("damages_helmet");
	TagKey<DamageType> BYPASSES_ARMOR = create("bypasses_armor");
	TagKey<DamageType> BYPASSES_SHIELD = create("bypasses_shield");
	TagKey<DamageType> BYPASSES_INVULNERABILITY = create("bypasses_invulnerability");
	TagKey<DamageType> BYPASSES_COOLDOWN = create("bypasses_cooldown");
	TagKey<DamageType> BYPASSES_EFFECTS = create("bypasses_effects");
	TagKey<DamageType> BYPASSES_RESISTANCE = create("bypasses_resistance");
	TagKey<DamageType> BYPASSES_ENCHANTMENTS = create("bypasses_enchantments");
	TagKey<DamageType> IS_FIRE = create("is_fire");
	TagKey<DamageType> IS_PROJECTILE = create("is_projectile");
	TagKey<DamageType> WITCH_RESISTANT_TO = create("witch_resistant_to");
	TagKey<DamageType> IS_EXPLOSION = create("is_explosion");
	TagKey<DamageType> IS_FALL = create("is_fall");
	TagKey<DamageType> IS_DROWNING = create("is_drowning");
	TagKey<DamageType> IS_FREEZING = create("is_freezing");
	TagKey<DamageType> IS_LIGHTNING = create("is_lightning");
	TagKey<DamageType> NO_ANGER = create("no_anger");
	TagKey<DamageType> NO_IMPACT = create("no_impact");
	TagKey<DamageType> ALWAYS_MOST_SIGNIFICANT_FALL = create("always_most_significant_fall");
	TagKey<DamageType> WITHER_IMMUNE_TO = create("wither_immune_to");
	TagKey<DamageType> IGNITES_ARMOR_STANDS = create("ignites_armor_stands");
	TagKey<DamageType> BURNS_ARMOR_STANDS = create("burns_armor_stands");
	TagKey<DamageType> AVOIDS_GUARDIAN_THORNS = create("avoids_guardian_thorns");
	TagKey<DamageType> ALWAYS_TRIGGERS_SILVERFISH = create("always_triggers_silverfish");
	TagKey<DamageType> ALWAYS_HURTS_ENDER_DRAGONS = create("always_hurts_ender_dragons");
	TagKey<DamageType> NO_KNOCKBACK = create("no_knockback");
	TagKey<DamageType> ALWAYS_KILLS_ARMOR_STANDS = create("always_kills_armor_stands");
	TagKey<DamageType> CAN_BREAK_ARMOR_STAND = create("can_break_armor_stand");
	TagKey<DamageType> BYPASSES_WOLF_ARMOR = create("bypasses_wolf_armor");
	TagKey<DamageType> IS_PLAYER_ATTACK = create("is_player_attack");
	TagKey<DamageType> BURN_FROM_STEPPING = create("burn_from_stepping");
	TagKey<DamageType> PANIC_CAUSES = create("panic_causes");
	TagKey<DamageType> PANIC_ENVIRONMENTAL_CAUSES = create("panic_environmental_causes");
	TagKey<DamageType> IS_MACE_SMASH = create("mace_smash");

	private static TagKey<DamageType> create(String string) {
		return TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.withDefaultNamespace(string));
	}
}
