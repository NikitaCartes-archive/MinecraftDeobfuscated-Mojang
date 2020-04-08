/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.material.Fluid;

public class FluidTags {
    private static final StaticTagHelper<Fluid> HELPER = new StaticTagHelper();
    public static final Tag.Named<Fluid> WATER = FluidTags.bind("water");
    public static final Tag.Named<Fluid> LAVA = FluidTags.bind("lava");

    private static Tag.Named<Fluid> bind(String string) {
        return HELPER.bind(string);
    }

    public static void reset(TagCollection<Fluid> tagCollection) {
        HELPER.reset(tagCollection);
    }

    @Environment(value=EnvType.CLIENT)
    public static void resetToEmpty() {
        HELPER.resetToEmpty();
    }

    public static TagCollection<Fluid> getAllTags() {
        return HELPER.getAllTags();
    }
}

