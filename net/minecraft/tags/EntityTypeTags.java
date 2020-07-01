/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.StaticTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.entity.EntityType;

public final class EntityTypeTags {
    protected static final StaticTagHelper<EntityType<?>> HELPER = StaticTags.create(new ResourceLocation("entity_type"), TagContainer::getEntityTypes);
    public static final Tag.Named<EntityType<?>> SKELETONS = EntityTypeTags.bind("skeletons");
    public static final Tag.Named<EntityType<?>> RAIDERS = EntityTypeTags.bind("raiders");
    public static final Tag.Named<EntityType<?>> BEEHIVE_INHABITORS = EntityTypeTags.bind("beehive_inhabitors");
    public static final Tag.Named<EntityType<?>> ARROWS = EntityTypeTags.bind("arrows");
    public static final Tag.Named<EntityType<?>> IMPACT_PROJECTILES = EntityTypeTags.bind("impact_projectiles");

    private static Tag.Named<EntityType<?>> bind(String string) {
        return HELPER.bind(string);
    }

    public static TagCollection<EntityType<?>> getAllTags() {
        return HELPER.getAllTags();
    }
}

