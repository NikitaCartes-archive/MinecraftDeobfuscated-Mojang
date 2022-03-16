/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

public class BuiltinDimensionTypes {
    public static final ResourceKey<DimensionType> OVERWORLD = BuiltinDimensionTypes.register("overworld");
    public static final ResourceKey<DimensionType> NETHER = BuiltinDimensionTypes.register("the_nether");
    public static final ResourceKey<DimensionType> END = BuiltinDimensionTypes.register("the_end");
    public static final ResourceKey<DimensionType> OVERWORLD_CAVES = BuiltinDimensionTypes.register("overworld_caves");
    public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("overworld");
    public static final ResourceLocation NETHER_EFFECTS = new ResourceLocation("the_nether");
    public static final ResourceLocation END_EFFECTS = new ResourceLocation("the_end");

    private static ResourceKey<DimensionType> register(String string) {
        return ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(string));
    }
}

