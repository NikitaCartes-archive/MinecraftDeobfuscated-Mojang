package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BasaltDeltasSurfaceBuilder extends NetherCappedSurfaceBuilder {
	private static final BlockState BASALT = Blocks.BASALT.defaultBlockState();
	private static final BlockState BLACKSTONE = Blocks.BLACKSTONE.defaultBlockState();
	private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
	private static final ImmutableList<BlockState> FLOOR_BLOCK_STATES = ImmutableList.of(BASALT, BLACKSTONE);
	private static final ImmutableList<BlockState> CEILING_BLOCK_STATES = ImmutableList.of(BASALT);

	public BasaltDeltasSurfaceBuilder(Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration> function) {
		super(function);
	}

	@Override
	protected ImmutableList<BlockState> getFloorBlockStates() {
		return FLOOR_BLOCK_STATES;
	}

	@Override
	protected ImmutableList<BlockState> getCeilingBlockStates() {
		return CEILING_BLOCK_STATES;
	}

	@Override
	protected BlockState getPatchBlockState() {
		return GRAVEL;
	}
}
