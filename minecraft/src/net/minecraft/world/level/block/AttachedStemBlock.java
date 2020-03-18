package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AttachedStemBlock extends BushBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private final StemGrownBlock fruit;
	private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(
		ImmutableMap.of(
			Direction.SOUTH,
			Block.box(6.0, 0.0, 6.0, 10.0, 10.0, 16.0),
			Direction.WEST,
			Block.box(0.0, 0.0, 6.0, 10.0, 10.0, 10.0),
			Direction.NORTH,
			Block.box(6.0, 0.0, 0.0, 10.0, 10.0, 10.0),
			Direction.EAST,
			Block.box(6.0, 0.0, 6.0, 16.0, 10.0, 10.0)
		)
	);

	protected AttachedStemBlock(StemGrownBlock stemGrownBlock, BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
		this.fruit = stemGrownBlock;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)AABBS.get(blockState.getValue(FACING));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return blockState2.getBlock() != this.fruit && direction == blockState.getValue(FACING)
			? this.fruit.getStem().defaultBlockState().setValue(StemBlock.AGE, Integer.valueOf(7))
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.getBlock() == Blocks.FARMLAND;
	}

	@Environment(EnvType.CLIENT)
	protected Item getSeedItem() {
		if (this.fruit == Blocks.PUMPKIN) {
			return Items.PUMPKIN_SEEDS;
		} else {
			return this.fruit == Blocks.MELON ? Items.MELON_SEEDS : Items.AIR;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(this.getSeedItem());
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
