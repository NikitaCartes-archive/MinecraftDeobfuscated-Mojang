package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallSignBlock extends SignBlock {
	public static final MapCodec<WallSignBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(WoodType.CODEC.fieldOf("wood_type").forGetter(SignBlock::type), propertiesCodec()).apply(instance, WallSignBlock::new)
	);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	protected static final float AABB_THICKNESS = 2.0F;
	protected static final float AABB_BOTTOM = 4.5F;
	protected static final float AABB_TOP = 12.5F;
	private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(
		ImmutableMap.of(
			Direction.NORTH,
			Block.box(0.0, 4.5, 14.0, 16.0, 12.5, 16.0),
			Direction.SOUTH,
			Block.box(0.0, 4.5, 0.0, 16.0, 12.5, 2.0),
			Direction.EAST,
			Block.box(0.0, 4.5, 0.0, 2.0, 12.5, 16.0),
			Direction.WEST,
			Block.box(14.0, 4.5, 0.0, 16.0, 12.5, 16.0)
		)
	);

	@Override
	public MapCodec<WallSignBlock> codec() {
		return CODEC;
	}

	public WallSignBlock(WoodType woodType, BlockBehaviour.Properties properties) {
		super(woodType, properties.sound(woodType.soundType()));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
	}

	@Override
	public String getDescriptionId() {
		return this.asItem().getDescriptionId();
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)AABBS.get(blockState.getValue(FACING));
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return levelReader.getBlockState(blockPos.relative(((Direction)blockState.getValue(FACING)).getOpposite())).isSolid();
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = this.defaultBlockState();
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		LevelReader levelReader = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		Direction[] directions = blockPlaceContext.getNearestLookingDirections();

		for (Direction direction : directions) {
			if (direction.getAxis().isHorizontal()) {
				Direction direction2 = direction.getOpposite();
				blockState = blockState.setValue(FACING, direction2);
				if (blockState.canSurvive(levelReader, blockPos)) {
					return blockState.setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
				}
			}
		}

		return null;
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public float getYRotationDegrees(BlockState blockState) {
		return ((Direction)blockState.getValue(FACING)).toYRot();
	}

	@Override
	public Vec3 getSignHitboxCenterPosition(BlockState blockState) {
		VoxelShape voxelShape = (VoxelShape)AABBS.get(blockState.getValue(FACING));
		return voxelShape.bounds().getCenter();
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
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}
}
