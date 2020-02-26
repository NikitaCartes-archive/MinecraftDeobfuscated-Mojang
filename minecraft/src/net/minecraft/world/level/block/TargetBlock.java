package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TargetBlock extends Block {
	private static final IntegerProperty OUTPUT_POWER = BlockStateProperties.POWER;

	public TargetBlock(Block.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(OUTPUT_POWER, Integer.valueOf(0)));
	}

	@Override
	public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Entity entity) {
		int i = updateRedstoneOutput(level, blockState, blockHitResult, entity);
		Entity entity2 = null;
		if (entity instanceof AbstractArrow) {
			entity2 = ((AbstractArrow)entity).getOwner();
		} else if (entity instanceof ThrowableProjectile) {
			entity2 = ((ThrowableProjectile)entity).getOwner();
		}

		if (entity2 instanceof ServerPlayer) {
			ServerPlayer serverPlayer = (ServerPlayer)entity2;
			serverPlayer.awardStat(Stats.TARGET_HIT);
			CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverPlayer, i);
		}
	}

	private static int updateRedstoneOutput(LevelAccessor levelAccessor, BlockState blockState, BlockHitResult blockHitResult, Entity entity) {
		int i = getRedstoneStrength(blockHitResult, blockHitResult.getLocation());
		int j = entity instanceof AbstractArrow ? 20 : 8;
		if (!levelAccessor.getBlockTicks().hasScheduledTick(blockHitResult.getBlockPos(), blockState.getBlock())) {
			setOutputPower(levelAccessor, blockState, i, blockHitResult.getBlockPos(), j);
		}

		return i;
	}

	private static int getRedstoneStrength(BlockHitResult blockHitResult, Vec3 vec3) {
		Direction direction = blockHitResult.getDirection();
		double d = Math.abs(Mth.frac(vec3.x) - 0.5);
		double e = Math.abs(Mth.frac(vec3.y) - 0.5);
		double f = Math.abs(Mth.frac(vec3.z) - 0.5);
		Direction.Axis axis = direction.getAxis();
		double g;
		if (axis == Direction.Axis.Y) {
			g = Math.max(d, f);
		} else if (axis == Direction.Axis.Z) {
			g = Math.max(d, e);
		} else {
			g = Math.max(e, f);
		}

		return Mth.ceil(15.0 * Mth.clamp((0.5 - g) / 0.5, 0.0, 1.0));
	}

	private static void setOutputPower(LevelAccessor levelAccessor, BlockState blockState, int i, BlockPos blockPos, int j) {
		levelAccessor.setBlock(blockPos, blockState.setValue(OUTPUT_POWER, Integer.valueOf(i)), 3);
		levelAccessor.getBlockTicks().scheduleTick(blockPos, blockState.getBlock(), j);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if ((Integer)blockState.getValue(OUTPUT_POWER) != 0) {
			serverLevel.setBlock(blockPos, blockState.setValue(OUTPUT_POWER, Integer.valueOf(0)), 3);
		}
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return (Integer)blockState.getValue(OUTPUT_POWER);
	}

	@Override
	public boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(OUTPUT_POWER);
	}
}
