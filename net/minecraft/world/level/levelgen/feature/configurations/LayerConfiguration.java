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

public class LayerConfiguration
implements FeatureConfiguration {
    public static final Codec<LayerConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(0, 255).fieldOf("height")).forGetter(layerConfiguration -> layerConfiguration.height), ((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(layerConfiguration -> layerConfiguration.state)).apply((Applicative<LayerConfiguration, ?>)instance, LayerConfiguration::new));
    public final int height;
    public final BlockState state;

    public LayerConfiguration(int i, BlockState blockState) {
        this.height = i;
        this.state = blockState;
    }
}

