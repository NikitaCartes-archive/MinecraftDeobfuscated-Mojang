package net.minecraft.world.level;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nullable;
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
import net.minecraft.world.level.block.state.BlockState;
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

	public static void spawnCategoryForChunk(MobCategory mobCategory, ServerLevel serverLevel, LevelChunk levelChunk, BlockPos blockPos) {
		ChunkGenerator<?> chunkGenerator = serverLevel.getChunkSource().getGenerator();
		int i = 0;
		BlockPos blockPos2 = getRandomPosWithin(serverLevel, levelChunk);
		int j = blockPos2.getX();
		int k = blockPos2.getY();
		int l = blockPos2.getZ();
		if (k >= 1) {
			BlockState blockState = levelChunk.getBlockState(blockPos2);
			if (!blockState.isRedstoneConductor(levelChunk, blockPos2)) {
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
				int m = 0;

				while (m < 3) {
					int n = j;
					int o = l;
					int p = 6;
					Biome.SpawnerData spawnerData = null;
					SpawnGroupData spawnGroupData = null;
					int q = Mth.ceil(Math.random() * 4.0);
					int r = 0;
					int s = 0;

					while (true) {
						label115: {
							label114:
							if (s < q) {
								n += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6);
								o += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6);
								mutableBlockPos.set(n, k, o);
								float f = (float)n + 0.5F;
								float g = (float)o + 0.5F;
								Player player = serverLevel.getNearestPlayerIgnoreY((double)f, (double)g, -1.0);
								if (player == null) {
									break label115;
								}

								double d = player.distanceToSqr((double)f, (double)k, (double)g);
								if (d <= 576.0 || blockPos.closerThan(new Vec3((double)f, (double)k, (double)g), 24.0)) {
									break label115;
								}

								ChunkPos chunkPos = new ChunkPos(mutableBlockPos);
								if (!Objects.equals(chunkPos, levelChunk.getPos()) && !serverLevel.getChunkSource().isEntityTickingChunk(chunkPos)) {
									break label115;
								}

								if (spawnerData == null) {
									spawnerData = getRandomSpawnMobAt(chunkGenerator, mobCategory, serverLevel.random, mutableBlockPos);
									if (spawnerData == null) {
										break label114;
									}

									q = spawnerData.minCount + serverLevel.random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
								}

								if (spawnerData.type.getCategory() == MobCategory.MISC || !spawnerData.type.canSpawnFarFromPlayer() && d > 16384.0) {
									break label115;
								}

								EntityType<?> entityType = spawnerData.type;
								if (!entityType.canSummon() || !canSpawnMobAt(chunkGenerator, mobCategory, spawnerData, mutableBlockPos)) {
									break label115;
								}

								SpawnPlacements.Type type = SpawnPlacements.getPlacementType(entityType);
								if (!isSpawnPositionOk(type, serverLevel, mutableBlockPos, entityType)
									|| !SpawnPlacements.checkSpawnRules(entityType, serverLevel, MobSpawnType.NATURAL, mutableBlockPos, serverLevel.random)
									|| !serverLevel.noCollision(entityType.getAABB((double)f, (double)k, (double)g))) {
									break label115;
								}

								Mob mob;
								try {
									Entity entity = entityType.create(serverLevel);
									if (!(entity instanceof Mob)) {
										throw new IllegalStateException("Trying to spawn a non-mob: " + Registry.ENTITY_TYPE.getKey(entityType));
									}

									mob = (Mob)entity;
								} catch (Exception var31) {
									LOGGER.warn("Failed to create mob", (Throwable)var31);
									return;
								}

								mob.moveTo((double)f, (double)k, (double)g, serverLevel.random.nextFloat() * 360.0F, 0.0F);
								if (d > 16384.0 && mob.removeWhenFarAway(d) || !mob.checkSpawnRules(serverLevel, MobSpawnType.NATURAL) || !mob.checkSpawnObstruction(serverLevel)) {
									break label115;
								}

								spawnGroupData = mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(new BlockPos(mob)), MobSpawnType.NATURAL, spawnGroupData, null);
								i++;
								r++;
								serverLevel.addFreshEntity(mob);
								if (i >= mob.getMaxSpawnClusterSize()) {
									return;
								}

								if (!mob.isMaxGroupSizeReached(r)) {
									break label115;
								}
							}

							m++;
							break;
						}

						s++;
					}
				}
			}
		}
	}

	@Nullable
	private static Biome.SpawnerData getRandomSpawnMobAt(ChunkGenerator<?> chunkGenerator, MobCategory mobCategory, Random random, BlockPos blockPos) {
		List<Biome.SpawnerData> list = chunkGenerator.getMobsAt(mobCategory, blockPos);
		return list.isEmpty() ? null : WeighedRandom.getRandomItem(random, list);
	}

	private static boolean canSpawnMobAt(ChunkGenerator<?> chunkGenerator, MobCategory mobCategory, Biome.SpawnerData spawnerData, BlockPos blockPos) {
		List<Biome.SpawnerData> list = chunkGenerator.getMobsAt(mobCategory, blockPos);
		return list.isEmpty() ? false : list.contains(spawnerData);
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
			return !fluidState.isEmpty() ? false : !blockState.is(BlockTags.RAILS);
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
										levelAccessor, levelAccessor.getCurrentDifficultyAt(new BlockPos(mob)), MobSpawnType.CHUNK_GENERATION, spawnGroupData, null
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
}
