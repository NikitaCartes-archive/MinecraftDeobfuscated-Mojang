/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;

public class NoneCarverConfiguration
implements CarverConfiguration {
    public static final Codec<NoneCarverConfiguration> CODEC = Codec.unit(() -> INSTANCE);
    public static final NoneCarverConfiguration INSTANCE = new NoneCarverConfiguration();
}

