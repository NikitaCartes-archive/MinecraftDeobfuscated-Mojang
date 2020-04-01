package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;

public class SurfaceBuilderBaseConfiguration implements SurfaceBuilderConfiguration {
	private final BlockState topMaterial;
	private final BlockState underMaterial;
	private final BlockState underwaterMaterial;

	public SurfaceBuilderBaseConfiguration(BlockState blockState, BlockState blockState2, BlockState blockState3) {
		this.topMaterial = blockState;
		this.underMaterial = blockState2;
		this.underwaterMaterial = blockState3;
	}

	@Override
	public BlockState getTopMaterial() {
		return this.topMaterial;
	}

	@Override
	public BlockState getUnderMaterial() {
		return this.underMaterial;
	}

	public BlockState getUnderwaterMaterial() {
		return this.underwaterMaterial;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("top_material"),
					BlockState.serialize(dynamicOps, this.topMaterial).getValue(),
					dynamicOps.createString("under_material"),
					BlockState.serialize(dynamicOps, this.underMaterial).getValue(),
					dynamicOps.createString("underwater_material"),
					BlockState.serialize(dynamicOps, this.underwaterMaterial).getValue()
				)
			)
		);
	}

	public static SurfaceBuilderBaseConfiguration deserialize(Dynamic<?> dynamic) {
		BlockState blockState = (BlockState)dynamic.get("top_material").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		BlockState blockState2 = (BlockState)dynamic.get("under_material").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		BlockState blockState3 = (BlockState)dynamic.get("underwater_material").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		return new SurfaceBuilderBaseConfiguration(blockState, blockState2, blockState3);
	}

	public static SurfaceBuilderBaseConfiguration random(Random random) {
		BlockState blockState = Util.randomObject(random, OverworldGeneratorSettings.SAFE_BLOCKS);
		BlockState blockState2 = Util.randomObject(random, OverworldGeneratorSettings.SAFE_BLOCKS);
		BlockState blockState3 = Util.randomObject(random, OverworldGeneratorSettings.SAFE_BLOCKS);
		return new SurfaceBuilderBaseConfiguration(blockState, blockState2, blockState3);
	}
}
