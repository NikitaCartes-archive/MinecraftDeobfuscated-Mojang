/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.ForestFlowerProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.PlainFlowerProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

public class BlockStateProviderType<P extends BlockStateProvider> {
    public static final BlockStateProviderType<SimpleStateProvider> SIMPLE_STATE_PROVIDER = BlockStateProviderType.register("simple_state_provider", SimpleStateProvider::new);
    public static final BlockStateProviderType<WeightedStateProvider> WEIGHTED_STATE_PROVIDER = BlockStateProviderType.register("weighted_state_provider", WeightedStateProvider::new);
    public static final BlockStateProviderType<PlainFlowerProvider> PLAIN_FLOWER_PROVIDER = BlockStateProviderType.register("plain_flower_provider", PlainFlowerProvider::new);
    public static final BlockStateProviderType<ForestFlowerProvider> FOREST_FLOWER_PROVIDER = BlockStateProviderType.register("forest_flower_provider", ForestFlowerProvider::new);
    private final Function<Dynamic<?>, P> deserializer;

    private static <P extends BlockStateProvider> BlockStateProviderType<P> register(String string, Function<Dynamic<?>, P> function) {
        return Registry.register(Registry.BLOCKSTATE_PROVIDER_TYPES, string, new BlockStateProviderType<P>(function));
    }

    private BlockStateProviderType(Function<Dynamic<?>, P> function) {
        this.deserializer = function;
    }

    public P deserialize(Dynamic<?> dynamic) {
        return (P)((BlockStateProvider)this.deserializer.apply(dynamic));
    }
}

