package net.minecraft.world.level.block.entity;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.TargetColorParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.creaking.CreakingTransient;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class CreakingHeartBlockEntity extends BlockEntity {
	private static final int PLAYER_DETECTION_RANGE = 32;
	public static final int DISTANCE_TO_HOME_SQ = 1024;
	private static final int DISTANCE_CREAKING_TOO_FAR_SQ = 1156;
	private static final int SPAWN_RANGE_XZ = 16;
	private static final int SPAWN_RANGE_Y = 8;
	private static final int ATTEMPTS_PER_SPAWN = 5;
	private static final int UPDATE_TICKS = 20;
	private static final int HURT_CALL_TOTAL_TICKS = 100;
	private static final int NUMBER_OF_HURT_CALLS = 10;
	private static final int HURT_CALL_INTERVAL = 10;
	private static final int HURT_CALL_PARTICLE_TICKS = 50;
	@Nullable
	private CreakingTransient creaking;
	private int ticker;
	private int emitter;
	@Nullable
	private Vec3 emitterTarget;

	public CreakingHeartBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.CREAKING_HEART, blockPos, blockState);
	}

	public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, CreakingHeartBlockEntity creakingHeartBlockEntity) {
		if (creakingHeartBlockEntity.emitter > 0) {
			if (creakingHeartBlockEntity.emitter > 50) {
				creakingHeartBlockEntity.emitParticles((ServerLevel)level, 1, true);
				creakingHeartBlockEntity.emitParticles((ServerLevel)level, 1, false);
			}

			if (creakingHeartBlockEntity.emitter % 10 == 0 && level instanceof ServerLevel serverLevel && creakingHeartBlockEntity.emitterTarget != null) {
				if (creakingHeartBlockEntity.creaking != null) {
					creakingHeartBlockEntity.emitterTarget = creakingHeartBlockEntity.creaking.getBoundingBox().getCenter();
				}

				Vec3 vec3 = Vec3.atCenterOf(blockPos);
				float f = 0.2F + 0.8F * (float)(100 - creakingHeartBlockEntity.emitter) / 100.0F;
				Vec3 vec32 = vec3.subtract(creakingHeartBlockEntity.emitterTarget).scale((double)f).add(creakingHeartBlockEntity.emitterTarget);
				BlockPos blockPos2 = BlockPos.containing(vec32);
				float g = (float)creakingHeartBlockEntity.emitter / 2.0F / 100.0F + 0.5F;
				serverLevel.playSound(null, blockPos2, SoundEvents.CREAKING_HEART_HURT, SoundSource.BLOCKS, g, 1.0F);
			}

			creakingHeartBlockEntity.emitter--;
		}

		if (creakingHeartBlockEntity.ticker-- < 0) {
			creakingHeartBlockEntity.ticker = 20;
			if (creakingHeartBlockEntity.creaking != null) {
				if (CreakingHeartBlock.canSummonCreaking(level) && !(creakingHeartBlockEntity.creaking.distanceToSqr(Vec3.atBottomCenterOf(blockPos)) > 1156.0)) {
					if (creakingHeartBlockEntity.creaking.isRemoved()) {
						creakingHeartBlockEntity.creaking = null;
					}

					if (!CreakingHeartBlock.hasRequiredLogs(blockState, level, blockPos) && creakingHeartBlockEntity.creaking == null) {
						level.setBlock(blockPos, blockState.setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.CreakingHeartState.DISABLED), 3);
					}
				} else {
					creakingHeartBlockEntity.removeProtector(null);
				}
			} else if (!CreakingHeartBlock.hasRequiredLogs(blockState, level, blockPos)) {
				level.setBlock(blockPos, blockState.setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.CreakingHeartState.DISABLED), 3);
			} else {
				if (!CreakingHeartBlock.canSummonCreaking(level)) {
					if (blockState.getValue(CreakingHeartBlock.CREAKING) == CreakingHeartBlock.CreakingHeartState.ACTIVE) {
						level.setBlock(blockPos, blockState.setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.CreakingHeartState.DORMANT), 3);
						return;
					}
				} else if (blockState.getValue(CreakingHeartBlock.CREAKING) == CreakingHeartBlock.CreakingHeartState.DORMANT) {
					level.setBlock(blockPos, blockState.setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.CreakingHeartState.ACTIVE), 3);
					return;
				}

				if (blockState.getValue(CreakingHeartBlock.CREAKING) == CreakingHeartBlock.CreakingHeartState.ACTIVE) {
					if (level.getDifficulty() != Difficulty.PEACEFUL) {
						Player player = level.getNearestPlayer((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 32.0, false);
						if (player != null) {
							creakingHeartBlockEntity.creaking = spawnProtector((ServerLevel)level, creakingHeartBlockEntity);
							if (creakingHeartBlockEntity.creaking != null) {
								creakingHeartBlockEntity.creaking.makeSound(SoundEvents.CREAKING_SPAWN);
								level.playSound(null, creakingHeartBlockEntity.getBlockPos(), SoundEvents.CREAKING_HEART_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
							}
						}
					}
				}
			}
		}
	}

	@Nullable
	private static CreakingTransient spawnProtector(ServerLevel serverLevel, CreakingHeartBlockEntity creakingHeartBlockEntity) {
		BlockPos blockPos = creakingHeartBlockEntity.getBlockPos();
		Optional<CreakingTransient> optional = SpawnUtil.trySpawnMob(
			EntityType.CREAKING_TRANSIENT, EntitySpawnReason.SPAWNER, serverLevel, blockPos, 5, 16, 8, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER_NO_LEAVES
		);
		if (optional.isEmpty()) {
			return null;
		} else {
			CreakingTransient creakingTransient = (CreakingTransient)optional.get();
			serverLevel.gameEvent(creakingTransient, GameEvent.ENTITY_PLACE, creakingTransient.position());
			creakingTransient.spawnAnim();
			creakingTransient.bindToCreakingHeart(blockPos);
			return creakingTransient;
		}
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return this.saveCustomOnly(provider);
	}

	public void creakingHurt() {
		if (this.creaking != null) {
			if (this.level instanceof ServerLevel serverLevel) {
				this.emitParticles(serverLevel, 20, false);
				this.emitter = 100;
				this.emitterTarget = this.creaking.getBoundingBox().getCenter();
			}
		}
	}

	private void emitParticles(ServerLevel serverLevel, int i, boolean bl) {
		if (this.creaking != null) {
			int j = bl ? 16545810 : 6250335;
			RandomSource randomSource = serverLevel.random;

			for (double d = 0.0; d < (double)i; d++) {
				Vec3 vec3 = this.creaking
					.getBoundingBox()
					.getMinPosition()
					.add(
						randomSource.nextDouble() * this.creaking.getBoundingBox().getXsize(),
						randomSource.nextDouble() * this.creaking.getBoundingBox().getYsize(),
						randomSource.nextDouble() * this.creaking.getBoundingBox().getZsize()
					);
				Vec3 vec32 = Vec3.atLowerCornerOf(this.getBlockPos()).add(randomSource.nextDouble(), randomSource.nextDouble(), randomSource.nextDouble());
				if (bl) {
					Vec3 vec33 = vec3;
					vec3 = vec32;
					vec32 = vec33;
				}

				TargetColorParticleOption targetColorParticleOption = new TargetColorParticleOption(vec32, j);
				serverLevel.sendParticles(targetColorParticleOption, vec3.x, vec3.y, vec3.z, 1, 0.0, 0.0, 0.0, 0.0);
			}
		}
	}

	public void removeProtector(@Nullable DamageSource damageSource) {
		if (this.creaking != null) {
			this.creaking.tearDown(damageSource);
			this.creaking = null;
		}
	}

	public boolean isProtector(Creaking creaking) {
		return this.creaking == creaking;
	}
}
