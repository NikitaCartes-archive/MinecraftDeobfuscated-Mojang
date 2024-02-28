package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LightBlock extends Block implements SimpleWaterloggedBlock {
	public static final MapCodec<LightBlock> CODEC = simpleCodec(LightBlock::new);
	public static final int MAX_LEVEL = 15;
	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final ToIntFunction<BlockState> LIGHT_EMISSION = blockState -> (Integer)blockState.getValue(LEVEL);

	@Override
	public MapCodec<LightBlock> codec() {
		return CODEC;
	}

	public LightBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(15)).setValue(WATERLOGGED, Boolean.valueOf(false)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL, WATERLOGGED);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (!level.isClientSide && player.canUseGameMasterBlocks()) {
			level.setBlock(blockPos, blockState.cycle(LEVEL), 2);
			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.CONSUME;
		}
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return collisionContext.isHoldingItem(Items.LIGHT) ? Shapes.block() : Shapes.empty();
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return true;
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return 1.0F;
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return setLightOnStack(super.getCloneItemStack(levelReader, blockPos, blockState), (Integer)blockState.getValue(LEVEL));
	}

	public static ItemStack setLightOnStack(ItemStack itemStack, int i) {
		if (i != 15) {
			itemStack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(LEVEL, i));
		}

		return itemStack;
	}
}
