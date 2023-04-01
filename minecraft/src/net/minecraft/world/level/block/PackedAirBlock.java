package net.minecraft.world.level.block;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PackedAirBlock extends AirBlock {
	private static final ThreadLocal<Boolean> isExpanding = ThreadLocal.withInitial(() -> false);
	public static final int MAX_COUNT = 128;
	private static final int TICK_RATE = 2;
	private static final Direction[] ALL_DIRECTIONS = Direction.values();

	protected PackedAirBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
			level.scheduleTick(blockPos, this, 2);
		}
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		this.checkAirTightness(serverLevel, blockPos);
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
		level.scheduleTick(blockPos, this, 2);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (levelAccessor instanceof ServerLevel serverLevel && blockState2.canAirPass(serverLevel, blockPos2, direction.getOpposite())) {
			serverLevel.scheduleTick(blockPos, this, 2);
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	private void checkAirTightness(ServerLevel serverLevel, BlockPos blockPos) {
		if (!(Boolean)isExpanding.get()) {
			if (!Rules.AIR_BLOCKS.get()) {
				if (serverLevel.getBlockState(blockPos).is(this)) {
					serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
				}
			} else {
				Function<Predicate<BlockState>, Set<BlockPos>> function = predicate -> {
					Set<BlockPos> setx = new HashSet();
					BlockPos.breadthFirstTraversal(blockPos, 128, 128, (blockPosxx, consumer) -> {
						BlockState blockState = serverLevel.getBlockState(blockPosxx);

						for (Direction direction : ALL_DIRECTIONS) {
							if (blockState.canAirPass(serverLevel, blockPosxx, direction)) {
								BlockPos blockPos2 = blockPosxx.relative(direction);
								BlockState blockState2 = serverLevel.getBlockState(blockPos2);
								if (blockState2.canAirPass(serverLevel, blockPosxx, direction.getOpposite())) {
									consumer.accept(blockPosxx.relative(direction));
								}
							}
						}
					}, blockPos2 -> {
						if (blockPos2.equals(blockPos)) {
							setx.add(blockPos2);
							return true;
						} else {
							BlockState blockState = serverLevel.getBlockState(blockPos2);
							if (predicate.test(blockState)) {
								setx.add(blockPos2);
								return true;
							} else {
								return false;
							}
						}
					});
					return setx;
				};
				Set<BlockPos> set = (Set<BlockPos>)function.apply((Predicate)blockState -> !blockState.is(this));
				if (!serverLevel.isMoon()) {
					set.forEach(
						blockPosx -> {
							if (serverLevel.getBlockState(blockPosx).is(this)) {
								serverLevel.setBlock(blockPosx, Blocks.AIR.defaultBlockState(), 3);
							}

							serverLevel.playSound(null, blockPosx, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.1F, 1.5F + 0.5F * serverLevel.random.nextFloat());
							serverLevel.sendParticles(
								ParticleTypes.CLOUD, (double)blockPosx.getX() + 0.5, (double)blockPosx.getY() + 0.5, (double)blockPosx.getZ() + 0.5, 1, 0.25, 0.25, 0.25, 1.0
							);
						}
					);
				} else {
					int i = set.size();
					if (i != 1) {
						try {
							isExpanding.set(true);
							if (i >= 128) {
								((Set)function.apply((Predicate)blockState -> true))
									.forEach(
										blockPosx -> {
											if (serverLevel.getBlockState(blockPosx).is(this)) {
												serverLevel.setBlock(blockPosx, Blocks.AIR.defaultBlockState(), 3);
											}

											serverLevel.playSound(null, blockPosx, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.1F, 1.5F + 0.5F * serverLevel.random.nextFloat());
											serverLevel.sendParticles(
												ParticleTypes.CLOUD, (double)blockPosx.getX() + 0.5, (double)blockPosx.getY() + 0.5, (double)blockPosx.getZ() + 0.5, 1, 0.25, 0.25, 0.25, 1.0
											);
										}
									);
							} else if (i > 1) {
								AtomicInteger atomicInteger = new AtomicInteger(0);
								set.forEach(
									blockPosx -> {
										if (atomicInteger.get() > 2) {
											serverLevel.playSound(null, blockPosx, SoundEvents.PUFFER_FISH_BLOW_OUT, SoundSource.BLOCKS, 0.2F, 1.5F + 0.5F * serverLevel.random.nextFloat());
											serverLevel.sendParticles(
												ParticleTypes.CLOUD, (double)blockPosx.getX() + 0.5, (double)blockPosx.getY() + 0.5, (double)blockPosx.getZ() + 0.5, 1, 0.25, 0.25, 0.25, 0.0
											);
										}

										BlockState blockState = serverLevel.getBlockState(blockPosx);
										if (blockState.isAir() && !blockState.is(this)) {
											atomicInteger.incrementAndGet();
											serverLevel.setBlock(blockPosx, this.defaultBlockState(), 3);
										}
									}
								);
							}
						} finally {
							isExpanding.set(false);
						}
					}
				}
			}
		}
	}
}
