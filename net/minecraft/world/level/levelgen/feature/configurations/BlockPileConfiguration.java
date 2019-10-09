/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class BlockPileConfiguration
implements FeatureConfiguration {
    public final BlockStateProvider stateProvider;

    public BlockPileConfiguration(BlockStateProvider blockStateProvider) {
        this.stateProvider = blockStateProvider;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(dynamicOps.createString("state_provider"), this.stateProvider.serialize(dynamicOps));
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(builder.build()));
    }

    public static <T> BlockPileConfiguration deserialize(Dynamic<T> dynamic) {
        BlockStateProviderType<T> blockStateProviderType = Registry.BLOCKSTATE_PROVIDER_TYPES.get(new ResourceLocation(dynamic.get("state_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        return new BlockPileConfiguration((BlockStateProvider)blockStateProviderType.deserialize(dynamic.get("state_provider").orElseEmptyMap()));
    }
}

