/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class BlockPileConfiguration
implements FeatureConfiguration {
    public static final Codec<BlockPileConfiguration> CODEC = ((MapCodec)BlockStateProvider.CODEC.fieldOf("state_provider")).xmap(BlockPileConfiguration::new, blockPileConfiguration -> blockPileConfiguration.stateProvider).codec();
    public final BlockStateProvider stateProvider;

    public BlockPileConfiguration(BlockStateProvider blockStateProvider) {
        this.stateProvider = blockStateProvider;
    }
}

