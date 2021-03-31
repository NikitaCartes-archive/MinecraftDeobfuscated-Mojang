package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;

public final class EntityTypeTags {
	protected static final StaticTagHelper<EntityType<?>> HELPER = StaticTags.create(Registry.ENTITY_TYPE_REGISTRY, "tags/entity_types");
	public static final Tag.Named<EntityType<?>> SKELETONS = bind("skeletons");
	public static final Tag.Named<EntityType<?>> RAIDERS = bind("raiders");
	public static final Tag.Named<EntityType<?>> BEEHIVE_INHABITORS = bind("beehive_inhabitors");
	public static final Tag.Named<EntityType<?>> ARROWS = bind("arrows");
	public static final Tag.Named<EntityType<?>> IMPACT_PROJECTILES = bind("impact_projectiles");
	public static final Tag.Named<EntityType<?>> POWDER_SNOW_WALKABLE_MOBS = bind("powder_snow_walkable_mobs");
	public static final Tag.Named<EntityType<?>> AXOLOTL_ALWAYS_HOSTILES = bind("axolotl_always_hostiles");
	public static final Tag.Named<EntityType<?>> AXOLOTL_HUNT_TARGETS = bind("axolotl_hunt_targets");
	public static final Tag.Named<EntityType<?>> FREEZE_IMMUNE_ENTITY_TYPES = bind("freeze_immune_entity_types");
	public static final Tag.Named<EntityType<?>> FREEZE_HURTS_EXTRA_TYPES = bind("freeze_hurts_extra_types");

	private EntityTypeTags() {
	}

	private static Tag.Named<EntityType<?>> bind(String string) {
		return HELPER.bind(string);
	}

	public static TagCollection<EntityType<?>> getAllTags() {
		return HELPER.getAllTags();
	}
}
