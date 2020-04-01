package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class BlockPileConfiguration implements FeatureConfiguration {
	public final BlockStateProvider stateProvider;

	public BlockPileConfiguration(BlockStateProvider blockStateProvider) {
		this.stateProvider = blockStateProvider;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("state_provider"), this.stateProvider.serialize(dynamicOps));
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build()));
	}

	public static <T> BlockPileConfiguration deserialize(Dynamic<T> dynamic) {
		BlockStateProviderType<?> blockStateProviderType = Registry.BLOCKSTATE_PROVIDER_TYPES
			.get(new ResourceLocation((String)dynamic.get("state_provider").get("type").asString().orElseThrow(RuntimeException::new)));
		return new BlockPileConfiguration(blockStateProviderType.deserialize(dynamic.get("state_provider").orElseEmptyMap()));
	}

	public static BlockPileConfiguration random(Random random) {
		return new BlockPileConfiguration(BlockStateProvider.random(random));
	}
}
