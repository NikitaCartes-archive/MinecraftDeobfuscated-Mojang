package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SuspiciousSandBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

public class SuspiciousSandBlock extends BaseEntityBlock implements Fallable {
	private static final IntegerProperty DUSTED = BlockStateProperties.DUSTED;
	public static final int TICK_DELAY = 2;

	public SuspiciousSandBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(DUSTED, Integer.valueOf(0)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DUSTED);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new SuspiciousSandBlockEntity(blockPos, blockState);
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState blockState) {
		return PushReaction.DESTROY;
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		level.scheduleTick(blockPos, this, 2);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		levelAccessor.scheduleTick(blockPos, this, 2);
		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (serverLevel.getBlockEntity(blockPos) instanceof SuspiciousSandBlockEntity suspiciousSandBlockEntity) {
			suspiciousSandBlockEntity.checkReset();
		}

		if (FallingBlock.isFree(serverLevel.getBlockState(blockPos.below())) && blockPos.getY() >= serverLevel.getMinBuildHeight()) {
			FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(serverLevel, blockPos, blockState);
			fallingBlockEntity.disableDrop();
		}
	}

	@Override
	public void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
		Vec3 vec3 = fallingBlockEntity.getBoundingBox().getCenter();
		level.levelEvent(2001, new BlockPos(vec3), Block.getId(fallingBlockEntity.getBlockState()));
		level.gameEvent(fallingBlockEntity, GameEvent.BLOCK_DESTROY, vec3);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if (randomSource.nextInt(16) == 0) {
			BlockPos blockPos2 = blockPos.below();
			if (FallingBlock.isFree(level.getBlockState(blockPos2))) {
				double d = (double)blockPos.getX() + randomSource.nextDouble();
				double e = (double)blockPos.getY() - 0.05;
				double f = (double)blockPos.getZ() + randomSource.nextDouble();
				level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, blockState), d, e, f, 0.0, 0.0, 0.0);
			}
		}
	}
}
