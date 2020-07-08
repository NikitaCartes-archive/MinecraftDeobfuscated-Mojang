/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ShipwreckConfiguration
implements FeatureConfiguration {
    public static final Codec<ShipwreckConfiguration> CODEC = ((MapCodec)Codec.BOOL.fieldOf("is_beached")).orElse(false).xmap(ShipwreckConfiguration::new, shipwreckConfiguration -> shipwreckConfiguration.isBeached).codec();
    public final boolean isBeached;

    public ShipwreckConfiguration(boolean bl) {
        this.isBeached = bl;
    }
}

