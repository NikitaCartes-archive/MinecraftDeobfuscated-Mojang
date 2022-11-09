/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.ClampedNormalInt;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;

public interface IntProviderType<P extends IntProvider> {
    public static final IntProviderType<ConstantInt> CONSTANT = IntProviderType.register("constant", ConstantInt.CODEC);
    public static final IntProviderType<UniformInt> UNIFORM = IntProviderType.register("uniform", UniformInt.CODEC);
    public static final IntProviderType<BiasedToBottomInt> BIASED_TO_BOTTOM = IntProviderType.register("biased_to_bottom", BiasedToBottomInt.CODEC);
    public static final IntProviderType<ClampedInt> CLAMPED = IntProviderType.register("clamped", ClampedInt.CODEC);
    public static final IntProviderType<WeightedListInt> WEIGHTED_LIST = IntProviderType.register("weighted_list", WeightedListInt.CODEC);
    public static final IntProviderType<ClampedNormalInt> CLAMPED_NORMAL = IntProviderType.register("clamped_normal", ClampedNormalInt.CODEC);

    public Codec<P> codec();

    public static <P extends IntProvider> IntProviderType<P> register(String string, Codec<P> codec) {
        return Registry.register(BuiltInRegistries.INT_PROVIDER_TYPE, string, () -> codec);
    }
}

