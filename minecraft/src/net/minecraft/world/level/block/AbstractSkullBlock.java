package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;

public abstract class AbstractSkullBlock extends BaseEntityBlock {
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	private final SkullBlock.Type type;

	public AbstractSkullBlock(SkullBlock.Type type, BlockBehaviour.Properties properties) {
		super(properties);
		this.type = type;
		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)));
	}

	@Override
	protected abstract MapCodec<? extends AbstractSkullBlock> codec();

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new SkullBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		if (level.isClientSide) {
			boolean bl = blockState.is(Blocks.DRAGON_HEAD)
				|| blockState.is(Blocks.DRAGON_WALL_HEAD)
				|| blockState.is(Blocks.PIGLIN_HEAD)
				|| blockState.is(Blocks.PIGLIN_WALL_HEAD);
			if (bl) {
				return createTickerHelper(blockEntityType, BlockEntityType.SKULL, SkullBlockEntity::animation);
			}
		}

		return null;
	}

	public SkullBlock.Type getType() {
		return this.type;
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWERED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(POWERED, Boolean.valueOf(blockPlaceContext.getLevel().hasNeighborSignal(blockPlaceContext.getClickedPos())));
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
		if (!level.isClientSide) {
			boolean bl2 = level.hasNeighborSignal(blockPos);
			if (bl2 != (Boolean)blockState.getValue(POWERED)) {
				level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(bl2)), 2);
			}
		}
	}
}
