/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;

public record DiskConfiguration(RuleBasedBlockStateProvider stateProvider, BlockPredicate target, IntProvider radius, int halfHeight) implements FeatureConfiguration
{
    public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)RuleBasedBlockStateProvider.CODEC.fieldOf("state_provider")).forGetter(DiskConfiguration::stateProvider), ((MapCodec)BlockPredicate.CODEC.fieldOf("target")).forGetter(DiskConfiguration::target), ((MapCodec)IntProvider.codec(0, 8).fieldOf("radius")).forGetter(DiskConfiguration::radius), ((MapCodec)Codec.intRange(0, 4).fieldOf("half_height")).forGetter(DiskConfiguration::halfHeight)).apply((Applicative<DiskConfiguration, ?>)instance, DiskConfiguration::new));
}

