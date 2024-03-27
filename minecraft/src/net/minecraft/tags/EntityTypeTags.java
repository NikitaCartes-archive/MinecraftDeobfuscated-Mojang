package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public interface EntityTypeTags {
	TagKey<EntityType<?>> SKELETONS = create("skeletons");
	TagKey<EntityType<?>> ZOMBIES = create("zombies");
	TagKey<EntityType<?>> RAIDERS = create("raiders");
	TagKey<EntityType<?>> UNDEAD = create("undead");
	TagKey<EntityType<?>> BEEHIVE_INHABITORS = create("beehive_inhabitors");
	TagKey<EntityType<?>> ARROWS = create("arrows");
	TagKey<EntityType<?>> IMPACT_PROJECTILES = create("impact_projectiles");
	TagKey<EntityType<?>> POWDER_SNOW_WALKABLE_MOBS = create("powder_snow_walkable_mobs");
	TagKey<EntityType<?>> AXOLOTL_ALWAYS_HOSTILES = create("axolotl_always_hostiles");
	TagKey<EntityType<?>> AXOLOTL_HUNT_TARGETS = create("axolotl_hunt_targets");
	TagKey<EntityType<?>> FREEZE_IMMUNE_ENTITY_TYPES = create("freeze_immune_entity_types");
	TagKey<EntityType<?>> FREEZE_HURTS_EXTRA_TYPES = create("freeze_hurts_extra_types");
	TagKey<EntityType<?>> CAN_BREATHE_UNDER_WATER = create("can_breathe_under_water");
	TagKey<EntityType<?>> FROG_FOOD = create("frog_food");
	TagKey<EntityType<?>> FALL_DAMAGE_IMMUNE = create("fall_damage_immune");
	TagKey<EntityType<?>> DISMOUNTS_UNDERWATER = create("dismounts_underwater");
	TagKey<EntityType<?>> NON_CONTROLLING_RIDER = create("non_controlling_rider");
	TagKey<EntityType<?>> DEFLECTS_PROJECTILES = create("deflects_projectiles");
	TagKey<EntityType<?>> CAN_TURN_IN_BOATS = create("can_turn_in_boats");
	TagKey<EntityType<?>> ILLAGER = create("illager");
	TagKey<EntityType<?>> AQUATIC = create("aquatic");
	TagKey<EntityType<?>> ARTHROPOD = create("arthropod");
	TagKey<EntityType<?>> IGNORES_POISON_AND_REGEN = create("ignores_poison_and_regen");
	TagKey<EntityType<?>> INVERTED_HEALING_AND_HARM = create("inverted_healing_and_harm");
	TagKey<EntityType<?>> WITHER_FRIENDS = create("wither_friends");
	TagKey<EntityType<?>> ILLAGER_FRIENDS = create("illager_friends");
	TagKey<EntityType<?>> NOT_SCARY_FOR_PUFFERFISH = create("not_scary_for_pufferfish");
	TagKey<EntityType<?>> SENSITIVE_TO_IMPALING = create("sensitive_to_impaling");
	TagKey<EntityType<?>> SENSITIVE_TO_BANE_OF_ARTHROPODS = create("sensitive_to_bane_of_arthropods");
	TagKey<EntityType<?>> SENSITIVE_TO_SMITE = create("sensitive_to_smite");
	TagKey<EntityType<?>> NO_ANGER_FROM_WIND_CHARGE = create("no_anger_from_wind_charge");
	TagKey<EntityType<?>> IMMUNE_TO_OOZING = create("immune_to_oozing");
	TagKey<EntityType<?>> IMMUNE_TO_INFESTED = create("immune_to_infested");

	private static TagKey<EntityType<?>> create(String string) {
		return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(string));
	}
}
