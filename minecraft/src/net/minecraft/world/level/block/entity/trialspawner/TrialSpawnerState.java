package net.minecraft.world.level.block.entity.trialspawner;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.phys.Vec3;

public enum TrialSpawnerState implements StringRepresentable {
	INACTIVE("inactive", 0, TrialSpawnerState.ParticleEmission.NONE, -1.0, false),
	WAITING_FOR_PLAYERS("waiting_for_players", 4, TrialSpawnerState.ParticleEmission.SMALL_FLAMES, 200.0, true),
	ACTIVE("active", 8, TrialSpawnerState.ParticleEmission.FLAMES_AND_SMOKE, 1000.0, true),
	WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, TrialSpawnerState.ParticleEmission.SMALL_FLAMES, -1.0, false),
	EJECTING_REWARD("ejecting_reward", 8, TrialSpawnerState.ParticleEmission.SMALL_FLAMES, -1.0, false),
	COOLDOWN("cooldown", 0, TrialSpawnerState.ParticleEmission.SMOKE_INSIDE_AND_TOP_FACE, -1.0, false);

	private static final float DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40.0F;
	private static final int TIME_BETWEEN_EACH_EJECTION = Mth.floor(30.0F);
	private final String name;
	private final int lightLevel;
	private final double spinningMobSpeed;
	private final TrialSpawnerState.ParticleEmission particleEmission;
	private final boolean isCapableOfSpawning;

	private TrialSpawnerState(String string2, int j, TrialSpawnerState.ParticleEmission particleEmission, double d, boolean bl) {
		this.name = string2;
		this.lightLevel = j;
		this.particleEmission = particleEmission;
		this.spinningMobSpeed = d;
		this.isCapableOfSpawning = bl;
	}

	TrialSpawnerState tickAndGetNext(BlockPos blockPos, TrialSpawner trialSpawner, ServerLevel serverLevel) {
		TrialSpawnerData trialSpawnerData = trialSpawner.getData();
		TrialSpawnerConfig trialSpawnerConfig = trialSpawner.getConfig();
		PlayerDetector playerDetector = trialSpawner.getPlayerDetector();

		return switch (this) {
			case INACTIVE -> trialSpawnerData.getOrCreateDisplayEntity(trialSpawner, serverLevel, WAITING_FOR_PLAYERS) == null ? this : WAITING_FOR_PLAYERS;
			case WAITING_FOR_PLAYERS -> {
				if (!trialSpawnerData.hasMobToSpawn()) {
					yield INACTIVE;
				} else {
					trialSpawnerData.tryDetectPlayers(serverLevel, blockPos, playerDetector, trialSpawnerConfig.requiredPlayerRange());
					yield trialSpawnerData.detectedPlayers.isEmpty() ? this : ACTIVE;
				}
			}
			case ACTIVE -> {
				if (!trialSpawnerData.hasMobToSpawn()) {
					yield INACTIVE;
				} else {
					int i = trialSpawnerData.countAdditionalPlayers(blockPos);
					trialSpawnerData.tryDetectPlayers(serverLevel, blockPos, playerDetector, trialSpawnerConfig.requiredPlayerRange());
					if (trialSpawnerData.hasFinishedSpawningAllMobs(trialSpawnerConfig, i)) {
						if (trialSpawnerData.haveAllCurrentMobsDied()) {
							trialSpawnerData.cooldownEndsAt = serverLevel.getGameTime() + (long)trialSpawnerConfig.targetCooldownLength();
							trialSpawnerData.totalMobsSpawned = 0;
							trialSpawnerData.nextMobSpawnsAt = 0L;
							yield WAITING_FOR_REWARD_EJECTION;
						}
					} else if (trialSpawnerData.isReadyToSpawnNextMob(serverLevel, trialSpawnerConfig, i)) {
						trialSpawner.spawnMob(serverLevel, blockPos).ifPresent(uUID -> {
							trialSpawnerData.currentMobs.add(uUID);
							trialSpawnerData.totalMobsSpawned++;
							trialSpawnerData.nextMobSpawnsAt = serverLevel.getGameTime() + (long)trialSpawnerConfig.ticksBetweenSpawn();
							trialSpawnerData.spawnPotentials.getRandom(serverLevel.getRandom()).ifPresent(wrapper -> {
								trialSpawnerData.nextSpawnData = Optional.of((SpawnData)wrapper.getData());
								trialSpawner.markUpdated();
							});
						});
					}

					yield this;
				}
			}
			case WAITING_FOR_REWARD_EJECTION -> {
				if (trialSpawnerData.isReadyToOpenShutter(serverLevel, trialSpawnerConfig, 40.0F)) {
					serverLevel.playSound(null, blockPos, SoundEvents.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS);
					yield EJECTING_REWARD;
				} else {
					yield this;
				}
			}
			case EJECTING_REWARD -> {
				if (!trialSpawnerData.isReadyToEjectItems(serverLevel, trialSpawnerConfig, (float)TIME_BETWEEN_EACH_EJECTION)) {
					yield this;
				} else if (trialSpawnerData.detectedPlayers.isEmpty()) {
					serverLevel.playSound(null, blockPos, SoundEvents.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundSource.BLOCKS);
					trialSpawnerData.ejectingLootTable = Optional.empty();
					yield COOLDOWN;
				} else {
					if (trialSpawnerData.ejectingLootTable.isEmpty()) {
						trialSpawnerData.ejectingLootTable = trialSpawnerConfig.lootTablesToEject().getRandomValue(serverLevel.getRandom());
					}

					trialSpawnerData.ejectingLootTable.ifPresent(resourceLocation -> trialSpawner.ejectReward(serverLevel, blockPos, resourceLocation));
					trialSpawnerData.detectedPlayers.remove(trialSpawnerData.detectedPlayers.iterator().next());
					yield this;
				}
			}
			case COOLDOWN -> {
				if (trialSpawnerData.isCooldownFinished(serverLevel)) {
					trialSpawnerData.cooldownEndsAt = 0L;
					yield WAITING_FOR_PLAYERS;
				} else {
					yield this;
				}
			}
		};
	}

	public int lightLevel() {
		return this.lightLevel;
	}

	public double spinningMobSpeed() {
		return this.spinningMobSpeed;
	}

	public boolean hasSpinningMob() {
		return this.spinningMobSpeed >= 0.0;
	}

	public boolean isCapableOfSpawning() {
		return this.isCapableOfSpawning;
	}

	public void emitParticles(Level level, BlockPos blockPos) {
		this.particleEmission.emit(level, level.getRandom(), blockPos);
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	static class LightLevel {
		private static final int UNLIT = 0;
		private static final int HALF_LIT = 4;
		private static final int LIT = 8;

		private LightLevel() {
		}
	}

	interface ParticleEmission {
		TrialSpawnerState.ParticleEmission NONE = (level, randomSource, blockPos) -> {
		};
		TrialSpawnerState.ParticleEmission SMALL_FLAMES = (level, randomSource, blockPos) -> {
			if (randomSource.nextInt(2) == 0) {
				Vec3 vec3 = blockPos.getCenter().offsetRandom(randomSource, 0.9F);
				addParticle(ParticleTypes.SMALL_FLAME, vec3, level);
			}
		};
		TrialSpawnerState.ParticleEmission FLAMES_AND_SMOKE = (level, randomSource, blockPos) -> {
			Vec3 vec3 = blockPos.getCenter().offsetRandom(randomSource, 1.0F);
			addParticle(ParticleTypes.SMOKE, vec3, level);
			addParticle(ParticleTypes.FLAME, vec3, level);
		};
		TrialSpawnerState.ParticleEmission SMOKE_INSIDE_AND_TOP_FACE = (level, randomSource, blockPos) -> {
			Vec3 vec3 = blockPos.getCenter().offsetRandom(randomSource, 0.9F);
			if (randomSource.nextInt(3) == 0) {
				addParticle(ParticleTypes.SMOKE, vec3, level);
			}

			if (level.getGameTime() % 20L == 0L) {
				Vec3 vec32 = blockPos.getCenter().add(0.0, 0.5, 0.0);
				int i = level.getRandom().nextInt(4) + 20;

				for (int j = 0; j < i; j++) {
					addParticle(ParticleTypes.SMOKE, vec32, level);
				}
			}
		};

		private static void addParticle(SimpleParticleType simpleParticleType, Vec3 vec3, Level level) {
			level.addParticle(simpleParticleType, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
		}

		void emit(Level level, RandomSource randomSource, BlockPos blockPos);
	}

	static class SpinningMob {
		private static final double NONE = -1.0;
		private static final double SLOW = 200.0;
		private static final double FAST = 1000.0;

		private SpinningMob() {
		}
	}
}
