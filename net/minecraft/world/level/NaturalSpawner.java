/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
import org.jetbrains.annotations.Nullable;

public final class NaturalSpawner {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void spawnCategoryForChunk(MobCategory mobCategory, Level level, LevelChunk levelChunk, BlockPos blockPos) {
        ChunkGenerator<?> chunkGenerator = level.getChunkSource().getGenerator();
        int i = 0;
        BlockPos blockPos2 = NaturalSpawner.getRandomPosWithin(level, levelChunk);
        int j = blockPos2.getX();
        int k = blockPos2.getY();
        int l = blockPos2.getZ();
        if (k < 1) {
            return;
        }
        BlockState blockState = levelChunk.getBlockState(blockPos2);
        if (blockState.isRedstoneConductor(levelChunk, blockPos2)) {
            return;
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        block2: for (int m = 0; m < 3; ++m) {
            int n = j;
            int o = l;
            int p = 6;
            Biome.SpawnerData spawnerData = null;
            SpawnGroupData spawnGroupData = null;
            int q = Mth.ceil(Math.random() * 4.0);
            int r = 0;
            for (int s = 0; s < q; ++s) {
                Mob mob;
                SpawnPlacements.Type type;
                EntityType<?> entityType;
                ChunkPos chunkPos;
                double d;
                mutableBlockPos.set(n += level.random.nextInt(6) - level.random.nextInt(6), k, o += level.random.nextInt(6) - level.random.nextInt(6));
                float f = (float)n + 0.5f;
                float g = (float)o + 0.5f;
                Player player = level.getNearestPlayerIgnoreY(f, g, -1.0);
                if (player == null || (d = player.distanceToSqr(f, k, g)) <= 576.0 || blockPos.closerThan(new Vec3(f, k, g), 24.0) || !Objects.equals(chunkPos = new ChunkPos(mutableBlockPos), levelChunk.getPos()) && !level.getChunkSource().isEntityTickingChunk(chunkPos)) continue;
                if (spawnerData == null) {
                    spawnerData = NaturalSpawner.getRandomSpawnMobAt(chunkGenerator, mobCategory, level.random, mutableBlockPos);
                    if (spawnerData == null) continue block2;
                    q = spawnerData.minCount + level.random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
                }
                if (spawnerData.type.getCategory() == MobCategory.MISC || !spawnerData.type.canSpawnFarFromPlayer() && d > 16384.0 || !(entityType = spawnerData.type).canSummon() || !NaturalSpawner.canSpawnMobAt(chunkGenerator, mobCategory, spawnerData, mutableBlockPos) || !NaturalSpawner.isSpawnPositionOk(type = SpawnPlacements.getPlacementType(entityType), level, mutableBlockPos, entityType) || !SpawnPlacements.checkSpawnRules(entityType, level, MobSpawnType.NATURAL, mutableBlockPos, level.random) || !level.noCollision(entityType.getAABB(f, k, g))) continue;
                try {
                    Object entity = entityType.create(level);
                    if (!(entity instanceof Mob)) {
                        throw new IllegalStateException("Trying to spawn a non-mob: " + Registry.ENTITY_TYPE.getKey(entityType));
                    }
                    mob = (Mob)entity;
                } catch (Exception exception) {
                    LOGGER.warn("Failed to create mob", (Throwable)exception);
                    return;
                }
                mob.moveTo(f, k, g, level.random.nextFloat() * 360.0f, 0.0f);
                if (d > 16384.0 && mob.removeWhenFarAway(d) || !mob.checkSpawnRules(level, MobSpawnType.NATURAL) || !mob.checkSpawnObstruction(level)) continue;
                spawnGroupData = mob.finalizeSpawn(level, level.getCurrentDifficultyAt(new BlockPos(mob)), MobSpawnType.NATURAL, spawnGroupData, null);
                ++r;
                level.addFreshEntity(mob);
                if (++i >= mob.getMaxSpawnClusterSize()) {
                    return;
                }
                if (mob.isMaxGroupSizeReached(r)) continue block2;
            }
        }
    }

    @Nullable
    private static Biome.SpawnerData getRandomSpawnMobAt(ChunkGenerator<?> chunkGenerator, MobCategory mobCategory, Random random, BlockPos blockPos) {
        List<Biome.SpawnerData> list = chunkGenerator.getMobsAt(mobCategory, blockPos);
        if (list.isEmpty()) {
            return null;
        }
        return WeighedRandom.getRandomItem(random, list);
    }

    private static boolean canSpawnMobAt(ChunkGenerator<?> chunkGenerator, MobCategory mobCategory, Biome.SpawnerData spawnerData, BlockPos blockPos) {
        List<Biome.SpawnerData> list = chunkGenerator.getMobsAt(mobCategory, blockPos);
        if (list.isEmpty()) {
            return false;
        }
        return list.contains(spawnerData);
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
        }
        if (blockState.isSignalSource()) {
            return false;
        }
        if (!fluidState.isEmpty()) {
            return false;
        }
        return !blockState.is(BlockTags.RAILS);
    }

    public static boolean isSpawnPositionOk(SpawnPlacements.Type type, LevelReader levelReader, BlockPos blockPos, @Nullable EntityType<?> entityType) {
        if (type == SpawnPlacements.Type.NO_RESTRICTIONS) {
            return true;
        }
        if (entityType == null || !levelReader.getWorldBorder().isWithinBounds(blockPos)) {
            return false;
        }
        BlockState blockState = levelReader.getBlockState(blockPos);
        FluidState fluidState = levelReader.getFluidState(blockPos);
        BlockPos blockPos2 = blockPos.above();
        BlockPos blockPos3 = blockPos.below();
        switch (type) {
            case IN_WATER: {
                return fluidState.is(FluidTags.WATER) && levelReader.getFluidState(blockPos3).is(FluidTags.WATER) && !levelReader.getBlockState(blockPos2).isRedstoneConductor(levelReader, blockPos2);
            }
        }
        BlockState blockState2 = levelReader.getBlockState(blockPos3);
        if (!blockState2.isValidSpawn(levelReader, blockPos3, entityType)) {
            return false;
        }
        return NaturalSpawner.isValidEmptySpawnBlock(levelReader, blockPos, blockState, fluidState) && NaturalSpawner.isValidEmptySpawnBlock(levelReader, blockPos2, levelReader.getBlockState(blockPos2), levelReader.getFluidState(blockPos2));
    }

    public static void spawnMobsForChunkGeneration(LevelAccessor levelAccessor, Biome biome, int i, int j, Random random) {
        List<Biome.SpawnerData> list = biome.getMobs(MobCategory.CREATURE);
        if (list.isEmpty()) {
            return;
        }
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
            for (int r = 0; r < m; ++r) {
                boolean bl = false;
                for (int s = 0; !bl && s < 4; ++s) {
                    BlockPos blockPos = NaturalSpawner.getTopNonCollidingPos(levelAccessor, spawnerData.type, n, o);
                    if (spawnerData.type.canSummon() && NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, levelAccessor, blockPos, spawnerData.type)) {
                        Mob mob;
                        Object entity;
                        float f = spawnerData.type.getWidth();
                        double d = Mth.clamp((double)n, (double)k + (double)f, (double)k + 16.0 - (double)f);
                        double e = Mth.clamp((double)o, (double)l + (double)f, (double)l + 16.0 - (double)f);
                        if (!levelAccessor.noCollision(spawnerData.type.getAABB(d, blockPos.getY(), e)) || !SpawnPlacements.checkSpawnRules(spawnerData.type, levelAccessor, MobSpawnType.CHUNK_GENERATION, new BlockPos(d, (double)blockPos.getY(), e), levelAccessor.getRandom())) continue;
                        try {
                            entity = spawnerData.type.create(levelAccessor.getLevel());
                        } catch (Exception exception) {
                            LOGGER.warn("Failed to create mob", (Throwable)exception);
                            continue;
                        }
                        ((Entity)entity).moveTo(d, blockPos.getY(), e, random.nextFloat() * 360.0f, 0.0f);
                        if (entity instanceof Mob && (mob = (Mob)entity).checkSpawnRules(levelAccessor, MobSpawnType.CHUNK_GENERATION) && mob.checkSpawnObstruction(levelAccessor)) {
                            spawnGroupData = mob.finalizeSpawn(levelAccessor, levelAccessor.getCurrentDifficultyAt(new BlockPos(mob)), MobSpawnType.CHUNK_GENERATION, spawnGroupData, null);
                            levelAccessor.addFreshEntity(mob);
                            bl = true;
                        }
                    }
                    n += random.nextInt(5) - random.nextInt(5);
                    o += random.nextInt(5) - random.nextInt(5);
                    while (n < k || n >= k + 16 || o < l || o >= l + 16) {
                        n = p + random.nextInt(5) - random.nextInt(5);
                        o = q + random.nextInt(5) - random.nextInt(5);
                    }
                }
            }
        }
    }

    private static BlockPos getTopNonCollidingPos(LevelReader levelReader, @Nullable EntityType<?> entityType, int i, int j) {
        BlockPos blockPos = new BlockPos(i, levelReader.getHeight(SpawnPlacements.getHeightmapType(entityType), i, j), j);
        BlockPos blockPos2 = blockPos.below();
        if (levelReader.getBlockState(blockPos2).isPathfindable(levelReader, blockPos2, PathComputationType.LAND)) {
            return blockPos2;
        }
        return blockPos;
    }
}

