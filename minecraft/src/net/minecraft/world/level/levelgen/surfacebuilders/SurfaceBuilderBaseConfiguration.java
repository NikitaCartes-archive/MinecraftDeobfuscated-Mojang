package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.datafixers.Dynamic;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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

	public static SurfaceBuilderBaseConfiguration deserialize(Dynamic<?> dynamic) {
		BlockState blockState = (BlockState)dynamic.get("top_material").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		BlockState blockState2 = (BlockState)dynamic.get("under_material").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		BlockState blockState3 = (BlockState)dynamic.get("underwater_material").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		return new SurfaceBuilderBaseConfiguration(blockState, blockState2, blockState3);
	}
}
