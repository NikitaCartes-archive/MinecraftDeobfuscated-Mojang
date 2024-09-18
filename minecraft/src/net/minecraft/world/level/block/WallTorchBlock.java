package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallTorchBlock extends TorchBlock {
	public static final MapCodec<WallTorchBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(PARTICLE_OPTIONS_FIELD.forGetter(wallTorchBlock -> wallTorchBlock.flameParticle), propertiesCodec())
				.apply(instance, WallTorchBlock::new)
	);
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	protected static final float AABB_OFFSET = 2.5F;
	private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(
		ImmutableMap.of(
			Direction.NORTH,
			Block.box(5.5, 3.0, 11.0, 10.5, 13.0, 16.0),
			Direction.SOUTH,
			Block.box(5.5, 3.0, 0.0, 10.5, 13.0, 5.0),
			Direction.WEST,
			Block.box(11.0, 3.0, 5.5, 16.0, 13.0, 10.5),
			Direction.EAST,
			Block.box(0.0, 3.0, 5.5, 5.0, 13.0, 10.5)
		)
	);

	@Override
	public MapCodec<WallTorchBlock> codec() {
		return CODEC;
	}

	protected WallTorchBlock(SimpleParticleType simpleParticleType, BlockBehaviour.Properties properties) {
		super(simpleParticleType, properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return getShape(blockState);
	}

	public static VoxelShape getShape(BlockState blockState) {
		return (VoxelShape)AABBS.get(blockState.getValue(FACING));
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return canSurvive(levelReader, blockPos, blockState.getValue(FACING));
	}

	public static boolean canSurvive(LevelReader levelReader, BlockPos blockPos, Direction direction) {
		BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
		BlockState blockState = levelReader.getBlockState(blockPos2);
		return blockState.isFaceSturdy(levelReader, blockPos2, direction);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = this.defaultBlockState();
		LevelReader levelReader = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		Direction[] directions = blockPlaceContext.getNearestLookingDirections();

		for (Direction direction : directions) {
			if (direction.getAxis().isHorizontal()) {
				Direction direction2 = direction.getOpposite();
				blockState = blockState.setValue(FACING, direction2);
				if (blockState.canSurvive(levelReader, blockPos)) {
					return blockState;
				}
			}
		}

		return null;
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState,
		LevelReader levelReader,
		ScheduledTickAccess scheduledTickAccess,
		BlockPos blockPos,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		RandomSource randomSource
	) {
		return direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelReader, blockPos) ? Blocks.AIR.defaultBlockState() : blockState;
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		Direction direction = blockState.getValue(FACING);
		double d = (double)blockPos.getX() + 0.5;
		double e = (double)blockPos.getY() + 0.7;
		double f = (double)blockPos.getZ() + 0.5;
		double g = 0.22;
		double h = 0.27;
		Direction direction2 = direction.getOpposite();
		level.addParticle(ParticleTypes.SMOKE, d + 0.27 * (double)direction2.getStepX(), e + 0.22, f + 0.27 * (double)direction2.getStepZ(), 0.0, 0.0, 0.0);
		level.addParticle(this.flameParticle, d + 0.27 * (double)direction2.getStepX(), e + 0.22, f + 0.27 * (double)direction2.getStepZ(), 0.0, 0.0, 0.0);
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
		builder.add(FACING);
	}
}
