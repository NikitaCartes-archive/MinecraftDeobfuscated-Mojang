package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseFireBlock extends Block {
	private static final int SECONDS_ON_FIRE = 8;
	private final float fireDamage;
	protected static final float AABB_OFFSET = 1.0F;
	protected static final VoxelShape DOWN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

	public BaseFireBlock(BlockBehaviour.Properties properties, float f) {
		super(properties);
		this.fireDamage = f;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return getState(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
	}

	public static BlockState getState(BlockGetter blockGetter, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState = blockGetter.getBlockState(blockPos2);
		return SoulFireBlock.canSurviveOnBlock(blockState)
			? Blocks.SOUL_FIRE.defaultBlockState()
			: ((FireBlock)Blocks.FIRE).getStateForPlacement(blockGetter, blockPos);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return DOWN_AABB;
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if (random.nextInt(24) == 0) {
			level.playLocalSound(
				(double)blockPos.getX() + 0.5,
				(double)blockPos.getY() + 0.5,
				(double)blockPos.getZ() + 0.5,
				SoundEvents.FIRE_AMBIENT,
				SoundSource.BLOCKS,
				1.0F + random.nextFloat(),
				random.nextFloat() * 0.7F + 0.3F,
				false
			);
		}

		BlockPos blockPos2 = blockPos.below();
		BlockState blockState2 = level.getBlockState(blockPos2);
		if (!this.canBurn(blockState2) && !blockState2.isFaceSturdy(level, blockPos2, Direction.UP)) {
			if (this.canBurn(level.getBlockState(blockPos.west()))) {
				for (int i = 0; i < 2; i++) {
					double d = (double)blockPos.getX() + random.nextDouble() * 0.1F;
					double e = (double)blockPos.getY() + random.nextDouble();
					double f = (double)blockPos.getZ() + random.nextDouble();
					level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
				}
			}

			if (this.canBurn(level.getBlockState(blockPos.east()))) {
				for (int i = 0; i < 2; i++) {
					double d = (double)(blockPos.getX() + 1) - random.nextDouble() * 0.1F;
					double e = (double)blockPos.getY() + random.nextDouble();
					double f = (double)blockPos.getZ() + random.nextDouble();
					level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
				}
			}

			if (this.canBurn(level.getBlockState(blockPos.north()))) {
				for (int i = 0; i < 2; i++) {
					double d = (double)blockPos.getX() + random.nextDouble();
					double e = (double)blockPos.getY() + random.nextDouble();
					double f = (double)blockPos.getZ() + random.nextDouble() * 0.1F;
					level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
				}
			}

			if (this.canBurn(level.getBlockState(blockPos.south()))) {
				for (int i = 0; i < 2; i++) {
					double d = (double)blockPos.getX() + random.nextDouble();
					double e = (double)blockPos.getY() + random.nextDouble();
					double f = (double)(blockPos.getZ() + 1) - random.nextDouble() * 0.1F;
					level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
				}
			}

			if (this.canBurn(level.getBlockState(blockPos.above()))) {
				for (int i = 0; i < 2; i++) {
					double d = (double)blockPos.getX() + random.nextDouble();
					double e = (double)(blockPos.getY() + 1) - random.nextDouble() * 0.1F;
					double f = (double)blockPos.getZ() + random.nextDouble();
					level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
				}
			}
		} else {
			for (int i = 0; i < 3; i++) {
				double d = (double)blockPos.getX() + random.nextDouble();
				double e = (double)blockPos.getY() + random.nextDouble() * 0.5 + 0.5;
				double f = (double)blockPos.getZ() + random.nextDouble();
				level.addParticle(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
			}
		}
	}

	protected abstract boolean canBurn(BlockState blockState);

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!entity.fireImmune()) {
			entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
			if (entity.getRemainingFireTicks() == 0) {
				entity.setSecondsOnFire(8);
			}

			entity.hurt(DamageSource.IN_FIRE, this.fireDamage);
		}

		super.entityInside(blockState, level, blockPos, entity);
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
			if (inPortalDimension(level)) {
				Optional<PortalShape> optional = PortalShape.findEmptyPortalShape(level, blockPos, Direction.Axis.X);
				if (optional.isPresent()) {
					((PortalShape)optional.get()).createPortalBlocks();
					return;
				}
			}

			if (!blockState.canSurvive(level, blockPos)) {
				level.removeBlock(blockPos, false);
			}
		}
	}

	private static boolean inPortalDimension(Level level) {
		return level.dimension() == Level.OVERWORLD || level.dimension() == Level.NETHER;
	}

	@Override
	protected void spawnDestroyParticles(Level level, Player player, BlockPos blockPos, BlockState blockState) {
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (!level.isClientSide()) {
			level.levelEvent(null, 1009, blockPos, 0);
		}

		super.playerWillDestroy(level, blockPos, blockState, player);
	}

	public static boolean canBePlacedAt(Level level, BlockPos blockPos, Direction direction) {
		BlockState blockState = level.getBlockState(blockPos);
		return !blockState.isAir() ? false : getState(level, blockPos).canSurvive(level, blockPos) || isPortal(level, blockPos, direction);
	}

	private static boolean isPortal(Level level, BlockPos blockPos, Direction direction) {
		if (!inPortalDimension(level)) {
			return false;
		} else {
			BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
			boolean bl = false;

			for (Direction direction2 : Direction.values()) {
				if (level.getBlockState(mutableBlockPos.set(blockPos).move(direction2)).is(Blocks.OBSIDIAN)) {
					bl = true;
					break;
				}
			}

			if (!bl) {
				return false;
			} else {
				Direction.Axis axis = direction.getAxis().isHorizontal()
					? direction.getCounterClockWise().getAxis()
					: Direction.Plane.HORIZONTAL.getRandomAxis(level.random);
				return PortalShape.findEmptyPortalShape(level, blockPos, axis).isPresent();
			}
		}
	}
}
