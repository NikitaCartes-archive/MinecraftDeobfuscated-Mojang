package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GenericItemBlock extends Block implements SimpleWaterloggedBlock {
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
	private static final IntegerProperty ITEM = BlockStateProperties.ITEM;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	protected GenericItemBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(ITEM, Integer.valueOf(0)).setValue(WATERLOGGED, Boolean.valueOf(false)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ITEM, WATERLOGGED);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		LevelAccessor levelAccessor = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(levelAccessor.getFluidState(blockPos).getType() == Fluids.WATER));
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return !levelReader.isEmptyBlock(blockPos.below());
	}

	@Nullable
	public static BlockState wrap(BlockState blockState) {
		return genericBlockFromItem(blockState.getBlock().asItem());
	}

	@Nullable
	public static BlockState unwrap(BlockState blockState) {
		return itemFromGenericBlock(blockState) instanceof BlockItem blockItem ? blockItem.getBlock().defaultBlockState() : null;
	}

	@Nullable
	public static BlockState genericBlockFromItem(Item item) {
		if (item == Items.AIR) {
			return null;
		} else {
			int i = Registry.ITEM.getId(item);
			return i != -1 ? Blocks.GENERIC_ITEM_BLOCK.defaultBlockState().setValue(ITEM, Integer.valueOf(i)) : null;
		}
	}

	@Nullable
	public static Item itemFromGenericBlock(BlockState blockState) {
		if (blockState.hasProperty(ITEM)) {
			Item item = Registry.ITEM.byId((Integer)blockState.getValue(ITEM));
			return item != Items.AIR ? item : null;
		} else {
			return null;
		}
	}

	public static final class GenericBlockItem extends BlockItem {
		public GenericBlockItem(Block block, Item.Properties properties) {
			super(block, properties);
		}

		@Override
		public Component getName(ItemStack itemStack) {
			return new TextComponent("How did we get here?");
		}
	}
}
