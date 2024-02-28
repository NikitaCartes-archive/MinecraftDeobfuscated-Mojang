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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AnvilBlock extends FallingBlock {
	public static final MapCodec<AnvilBlock> CODEC = simpleCodec(AnvilBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape BASE = Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0);
	private static final VoxelShape X_LEG1 = Block.box(3.0, 4.0, 4.0, 13.0, 5.0, 12.0);
	private static final VoxelShape X_LEG2 = Block.box(4.0, 5.0, 6.0, 12.0, 10.0, 10.0);
	private static final VoxelShape X_TOP = Block.box(0.0, 10.0, 3.0, 16.0, 16.0, 13.0);
	private static final VoxelShape Z_LEG1 = Block.box(4.0, 4.0, 3.0, 12.0, 5.0, 13.0);
	private static final VoxelShape Z_LEG2 = Block.box(6.0, 5.0, 4.0, 10.0, 10.0, 12.0);
	private static final VoxelShape Z_TOP = Block.box(3.0, 10.0, 0.0, 13.0, 16.0, 16.0);
	private static final VoxelShape X_AXIS_AABB = Shapes.or(BASE, X_LEG1, X_LEG2, X_TOP);
	private static final VoxelShape Z_AXIS_AABB = Shapes.or(BASE, Z_LEG1, Z_LEG2, Z_TOP);
	private static final Component CONTAINER_TITLE = Component.translatable("container.repair");
	private static final float FALL_DAMAGE_PER_DISTANCE = 2.0F;
	private static final int FALL_DAMAGE_MAX = 40;

	@Override
	public MapCodec<AnvilBlock> codec() {
		return CODEC;
	}

	public AnvilBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getClockWise());
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			player.openMenu(blockState.getMenuProvider(level, blockPos));
			player.awardStat(Stats.INTERACT_WITH_ANVIL);
			return InteractionResult.CONSUME;
		}
	}

	@Nullable
	@Override
	protected MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
		return new SimpleMenuProvider((i, inventory, player) -> new AnvilMenu(i, inventory, ContainerLevelAccess.create(level, blockPos)), CONTAINER_TITLE);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Direction direction = blockState.getValue(FACING);
		return direction.getAxis() == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
	}

	@Override
	protected void falling(FallingBlockEntity fallingBlockEntity) {
		fallingBlockEntity.setHurtsEntities(2.0F, 40);
	}

	@Override
	public void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
		if (!fallingBlockEntity.isSilent()) {
			level.levelEvent(1031, blockPos, 0);
		}
	}

	@Override
	public void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
		if (!fallingBlockEntity.isSilent()) {
			level.levelEvent(1029, blockPos, 0);
		}
	}

	@Override
	public DamageSource getFallDamageSource(Entity entity) {
		return entity.damageSources().anvil(entity);
	}

	@Nullable
	public static BlockState damage(BlockState blockState) {
		if (blockState.is(Blocks.ANVIL)) {
			return Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(FACING, (Direction)blockState.getValue(FACING));
		} else {
			return blockState.is(Blocks.CHIPPED_ANVIL) ? Blocks.DAMAGED_ANVIL.defaultBlockState().setValue(FACING, (Direction)blockState.getValue(FACING)) : null;
		}
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.getMapColor(blockGetter, blockPos).col;
	}
}
