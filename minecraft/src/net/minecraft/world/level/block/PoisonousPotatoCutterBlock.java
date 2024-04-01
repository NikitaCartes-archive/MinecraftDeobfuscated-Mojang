package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.PoisonousPotatoCutterMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PoisonousPotatoCutterBlock extends Block {
	public static final MapCodec<PoisonousPotatoCutterBlock> CODEC = simpleCodec(PoisonousPotatoCutterBlock::new);
	private static final Component CONTAINER_TITLE = Component.translatable("container.poisonous_potato_cutter");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);

	@Override
	public MapCodec<PoisonousPotatoCutterBlock> codec() {
		return CODEC;
	}

	public PoisonousPotatoCutterBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			player.openMenu(blockState.getMenuProvider(level, blockPos));
			player.awardStat(Stats.INTERACT_WITH_STONECUTTER);
			return InteractionResult.CONSUME;
		}
	}

	@Nullable
	@Override
	protected MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
		return new SimpleMenuProvider(
			(i, inventory, player) -> new PoisonousPotatoCutterMenu(i, inventory, ContainerLevelAccess.create(level, blockPos)), CONTAINER_TITLE
		);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
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

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}
}
