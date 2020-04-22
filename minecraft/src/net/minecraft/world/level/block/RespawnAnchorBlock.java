package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RespawnAnchorBlock extends Block {
	public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;

	public RespawnAnchorBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(CHARGE, Integer.valueOf(0)));
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.getItem() == Items.GLOWSTONE && (Integer)blockState.getValue(CHARGE) < 4) {
			charge(level, blockPos, blockState);
			if (!player.abilities.instabuild) {
				itemStack.shrink(1);
			}

			return InteractionResult.SUCCESS;
		} else if ((Integer)blockState.getValue(CHARGE) == 0) {
			return InteractionResult.PASS;
		} else if (!canSetSpawn(level)) {
			if (!level.isClientSide) {
				level.removeBlock(blockPos, false);
				level.explode(
					null,
					DamageSource.badRespawnPointExplosion(),
					(double)blockPos.getX() + 0.5,
					(double)blockPos.getY() + 0.5,
					(double)blockPos.getZ() + 0.5,
					5.0F,
					true,
					Explosion.BlockInteraction.DESTROY
				);
			}

			return InteractionResult.SUCCESS;
		} else {
			if (!level.isClientSide) {
				ServerPlayer serverPlayer = (ServerPlayer)player;
				if (serverPlayer.getRespawnDimension() != level.dimension.getType() || !serverPlayer.getRespawnPosition().equals(blockPos)) {
					serverPlayer.setRespawnPosition(level.dimension.getType(), blockPos, false, true);
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

			return blockState.getValue(CHARGE) < 4 ? InteractionResult.PASS : InteractionResult.CONSUME;
		}
	}

	public static boolean canSetSpawn(Level level) {
		return level.dimension.getType() == DimensionType.NETHER;
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

			double d = (double)blockPos.getX() + 0.5 + (double)(0.5F - random.nextFloat());
			double e = (double)blockPos.getY() + 1.0;
			double f = (double)blockPos.getZ() + 0.5 + (double)(0.5F - random.nextFloat());
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

	public static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, LevelReader levelReader, BlockPos blockPos) {
		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-1, -1, -1), blockPos.offset(1, 1, 1))) {
			Optional<Vec3> optional = BedBlock.getStandingLocationAtOrBelow(entityType, levelReader, blockPos2);
			if (optional.isPresent()) {
				return optional;
			}
		}

		return Optional.empty();
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
