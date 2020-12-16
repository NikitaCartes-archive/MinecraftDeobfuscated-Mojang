/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.StaticTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.entity.EntityType;

public final class EntityTypeTags {
    protected static final StaticTagHelper<EntityType<?>> HELPER = StaticTags.create(Registry.ENTITY_TYPE_REGISTRY, "tags/entity_types");
    public static final Tag.Named<EntityType<?>> SKELETONS = EntityTypeTags.bind("skeletons");
    public static final Tag.Named<EntityType<?>> RAIDERS = EntityTypeTags.bind("raiders");
    public static final Tag.Named<EntityType<?>> BEEHIVE_INHABITORS = EntityTypeTags.bind("beehive_inhabitors");
    public static final Tag.Named<EntityType<?>> ARROWS = EntityTypeTags.bind("arrows");
    public static final Tag.Named<EntityType<?>> IMPACT_PROJECTILES = EntityTypeTags.bind("impact_projectiles");
    public static final Tag.Named<EntityType<?>> POWDER_SNOW_WALKABLE_MOBS = EntityTypeTags.bind("powder_snow_walkable_mobs");
    public static final Tag.Named<EntityType<?>> AXOLOTL_ALWAYS_HOSTILES = EntityTypeTags.bind("axolotl_always_hostiles");
    public static final Tag.Named<EntityType<?>> AXOLOTL_TEMPTED_HOSTILES = EntityTypeTags.bind("axolotl_tempted_hostiles");

    private static Tag.Named<EntityType<?>> bind(String string) {
        return HELPER.bind(string);
    }

    public static TagCollection<EntityType<?>> getAllTags() {
        return HELPER.getAllTags();
    }
}

