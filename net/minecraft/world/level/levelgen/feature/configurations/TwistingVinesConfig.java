/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record TwistingVinesConfig(int spreadWidth, int spreadHeight, int maxHeight) implements FeatureConfiguration
{
    public static final Codec<TwistingVinesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("spread_width")).forGetter(TwistingVinesConfig::spreadWidth), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("spread_height")).forGetter(TwistingVinesConfig::spreadHeight), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("max_height")).forGetter(TwistingVinesConfig::maxHeight)).apply((Applicative<TwistingVinesConfig, ?>)instance, TwistingVinesConfig::new));
}

