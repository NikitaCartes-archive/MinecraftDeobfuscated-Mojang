package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
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
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		super.onPlace(blockState, level, blockPos, blockState2, bl);
		level.getBlockTicks().scheduleTick(blockPos, blockState.getBlock(), 20);
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		int i = (Integer)blockState.getValue(CHARGE);
		if (player.getItemInHand(interactionHand).getItem() == Items.GLOWSTONE) {
			if (i < 4) {
				level.setBlock(blockPos, blockState.setValue(CHARGE, Integer.valueOf(i + 1)), 3);
				level.playLocalSound(
					(double)blockPos.getX() + 0.5,
					(double)blockPos.getY() + 0.5,
					(double)blockPos.getZ() + 0.5,
					SoundEvents.RESPAWN_ANCHOR_CHARGE,
					SoundSource.BLOCKS,
					1.0F,
					1.0F,
					false
				);
				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.CONSUME;
			}
		} else if (i == 0) {
			return InteractionResult.CONSUME;
		} else {
			if (level.dimension.getType() == DimensionType.NETHER) {
				if (!level.isClientSide) {
					((ServerPlayer)player).setRespawnPosition(level.dimension.getType(), blockPos, false, true);
				}

				level.playLocalSound(
					(double)blockPos.getX() + 0.5,
					(double)blockPos.getY() + 0.5,
					(double)blockPos.getZ() + 0.5,
					SoundEvents.RESPAWN_ANCHOR_SET_SPAWN,
					SoundSource.BLOCKS,
					1.0F,
					1.0F,
					false
				);
			} else {
				level.removeBlock(blockPos, false);
				level.explode(
					null,
					DamageSource.netherBedExplosion(),
					(double)blockPos.getX() + 0.5,
					(double)blockPos.getY() + 0.5,
					(double)blockPos.getZ() + 0.5,
					5.0F,
					true,
					Explosion.BlockInteraction.DESTROY
				);
			}

			return InteractionResult.SUCCESS;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if ((Integer)blockState.getValue(CHARGE) != 0) {
			if (random.nextInt(100) == 0) {
				level.playLocalSound(
					(double)blockPos.getX() + 0.5,
					(double)blockPos.getY() + 0.5,
					(double)blockPos.getZ() + 0.5,
					SoundEvents.RESPAWN_ANCHOR_AMBIENT,
					SoundSource.BLOCKS,
					0.5F,
					random.nextFloat() * 0.4F + 0.8F,
					false
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

	public static Optional<Vec3> findStandUpPosition(LevelReader levelReader, BlockPos blockPos) {
		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-1, 0, -1), blockPos.offset(1, 0, 1))) {
			BlockPos blockPos3 = blockPos2.below();
			BlockPos blockPos4 = blockPos2.above();
			if (levelReader.getBlockState(blockPos3).isFaceSturdy(levelReader, blockPos3, Direction.DOWN)
				&& levelReader.getBlockState(blockPos2).getCollisionShape(levelReader, blockPos2).isEmpty()
				&& levelReader.getBlockState(blockPos4).getCollisionShape(levelReader, blockPos4).isEmpty()) {
				return Optional.of(Vec3.atBottomCenterOf(blockPos2));
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
