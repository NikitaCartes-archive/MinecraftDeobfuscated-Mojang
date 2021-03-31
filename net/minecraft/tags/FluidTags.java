/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.StaticTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.material.Fluid;

public final class FluidTags {
    protected static final StaticTagHelper<Fluid> HELPER = StaticTags.create(Registry.FLUID_REGISTRY, "tags/fluids");
    private static final List<Tag<Fluid>> KNOWN_TAGS = Lists.newArrayList();
    public static final Tag.Named<Fluid> WATER = FluidTags.bind("water");
    public static final Tag.Named<Fluid> LAVA = FluidTags.bind("lava");

    private FluidTags() {
    }

    private static Tag.Named<Fluid> bind(String string) {
        Tag.Named<Fluid> named = HELPER.bind(string);
        KNOWN_TAGS.add(named);
        return named;
    }

    public static TagCollection<Fluid> getAllTags() {
        return HELPER.getAllTags();
    }

    @Deprecated
    public static List<Tag<Fluid>> getStaticTags() {
        return KNOWN_TAGS;
    }
}

