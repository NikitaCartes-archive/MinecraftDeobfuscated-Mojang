/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.ForestFlowerProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.PlainFlowerProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RotatedBlockProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

public class BlockStateProviderType<P extends BlockStateProvider> {
    public static final BlockStateProviderType<SimpleStateProvider> SIMPLE_STATE_PROVIDER = BlockStateProviderType.register("simple_state_provider", SimpleStateProvider.CODEC);
    public static final BlockStateProviderType<WeightedStateProvider> WEIGHTED_STATE_PROVIDER = BlockStateProviderType.register("weighted_state_provider", WeightedStateProvider.CODEC);
    public static final BlockStateProviderType<PlainFlowerProvider> PLAIN_FLOWER_PROVIDER = BlockStateProviderType.register("plain_flower_provider", PlainFlowerProvider.CODEC);
    public static final BlockStateProviderType<ForestFlowerProvider> FOREST_FLOWER_PROVIDER = BlockStateProviderType.register("forest_flower_provider", ForestFlowerProvider.CODEC);
    public static final BlockStateProviderType<RotatedBlockProvider> ROTATED_BLOCK_PROVIDER = BlockStateProviderType.register("rotated_block_provider", RotatedBlockProvider.CODEC);
    private final Codec<P> codec;

    private static <P extends BlockStateProvider> BlockStateProviderType<P> register(String string, Codec<P> codec) {
        return Registry.register(Registry.BLOCKSTATE_PROVIDER_TYPES, string, new BlockStateProviderType<P>(codec));
    }

    private BlockStateProviderType(Codec<P> codec) {
        this.codec = codec;
    }

    public Codec<P> codec() {
        return this.codec;
    }
}

