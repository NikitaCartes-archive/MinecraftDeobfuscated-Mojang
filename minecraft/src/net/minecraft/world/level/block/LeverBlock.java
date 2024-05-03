package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeverBlock extends FaceAttachedHorizontalDirectionalBlock {
	public static final MapCodec<LeverBlock> CODEC = simpleCodec(LeverBlock::new);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	protected static final int DEPTH = 6;
	protected static final int WIDTH = 6;
	protected static final int HEIGHT = 8;
	protected static final VoxelShape NORTH_AABB = Block.box(5.0, 4.0, 10.0, 11.0, 12.0, 16.0);
	protected static final VoxelShape SOUTH_AABB = Block.box(5.0, 4.0, 0.0, 11.0, 12.0, 6.0);
	protected static final VoxelShape WEST_AABB = Block.box(10.0, 4.0, 5.0, 16.0, 12.0, 11.0);
	protected static final VoxelShape EAST_AABB = Block.box(0.0, 4.0, 5.0, 6.0, 12.0, 11.0);
	protected static final VoxelShape UP_AABB_Z = Block.box(5.0, 0.0, 4.0, 11.0, 6.0, 12.0);
	protected static final VoxelShape UP_AABB_X = Block.box(4.0, 0.0, 5.0, 12.0, 6.0, 11.0);
	protected static final VoxelShape DOWN_AABB_Z = Block.box(5.0, 10.0, 4.0, 11.0, 16.0, 12.0);
	protected static final VoxelShape DOWN_AABB_X = Block.box(4.0, 10.0, 5.0, 12.0, 16.0, 11.0);

	@Override
	public MapCodec<LeverBlock> codec() {
		return CODEC;
	}

	protected LeverBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(FACE, AttachFace.WALL)
		);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		switch ((AttachFace)blockState.getValue(FACE)) {
			case FLOOR:
				switch (((Direction)blockState.getValue(FACING)).getAxis()) {
					case X:
						return UP_AABB_X;
					case Z:
					default:
						return UP_AABB_Z;
				}
			case WALL:
				switch ((Direction)blockState.getValue(FACING)) {
					case EAST:
						return EAST_AABB;
					case WEST:
						return WEST_AABB;
					case SOUTH:
						return SOUTH_AABB;
					case NORTH:
					default:
						return NORTH_AABB;
				}
			case CEILING:
			default:
				switch (((Direction)blockState.getValue(FACING)).getAxis()) {
					case X:
						return DOWN_AABB_X;
					case Z:
					default:
						return DOWN_AABB_Z;
				}
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (level.isClientSide) {
			BlockState blockState2 = blockState.cycle(POWERED);
			if ((Boolean)blockState2.getValue(POWERED)) {
				makeParticle(blockState2, level, blockPos, 1.0F);
			}

			return InteractionResult.SUCCESS;
		} else {
			this.pull(blockState, level, blockPos, null);
			return InteractionResult.CONSUME;
		}
	}

	@Override
	protected void onExplosionHit(BlockState blockState, Level level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
		if (explosion.canTriggerBlocks()) {
			this.pull(blockState, level, blockPos, null);
		}

		super.onExplosionHit(blockState, level, blockPos, explosion, biConsumer);
	}

	public void pull(BlockState blockState, Level level, BlockPos blockPos, @Nullable Player player) {
		blockState = blockState.cycle(POWERED);
		level.setBlock(blockPos, blockState, 3);
		this.updateNeighbours(blockState, level, blockPos);
		playSound(player, level, blockPos, blockState);
		level.gameEvent(player, blockState.getValue(POWERED) ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, blockPos);
	}

	protected static void playSound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
		float f = blockState.getValue(POWERED) ? 0.6F : 0.5F;
		levelAccessor.playSound(player, blockPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
	}

	private static void makeParticle(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, float f) {
		Direction direction = ((Direction)blockState.getValue(FACING)).getOpposite();
		Direction direction2 = getConnectedDirection(blockState).getOpposite();
		double d = (double)blockPos.getX() + 0.5 + 0.1 * (double)direction.getStepX() + 0.2 * (double)direction2.getStepX();
		double e = (double)blockPos.getY() + 0.5 + 0.1 * (double)direction.getStepY() + 0.2 * (double)direction2.getStepY();
		double g = (double)blockPos.getZ() + 0.5 + 0.1 * (double)direction.getStepZ() + 0.2 * (double)direction2.getStepZ();
		levelAccessor.addParticle(new DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, f), d, e, g, 0.0, 0.0, 0.0);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if ((Boolean)blockState.getValue(POWERED) && randomSource.nextFloat() < 0.25F) {
			makeParticle(blockState, level, blockPos, 0.5F);
		}
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!bl && !blockState.is(blockState2.getBlock())) {
			if ((Boolean)blockState.getValue(POWERED)) {
				this.updateNeighbours(blockState, level, blockPos);
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(POWERED) && getConnectedDirection(blockState) == direction ? 15 : 0;
	}

	@Override
	protected boolean isSignalSource(BlockState blockState) {
		return true;
	}

	private void updateNeighbours(BlockState blockState, Level level, BlockPos blockPos) {
		level.updateNeighborsAt(blockPos, this);
		level.updateNeighborsAt(blockPos.relative(getConnectedDirection(blockState).getOpposite()), this);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACE, FACING, POWERED);
	}
}
