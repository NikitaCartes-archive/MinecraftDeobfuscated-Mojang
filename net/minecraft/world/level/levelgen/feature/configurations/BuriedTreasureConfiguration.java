/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class BuriedTreasureConfiguration
implements FeatureConfiguration {
    public static final Codec<BuriedTreasureConfiguration> CODEC = Codec.FLOAT.xmap(BuriedTreasureConfiguration::new, buriedTreasureConfiguration -> Float.valueOf(buriedTreasureConfiguration.probability));
    public final float probability;

    public BuriedTreasureConfiguration(float f) {
        this.probability = f;
    }
}

