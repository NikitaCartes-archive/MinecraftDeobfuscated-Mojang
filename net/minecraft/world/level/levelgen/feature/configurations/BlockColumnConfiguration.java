/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record BlockColumnConfiguration(List<Layer> layers, Direction direction, boolean allowWater, boolean prioritizeTip) implements FeatureConfiguration
{
    public static final Codec<BlockColumnConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Layer.CODEC.listOf().fieldOf("layers")).forGetter(BlockColumnConfiguration::layers), ((MapCodec)Direction.CODEC.fieldOf("direction")).forGetter(BlockColumnConfiguration::direction), ((MapCodec)Codec.BOOL.fieldOf("allow_water")).forGetter(BlockColumnConfiguration::allowWater), ((MapCodec)Codec.BOOL.fieldOf("prioritize_tip")).forGetter(BlockColumnConfiguration::prioritizeTip)).apply((Applicative<BlockColumnConfiguration, ?>)instance, BlockColumnConfiguration::new));

    public static Layer layer(IntProvider intProvider, BlockStateProvider blockStateProvider) {
        return new Layer(intProvider, blockStateProvider);
    }

    public static BlockColumnConfiguration simple(IntProvider intProvider, BlockStateProvider blockStateProvider) {
        return new BlockColumnConfiguration(List.of(BlockColumnConfiguration.layer(intProvider, blockStateProvider)), Direction.UP, false, false);
    }

    public record Layer(IntProvider height, BlockStateProvider state) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)IntProvider.NON_NEGATIVE_CODEC.fieldOf("height")).forGetter(Layer::height), ((MapCodec)BlockStateProvider.CODEC.fieldOf("provider")).forGetter(Layer::state)).apply((Applicative<Layer, ?>)instance, Layer::new));
    }
}

