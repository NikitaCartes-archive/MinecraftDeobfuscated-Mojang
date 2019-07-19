/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.material.Fluid;

public class FluidTags {
    private static TagCollection<Fluid> source = new TagCollection(resourceLocation -> Optional.empty(), "", false, "");
    private static int resetCount;
    public static final Tag<Fluid> WATER;
    public static final Tag<Fluid> LAVA;

    public static void reset(TagCollection<Fluid> tagCollection) {
        source = tagCollection;
        ++resetCount;
    }

    private static Tag<Fluid> bind(String string) {
        return new Wrapper(new ResourceLocation(string));
    }

    static {
        WATER = FluidTags.bind("water");
        LAVA = FluidTags.bind("lava");
    }

    public static class Wrapper
    extends Tag<Fluid> {
        private int check = -1;
        private Tag<Fluid> actual;

        public Wrapper(ResourceLocation resourceLocation) {
            super(resourceLocation);
        }

        @Override
        public boolean contains(Fluid fluid) {
            if (this.check != resetCount) {
                this.actual = source.getTagOrEmpty(this.getId());
                this.check = resetCount;
            }
            return this.actual.contains(fluid);
        }

        @Override
        public Collection<Fluid> getValues() {
            if (this.check != resetCount) {
                this.actual = source.getTagOrEmpty(this.getId());
                this.check = resetCount;
            }
            return this.actual.getValues();
        }

        @Override
        public Collection<Tag.Entry<Fluid>> getSource() {
            if (this.check != resetCount) {
                this.actual = source.getTagOrEmpty(this.getId());
                this.check = resetCount;
            }
            return this.actual.getSource();
        }
    }
}

