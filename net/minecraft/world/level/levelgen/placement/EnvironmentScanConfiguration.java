/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public record EnvironmentScanConfiguration(Direction directionOfSearch, BlockPredicate targetCondition, int maxSteps) implements DecoratorConfiguration
{
    public static final Codec<EnvironmentScanConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Direction.VERTICAL_CODEC.fieldOf("direction_of_search")).forGetter(EnvironmentScanConfiguration::directionOfSearch), ((MapCodec)BlockPredicate.CODEC.fieldOf("target_condition")).forGetter(EnvironmentScanConfiguration::targetCondition), ((MapCodec)Codec.intRange(1, 32).fieldOf("max_steps")).forGetter(EnvironmentScanConfiguration::maxSteps)).apply((Applicative<EnvironmentScanConfiguration, ?>)instance, EnvironmentScanConfiguration::new));
}

