/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public record CatVariant(ResourceLocation texture) {
    public static final CatVariant TABBY = CatVariant.register("tabby", "textures/entity/cat/tabby.png");
    public static final CatVariant BLACK = CatVariant.register("black", "textures/entity/cat/black.png");
    public static final CatVariant RED = CatVariant.register("red", "textures/entity/cat/red.png");
    public static final CatVariant SIAMESE = CatVariant.register("siamese", "textures/entity/cat/siamese.png");
    public static final CatVariant BRITISH = CatVariant.register("british", "textures/entity/cat/british_shorthair.png");
    public static final CatVariant CALICO = CatVariant.register("calico", "textures/entity/cat/calico.png");
    public static final CatVariant PERSIAN = CatVariant.register("persian", "textures/entity/cat/persian.png");
    public static final CatVariant RAGDOLL = CatVariant.register("ragdoll", "textures/entity/cat/ragdoll.png");
    public static final CatVariant WHITE = CatVariant.register("white", "textures/entity/cat/white.png");
    public static final CatVariant JELLIE = CatVariant.register("jellie", "textures/entity/cat/jellie.png");
    public static final CatVariant ALL_BLACK = CatVariant.register("all_black", "textures/entity/cat/all_black.png");

    private static CatVariant register(String string, String string2) {
        return Registry.register(Registry.CAT_VARIANT, string, new CatVariant(new ResourceLocation(string2)));
    }
}

