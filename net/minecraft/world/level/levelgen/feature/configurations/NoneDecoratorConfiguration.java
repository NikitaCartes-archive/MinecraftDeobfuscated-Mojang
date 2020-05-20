/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class NoneDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<NoneDecoratorConfiguration> CODEC = Codec.unit(() -> INSTANCE);
    public static final NoneDecoratorConfiguration INSTANCE = new NoneDecoratorConfiguration();
}

