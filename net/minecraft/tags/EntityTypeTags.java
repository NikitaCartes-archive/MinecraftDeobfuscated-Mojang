/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.entity.EntityType;

public class EntityTypeTags {
    private static TagCollection<EntityType<?>> source = new TagCollection(resourceLocation -> Optional.empty(), "", false, "");
    private static int resetCount;
    public static final Tag<EntityType<?>> SKELETONS;
    public static final Tag<EntityType<?>> RAIDERS;
    public static final Tag<EntityType<?>> BEEHIVE_INHABITORS;
    public static final Tag<EntityType<?>> ARROWS;
    public static final Tag<EntityType<?>> IMPACT_PROJECTILES;

    public static void reset(TagCollection<EntityType<?>> tagCollection) {
        source = tagCollection;
        ++resetCount;
    }

    public static TagCollection<EntityType<?>> getAllTags() {
        return source;
    }

    private static Tag<EntityType<?>> bind(String string) {
        return new Wrapper(new ResourceLocation(string));
    }

    static {
        SKELETONS = EntityTypeTags.bind("skeletons");
        RAIDERS = EntityTypeTags.bind("raiders");
        BEEHIVE_INHABITORS = EntityTypeTags.bind("beehive_inhabitors");
        ARROWS = EntityTypeTags.bind("arrows");
        IMPACT_PROJECTILES = EntityTypeTags.bind("impact_projectiles");
    }

    public static class Wrapper
    extends Tag<EntityType<?>> {
        private int check = -1;
        private Tag<EntityType<?>> actual;

        public Wrapper(ResourceLocation resourceLocation) {
            super(resourceLocation);
        }

        @Override
        public boolean contains(EntityType<?> entityType) {
            if (this.check != resetCount) {
                this.actual = source.getTagOrEmpty(this.getId());
                this.check = resetCount;
            }
            return this.actual.contains(entityType);
        }

        @Override
        public Collection<EntityType<?>> getValues() {
            if (this.check != resetCount) {
                this.actual = source.getTagOrEmpty(this.getId());
                this.check = resetCount;
            }
            return this.actual.getValues();
        }

        @Override
        public Collection<Tag.Entry<EntityType<?>>> getSource() {
            if (this.check != resetCount) {
                this.actual = source.getTagOrEmpty(this.getId());
                this.check = resetCount;
            }
            return this.actual.getSource();
        }
    }
}

