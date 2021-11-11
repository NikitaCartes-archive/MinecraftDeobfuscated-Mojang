/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class NetherForestVegetationConfig
extends BlockPileConfiguration {
    public static final Codec<NetherForestVegetationConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("state_provider")).forGetter(netherForestVegetationConfig -> netherForestVegetationConfig.stateProvider), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("spread_width")).forGetter(netherForestVegetationConfig -> netherForestVegetationConfig.spreadWidth), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("spread_height")).forGetter(netherForestVegetationConfig -> netherForestVegetationConfig.spreadHeight)).apply((Applicative<NetherForestVegetationConfig, ?>)instance, NetherForestVegetationConfig::new));
    public final int spreadWidth;
    public final int spreadHeight;

    public NetherForestVegetationConfig(BlockStateProvider blockStateProvider, int i, int j) {
        super(blockStateProvider);
        this.spreadWidth = i;
        this.spreadHeight = j;
    }
}

