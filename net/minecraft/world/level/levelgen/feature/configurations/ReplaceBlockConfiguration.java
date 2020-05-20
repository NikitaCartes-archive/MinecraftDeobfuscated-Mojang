/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ReplaceBlockConfiguration
implements FeatureConfiguration {
    public static final Codec<ReplaceBlockConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockState.CODEC.fieldOf("target")).forGetter(replaceBlockConfiguration -> replaceBlockConfiguration.target), ((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(replaceBlockConfiguration -> replaceBlockConfiguration.state)).apply((Applicative<ReplaceBlockConfiguration, ?>)instance, ReplaceBlockConfiguration::new));
    public final BlockState target;
    public final BlockState state;

    public ReplaceBlockConfiguration(BlockState blockState, BlockState blockState2) {
        this.target = blockState;
        this.state = blockState2;
    }
}

