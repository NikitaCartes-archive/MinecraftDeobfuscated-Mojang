package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FarmBlock extends Block {
	public static final MapCodec<FarmBlock> CODEC = simpleCodec(FarmBlock::new);
	public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
	public static final int MAX_MOISTURE = 7;

	@Override
	public MapCodec<FarmBlock> codec() {
		return CODEC;
	}

	protected FarmBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, Integer.valueOf(0)));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction == Direction.UP && !blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.scheduleTick(blockPos, this, 1);
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockState blockState2 = levelReader.getBlockState(blockPos.above());
		return !blockState2.isSolid() || blockState2.getBlock() instanceof FenceGateBlock || blockState2.getBlock() instanceof MovingPistonBlock;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return !this.defaultBlockState().canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())
			? Blocks.DIRT.defaultBlockState()
			: super.getStateForPlacement(blockPlaceContext);
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (!blockState.canSurvive(serverLevel, blockPos)) {
			turnToDirt(null, blockState, serverLevel, blockPos);
		}
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		int i = (Integer)blockState.getValue(MOISTURE);
		if (!isNearWater(serverLevel, blockPos) && !serverLevel.isRainingAt(blockPos.above())) {
			if (i > 0) {
				serverLevel.setBlock(blockPos, blockState.setValue(MOISTURE, Integer.valueOf(i - 1)), 2);
			} else if (!shouldMaintainFarmland(serverLevel, blockPos)) {
				turnToDirt(null, blockState, serverLevel, blockPos);
			}
		} else if (i < 7) {
			serverLevel.setBlock(blockPos, blockState.setValue(MOISTURE, Integer.valueOf(7)), 2);
		}
	}

	@Override
	public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, float f) {
		if (!level.isClientSide
			&& level.random.nextFloat() < f - 0.5F
			&& entity instanceof LivingEntity
			&& (entity instanceof Player || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
			&& entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512F) {
			turnToDirt(entity, blockState, level, blockPos);
		}

		super.fallOn(level, blockState, blockPos, entity, f);
	}

	public static void turnToDirt(@Nullable Entity entity, BlockState blockState, Level level, BlockPos blockPos) {
		BlockState blockState2 = pushEntitiesUp(blockState, Blocks.DIRT.defaultBlockState(), level, blockPos);
		level.setBlockAndUpdate(blockPos, blockState2);
		level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
	}

	private static boolean shouldMaintainFarmland(BlockGetter blockGetter, BlockPos blockPos) {
		return blockGetter.getBlockState(blockPos.above()).is(BlockTags.MAINTAINS_FARMLAND);
	}

	private static boolean isNearWater(LevelReader levelReader, BlockPos blockPos) {
		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-4, 0, -4), blockPos.offset(4, 1, 4))) {
			if (levelReader.getFluidState(blockPos2).is(FluidTags.WATER)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(MOISTURE);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
