/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public interface HeightProviderType<P extends HeightProvider> {
    public static final HeightProviderType<ConstantHeight> CONSTANT = HeightProviderType.register("constant", ConstantHeight.CODEC);
    public static final HeightProviderType<UniformHeight> UNIFORM = HeightProviderType.register("uniform", UniformHeight.CODEC);
    public static final HeightProviderType<BiasedToBottomHeight> BIASED_TO_BOTTOM = HeightProviderType.register("biased_to_bottom", BiasedToBottomHeight.CODEC);

    public Codec<P> codec();

    public static <P extends HeightProvider> HeightProviderType<P> register(String string, Codec<P> codec) {
        return Registry.register(Registry.HEIGHT_PROVIDER_TYPES, string, () -> codec);
    }
}

