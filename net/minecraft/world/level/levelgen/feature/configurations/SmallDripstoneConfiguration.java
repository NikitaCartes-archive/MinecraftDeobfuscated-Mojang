/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class SmallDripstoneConfiguration
implements FeatureConfiguration {
    public static final Codec<SmallDripstoneConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(0, 100).fieldOf("max_placements")).orElse(5).forGetter(smallDripstoneConfiguration -> smallDripstoneConfiguration.maxPlacements), ((MapCodec)Codec.intRange(0, 20).fieldOf("empty_space_search_radius")).orElse(10).forGetter(smallDripstoneConfiguration -> smallDripstoneConfiguration.emptySpaceSearchRadius), ((MapCodec)Codec.intRange(0, 20).fieldOf("max_offset_from_origin")).orElse(2).forGetter(smallDripstoneConfiguration -> smallDripstoneConfiguration.maxOffsetFromOrigin), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("chance_of_taller_dripstone")).orElse(Float.valueOf(0.2f)).forGetter(smallDripstoneConfiguration -> Float.valueOf(smallDripstoneConfiguration.chanceOfTallerDripstone))).apply((Applicative<SmallDripstoneConfiguration, ?>)instance, SmallDripstoneConfiguration::new));
    public final int maxPlacements;
    public final int emptySpaceSearchRadius;
    public final int maxOffsetFromOrigin;
    public final float chanceOfTallerDripstone;

    public SmallDripstoneConfiguration(int i, int j, int k, float f) {
        this.maxPlacements = i;
        this.emptySpaceSearchRadius = j;
        this.maxOffsetFromOrigin = k;
        this.chanceOfTallerDripstone = f;
    }
}

