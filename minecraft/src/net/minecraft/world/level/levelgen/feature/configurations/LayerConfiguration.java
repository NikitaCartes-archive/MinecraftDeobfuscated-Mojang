package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class LayerConfiguration implements FeatureConfiguration {
	public final int height;
	public final BlockState state;

	public LayerConfiguration(int i, BlockState blockState) {
		this.height = i;
		this.state = blockState;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("height"),
					dynamicOps.createInt(this.height),
					dynamicOps.createString("state"),
					BlockState.serialize(dynamicOps, this.state).getValue()
				)
			)
		);
	}

	public static <T> LayerConfiguration deserialize(Dynamic<T> dynamic) {
		int i = dynamic.get("height").asInt(0);
		BlockState blockState = (BlockState)dynamic.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		return new LayerConfiguration(i, blockState);
	}

	public static LayerConfiguration random(Random random) {
		return new LayerConfiguration(random.nextInt(5), Registry.BLOCK.getRandom(random).defaultBlockState());
	}
}
