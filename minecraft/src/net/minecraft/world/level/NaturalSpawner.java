package net.minecraft.world.level;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.NearestNeighborBiomeZoomer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NaturalSpawner {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int MAGIC_NUMBER = (int)Math.pow(17.0, 2.0);
	private static final MobCategory[] SPAWNING_CATEGORIES = (MobCategory[])Stream.of(MobCategory.values())
		.filter(mobCategory -> mobCategory != MobCategory.MISC)
		.toArray(MobCategory[]::new);

	public static NaturalSpawner.SpawnState createState(int i, Iterable<Entity> iterable, NaturalSpawner.ChunkGetter chunkGetter) {
		PotentialCalculator potentialCalculator = new PotentialCalculator();
		Object2IntOpenHashMap<MobCategory> object2IntOpenHashMap = new Object2IntOpenHashMap<>();

		for (Entity entity : iterable) {
			if (entity instanceof Mob) {
				Mob mob = (Mob)entity;
				if (mob.isPersistenceRequired() || mob.requiresCustomPersistence()) {
					continue;
				}
			}

			MobCategory mobCategory = entity.getType().getCategory();
			if (mobCategory != MobCategory.MISC) {
				BlockPos blockPos = entity.blockPosition();
				long l = ChunkPos.asLong(blockPos.getX() >> 4, blockPos.getZ() >> 4);
				chunkGetter.query(l, levelChunk -> {
					Biome biome = getRoughBiome(blockPos, levelChunk);
					Biome.MobSpawnCost mobSpawnCost = biome.getMobSpawnCost(entity.getType());
					if (mobSpawnCost != null) {
						potentialCalculator.addCharge(entity.blockPosition(), mobSpawnCost.getCharge());
					}

					object2IntOpenHashMap.addTo(mobCategory, 1);
				});
			}
		}

		return new NaturalSpawner.SpawnState(i, object2IntOpenHashMap, potentialCalculator);
	}

	private static Biome getRoughBiome(BlockPos blockPos, ChunkAccess chunkAccess) {
		return NearestNeighborBiomeZoomer.INSTANCE.getBiome(0L, blockPos.getX(), blockPos.getY(), blockPos.getZ(), chunkAccess.getBiomes());
	}

	public static void spawnForChunk(ServerLevel serverLevel, LevelChunk levelChunk, NaturalSpawner.SpawnState spawnState, boolean bl, boolean bl2, boolean bl3) {
		serverLevel.getProfiler().push("spawner");

		for (MobCategory mobCategory : SPAWNING_CATEGORIES) {
			if ((bl || !mobCategory.isFriendly())
				&& (bl2 || mobCategory.isFriendly())
				&& (bl3 || !mobCategory.isPersistent())
				&& spawnState.canSpawnForCategory(mobCategory)) {
				spawnCategoryForChunk(
					mobCategory,
					serverLevel,
					levelChunk,
					(entityType, blockPos, chunkAccess) -> spawnState.canSpawn(entityType, blockPos, chunkAccess),
					(mob, chunkAccess) -> spawnState.afterSpawn(mob, chunkAccess)
				);
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
		if (blockPos.getY() >= 1) {
			spawnCategoryForPosition(mobCategory, serverLevel, levelChunk, blockPos, spawnPredicate, afterSpawnCallback);
		}
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
		ChunkGenerator<?> chunkGenerator = serverLevel.getChunkSource().getGenerator();
		int i = blockPos.getY();
		BlockState blockState = chunkAccess.getBlockState(blockPos);
		if (!blockState.isRedstoneConductor(chunkAccess, blockPos)) {
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			int j = 0;

			for (int k = 0; k < 3; k++) {
				int l = blockPos.getX();
				int m = blockPos.getZ();
				int n = 6;
				Biome.SpawnerData spawnerData = null;
				SpawnGroupData spawnGroupData = null;
				int o = Mth.ceil(serverLevel.random.nextFloat() * 4.0F);
				int p = 0;

				for (int q = 0; q < o; q++) {
					l += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6);
					m += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6);
					mutableBlockPos.set(l, i, m);
					float f = (float)l + 0.5F;
					float g = (float)m + 0.5F;
					Player player = serverLevel.getNearestPlayer((double)f, (double)i, (double)g, -1.0, false);
					if (player != null) {
						double d = player.distanceToSqr((double)f, (double)i, (double)g);
						if (isRightDistanceToPlayerAndSpawnPoint(serverLevel, chunkAccess, mutableBlockPos, d)) {
							if (spawnerData == null) {
								spawnerData = getRandomSpawnMobAt(structureFeatureManager, chunkGenerator, mobCategory, serverLevel.random, mutableBlockPos);
								if (spawnerData == null) {
									break;
								}

								o = spawnerData.minCount + serverLevel.random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
							}

							if (isValidSpawnPostitionForType(serverLevel, mobCategory, structureFeatureManager, chunkGenerator, spawnerData, mutableBlockPos, d)
								&& spawnPredicate.test(spawnerData.type, mutableBlockPos, chunkAccess)) {
								Mob mob = getMobForSpawn(serverLevel, spawnerData.type);
								if (mob == null) {
									return;
								}

								mob.moveTo((double)f, (double)i, (double)g, serverLevel.random.nextFloat() * 360.0F, 0.0F);
								if (isValidPositionForMob(serverLevel, mob, d)) {
									spawnGroupData = mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.NATURAL, spawnGroupData, null);
									j++;
									p++;
									serverLevel.addFreshEntity(mob);
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
		} else if (serverLevel.getSharedSpawnPos()
			.closerThan(new Vec3((double)((float)mutableBlockPos.getX() + 0.5F), (double)mutableBlockPos.getY(), (double)((float)mutableBlockPos.getZ() + 0.5F)), 24.0)) {
			return false;
		} else {
			ChunkPos chunkPos = new ChunkPos(mutableBlockPos);
			return Objects.equals(chunkPos, chunkAccess.getPos()) || serverLevel.getChunkSource().isEntityTickingChunk(chunkPos);
		}
	}

	private static boolean isValidSpawnPostitionForType(
		ServerLevel serverLevel,
		MobCategory mobCategory,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<?> chunkGenerator,
		Biome.SpawnerData spawnerData,
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
		} else if (entityType.canSummon() && canSpawnMobAt(structureFeatureManager, chunkGenerator, mobCategory, spawnerData, mutableBlockPos)) {
			SpawnPlacements.Type type = SpawnPlacements.getPlacementType(entityType);
			if (!isSpawnPositionOk(type, serverLevel, mutableBlockPos, entityType)) {
				return false;
			} else {
				return !SpawnPlacements.checkSpawnRules(entityType, serverLevel, MobSpawnType.NATURAL, mutableBlockPos, serverLevel.random)
					? false
					: serverLevel.noCollision(
						entityType.getAABB((double)((float)mutableBlockPos.getX() + 0.5F), (double)mutableBlockPos.getY(), (double)((float)mutableBlockPos.getZ() + 0.5F))
					);
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

	@Nullable
	private static Biome.SpawnerData getRandomSpawnMobAt(
		StructureFeatureManager structureFeatureManager, ChunkGenerator<?> chunkGenerator, MobCategory mobCategory, Random random, BlockPos blockPos
	) {
		List<Biome.SpawnerData> list = chunkGenerator.getMobsAt(structureFeatureManager, mobCategory, blockPos);
		return list.isEmpty() ? null : WeighedRandom.getRandomItem(random, list);
	}

	private static boolean canSpawnMobAt(
		StructureFeatureManager structureFeatureManager, ChunkGenerator<?> chunkGenerator, MobCategory mobCategory, Biome.SpawnerData spawnerData, BlockPos blockPos
	) {
		return chunkGenerator.getMobsAt(structureFeatureManager, mobCategory, blockPos).contains(spawnerData);
	}

	private static BlockPos getRandomPosWithin(Level level, LevelChunk levelChunk) {
		ChunkPos chunkPos = levelChunk.getPos();
		int i = chunkPos.getMinBlockX() + level.random.nextInt(16);
		int j = chunkPos.getMinBlockZ() + level.random.nextInt(16);
		int k = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i, j) + 1;
		int l = level.random.nextInt(k + 1);
		return new BlockPos(i, l, j);
	}

	public static boolean isValidEmptySpawnBlock(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
		if (blockState.isCollisionShapeFullBlock(blockGetter, blockPos)) {
			return false;
		} else if (blockState.isSignalSource()) {
			return false;
		} else {
			return !fluidState.isEmpty() ? false : !blockState.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE);
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
					return fluidState.is(FluidTags.WATER)
						&& levelReader.getFluidState(blockPos3).is(FluidTags.WATER)
						&& !levelReader.getBlockState(blockPos2).isRedstoneConductor(levelReader, blockPos2);
				case IN_LAVA:
					return fluidState.is(FluidTags.LAVA)
						&& levelReader.getFluidState(blockPos3).is(FluidTags.LAVA)
						&& !levelReader.getBlockState(blockPos2).isRedstoneConductor(levelReader, blockPos2);
				case ON_GROUND:
				default:
					BlockState blockState2 = levelReader.getBlockState(blockPos3);
					return !blockState2.isValidSpawn(levelReader, blockPos3, entityType)
						? false
						: isValidEmptySpawnBlock(levelReader, blockPos, blockState, fluidState)
							&& isValidEmptySpawnBlock(levelReader, blockPos2, levelReader.getBlockState(blockPos2), levelReader.getFluidState(blockPos2));
			}
		} else {
			return false;
		}
	}

	public static void spawnMobsForChunkGeneration(LevelAccessor levelAccessor, Biome biome, int i, int j, Random random) {
		List<Biome.SpawnerData> list = biome.getMobs(MobCategory.CREATURE);
		if (!list.isEmpty()) {
			int k = i << 4;
			int l = j << 4;

			while (random.nextFloat() < biome.getCreatureProbability()) {
				Biome.SpawnerData spawnerData = WeighedRandom.getRandomItem(random, list);
				int m = spawnerData.minCount + random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
				SpawnGroupData spawnGroupData = null;
				int n = k + random.nextInt(16);
				int o = l + random.nextInt(16);
				int p = n;
				int q = o;

				for (int r = 0; r < m; r++) {
					boolean bl = false;

					for (int s = 0; !bl && s < 4; s++) {
						BlockPos blockPos = getTopNonCollidingPos(levelAccessor, spawnerData.type, n, o);
						if (spawnerData.type.canSummon() && isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, levelAccessor, blockPos, spawnerData.type)) {
							float f = spawnerData.type.getWidth();
							double d = Mth.clamp((double)n, (double)k + (double)f, (double)k + 16.0 - (double)f);
							double e = Mth.clamp((double)o, (double)l + (double)f, (double)l + 16.0 - (double)f);
							if (!levelAccessor.noCollision(spawnerData.type.getAABB(d, (double)blockPos.getY(), e))
								|| !SpawnPlacements.checkSpawnRules(
									spawnerData.type, levelAccessor, MobSpawnType.CHUNK_GENERATION, new BlockPos(d, (double)blockPos.getY(), e), levelAccessor.getRandom()
								)) {
								continue;
							}

							Entity entity;
							try {
								entity = spawnerData.type.create(levelAccessor.getLevel());
							} catch (Exception var26) {
								LOGGER.warn("Failed to create mob", (Throwable)var26);
								continue;
							}

							entity.moveTo(d, (double)blockPos.getY(), e, random.nextFloat() * 360.0F, 0.0F);
							if (entity instanceof Mob) {
								Mob mob = (Mob)entity;
								if (mob.checkSpawnRules(levelAccessor, MobSpawnType.CHUNK_GENERATION) && mob.checkSpawnObstruction(levelAccessor)) {
									spawnGroupData = mob.finalizeSpawn(
										levelAccessor, levelAccessor.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.CHUNK_GENERATION, spawnGroupData, null
									);
									levelAccessor.addFreshEntity(mob);
									bl = true;
								}
							}
						}

						n += random.nextInt(5) - random.nextInt(5);

						for (o += random.nextInt(5) - random.nextInt(5); n < k || n >= k + 16 || o < l || o >= l + 16; o = q + random.nextInt(5) - random.nextInt(5)) {
							n = p + random.nextInt(5) - random.nextInt(5);
						}
					}
				}
			}
		}
	}

	private static BlockPos getTopNonCollidingPos(LevelReader levelReader, @Nullable EntityType<?> entityType, int i, int j) {
		BlockPos blockPos = new BlockPos(i, levelReader.getHeight(SpawnPlacements.getHeightmapType(entityType), i, j), j);
		BlockPos blockPos2 = blockPos.below();
		return levelReader.getBlockState(blockPos2).isPathfindable(levelReader, blockPos2, PathComputationType.LAND) ? blockPos2 : blockPos;
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
		@Nullable
		private BlockPos lastCheckedPos;
		@Nullable
		private EntityType<?> lastCheckedType;
		private double lastCharge;

		private SpawnState(int i, Object2IntOpenHashMap<MobCategory> object2IntOpenHashMap, PotentialCalculator potentialCalculator) {
			this.spawnableChunkCount = i;
			this.mobCategoryCounts = object2IntOpenHashMap;
			this.spawnPotential = potentialCalculator;
			this.unmodifiableMobCategoryCounts = Object2IntMaps.unmodifiable(object2IntOpenHashMap);
		}

		private boolean canSpawn(EntityType<?> entityType, BlockPos blockPos, ChunkAccess chunkAccess) {
			this.lastCheckedPos = blockPos;
			this.lastCheckedType = entityType;
			Biome biome = NaturalSpawner.getRoughBiome(blockPos, chunkAccess);
			Biome.MobSpawnCost mobSpawnCost = biome.getMobSpawnCost(entityType);
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
				Biome biome = NaturalSpawner.getRoughBiome(blockPos, chunkAccess);
				Biome.MobSpawnCost mobSpawnCost = biome.getMobSpawnCost(entityType);
				if (mobSpawnCost != null) {
					d = mobSpawnCost.getCharge();
				} else {
					d = 0.0;
				}
			}

			this.spawnPotential.addCharge(blockPos, d);
			this.mobCategoryCounts.addTo(entityType.getCategory(), 1);
		}

		@Environment(EnvType.CLIENT)
		public int getSpawnableChunkCount() {
			return this.spawnableChunkCount;
		}

		public Object2IntMap<MobCategory> getMobCategoryCounts() {
			return this.unmodifiableMobCategoryCounts;
		}

		private boolean canSpawnForCategory(MobCategory mobCategory) {
			int i = mobCategory.getMaxInstancesPerChunk() * this.spawnableChunkCount / NaturalSpawner.MAGIC_NUMBER;
			return this.mobCategoryCounts.getInt(mobCategory) < i;
		}
	}
}
