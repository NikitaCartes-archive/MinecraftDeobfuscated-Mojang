/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.util.ClampedNormalFloat;
import net.minecraft.util.ConstantFloat;
import net.minecraft.util.FloatProvider;
import net.minecraft.util.TrapezoidFloat;
import net.minecraft.util.UniformFloat;

public interface FloatProviderType<P extends FloatProvider> {
    public static final FloatProviderType<ConstantFloat> CONSTANT = FloatProviderType.register("constant", ConstantFloat.CODEC);
    public static final FloatProviderType<UniformFloat> UNIFORM = FloatProviderType.register("uniform", UniformFloat.CODEC);
    public static final FloatProviderType<ClampedNormalFloat> CLAMPED_NORMAL = FloatProviderType.register("clamped_normal", ClampedNormalFloat.CODEC);
    public static final FloatProviderType<TrapezoidFloat> TRAPEZOID = FloatProviderType.register("trapezoid", TrapezoidFloat.CODEC);

    public Codec<P> codec();

    public static <P extends FloatProvider> FloatProviderType<P> register(String string, Codec<P> codec) {
        return Registry.register(Registry.FLOAT_PROVIDER_TYPES, string, () -> codec);
    }
}

