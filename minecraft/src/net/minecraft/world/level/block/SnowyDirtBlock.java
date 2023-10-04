package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class SnowyDirtBlock extends Block {
	public static final MapCodec<SnowyDirtBlock> CODEC = simpleCodec(SnowyDirtBlock::new);
	public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

	@Override
	protected MapCodec<? extends SnowyDirtBlock> codec() {
		return CODEC;
	}

	protected SnowyDirtBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(SNOWY, Boolean.valueOf(false)));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return direction == Direction.UP
			? blockState.setValue(SNOWY, Boolean.valueOf(isSnowySetting(blockState2)))
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().above());
		return this.defaultBlockState().setValue(SNOWY, Boolean.valueOf(isSnowySetting(blockState)));
	}

	private static boolean isSnowySetting(BlockState blockState) {
		return blockState.is(BlockTags.SNOW);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SNOWY);
	}
}
