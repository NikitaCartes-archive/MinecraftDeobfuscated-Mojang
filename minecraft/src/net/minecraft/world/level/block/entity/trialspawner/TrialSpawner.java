package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class TrialSpawner {
	public static final String NORMAL_CONFIG_TAG_NAME = "normal_config";
	public static final String OMINOUS_CONFIG_TAG_NAME = "ominous_config";
	public static final int DETECT_PLAYER_SPAWN_BUFFER = 40;
	private static final int DEFAULT_TARGET_COOLDOWN_LENGTH = 36000;
	private static final int DEFAULT_PLAYER_SCAN_RANGE = 14;
	private static final int MAX_MOB_TRACKING_DISTANCE = 47;
	private static final int MAX_MOB_TRACKING_DISTANCE_SQR = Mth.square(47);
	private static final float SPAWNING_AMBIENT_SOUND_CHANCE = 0.02F;
	private final Holder<TrialSpawnerConfig> normalConfig;
	private final Holder<TrialSpawnerConfig> ominousConfig;
	private final TrialSpawnerData data;
	private final int requiredPlayerRange;
	private final int targetCooldownLength;
	private final TrialSpawner.StateAccessor stateAccessor;
	private PlayerDetector playerDetector;
	private final PlayerDetector.EntitySelector entitySelector;
	private boolean overridePeacefulAndMobSpawnRule;
	private boolean isOminous;

	public Codec<TrialSpawner> codec() {
		return RecordCodecBuilder.create(
			instance -> instance.group(
						TrialSpawnerConfig.CODEC.optionalFieldOf("normal_config", Holder.direct(TrialSpawnerConfig.DEFAULT)).forGetter(trialSpawner -> trialSpawner.normalConfig),
						TrialSpawnerConfig.CODEC
							.optionalFieldOf("ominous_config", Holder.direct(TrialSpawnerConfig.DEFAULT))
							.forGetter(trialSpawner -> trialSpawner.ominousConfig),
						TrialSpawnerData.MAP_CODEC.forGetter(TrialSpawner::getData),
						Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("target_cooldown_length", 36000).forGetter(TrialSpawner::getTargetCooldownLength),
						Codec.intRange(1, 128).optionalFieldOf("required_player_range", 14).forGetter(TrialSpawner::getRequiredPlayerRange)
					)
					.apply(
						instance,
						(holder, holder2, trialSpawnerData, integer, integer2) -> new TrialSpawner(
								holder, holder2, trialSpawnerData, integer, integer2, this.stateAccessor, this.playerDetector, this.entitySelector
							)
					)
		);
	}

	public TrialSpawner(TrialSpawner.StateAccessor stateAccessor, PlayerDetector playerDetector, PlayerDetector.EntitySelector entitySelector) {
		this(
			Holder.direct(TrialSpawnerConfig.DEFAULT),
			Holder.direct(TrialSpawnerConfig.DEFAULT),
			new TrialSpawnerData(),
			36000,
			14,
			stateAccessor,
			playerDetector,
			entitySelector
		);
	}

	public TrialSpawner(
		Holder<TrialSpawnerConfig> holder,
		Holder<TrialSpawnerConfig> holder2,
		TrialSpawnerData trialSpawnerData,
		int i,
		int j,
		TrialSpawner.StateAccessor stateAccessor,
		PlayerDetector playerDetector,
		PlayerDetector.EntitySelector entitySelector
	) {
		this.normalConfig = holder;
		this.ominousConfig = holder2;
		this.data = trialSpawnerData;
		this.targetCooldownLength = i;
		this.requiredPlayerRange = j;
		this.stateAccessor = stateAccessor;
		this.playerDetector = playerDetector;
		this.entitySelector = entitySelector;
	}

	public TrialSpawnerConfig getConfig() {
		return this.isOminous ? this.getOminousConfig() : this.getNormalConfig();
	}

	@VisibleForTesting
	public TrialSpawnerConfig getNormalConfig() {
		return this.normalConfig.value();
	}

	@VisibleForTesting
	public TrialSpawnerConfig getOminousConfig() {
		return this.ominousConfig.value();
	}

	public void applyOminous(ServerLevel serverLevel, BlockPos blockPos) {
		serverLevel.setBlock(blockPos, serverLevel.getBlockState(blockPos).setValue(TrialSpawnerBlock.OMINOUS, Boolean.valueOf(true)), 3);
		serverLevel.levelEvent(3020, blockPos, 1);
		this.isOminous = true;
		this.data.resetAfterBecomingOminous(this, serverLevel);
	}

	public void removeOminous(ServerLevel serverLevel, BlockPos blockPos) {
		serverLevel.setBlock(blockPos, serverLevel.getBlockState(blockPos).setValue(TrialSpawnerBlock.OMINOUS, Boolean.valueOf(false)), 3);
		this.isOminous = false;
	}

	public boolean isOminous() {
		return this.isOminous;
	}

	public TrialSpawnerData getData() {
		return this.data;
	}

	public int getTargetCooldownLength() {
		return this.targetCooldownLength;
	}

	public int getRequiredPlayerRange() {
		return this.requiredPlayerRange;
	}

	public TrialSpawnerState getState() {
		return this.stateAccessor.getState();
	}

	public void setState(Level level, TrialSpawnerState trialSpawnerState) {
		this.stateAccessor.setState(level, trialSpawnerState);
	}

	public void markUpdated() {
		this.stateAccessor.markUpdated();
	}

	public PlayerDetector getPlayerDetector() {
		return this.playerDetector;
	}

	public PlayerDetector.EntitySelector getEntitySelector() {
		return this.entitySelector;
	}

	public boolean canSpawnInLevel(Level level) {
		if (this.overridePeacefulAndMobSpawnRule) {
			return true;
		} else {
			return level.getDifficulty() == Difficulty.PEACEFUL ? false : level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
		}
	}

	public Optional<UUID> spawnMob(ServerLevel serverLevel, BlockPos blockPos) {
		RandomSource randomSource = serverLevel.getRandom();
		SpawnData spawnData = this.data.getOrCreateNextSpawnData(this, serverLevel.getRandom());
		CompoundTag compoundTag = spawnData.entityToSpawn();
		ListTag listTag = compoundTag.getList("Pos", 6);
		Optional<EntityType<?>> optional = EntityType.by(compoundTag);
		if (optional.isEmpty()) {
			return Optional.empty();
		} else {
			int i = listTag.size();
			double d = i >= 1
				? listTag.getDouble(0)
				: (double)blockPos.getX() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.getConfig().spawnRange() + 0.5;
			double e = i >= 2 ? listTag.getDouble(1) : (double)(blockPos.getY() + randomSource.nextInt(3) - 1);
			double f = i >= 3
				? listTag.getDouble(2)
				: (double)blockPos.getZ() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.getConfig().spawnRange() + 0.5;
			if (!serverLevel.noCollision(((EntityType)optional.get()).getSpawnAABB(d, e, f))) {
				return Optional.empty();
			} else {
				Vec3 vec3 = new Vec3(d, e, f);
				if (!inLineOfSight(serverLevel, blockPos.getCenter(), vec3)) {
					return Optional.empty();
				} else {
					BlockPos blockPos2 = BlockPos.containing(vec3);
					if (!SpawnPlacements.checkSpawnRules((EntityType)optional.get(), serverLevel, EntitySpawnReason.TRIAL_SPAWNER, blockPos2, serverLevel.getRandom())) {
						return Optional.empty();
					} else {
						if (spawnData.getCustomSpawnRules().isPresent()) {
							SpawnData.CustomSpawnRules customSpawnRules = (SpawnData.CustomSpawnRules)spawnData.getCustomSpawnRules().get();
							if (!customSpawnRules.isValidPosition(blockPos2, serverLevel)) {
								return Optional.empty();
							}
						}

						Entity entity = EntityType.loadEntityRecursive(compoundTag, serverLevel, EntitySpawnReason.TRIAL_SPAWNER, entityx -> {
							entityx.moveTo(d, e, f, randomSource.nextFloat() * 360.0F, 0.0F);
							return entityx;
						});
						if (entity == null) {
							return Optional.empty();
						} else {
							if (entity instanceof Mob mob) {
								if (!mob.checkSpawnObstruction(serverLevel)) {
									return Optional.empty();
								}

								boolean bl = spawnData.getEntityToSpawn().size() == 1 && spawnData.getEntityToSpawn().contains("id", 8);
								if (bl) {
									mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.TRIAL_SPAWNER, null);
								}

								mob.setPersistenceRequired();
								spawnData.getEquipment().ifPresent(mob::equip);
							}

							if (!serverLevel.tryAddFreshEntityWithPassengers(entity)) {
								return Optional.empty();
							} else {
								TrialSpawner.FlameParticle flameParticle = this.isOminous ? TrialSpawner.FlameParticle.OMINOUS : TrialSpawner.FlameParticle.NORMAL;
								serverLevel.levelEvent(3011, blockPos, flameParticle.encode());
								serverLevel.levelEvent(3012, blockPos2, flameParticle.encode());
								serverLevel.gameEvent(entity, GameEvent.ENTITY_PLACE, blockPos2);
								return Optional.of(entity.getUUID());
							}
						}
					}
				}
			}
		}
	}

	public void ejectReward(ServerLevel serverLevel, BlockPos blockPos, ResourceKey<LootTable> resourceKey) {
		LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(resourceKey);
		LootParams lootParams = new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY);
		ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams);
		if (!objectArrayList.isEmpty()) {
			for (ItemStack itemStack : objectArrayList) {
				DefaultDispenseItemBehavior.spawnItem(serverLevel, itemStack, 2, Direction.UP, Vec3.atBottomCenterOf(blockPos).relative(Direction.UP, 1.2));
			}

			serverLevel.levelEvent(3014, blockPos, 0);
		}
	}

	public void tickClient(Level level, BlockPos blockPos, boolean bl) {
		TrialSpawnerState trialSpawnerState = this.getState();
		trialSpawnerState.emitParticles(level, blockPos, bl);
		if (trialSpawnerState.hasSpinningMob()) {
			double d = (double)Math.max(0L, this.data.nextMobSpawnsAt - level.getGameTime());
			this.data.oSpin = this.data.spin;
			this.data.spin = (this.data.spin + trialSpawnerState.spinningMobSpeed() / (d + 200.0)) % 360.0;
		}

		if (trialSpawnerState.isCapableOfSpawning()) {
			RandomSource randomSource = level.getRandom();
			if (randomSource.nextFloat() <= 0.02F) {
				SoundEvent soundEvent = bl ? SoundEvents.TRIAL_SPAWNER_AMBIENT_OMINOUS : SoundEvents.TRIAL_SPAWNER_AMBIENT;
				level.playLocalSound(blockPos, soundEvent, SoundSource.BLOCKS, randomSource.nextFloat() * 0.25F + 0.75F, randomSource.nextFloat() + 0.5F, false);
			}
		}
	}

	public void tickServer(ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
		this.isOminous = bl;
		TrialSpawnerState trialSpawnerState = this.getState();
		if (this.data.currentMobs.removeIf(uUID -> shouldMobBeUntracked(serverLevel, blockPos, uUID))) {
			this.data.nextMobSpawnsAt = serverLevel.getGameTime() + (long)this.getConfig().ticksBetweenSpawn();
		}

		TrialSpawnerState trialSpawnerState2 = trialSpawnerState.tickAndGetNext(blockPos, this, serverLevel);
		if (trialSpawnerState2 != trialSpawnerState) {
			this.setState(serverLevel, trialSpawnerState2);
		}
	}

	private static boolean shouldMobBeUntracked(ServerLevel serverLevel, BlockPos blockPos, UUID uUID) {
		Entity entity = serverLevel.getEntity(uUID);
		return entity == null
			|| !entity.isAlive()
			|| !entity.level().dimension().equals(serverLevel.dimension())
			|| entity.blockPosition().distSqr(blockPos) > (double)MAX_MOB_TRACKING_DISTANCE_SQR;
	}

	private static boolean inLineOfSight(Level level, Vec3 vec3, Vec3 vec32) {
		BlockHitResult blockHitResult = level.clip(new ClipContext(vec32, vec3, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
		return blockHitResult.getBlockPos().equals(BlockPos.containing(vec3)) || blockHitResult.getType() == HitResult.Type.MISS;
	}

	public static void addSpawnParticles(Level level, BlockPos blockPos, RandomSource randomSource, SimpleParticleType simpleParticleType) {
		for (int i = 0; i < 20; i++) {
			double d = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
			double e = (double)blockPos.getY() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
			double f = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
			level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
			level.addParticle(simpleParticleType, d, e, f, 0.0, 0.0, 0.0);
		}
	}

	public static void addBecomeOminousParticles(Level level, BlockPos blockPos, RandomSource randomSource) {
		for (int i = 0; i < 20; i++) {
			double d = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
			double e = (double)blockPos.getY() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
			double f = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
			double g = randomSource.nextGaussian() * 0.02;
			double h = randomSource.nextGaussian() * 0.02;
			double j = randomSource.nextGaussian() * 0.02;
			level.addParticle(ParticleTypes.TRIAL_OMEN, d, e, f, g, h, j);
			level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d, e, f, g, h, j);
		}
	}

	public static void addDetectPlayerParticles(Level level, BlockPos blockPos, RandomSource randomSource, int i, ParticleOptions particleOptions) {
		for (int j = 0; j < 30 + Math.min(i, 10) * 5; j++) {
			double d = (double)(2.0F * randomSource.nextFloat() - 1.0F) * 0.65;
			double e = (double)(2.0F * randomSource.nextFloat() - 1.0F) * 0.65;
			double f = (double)blockPos.getX() + 0.5 + d;
			double g = (double)blockPos.getY() + 0.1 + (double)randomSource.nextFloat() * 0.8;
			double h = (double)blockPos.getZ() + 0.5 + e;
			level.addParticle(particleOptions, f, g, h, 0.0, 0.0, 0.0);
		}
	}

	public static void addEjectItemParticles(Level level, BlockPos blockPos, RandomSource randomSource) {
		for (int i = 0; i < 20; i++) {
			double d = (double)blockPos.getX() + 0.4 + randomSource.nextDouble() * 0.2;
			double e = (double)blockPos.getY() + 0.4 + randomSource.nextDouble() * 0.2;
			double f = (double)blockPos.getZ() + 0.4 + randomSource.nextDouble() * 0.2;
			double g = randomSource.nextGaussian() * 0.02;
			double h = randomSource.nextGaussian() * 0.02;
			double j = randomSource.nextGaussian() * 0.02;
			level.addParticle(ParticleTypes.SMALL_FLAME, d, e, f, g, h, j * 0.25);
			level.addParticle(ParticleTypes.SMOKE, d, e, f, g, h, j);
		}
	}

	@Deprecated(
		forRemoval = true
	)
	@VisibleForTesting
	public void setPlayerDetector(PlayerDetector playerDetector) {
		this.playerDetector = playerDetector;
	}

	@Deprecated(
		forRemoval = true
	)
	@VisibleForTesting
	public void overridePeacefulAndMobSpawnRule() {
		this.overridePeacefulAndMobSpawnRule = true;
	}

	public static enum FlameParticle {
		NORMAL(ParticleTypes.FLAME),
		OMINOUS(ParticleTypes.SOUL_FIRE_FLAME);

		public final SimpleParticleType particleType;

		private FlameParticle(final SimpleParticleType simpleParticleType) {
			this.particleType = simpleParticleType;
		}

		public static TrialSpawner.FlameParticle decode(int i) {
			TrialSpawner.FlameParticle[] flameParticles = values();
			return i <= flameParticles.length && i >= 0 ? flameParticles[i] : NORMAL;
		}

		public int encode() {
			return this.ordinal();
		}
	}

	public interface StateAccessor {
		void setState(Level level, TrialSpawnerState trialSpawnerState);

		TrialSpawnerState getState();

		void markUpdated();
	}
}
