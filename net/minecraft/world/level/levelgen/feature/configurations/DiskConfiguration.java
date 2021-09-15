/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record DiskConfiguration(BlockState state, IntProvider radius, int halfHeight, List<BlockState> targets) implements FeatureConfiguration
{
    public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(DiskConfiguration::state), ((MapCodec)IntProvider.codec(0, 8).fieldOf("radius")).forGetter(DiskConfiguration::radius), ((MapCodec)Codec.intRange(0, 4).fieldOf("half_height")).forGetter(DiskConfiguration::halfHeight), ((MapCodec)BlockState.CODEC.listOf().fieldOf("targets")).forGetter(DiskConfiguration::targets)).apply((Applicative<DiskConfiguration, ?>)instance, DiskConfiguration::new));
}

