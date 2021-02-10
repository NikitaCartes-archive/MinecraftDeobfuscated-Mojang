/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class UnderwaterMagmaConfiguration
implements FeatureConfiguration {
    public static final Codec<UnderwaterMagmaConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(0, 512).fieldOf("floor_search_range")).forGetter(underwaterMagmaConfiguration -> underwaterMagmaConfiguration.floorSearchRange), ((MapCodec)Codec.intRange(0, 64).fieldOf("placement_radius_around_floor")).forGetter(underwaterMagmaConfiguration -> underwaterMagmaConfiguration.placementRadiusAroundFloor), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("placement_probability_per_valid_position")).forGetter(underwaterMagmaConfiguration -> Float.valueOf(underwaterMagmaConfiguration.placementProbabilityPerValidPosition))).apply((Applicative<UnderwaterMagmaConfiguration, ?>)instance, UnderwaterMagmaConfiguration::new));
    public final int floorSearchRange;
    public final int placementRadiusAroundFloor;
    public final float placementProbabilityPerValidPosition;

    public UnderwaterMagmaConfiguration(int i, int j, float f) {
        this.floorSearchRange = i;
        this.placementRadiusAroundFloor = j;
        this.placementProbabilityPerValidPosition = f;
    }
}

