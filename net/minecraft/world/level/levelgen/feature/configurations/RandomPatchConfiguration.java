/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record RandomPatchConfiguration(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> feature) implements FeatureConfiguration
{
    public static final Codec<RandomPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("tries")).orElse(128).forGetter(RandomPatchConfiguration::tries), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread")).orElse(7).forGetter(RandomPatchConfiguration::xzSpread), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread")).orElse(3).forGetter(RandomPatchConfiguration::ySpread), ((MapCodec)PlacedFeature.CODEC.fieldOf("feature")).forGetter(RandomPatchConfiguration::feature)).apply((Applicative<RandomPatchConfiguration, ?>)instance, RandomPatchConfiguration::new));
}

