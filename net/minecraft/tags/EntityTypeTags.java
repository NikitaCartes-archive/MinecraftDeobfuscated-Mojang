/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.entity.EntityType;

public class EntityTypeTags {
    private static final StaticTagHelper<EntityType<?>> HELPER = new StaticTagHelper();
    public static final Tag.Named<EntityType<?>> SKELETONS = EntityTypeTags.bind("skeletons");
    public static final Tag.Named<EntityType<?>> RAIDERS = EntityTypeTags.bind("raiders");
    public static final Tag.Named<EntityType<?>> BEEHIVE_INHABITORS = EntityTypeTags.bind("beehive_inhabitors");
    public static final Tag.Named<EntityType<?>> ARROWS = EntityTypeTags.bind("arrows");
    public static final Tag.Named<EntityType<?>> IMPACT_PROJECTILES = EntityTypeTags.bind("impact_projectiles");

    private static Tag.Named<EntityType<?>> bind(String string) {
        return HELPER.bind(string);
    }

    public static void reset(TagCollection<EntityType<?>> tagCollection) {
        HELPER.reset(tagCollection);
    }

    @Environment(value=EnvType.CLIENT)
    public static void resetToEmpty() {
        HELPER.resetToEmpty();
    }

    public static TagCollection<EntityType<?>> getAllTags() {
        return HELPER.getAllTags();
    }

    public static Set<ResourceLocation> getMissingTags(TagCollection<EntityType<?>> tagCollection) {
        return HELPER.getMissingTags(tagCollection);
    }
}

