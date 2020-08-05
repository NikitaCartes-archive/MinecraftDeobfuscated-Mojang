/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.StaticTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.level.material.Fluid;

public final class FluidTags {
    protected static final StaticTagHelper<Fluid> HELPER = StaticTags.create(new ResourceLocation("fluid"), TagContainer::getFluids);
    public static final Tag.Named<Fluid> WATER = FluidTags.bind("water");
    public static final Tag.Named<Fluid> LAVA = FluidTags.bind("lava");

    private static Tag.Named<Fluid> bind(String string) {
        return HELPER.bind(string);
    }

    public static List<? extends Tag.Named<Fluid>> getWrappers() {
        return HELPER.getWrappers();
    }
}

