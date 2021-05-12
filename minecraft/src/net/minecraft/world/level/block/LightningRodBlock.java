package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LightningRodBlock extends RodBlock implements SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	private static final int ACTIVATION_TICKS = 8;
	public static final int RANGE = 128;
	private static final int SPARK_CYCLE = 200;

	public LightningRodBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false))
		);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		boolean bl = fluidState.getType() == Fluids.WATER;
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getClickedFace()).setValue(WATERLOGGED, Boolean.valueOf(bl));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(POWERED) && blockState.getValue(FACING) == direction ? 15 : 0;
	}

	public void onLightningStrike(BlockState blockState, Level level, BlockPos blockPos) {
		level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(true)), 3);
		this.updateNeighbours(blockState, level, blockPos);
		level.getBlockTicks().scheduleTick(blockPos, this, 8);
		level.levelEvent(3002, blockPos, ((Direction)blockState.getValue(FACING)).getAxis().ordinal());
	}

	private void updateNeighbours(BlockState blockState, Level level, BlockPos blockPos) {
		level.updateNeighborsAt(blockPos.relative(((Direction)blockState.getValue(FACING)).getOpposite()), this);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		serverLevel.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(false)), 3);
		this.updateNeighbours(blockState, serverLevel, blockPos);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if (level.isThundering()
			&& (long)level.random.nextInt(200) <= level.getGameTime() % 200L
			&& blockPos.getY() == level.getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX(), blockPos.getZ()) - 1) {
			ParticleUtils.spawnParticlesAlongAxis(
				((Direction)blockState.getValue(FACING)).getAxis(), level, blockPos, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(1, 2)
			);
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			if ((Boolean)blockState.getValue(POWERED)) {
				this.updateNeighbours(blockState, level, blockPos);
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			if ((Boolean)blockState.getValue(POWERED) && !level.getBlockTicks().hasScheduledTick(blockPos, this)) {
				level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(false)), 18);
			}
		}
	}

	@Override
	public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
		if (level.isThundering() && projectile instanceof ThrownTrident && ((ThrownTrident)projectile).isChanneling()) {
			BlockPos blockPos = blockHitResult.getBlockPos();
			if (level.canSeeSky(blockPos)) {
				LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
				lightningBolt.moveTo(Vec3.atBottomCenterOf(blockPos.above()));
				Entity entity = projectile.getOwner();
				lightningBolt.setCause(entity instanceof ServerPlayer ? (ServerPlayer)entity : null);
				level.addFreshEntity(lightningBolt);
				level.playSound(null, blockPos, SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED, WATERLOGGED);
	}

	@Override
	public boolean isSignalSource(BlockState blockState) {
		return true;
	}
}
