package net.minecraft.world.level;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.NetherFortressFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NaturalSpawner {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int MIN_SPAWN_DISTANCE = 24;
	public static final int SPAWN_DISTANCE_CHUNK = 8;
	public static final int SPAWN_DISTANCE_BLOCK = 128;
	static final int MAGIC_NUMBER = (int)Math.pow(17.0, 2.0);
	private static final MobCategory[] SPAWNING_CATEGORIES = (MobCategory[])Stream.of(MobCategory.values())
		.filter(mobCategory -> mobCategory != MobCategory.MISC)
		.toArray(MobCategory[]::new);

	private NaturalSpawner() {
	}

	public static NaturalSpawner.SpawnState createState(
		int i, Iterable<Entity> iterable, NaturalSpawner.ChunkGetter chunkGetter, LocalMobCapCalculator localMobCapCalculator
	) {
		PotentialCalculator potentialCalculator = new PotentialCalculator();
		Object2IntOpenHashMap<MobCategory> object2IntOpenHashMap = new Object2IntOpenHashMap<>();

		for (Entity entity : iterable) {
			if (entity instanceof Mob mob && (mob.isPersistenceRequired() || mob.requiresCustomPersistence())) {
				continue;
			}

			MobCategory mobCategory = entity.getType().getCategory();
			if (mobCategory != MobCategory.MISC) {
				BlockPos blockPos = entity.blockPosition();
				chunkGetter.query(ChunkPos.asLong(blockPos), levelChunk -> {
					MobSpawnSettings.MobSpawnCost mobSpawnCost = getRoughBiome(blockPos, levelChunk).getMobSettings().getMobSpawnCost(entity.getType());
					if (mobSpawnCost != null) {
						potentialCalculator.addCharge(entity.blockPosition(), mobSpawnCost.getCharge());
					}

					if (entity instanceof Mob) {
						localMobCapCalculator.addMob(levelChunk.getPos(), mobCategory);
					}

					object2IntOpenHashMap.addTo(mobCategory, 1);
				});
			}
		}

		return new NaturalSpawner.SpawnState(i, object2IntOpenHashMap, potentialCalculator, localMobCapCalculator);
	}

	static Biome getRoughBiome(BlockPos blockPos, ChunkAccess chunkAccess) {
		return chunkAccess.getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getY()), QuartPos.fromBlock(blockPos.getZ()));
	}

	public static void spawnForChunk(ServerLevel serverLevel, LevelChunk levelChunk, NaturalSpawner.SpawnState spawnState, boolean bl, boolean bl2, boolean bl3) {
		serverLevel.getProfiler().push("spawner");

		for (MobCategory mobCategory : SPAWNING_CATEGORIES) {
			if ((bl || !mobCategory.isFriendly())
				&& (bl2 || mobCategory.isFriendly())
				&& (bl3 || !mobCategory.isPersistent())
				&& spawnState.canSpawnForCategory(mobCategory, levelChunk.getPos())) {
				spawnCategoryForChunk(mobCategory, serverLevel, levelChunk, spawnState::canSpawn, spawnState::afterSpawn);
			}
		}

		serverLevel.getProfiler().pop();
	}

	public static void spawnCategoryForChunk(
		MobCategory mobCategory,
		ServerLevel serverLevel,
		LevelChunk levelChunk,
		NaturalSpawner.SpawnPredicate spawnPredicate,
		NaturalSpawner.AfterSpawnCallback afterSpawnCallback
	) {
		BlockPos blockPos = getRandomPosWithin(serverLevel, levelChunk);
		if (blockPos.getY() >= serverLevel.getMinBuildHeight() + 1) {
			spawnCategoryForPosition(mobCategory, serverLevel, levelChunk, blockPos, spawnPredicate, afterSpawnCallback);
		}
	}

	@VisibleForDebug
	public static void spawnCategoryForPosition(MobCategory mobCategory, ServerLevel serverLevel, BlockPos blockPos) {
		spawnCategoryForPosition(
			mobCategory, serverLevel, serverLevel.getChunk(blockPos), blockPos, (entityType, blockPosx, chunkAccess) -> true, (mob, chunkAccess) -> {
			}
		);
	}

	public static void spawnCategoryForPosition(
		MobCategory mobCategory,
		ServerLevel serverLevel,
		ChunkAccess chunkAccess,
		BlockPos blockPos,
		NaturalSpawner.SpawnPredicate spawnPredicate,
		NaturalSpawner.AfterSpawnCallback afterSpawnCallback
	) {
		StructureFeatureManager structureFeatureManager = serverLevel.structureFeatureManager();
		ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
		int i = blockPos.getY();
		BlockState blockState = chunkAccess.getBlockState(blockPos);
		if (!blockState.isRedstoneConductor(chunkAccess, blockPos)) {
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			int j = 0;

			for (int k = 0; k < 3; k++) {
				int l = blockPos.getX();
				int m = blockPos.getZ();
				int n = 6;
				MobSpawnSettings.SpawnerData spawnerData = null;
				SpawnGroupData spawnGroupData = null;
				int o = Mth.ceil(serverLevel.random.nextFloat() * 4.0F);
				int p = 0;

				for (int q = 0; q < o; q++) {
					l += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6);
					m += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6);
					mutableBlockPos.set(l, i, m);
					double d = (double)l + 0.5;
					double e = (double)m + 0.5;
					Player player = serverLevel.getNearestPlayer(d, (double)i, e, -1.0, false);
					if (player != null) {
						double f = player.distanceToSqr(d, (double)i, e);
						if (isRightDistanceToPlayerAndSpawnPoint(serverLevel, chunkAccess, mutableBlockPos, f)) {
							if (spawnerData == null) {
								Optional<MobSpawnSettings.SpawnerData> optional = getRandomSpawnMobAt(
									serverLevel, structureFeatureManager, chunkGenerator, mobCategory, serverLevel.random, mutableBlockPos
								);
								if (optional.isEmpty()) {
									break;
								}

								spawnerData = (MobSpawnSettings.SpawnerData)optional.get();
								o = spawnerData.minCount + serverLevel.random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
							}

							if (isValidSpawnPostitionForType(serverLevel, mobCategory, structureFeatureManager, chunkGenerator, spawnerData, mutableBlockPos, f)
								&& spawnPredicate.test(spawnerData.type, mutableBlockPos, chunkAccess)) {
								Mob mob = getMobForSpawn(serverLevel, spawnerData.type);
								if (mob == null) {
									return;
								}

								mob.moveTo(d, (double)i, e, serverLevel.random.nextFloat() * 360.0F, 0.0F);
								if (isValidPositionForMob(serverLevel, mob, f)) {
									spawnGroupData = mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.NATURAL, spawnGroupData, null);
									j++;
									p++;
									serverLevel.addFreshEntityWithPassengers(mob);
									afterSpawnCallback.run(mob, chunkAccess);
									if (j >= mob.getMaxSpawnClusterSize()) {
										return;
									}

									if (mob.isMaxGroupSizeReached(p)) {
										break;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private static boolean isRightDistanceToPlayerAndSpawnPoint(
		ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos.MutableBlockPos mutableBlockPos, double d
	) {
		if (d <= 576.0) {
			return false;
		} else {
			return serverLevel.getSharedSpawnPos()
					.closerThan(new Vec3((double)mutableBlockPos.getX() + 0.5, (double)mutableBlockPos.getY(), (double)mutableBlockPos.getZ() + 0.5), 24.0)
				? false
				: Objects.equals(new ChunkPos(mutableBlockPos), chunkAccess.getPos()) || serverLevel.isNaturalSpawningAllowed(mutableBlockPos);
		}
	}

	private static boolean isValidSpawnPostitionForType(
		ServerLevel serverLevel,
		MobCategory mobCategory,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		MobSpawnSettings.SpawnerData spawnerData,
		BlockPos.MutableBlockPos mutableBlockPos,
		double d
	) {
		EntityType<?> entityType = spawnerData.type;
		if (entityType.getCategory() == MobCategory.MISC) {
			return false;
		} else if (!entityType.canSpawnFarFromPlayer() && d > (double)(entityType.getCategory().getDespawnDistance() * entityType.getCategory().getDespawnDistance())
			)
		 {
			return false;
		} else if (entityType.canSummon() && canSpawnMobAt(serverLevel, structureFeatureManager, chunkGenerator, mobCategory, spawnerData, mutableBlockPos)) {
			SpawnPlacements.Type type = SpawnPlacements.getPlacementType(entityType);
			if (!isSpawnPositionOk(type, serverLevel, mutableBlockPos, entityType)) {
				return false;
			} else {
				return !SpawnPlacements.checkSpawnRules(entityType, serverLevel, MobSpawnType.NATURAL, mutableBlockPos, serverLevel.random)
					? false
					: serverLevel.noCollision(entityType.getAABB((double)mutableBlockPos.getX() + 0.5, (double)mutableBlockPos.getY(), (double)mutableBlockPos.getZ() + 0.5));
			}
		} else {
			return false;
		}
	}

	@Nullable
	private static Mob getMobForSpawn(ServerLevel serverLevel, EntityType<?> entityType) {
		try {
			Entity entity = entityType.create(serverLevel);
			if (!(entity instanceof Mob)) {
				throw new IllegalStateException("Trying to spawn a non-mob: " + Registry.ENTITY_TYPE.getKey(entityType));
			} else {
				return (Mob)entity;
			}
		} catch (Exception var4) {
			LOGGER.warn("Failed to create mob", (Throwable)var4);
			return null;
		}
	}

	private static boolean isValidPositionForMob(ServerLevel serverLevel, Mob mob, double d) {
		return d > (double)(mob.getType().getCategory().getDespawnDistance() * mob.getType().getCategory().getDespawnDistance()) && mob.removeWhenFarAway(d)
			? false
			: mob.checkSpawnRules(serverLevel, MobSpawnType.NATURAL) && mob.checkSpawnObstruction(serverLevel);
	}

	private static Optional<MobSpawnSettings.SpawnerData> getRandomSpawnMobAt(
		ServerLevel serverLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		MobCategory mobCategory,
		Random random,
		BlockPos blockPos
	) {
		Biome biome = serverLevel.getBiome(blockPos);
		return mobCategory == MobCategory.WATER_AMBIENT && biome.getBiomeCategory() == Biome.BiomeCategory.RIVER && random.nextFloat() < 0.98F
			? Optional.empty()
			: mobsAt(serverLevel, structureFeatureManager, chunkGenerator, mobCategory, blockPos, biome).getRandom(random);
	}

	private static boolean canSpawnMobAt(
		ServerLevel serverLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		MobCategory mobCategory,
		MobSpawnSettings.SpawnerData spawnerData,
		BlockPos blockPos
	) {
		return mobsAt(serverLevel, structureFeatureManager, chunkGenerator, mobCategory, blockPos, null).unwrap().contains(spawnerData);
	}

	private static WeightedRandomList<MobSpawnSettings.SpawnerData> mobsAt(
		ServerLevel serverLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		MobCategory mobCategory,
		BlockPos blockPos,
		@Nullable Biome biome
	) {
		return isInNetherFortressBounds(blockPos, serverLevel, mobCategory, structureFeatureManager)
			? NetherFortressFeature.FORTRESS_ENEMIES
			: chunkGenerator.getMobsAt(biome != null ? biome : serverLevel.getBiome(blockPos), structureFeatureManager, mobCategory, blockPos);
	}

	public static boolean isInNetherFortressBounds(
		BlockPos blockPos, ServerLevel serverLevel, MobCategory mobCategory, StructureFeatureManager structureFeatureManager
	) {
		return mobCategory == MobCategory.MONSTER
			&& serverLevel.getBlockState(blockPos.below()).is(Blocks.NETHER_BRICKS)
			&& structureFeatureManager.getStructureAt(blockPos, StructureFeature.NETHER_BRIDGE).isValid();
	}

	private static BlockPos getRandomPosWithin(Level level, LevelChunk levelChunk) {
		ChunkPos chunkPos = levelChunk.getPos();
		int i = chunkPos.getMinBlockX() + level.random.nextInt(16);
		int j = chunkPos.getMinBlockZ() + level.random.nextInt(16);
		int k = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i, j) + 1;
		int l = Mth.randomBetweenInclusive(level.random, level.getMinBuildHeight(), k);
		return new BlockPos(i, l, j);
	}

	public static boolean isValidEmptySpawnBlock(
		BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState, EntityType<?> entityType
	) {
		if (blockState.isCollisionShapeFullBlock(blockGetter, blockPos)) {
			return false;
		} else if (blockState.isSignalSource()) {
			return false;
		} else if (!fluidState.isEmpty()) {
			return false;
		} else {
			return blockState.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE) ? false : !entityType.isBlockDangerous(blockState);
		}
	}

	public static boolean isSpawnPositionOk(SpawnPlacements.Type type, LevelReader levelReader, BlockPos blockPos, @Nullable EntityType<?> entityType) {
		if (type == SpawnPlacements.Type.NO_RESTRICTIONS) {
			return true;
		} else if (entityType != null && levelReader.getWorldBorder().isWithinBounds(blockPos)) {
			BlockState blockState = levelReader.getBlockState(blockPos);
			FluidState fluidState = levelReader.getFluidState(blockPos);
			BlockPos blockPos2 = blockPos.above();
			BlockPos blockPos3 = blockPos.below();
			switch (type) {
				case IN_WATER:
					return fluidState.is(FluidTags.WATER) && !levelReader.getBlockState(blockPos2).isRedstoneConductor(levelReader, blockPos2);
				case IN_LAVA:
					return fluidState.is(FluidTags.LAVA);
				case ON_GROUND:
				default:
					BlockState blockState2 = levelReader.getBlockState(blockPos3);
					return !blockState2.isValidSpawn(levelReader, blockPos3, entityType)
						? false
						: isValidEmptySpawnBlock(levelReader, blockPos, blockState, fluidState, entityType)
							&& isValidEmptySpawnBlock(levelReader, blockPos2, levelReader.getBlockState(blockPos2), levelReader.getFluidState(blockPos2), entityType);
			}
		} else {
			return false;
		}
	}

	public static void spawnMobsForChunkGeneration(ServerLevelAccessor serverLevelAccessor, Biome biome, ChunkPos chunkPos, Random random) {
		MobSpawnSettings mobSpawnSettings = biome.getMobSettings();
		WeightedRandomList<MobSpawnSettings.SpawnerData> weightedRandomList = mobSpawnSettings.getMobs(MobCategory.CREATURE);
		if (!weightedRandomList.isEmpty()) {
			int i = chunkPos.getMinBlockX();
			int j = chunkPos.getMinBlockZ();

			while (random.nextFloat() < mobSpawnSettings.getCreatureProbability()) {
				Optional<MobSpawnSettings.SpawnerData> optional = weightedRandomList.getRandom(random);
				if (optional.isPresent()) {
					MobSpawnSettings.SpawnerData spawnerData = (MobSpawnSettings.SpawnerData)optional.get();
					int k = spawnerData.minCount + random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
					SpawnGroupData spawnGroupData = null;
					int l = i + random.nextInt(16);
					int m = j + random.nextInt(16);
					int n = l;
					int o = m;

					for (int p = 0; p < k; p++) {
						boolean bl = false;

						for (int q = 0; !bl && q < 4; q++) {
							BlockPos blockPos = getTopNonCollidingPos(serverLevelAccessor, spawnerData.type, l, m);
							if (spawnerData.type.canSummon()
								&& isSpawnPositionOk(SpawnPlacements.getPlacementType(spawnerData.type), serverLevelAccessor, blockPos, spawnerData.type)) {
								float f = spawnerData.type.getWidth();
								double d = Mth.clamp((double)l, (double)i + (double)f, (double)i + 16.0 - (double)f);
								double e = Mth.clamp((double)m, (double)j + (double)f, (double)j + 16.0 - (double)f);
								if (!serverLevelAccessor.noCollision(spawnerData.type.getAABB(d, (double)blockPos.getY(), e))
									|| !SpawnPlacements.checkSpawnRules(
										spawnerData.type, serverLevelAccessor, MobSpawnType.CHUNK_GENERATION, new BlockPos(d, (double)blockPos.getY(), e), serverLevelAccessor.getRandom()
									)) {
									continue;
								}

								Entity entity;
								try {
									entity = spawnerData.type.create(serverLevelAccessor.getLevel());
								} catch (Exception var27) {
									LOGGER.warn("Failed to create mob", (Throwable)var27);
									continue;
								}

								entity.moveTo(d, (double)blockPos.getY(), e, random.nextFloat() * 360.0F, 0.0F);
								if (entity instanceof Mob mob
									&& mob.checkSpawnRules(serverLevelAccessor, MobSpawnType.CHUNK_GENERATION)
									&& mob.checkSpawnObstruction(serverLevelAccessor)) {
									spawnGroupData = mob.finalizeSpawn(
										serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.CHUNK_GENERATION, spawnGroupData, null
									);
									serverLevelAccessor.addFreshEntityWithPassengers(mob);
									bl = true;
								}
							}

							l += random.nextInt(5) - random.nextInt(5);

							for (m += random.nextInt(5) - random.nextInt(5); l < i || l >= i + 16 || m < j || m >= j + 16; m = o + random.nextInt(5) - random.nextInt(5)) {
								l = n + random.nextInt(5) - random.nextInt(5);
							}
						}
					}
				}
			}
		}
	}

	private static BlockPos getTopNonCollidingPos(LevelReader levelReader, EntityType<?> entityType, int i, int j) {
		int k = levelReader.getHeight(SpawnPlacements.getHeightmapType(entityType), i, j);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, k, j);
		if (levelReader.dimensionType().hasCeiling()) {
			do {
				mutableBlockPos.move(Direction.DOWN);
			} while (!levelReader.getBlockState(mutableBlockPos).isAir());

			do {
				mutableBlockPos.move(Direction.DOWN);
			} while (levelReader.getBlockState(mutableBlockPos).isAir() && mutableBlockPos.getY() > levelReader.getMinBuildHeight());
		}

		if (SpawnPlacements.getPlacementType(entityType) == SpawnPlacements.Type.ON_GROUND) {
			BlockPos blockPos = mutableBlockPos.below();
			if (levelReader.getBlockState(blockPos).isPathfindable(levelReader, blockPos, PathComputationType.LAND)) {
				return blockPos;
			}
		}

		return mutableBlockPos.immutable();
	}

	@FunctionalInterface
	public interface AfterSpawnCallback {
		void run(Mob mob, ChunkAccess chunkAccess);
	}

	@FunctionalInterface
	public interface ChunkGetter {
		void query(long l, Consumer<LevelChunk> consumer);
	}

	@FunctionalInterface
	public interface SpawnPredicate {
		boolean test(EntityType<?> entityType, BlockPos blockPos, ChunkAccess chunkAccess);
	}

	public static class SpawnState {
		private final int spawnableChunkCount;
		private final Object2IntOpenHashMap<MobCategory> mobCategoryCounts;
		private final PotentialCalculator spawnPotential;
		private final Object2IntMap<MobCategory> unmodifiableMobCategoryCounts;
		private final LocalMobCapCalculator localMobCapCalculator;
		@Nullable
		private BlockPos lastCheckedPos;
		@Nullable
		private EntityType<?> lastCheckedType;
		private double lastCharge;

		SpawnState(
			int i, Object2IntOpenHashMap<MobCategory> object2IntOpenHashMap, PotentialCalculator potentialCalculator, LocalMobCapCalculator localMobCapCalculator
		) {
			this.spawnableChunkCount = i;
			this.mobCategoryCounts = object2IntOpenHashMap;
			this.spawnPotential = potentialCalculator;
			this.localMobCapCalculator = localMobCapCalculator;
			this.unmodifiableMobCategoryCounts = Object2IntMaps.unmodifiable(object2IntOpenHashMap);
		}

		private boolean canSpawn(EntityType<?> entityType, BlockPos blockPos, ChunkAccess chunkAccess) {
			this.lastCheckedPos = blockPos;
			this.lastCheckedType = entityType;
			MobSpawnSettings.MobSpawnCost mobSpawnCost = NaturalSpawner.getRoughBiome(blockPos, chunkAccess).getMobSettings().getMobSpawnCost(entityType);
			if (mobSpawnCost == null) {
				this.lastCharge = 0.0;
				return true;
			} else {
				double d = mobSpawnCost.getCharge();
				this.lastCharge = d;
				double e = this.spawnPotential.getPotentialEnergyChange(blockPos, d);
				return e <= mobSpawnCost.getEnergyBudget();
			}
		}

		private void afterSpawn(Mob mob, ChunkAccess chunkAccess) {
			EntityType<?> entityType = mob.getType();
			BlockPos blockPos = mob.blockPosition();
			double d;
			if (blockPos.equals(this.lastCheckedPos) && entityType == this.lastCheckedType) {
				d = this.lastCharge;
			} else {
				MobSpawnSettings.MobSpawnCost mobSpawnCost = NaturalSpawner.getRoughBiome(blockPos, chunkAccess).getMobSettings().getMobSpawnCost(entityType);
				if (mobSpawnCost != null) {
					d = mobSpawnCost.getCharge();
				} else {
					d = 0.0;
				}
			}

			this.spawnPotential.addCharge(blockPos, d);
			MobCategory mobCategory = entityType.getCategory();
			this.mobCategoryCounts.addTo(mobCategory, 1);
			this.localMobCapCalculator.addMob(new ChunkPos(blockPos), mobCategory);
		}

		public int getSpawnableChunkCount() {
			return this.spawnableChunkCount;
		}

		public Object2IntMap<MobCategory> getMobCategoryCounts() {
			return this.unmodifiableMobCategoryCounts;
		}

		boolean canSpawnForCategory(MobCategory mobCategory, ChunkPos chunkPos) {
			int i = mobCategory.getMaxInstancesPerChunk() * this.spawnableChunkCount / NaturalSpawner.MAGIC_NUMBER;
			return this.mobCategoryCounts.getInt(mobCategory) >= i ? false : this.localMobCapCalculator.canSpawn(mobCategory, chunkPos);
		}
	}
}
