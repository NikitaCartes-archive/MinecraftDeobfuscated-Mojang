package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AmethystClusterBlock extends AmethystBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<AmethystClusterBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.FLOAT.fieldOf("height").forGetter(amethystClusterBlock -> amethystClusterBlock.height),
					Codec.FLOAT.fieldOf("aabb_offset").forGetter(amethystClusterBlock -> amethystClusterBlock.aabbOffset),
					propertiesCodec()
				)
				.apply(instance, AmethystClusterBlock::new)
	);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	private final float height;
	private final float aabbOffset;
	protected final VoxelShape northAabb;
	protected final VoxelShape southAabb;
	protected final VoxelShape eastAabb;
	protected final VoxelShape westAabb;
	protected final VoxelShape upAabb;
	protected final VoxelShape downAabb;

	@Override
	public MapCodec<AmethystClusterBlock> codec() {
		return CODEC;
	}

	public AmethystClusterBlock(float f, float g, BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.UP));
		this.upAabb = Block.box((double)g, 0.0, (double)g, (double)(16.0F - g), (double)f, (double)(16.0F - g));
		this.downAabb = Block.box((double)g, (double)(16.0F - f), (double)g, (double)(16.0F - g), 16.0, (double)(16.0F - g));
		this.northAabb = Block.box((double)g, (double)g, (double)(16.0F - f), (double)(16.0F - g), (double)(16.0F - g), 16.0);
		this.southAabb = Block.box((double)g, (double)g, 0.0, (double)(16.0F - g), (double)(16.0F - g), (double)f);
		this.eastAabb = Block.box(0.0, (double)g, (double)g, (double)f, (double)(16.0F - g), (double)(16.0F - g));
		this.westAabb = Block.box((double)(16.0F - f), (double)g, (double)g, 16.0, (double)(16.0F - g), (double)(16.0F - g));
		this.height = f;
		this.aabbOffset = g;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Direction direction = blockState.getValue(FACING);
		switch (direction) {
			case NORTH:
				return this.northAabb;
			case SOUTH:
				return this.southAabb;
			case EAST:
				return this.eastAabb;
			case WEST:
				return this.westAabb;
			case DOWN:
				return this.downAabb;
			case UP:
			default:
				return this.upAabb;
		}
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		Direction direction = blockState.getValue(FACING);
		BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
		return levelReader.getBlockState(blockPos2).isFaceSturdy(levelReader, blockPos2, direction);
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return direction == ((Direction)blockState.getValue(FACING)).getOpposite() && !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		LevelAccessor levelAccessor = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		return this.defaultBlockState()
			.setValue(WATERLOGGED, Boolean.valueOf(levelAccessor.getFluidState(blockPos).getType() == Fluids.WATER))
			.setValue(FACING, blockPlaceContext.getClickedFace());
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED, FACING);
	}
}
