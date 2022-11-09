/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public record FrogVariant(ResourceLocation texture) {
    public static final FrogVariant TEMPERATE = FrogVariant.register("temperate", "textures/entity/frog/temperate_frog.png");
    public static final FrogVariant WARM = FrogVariant.register("warm", "textures/entity/frog/warm_frog.png");
    public static final FrogVariant COLD = FrogVariant.register("cold", "textures/entity/frog/cold_frog.png");

    private static FrogVariant register(String string, String string2) {
        return Registry.register(BuiltInRegistries.FROG_VARIANT, string, new FrogVariant(new ResourceLocation(string2)));
    }
}

