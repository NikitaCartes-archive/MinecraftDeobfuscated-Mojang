/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.CatVariant;

public class CatVariantTags {
    public static final TagKey<CatVariant> DEFAULT_SPAWNS = CatVariantTags.create("default_spawns");
    public static final TagKey<CatVariant> FULL_MOON_SPAWNS = CatVariantTags.create("full_moon_spawns");

    private CatVariantTags() {
    }

    private static TagKey<CatVariant> create(String string) {
        return TagKey.create(Registry.CAT_VARIANT_REGISTRY, new ResourceLocation(string));
    }
}

