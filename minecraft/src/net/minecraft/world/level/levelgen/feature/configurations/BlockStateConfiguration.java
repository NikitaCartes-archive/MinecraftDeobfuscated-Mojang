package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;

public class BlockStateConfiguration implements FeatureConfiguration {
	public final BlockState state;

	public BlockStateConfiguration(BlockState blockState) {
		this.state = blockState;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("state"), BlockState.serialize(dynamicOps, this.state).getValue()))
		);
	}

	public static <T> BlockStateConfiguration deserialize(Dynamic<T> dynamic) {
		BlockState blockState = (BlockState)dynamic.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		return new BlockStateConfiguration(blockState);
	}

	public static BlockStateConfiguration safeRandom(Random random) {
		return new BlockStateConfiguration(Util.randomObject(random, OverworldGeneratorSettings.SAFE_BLOCKS));
	}
}
