package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class HugeMushroomFeatureConfiguration implements FeatureConfiguration {
	public final BlockStateProvider capProvider;
	public final BlockStateProvider stemProvider;
	public final int foliageRadius;

	public HugeMushroomFeatureConfiguration(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, int i) {
		this.capProvider = blockStateProvider;
		this.stemProvider = blockStateProvider2;
		this.foliageRadius = i;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("cap_provider"), this.capProvider.serialize(dynamicOps))
			.put(dynamicOps.createString("stem_provider"), this.stemProvider.serialize(dynamicOps))
			.put(dynamicOps.createString("foliage_radius"), dynamicOps.createInt(this.foliageRadius));
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build()));
	}

	public static <T> HugeMushroomFeatureConfiguration deserialize(Dynamic<T> dynamic) {
		BlockStateProviderType<?> blockStateProviderType = Registry.BLOCKSTATE_PROVIDER_TYPES
			.get(new ResourceLocation((String)dynamic.get("cap_provider").get("type").asString().orElseThrow(RuntimeException::new)));
		BlockStateProviderType<?> blockStateProviderType2 = Registry.BLOCKSTATE_PROVIDER_TYPES
			.get(new ResourceLocation((String)dynamic.get("stem_provider").get("type").asString().orElseThrow(RuntimeException::new)));
		return new HugeMushroomFeatureConfiguration(
			blockStateProviderType.deserialize(dynamic.get("cap_provider").orElseEmptyMap()),
			blockStateProviderType2.deserialize(dynamic.get("stem_provider").orElseEmptyMap()),
			dynamic.get("foliage_radius").asInt(2)
		);
	}
}
