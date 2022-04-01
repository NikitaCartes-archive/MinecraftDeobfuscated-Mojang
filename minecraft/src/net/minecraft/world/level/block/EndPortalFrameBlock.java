package net.minecraft.world.level.block;

import com.google.common.base.Predicates;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ConfiguredStructureTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EndPortalFrameBlock extends Block {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	protected static final VoxelShape BASE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 13.0, 16.0);
	protected static final VoxelShape EYE_SHAPE = Block.box(4.0, 13.0, 4.0, 12.0, 16.0, 12.0);
	protected static final VoxelShape FULL_SHAPE = Shapes.or(BASE_SHAPE, EYE_SHAPE);
	@Nullable
	private static BlockPattern portalShape;

	public EndPortalFrameBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Level level = blockPlaceContext.getLevel();
		if (level instanceof ServerLevel serverLevel) {
			BlockPos blockPos = blockPlaceContext.getClickedPos();
			if (serverLevel.structureFeatureManager().getStructureWithPieceAt(blockPos, BuiltinStructures.STRONGHOLD).isValid()) {
				return this.defaultBlockState().setValue(FACING, Direction.UP);
			}

			BlockPos blockPos2 = serverLevel.findNearestMapFeature(ConfiguredStructureTags.EYE_OF_ENDER_LOCATED, blockPos, 100, false);
			if (blockPos2 != null) {
				BlockPos blockPos3 = blockPos2.subtract(blockPos);
				Direction direction = Direction.getNearest((float)blockPos3.getX(), (float)blockPos3.getY(), (float)blockPos3.getZ());
				return this.defaultBlockState().setValue(FACING, direction);
			}
		}

		return this.defaultBlockState().setValue(FACING, Direction.getRandom(level.random));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
		BlockPattern.BlockPatternMatch blockPatternMatch = getOrCreatePortalShape().find(level, blockPos);
		if (blockPatternMatch != null) {
			BlockPos blockPos2 = blockPatternMatch.getFrontTopLeft().offset(-3, 0, -3);

			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					level.setBlock(blockPos2.offset(i, 0, j), Blocks.END_PORTAL.defaultBlockState(), 2);
				}
			}

			level.globalLevelEvent(1038, blockPos2.offset(1, 0, 1), 0);
		}
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

	public static BlockPattern getOrCreatePortalShape() {
		if (portalShape == null) {
			portalShape = BlockPatternBuilder.start()
				.aisle("?xxx?", "x???x", "x???x", "x???x", "?xxx?")
				.where('?', BlockInWorld.hasState(BlockStatePredicate.ANY))
				.where('x', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME).where(FACING, Predicates.equalTo(Direction.UP))))
				.build();
		}

		return portalShape;
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
