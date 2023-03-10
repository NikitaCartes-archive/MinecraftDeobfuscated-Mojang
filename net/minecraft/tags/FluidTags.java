/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class FluidTags {
    public static final TagKey<Fluid> WATER = FluidTags.create("water");
    public static final TagKey<Fluid> LAVA = FluidTags.create("lava");

    private FluidTags() {
    }

    private static TagKey<Fluid> create(String string) {
        return TagKey.create(Registries.FLUID, new ResourceLocation(string));
    }
}

