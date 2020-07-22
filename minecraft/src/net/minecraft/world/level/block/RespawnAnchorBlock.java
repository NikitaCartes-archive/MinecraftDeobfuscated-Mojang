package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RespawnAnchorBlock extends Block {
	public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
	private static final ImmutableList<Vec3i> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of(
		new Vec3i(0, 0, -1),
		new Vec3i(-1, 0, 0),
		new Vec3i(0, 0, 1),
		new Vec3i(1, 0, 0),
		new Vec3i(-1, 0, -1),
		new Vec3i(1, 0, -1),
		new Vec3i(-1, 0, 1),
		new Vec3i(1, 0, 1)
	);
	private static final ImmutableList<Vec3i> RESPAWN_OFFSETS = new Builder<Vec3i>()
		.addAll(RESPAWN_HORIZONTAL_OFFSETS)
		.addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::below).iterator())
		.addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::above).iterator())
		.add(new Vec3i(0, 1, 0))
		.build();

	public RespawnAnchorBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(CHARGE, Integer.valueOf(0)));
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (interactionHand == InteractionHand.MAIN_HAND && !isRespawnFuel(itemStack) && isRespawnFuel(player.getItemInHand(InteractionHand.OFF_HAND))) {
			return InteractionResult.PASS;
		} else if (isRespawnFuel(itemStack) && canBeCharged(blockState)) {
			charge(level, blockPos, blockState);
			if (!player.abilities.instabuild) {
				itemStack.shrink(1);
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		} else if ((Integer)blockState.getValue(CHARGE) == 0) {
			return InteractionResult.PASS;
		} else if (!canSetSpawn(level)) {
			if (!level.isClientSide) {
				this.explode(blockState, level, blockPos);
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			if (!level.isClientSide) {
				ServerPlayer serverPlayer = (ServerPlayer)player;
				if (serverPlayer.getRespawnDimension() != level.dimension() || !serverPlayer.getRespawnPosition().equals(blockPos)) {
					serverPlayer.setRespawnPosition(level.dimension(), blockPos, 0.0F, false, true);
					level.playSound(
						null,
						(double)blockPos.getX() + 0.5,
						(double)blockPos.getY() + 0.5,
						(double)blockPos.getZ() + 0.5,
						SoundEvents.RESPAWN_ANCHOR_SET_SPAWN,
						SoundSource.BLOCKS,
						1.0F,
						1.0F
					);
					return InteractionResult.SUCCESS;
				}
			}

			return InteractionResult.CONSUME;
		}
	}

	private static boolean isRespawnFuel(ItemStack itemStack) {
		return itemStack.getItem() == Items.GLOWSTONE;
	}

	private static boolean canBeCharged(BlockState blockState) {
		return (Integer)blockState.getValue(CHARGE) < 4;
	}

	private static boolean isWaterThatWouldFlow(BlockPos blockPos, Level level) {
		FluidState fluidState = level.getFluidState(blockPos);
		if (!fluidState.is(FluidTags.WATER)) {
			return false;
		} else if (fluidState.isSource()) {
			return true;
		} else {
			float f = (float)fluidState.getAmount();
			if (f < 2.0F) {
				return false;
			} else {
				FluidState fluidState2 = level.getFluidState(blockPos.below());
				return !fluidState2.is(FluidTags.WATER);
			}
		}
	}

	private void explode(BlockState blockState, Level level, BlockPos blockPos) {
		level.removeBlock(blockPos, false);
		boolean bl = Direction.Plane.HORIZONTAL.stream().map(blockPos::relative).anyMatch(blockPosx -> isWaterThatWouldFlow(blockPosx, level));
		final boolean bl2 = bl || level.getFluidState(blockPos.above()).is(FluidTags.WATER);
		ExplosionDamageCalculator explosionDamageCalculator = new ExplosionDamageCalculator() {
			@Override
			public Optional<Float> getBlockExplosionResistance(
				Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState
			) {
				return blockPos.equals(blockPos) && bl2
					? Optional.of(Blocks.WATER.getExplosionResistance())
					: super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState);
			}
		};
		level.explode(
			null,
			DamageSource.badRespawnPointExplosion(),
			explosionDamageCalculator,
			(double)blockPos.getX() + 0.5,
			(double)blockPos.getY() + 0.5,
			(double)blockPos.getZ() + 0.5,
			5.0F,
			true,
			Explosion.BlockInteraction.DESTROY
		);
	}

	public static boolean canSetSpawn(Level level) {
		return level.dimensionType().respawnAnchorWorks();
	}

	public static void charge(Level level, BlockPos blockPos, BlockState blockState) {
		level.setBlock(blockPos, blockState.setValue(CHARGE, Integer.valueOf((Integer)blockState.getValue(CHARGE) + 1)), 3);
		level.playSound(
			null,
			(double)blockPos.getX() + 0.5,
			(double)blockPos.getY() + 0.5,
			(double)blockPos.getZ() + 0.5,
			SoundEvents.RESPAWN_ANCHOR_CHARGE,
			SoundSource.BLOCKS,
			1.0F,
			1.0F
		);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if ((Integer)blockState.getValue(CHARGE) != 0) {
			if (random.nextInt(100) == 0) {
				level.playSound(
					null,
					(double)blockPos.getX() + 0.5,
					(double)blockPos.getY() + 0.5,
					(double)blockPos.getZ() + 0.5,
					SoundEvents.RESPAWN_ANCHOR_AMBIENT,
					SoundSource.BLOCKS,
					1.0F,
					1.0F
				);
			}

			double d = (double)blockPos.getX() + 0.5 + (0.5 - random.nextDouble());
			double e = (double)blockPos.getY() + 1.0;
			double f = (double)blockPos.getZ() + 0.5 + (0.5 - random.nextDouble());
			double g = (double)random.nextFloat() * 0.04;
			level.addParticle(ParticleTypes.REVERSE_PORTAL, d, e, f, 0.0, g, 0.0);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(CHARGE);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	public static int getScaledChargeLevel(BlockState blockState, int i) {
		return Mth.floor((float)((Integer)blockState.getValue(CHARGE) - 0) / 4.0F * (float)i);
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return getScaledChargeLevel(blockState, 15);
	}

	public static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos) {
		Optional<Vec3> optional = findStandUpPosition(entityType, collisionGetter, blockPos, true);
		return optional.isPresent() ? optional : findStandUpPosition(entityType, collisionGetter, blockPos, false);
	}

	private static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, boolean bl) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Vec3i vec3i : RESPAWN_OFFSETS) {
			mutableBlockPos.set(blockPos).move(vec3i);
			Vec3 vec3 = DismountHelper.findSafeDismountLocation(entityType, collisionGetter, mutableBlockPos, bl);
			if (vec3 != null) {
				return Optional.of(vec3);
			}
		}

		return Optional.empty();
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
