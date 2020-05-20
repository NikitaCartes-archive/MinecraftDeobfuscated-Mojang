package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SoulSandValleySurfaceBuilder extends NetherCappedSurfaceBuilder {
	private static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
	private static final BlockState SOUL_SOIL = Blocks.SOUL_SOIL.defaultBlockState();
	private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
	private static final ImmutableList<BlockState> BLOCK_STATES = ImmutableList.of(SOUL_SAND, SOUL_SOIL);

	public SoulSandValleySurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
		super(codec);
	}

	@Override
	protected ImmutableList<BlockState> getFloorBlockStates() {
		return BLOCK_STATES;
	}

	@Override
	protected ImmutableList<BlockState> getCeilingBlockStates() {
		return BLOCK_STATES;
	}

	@Override
	protected BlockState getPatchBlockState() {
		return GRAVEL;
	}
}
