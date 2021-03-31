/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;

public interface IntProviderType<P extends IntProvider> {
    public static final IntProviderType<ConstantInt> CONSTANT = IntProviderType.register("constant", ConstantInt.CODEC);
    public static final IntProviderType<UniformInt> UNIFORM = IntProviderType.register("uniform", UniformInt.CODEC);

    public Codec<P> codec();

    public static <P extends IntProvider> IntProviderType<P> register(String string, Codec<P> codec) {
        return Registry.register(Registry.INT_PROVIDER_TYPES, string, () -> codec);
    }
}

