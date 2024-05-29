package net.minecraft.world.level.block.entity.trialspawner;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

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

	private TrialSpawnerState(final String string2, final int j, final TrialSpawnerState.ParticleEmission particleEmission, final double d, final boolean bl) {
		this.name = string2;
		this.lightLevel = j;
		this.particleEmission = particleEmission;
		this.spinningMobSpeed = d;
		this.isCapableOfSpawning = bl;
	}

	TrialSpawnerState tickAndGetNext(BlockPos blockPos, TrialSpawner trialSpawner, ServerLevel serverLevel) {
		TrialSpawnerData trialSpawnerData = trialSpawner.getData();
		TrialSpawnerConfig trialSpawnerConfig = trialSpawner.getConfig();

		return switch (this) {
			case INACTIVE -> trialSpawnerData.getOrCreateDisplayEntity(trialSpawner, serverLevel, WAITING_FOR_PLAYERS) == null ? this : WAITING_FOR_PLAYERS;
			case WAITING_FOR_PLAYERS -> {
				if (!trialSpawner.canSpawnInLevel(serverLevel)) {
					trialSpawnerData.reset();
					yield this;
				} else if (!trialSpawnerData.hasMobToSpawn(trialSpawner, serverLevel.random)) {
					yield INACTIVE;
				} else {
					trialSpawnerData.tryDetectPlayers(serverLevel, blockPos, trialSpawner);
					yield trialSpawnerData.detectedPlayers.isEmpty() ? this : ACTIVE;
				}
			}
			case ACTIVE -> {
				if (!trialSpawner.canSpawnInLevel(serverLevel)) {
					trialSpawnerData.reset();
					yield WAITING_FOR_PLAYERS;
				} else if (!trialSpawnerData.hasMobToSpawn(trialSpawner, serverLevel.random)) {
					yield INACTIVE;
				} else {
					int i = trialSpawnerData.countAdditionalPlayers(blockPos);
					trialSpawnerData.tryDetectPlayers(serverLevel, blockPos, trialSpawner);
					if (trialSpawner.isOminous()) {
						this.spawnOminousOminousItemSpawner(serverLevel, blockPos, trialSpawner);
					}

					if (trialSpawnerData.hasFinishedSpawningAllMobs(trialSpawnerConfig, i)) {
						if (trialSpawnerData.haveAllCurrentMobsDied()) {
							trialSpawnerData.cooldownEndsAt = serverLevel.getGameTime() + (long)trialSpawner.getTargetCooldownLength();
							trialSpawnerData.totalMobsSpawned = 0;
							trialSpawnerData.nextMobSpawnsAt = 0L;
							yield WAITING_FOR_REWARD_EJECTION;
						}
					} else if (trialSpawnerData.isReadyToSpawnNextMob(serverLevel, trialSpawnerConfig, i)) {
						trialSpawner.spawnMob(serverLevel, blockPos).ifPresent(uUID -> {
							trialSpawnerData.currentMobs.add(uUID);
							trialSpawnerData.totalMobsSpawned++;
							trialSpawnerData.nextMobSpawnsAt = serverLevel.getGameTime() + (long)trialSpawnerConfig.ticksBetweenSpawn();
							trialSpawnerConfig.spawnPotentialsDefinition().getRandom(serverLevel.getRandom()).ifPresent(wrapper -> {
								trialSpawnerData.nextSpawnData = Optional.of((SpawnData)wrapper.data());
								trialSpawner.markUpdated();
							});
						});
					}

					yield this;
				}
			}
			case WAITING_FOR_REWARD_EJECTION -> {
				if (trialSpawnerData.isReadyToOpenShutter(serverLevel, 40.0F, trialSpawner.getTargetCooldownLength())) {
					serverLevel.playSound(null, blockPos, SoundEvents.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS);
					yield EJECTING_REWARD;
				} else {
					yield this;
				}
			}
			case EJECTING_REWARD -> {
				if (!trialSpawnerData.isReadyToEjectItems(serverLevel, (float)TIME_BETWEEN_EACH_EJECTION, trialSpawner.getTargetCooldownLength())) {
					yield this;
				} else if (trialSpawnerData.detectedPlayers.isEmpty()) {
					serverLevel.playSound(null, blockPos, SoundEvents.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundSource.BLOCKS);
					trialSpawnerData.ejectingLootTable = Optional.empty();
					yield COOLDOWN;
				} else {
					if (trialSpawnerData.ejectingLootTable.isEmpty()) {
						trialSpawnerData.ejectingLootTable = trialSpawnerConfig.lootTablesToEject().getRandomValue(serverLevel.getRandom());
					}

					trialSpawnerData.ejectingLootTable.ifPresent(resourceKey -> trialSpawner.ejectReward(serverLevel, blockPos, resourceKey));
					trialSpawnerData.detectedPlayers.remove(trialSpawnerData.detectedPlayers.iterator().next());
					yield this;
				}
			}
			case COOLDOWN -> {
				trialSpawnerData.tryDetectPlayers(serverLevel, blockPos, trialSpawner);
				if (!trialSpawnerData.detectedPlayers.isEmpty()) {
					trialSpawnerData.totalMobsSpawned = 0;
					trialSpawnerData.nextMobSpawnsAt = 0L;
					yield ACTIVE;
				} else if (trialSpawnerData.isCooldownFinished(serverLevel)) {
					trialSpawner.removeOminous(serverLevel, blockPos);
					trialSpawnerData.reset();
					yield WAITING_FOR_PLAYERS;
				} else {
					yield this;
				}
			}
		};
	}

	private void spawnOminousOminousItemSpawner(ServerLevel serverLevel, BlockPos blockPos, TrialSpawner trialSpawner) {
		TrialSpawnerData trialSpawnerData = trialSpawner.getData();
		TrialSpawnerConfig trialSpawnerConfig = trialSpawner.getConfig();
		ItemStack itemStack = (ItemStack)trialSpawnerData.getDispensingItems(serverLevel, trialSpawnerConfig, blockPos)
			.getRandomValue(serverLevel.random)
			.orElse(ItemStack.EMPTY);
		if (!itemStack.isEmpty()) {
			if (this.timeToSpawnItemSpawner(serverLevel, trialSpawnerData)) {
				calculatePositionToSpawnSpawner(serverLevel, blockPos, trialSpawner, trialSpawnerData).ifPresent(vec3 -> {
					OminousItemSpawner ominousItemSpawner = OminousItemSpawner.create(serverLevel, itemStack);
					ominousItemSpawner.moveTo(vec3);
					serverLevel.addFreshEntity(ominousItemSpawner);
					float f = (serverLevel.getRandom().nextFloat() - serverLevel.getRandom().nextFloat()) * 0.2F + 1.0F;
					serverLevel.playSound(null, BlockPos.containing(vec3), SoundEvents.TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundSource.BLOCKS, 1.0F, f);
					trialSpawnerData.cooldownEndsAt = serverLevel.getGameTime() + trialSpawner.getOminousConfig().ticksBetweenItemSpawners();
				});
			}
		}
	}

	private static Optional<Vec3> calculatePositionToSpawnSpawner(
		ServerLevel serverLevel, BlockPos blockPos, TrialSpawner trialSpawner, TrialSpawnerData trialSpawnerData
	) {
		List<Player> list = trialSpawnerData.detectedPlayers
			.stream()
			.map(serverLevel::getPlayerByUUID)
			.filter(Objects::nonNull)
			.filter(
				player -> !player.isCreative()
						&& !player.isSpectator()
						&& player.isAlive()
						&& player.distanceToSqr(blockPos.getCenter()) <= (double)Mth.square(trialSpawner.getRequiredPlayerRange())
			)
			.toList();
		if (list.isEmpty()) {
			return Optional.empty();
		} else {
			Entity entity = selectEntityToSpawnItemAbove(list, trialSpawnerData.currentMobs, trialSpawner, blockPos, serverLevel);
			return entity == null ? Optional.empty() : calculatePositionAbove(entity, serverLevel);
		}
	}

	private static Optional<Vec3> calculatePositionAbove(Entity entity, ServerLevel serverLevel) {
		Vec3 vec3 = entity.position();
		Vec3 vec32 = vec3.relative(Direction.UP, (double)(entity.getBbHeight() + 2.0F + (float)serverLevel.random.nextInt(4)));
		BlockHitResult blockHitResult = serverLevel.clip(new ClipContext(vec3, vec32, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
		Vec3 vec33 = blockHitResult.getBlockPos().getCenter().relative(Direction.DOWN, 1.0);
		BlockPos blockPos = BlockPos.containing(vec33);
		return !serverLevel.getBlockState(blockPos).getCollisionShape(serverLevel, blockPos).isEmpty() ? Optional.empty() : Optional.of(vec33);
	}

	@Nullable
	private static Entity selectEntityToSpawnItemAbove(List<Player> list, Set<UUID> set, TrialSpawner trialSpawner, BlockPos blockPos, ServerLevel serverLevel) {
		Stream<Entity> stream = set.stream()
			.map(serverLevel::getEntity)
			.filter(Objects::nonNull)
			.filter(entity -> entity.isAlive() && entity.distanceToSqr(blockPos.getCenter()) <= (double)Mth.square(trialSpawner.getRequiredPlayerRange()));
		List<? extends Entity> list2 = serverLevel.random.nextBoolean() ? stream.toList() : list;
		if (list2.isEmpty()) {
			return null;
		} else {
			return list2.size() == 1 ? (Entity)list2.getFirst() : Util.getRandom(list2, serverLevel.random);
		}
	}

	private boolean timeToSpawnItemSpawner(ServerLevel serverLevel, TrialSpawnerData trialSpawnerData) {
		return serverLevel.getGameTime() >= trialSpawnerData.cooldownEndsAt;
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

	public void emitParticles(Level level, BlockPos blockPos, boolean bl) {
		this.particleEmission.emit(level, level.getRandom(), blockPos, bl);
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
		TrialSpawnerState.ParticleEmission NONE = (level, randomSource, blockPos, bl) -> {
		};
		TrialSpawnerState.ParticleEmission SMALL_FLAMES = (level, randomSource, blockPos, bl) -> {
			if (randomSource.nextInt(2) == 0) {
				Vec3 vec3 = blockPos.getCenter().offsetRandom(randomSource, 0.9F);
				addParticle(bl ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, vec3, level);
			}
		};
		TrialSpawnerState.ParticleEmission FLAMES_AND_SMOKE = (level, randomSource, blockPos, bl) -> {
			Vec3 vec3 = blockPos.getCenter().offsetRandom(randomSource, 1.0F);
			addParticle(ParticleTypes.SMOKE, vec3, level);
			addParticle(bl ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, vec3, level);
		};
		TrialSpawnerState.ParticleEmission SMOKE_INSIDE_AND_TOP_FACE = (level, randomSource, blockPos, bl) -> {
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

		void emit(Level level, RandomSource randomSource, BlockPos blockPos, boolean bl);
	}

	static class SpinningMob {
		private static final double NONE = -1.0;
		private static final double SLOW = 200.0;
		private static final double FAST = 1000.0;

		private SpinningMob() {
		}
	}
}
