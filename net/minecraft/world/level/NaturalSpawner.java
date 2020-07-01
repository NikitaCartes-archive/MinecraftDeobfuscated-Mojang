/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.PotentialCalculator;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.NearestNeighborBiomeZoomer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class NaturalSpawner {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MAGIC_NUMBER = (int)Math.pow(17.0, 2.0);
    private static final MobCategory[] SPAWNING_CATEGORIES = (MobCategory[])Stream.of(MobCategory.values()).filter(mobCategory -> mobCategory != MobCategory.MISC).toArray(MobCategory[]::new);

    public static SpawnState createState(int i, Iterable<Entity> iterable, ChunkGetter chunkGetter) {
        PotentialCalculator potentialCalculator = new PotentialCalculator();
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        for (Entity entity : iterable) {
            MobCategory mobCategory;
            Mob mob;
            if (entity instanceof Mob && ((mob = (Mob)entity).isPersistenceRequired() || mob.requiresCustomPersistence()) || (mobCategory = entity.getType().getCategory()) == MobCategory.MISC) continue;
            BlockPos blockPos = entity.blockPosition();
            long l = ChunkPos.asLong(blockPos.getX() >> 4, blockPos.getZ() >> 4);
            chunkGetter.query(l, levelChunk -> {
                Biome biome = NaturalSpawner.getRoughBiome(blockPos, levelChunk);
                Biome.MobSpawnCost mobSpawnCost = biome.getMobSpawnCost(entity.getType());
                if (mobSpawnCost != null) {
                    potentialCalculator.addCharge(entity.blockPosition(), mobSpawnCost.getCharge());
                }
                object2IntOpenHashMap.addTo(mobCategory, 1);
            });
        }
        return new SpawnState(i, object2IntOpenHashMap, potentialCalculator);
    }

    private static Biome getRoughBiome(BlockPos blockPos, ChunkAccess chunkAccess) {
        return NearestNeighborBiomeZoomer.INSTANCE.getBiome(0L, blockPos.getX(), blockPos.getY(), blockPos.getZ(), chunkAccess.getBiomes());
    }

    public static void spawnForChunk(ServerLevel serverLevel, LevelChunk levelChunk, SpawnState spawnState, boolean bl, boolean bl2, boolean bl3) {
        serverLevel.getProfiler().push("spawner");
        for (MobCategory mobCategory : SPAWNING_CATEGORIES) {
            if (!bl && mobCategory.isFriendly() || !bl2 && !mobCategory.isFriendly() || !bl3 && mobCategory.isPersistent() || !spawnState.canSpawnForCategory(mobCategory)) continue;
            NaturalSpawner.spawnCategoryForChunk(mobCategory, serverLevel, levelChunk, (entityType, blockPos, chunkAccess) -> spawnState.canSpawn(entityType, blockPos, chunkAccess), (mob, chunkAccess) -> spawnState.afterSpawn(mob, chunkAccess));
        }
        serverLevel.getProfiler().pop();
    }

    public static void spawnCategoryForChunk(MobCategory mobCategory, ServerLevel serverLevel, LevelChunk levelChunk, SpawnPredicate spawnPredicate, AfterSpawnCallback afterSpawnCallback) {
        BlockPos blockPos = NaturalSpawner.getRandomPosWithin(serverLevel, levelChunk);
        if (blockPos.getY() < 1) {
            return;
        }
        NaturalSpawner.spawnCategoryForPosition(mobCategory, serverLevel, levelChunk, blockPos, spawnPredicate, afterSpawnCallback);
    }

    public static void spawnCategoryForPosition(MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, SpawnPredicate spawnPredicate, AfterSpawnCallback afterSpawnCallback) {
        StructureFeatureManager structureFeatureManager = serverLevel.structureFeatureManager();
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        int i = blockPos.getY();
        BlockState blockState = chunkAccess.getBlockState(blockPos);
        if (blockState.isRedstoneConductor(chunkAccess, blockPos)) {
            return;
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int j = 0;
        block0: for (int k = 0; k < 3; ++k) {
            int l = blockPos.getX();
            int m = blockPos.getZ();
            int n = 6;
            Biome.SpawnerData spawnerData = null;
            SpawnGroupData spawnGroupData = null;
            int o = Mth.ceil(serverLevel.random.nextFloat() * 4.0f);
            int p = 0;
            for (int q = 0; q < o; ++q) {
                double f;
                mutableBlockPos.set(l += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6), i, m += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6));
                double d = (double)l + 0.5;
                double e = (double)m + 0.5;
                Player player = serverLevel.getNearestPlayer(d, (double)i, e, -1.0, false);
                if (player == null || !NaturalSpawner.isRightDistanceToPlayerAndSpawnPoint(serverLevel, chunkAccess, mutableBlockPos, f = player.distanceToSqr(d, i, e))) continue;
                if (spawnerData == null) {
                    spawnerData = NaturalSpawner.getRandomSpawnMobAt(serverLevel, structureFeatureManager, chunkGenerator, mobCategory, serverLevel.random, mutableBlockPos);
                    if (spawnerData == null) continue block0;
                    o = spawnerData.minCount + serverLevel.random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
                }
                if (!NaturalSpawner.isValidSpawnPostitionForType(serverLevel, mobCategory, structureFeatureManager, chunkGenerator, spawnerData, mutableBlockPos, f) || !spawnPredicate.test(spawnerData.type, mutableBlockPos, chunkAccess)) continue;
                Mob mob = NaturalSpawner.getMobForSpawn(serverLevel, spawnerData.type);
                if (mob == null) {
                    return;
                }
                mob.moveTo(d, i, e, serverLevel.random.nextFloat() * 360.0f, 0.0f);
                if (!NaturalSpawner.isValidPositionForMob(serverLevel, mob, f)) continue;
                spawnGroupData = mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.NATURAL, spawnGroupData, null);
                ++p;
                serverLevel.addFreshEntity(mob);
                afterSpawnCallback.run(mob, chunkAccess);
                if (++j >= mob.getMaxSpawnClusterSize()) {
                    return;
                }
                if (mob.isMaxGroupSizeReached(p)) continue block0;
            }
        }
    }

    private static boolean isRightDistanceToPlayerAndSpawnPoint(ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos.MutableBlockPos mutableBlockPos, double d) {
        if (d <= 576.0) {
            return false;
        }
        if (serverLevel.getSharedSpawnPos().closerThan(new Vec3((double)mutableBlockPos.getX() + 0.5, mutableBlockPos.getY(), (double)mutableBlockPos.getZ() + 0.5), 24.0)) {
            return false;
        }
        ChunkPos chunkPos = new ChunkPos(mutableBlockPos);
        return Objects.equals(chunkPos, chunkAccess.getPos()) || serverLevel.getChunkSource().isEntityTickingChunk(chunkPos);
    }

    private static boolean isValidSpawnPostitionForType(ServerLevel serverLevel, MobCategory mobCategory, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Biome.SpawnerData spawnerData, BlockPos.MutableBlockPos mutableBlockPos, double d) {
        EntityType<?> entityType = spawnerData.type;
        if (entityType.getCategory() == MobCategory.MISC) {
            return false;
        }
        if (!entityType.canSpawnFarFromPlayer() && d > (double)(entityType.getCategory().getDespawnDistance() * entityType.getCategory().getDespawnDistance())) {
            return false;
        }
        if (!entityType.canSummon() || !NaturalSpawner.canSpawnMobAt(serverLevel, structureFeatureManager, chunkGenerator, mobCategory, spawnerData, mutableBlockPos)) {
            return false;
        }
        SpawnPlacements.Type type = SpawnPlacements.getPlacementType(entityType);
        if (!NaturalSpawner.isSpawnPositionOk(type, serverLevel, mutableBlockPos, entityType)) {
            return false;
        }
        if (!SpawnPlacements.checkSpawnRules(entityType, serverLevel, MobSpawnType.NATURAL, mutableBlockPos, serverLevel.random)) {
            return false;
        }
        return serverLevel.noCollision(entityType.getAABB((double)mutableBlockPos.getX() + 0.5, mutableBlockPos.getY(), (double)mutableBlockPos.getZ() + 0.5));
    }

    @Nullable
    private static Mob getMobForSpawn(ServerLevel serverLevel, EntityType<?> entityType) {
        Mob mob;
        try {
            Object entity = entityType.create(serverLevel);
            if (!(entity instanceof Mob)) {
                throw new IllegalStateException("Trying to spawn a non-mob: " + Registry.ENTITY_TYPE.getKey(entityType));
            }
            mob = (Mob)entity;
        } catch (Exception exception) {
            LOGGER.warn("Failed to create mob", (Throwable)exception);
            return null;
        }
        return mob;
    }

    private static boolean isValidPositionForMob(ServerLevel serverLevel, Mob mob, double d) {
        if (d > (double)(mob.getType().getCategory().getDespawnDistance() * mob.getType().getCategory().getDespawnDistance()) && mob.removeWhenFarAway(d)) {
            return false;
        }
        return mob.checkSpawnRules(serverLevel, MobSpawnType.NATURAL) && mob.checkSpawnObstruction(serverLevel);
    }

    @Nullable
    private static Biome.SpawnerData getRandomSpawnMobAt(ServerLevel serverLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, Random random, BlockPos blockPos) {
        Biome biome = serverLevel.getBiome(blockPos);
        if (mobCategory == MobCategory.WATER_AMBIENT && biome.getBiomeCategory() == Biome.BiomeCategory.RIVER && random.nextFloat() < 0.98f) {
            return null;
        }
        List<Biome.SpawnerData> list = NaturalSpawner.mobsAt(serverLevel, structureFeatureManager, chunkGenerator, mobCategory, blockPos, biome);
        if (list.isEmpty()) {
            return null;
        }
        return WeighedRandom.getRandomItem(random, list);
    }

    private static boolean canSpawnMobAt(ServerLevel serverLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, Biome.SpawnerData spawnerData, BlockPos blockPos) {
        return NaturalSpawner.mobsAt(serverLevel, structureFeatureManager, chunkGenerator, mobCategory, blockPos, null).contains(spawnerData);
    }

    private static List<Biome.SpawnerData> mobsAt(ServerLevel serverLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, BlockPos blockPos, @Nullable Biome biome) {
        if (mobCategory == MobCategory.MONSTER && serverLevel.getBlockState(blockPos.below()).getBlock() == Blocks.NETHER_BRICKS && structureFeatureManager.getStructureAt(blockPos, false, StructureFeature.NETHER_BRIDGE).isValid()) {
            return StructureFeature.NETHER_BRIDGE.getSpecialEnemies();
        }
        return chunkGenerator.getMobsAt(biome != null ? biome : serverLevel.getBiome(blockPos), structureFeatureManager, mobCategory, blockPos);
    }

    private static BlockPos getRandomPosWithin(Level level, LevelChunk levelChunk) {
        ChunkPos chunkPos = levelChunk.getPos();
        int i = chunkPos.getMinBlockX() + level.random.nextInt(16);
        int j = chunkPos.getMinBlockZ() + level.random.nextInt(16);
        int k = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i, j) + 1;
        int l = level.random.nextInt(k + 1);
        return new BlockPos(i, l, j);
    }

    public static boolean isValidEmptySpawnBlock(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState, EntityType<?> entityType) {
        if (blockState.isCollisionShapeFullBlock(blockGetter, blockPos)) {
            return false;
        }
        if (blockState.isSignalSource()) {
            return false;
        }
        if (!fluidState.isEmpty()) {
            return false;
        }
        if (blockState.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
            return false;
        }
        return !entityType.isBlockDangerous(blockState);
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
            case IN_LAVA: {
                return fluidState.is(FluidTags.LAVA);
            }
        }
        BlockState blockState2 = levelReader.getBlockState(blockPos3);
        if (!blockState2.isValidSpawn(levelReader, blockPos3, entityType)) {
            return false;
        }
        return NaturalSpawner.isValidEmptySpawnBlock(levelReader, blockPos, blockState, fluidState, entityType) && NaturalSpawner.isValidEmptySpawnBlock(levelReader, blockPos2, levelReader.getBlockState(blockPos2), levelReader.getFluidState(blockPos2), entityType);
    }

    public static void spawnMobsForChunkGeneration(ServerLevelAccessor serverLevelAccessor, Biome biome, int i, int j, Random random) {
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
                    BlockPos blockPos = NaturalSpawner.getTopNonCollidingPos(serverLevelAccessor, spawnerData.type, n, o);
                    if (spawnerData.type.canSummon() && NaturalSpawner.isSpawnPositionOk(SpawnPlacements.getPlacementType(spawnerData.type), serverLevelAccessor, blockPos, spawnerData.type)) {
                        Mob mob;
                        Object entity;
                        float f = spawnerData.type.getWidth();
                        double d = Mth.clamp((double)n, (double)k + (double)f, (double)k + 16.0 - (double)f);
                        double e = Mth.clamp((double)o, (double)l + (double)f, (double)l + 16.0 - (double)f);
                        if (!serverLevelAccessor.noCollision(spawnerData.type.getAABB(d, blockPos.getY(), e)) || !SpawnPlacements.checkSpawnRules(spawnerData.type, serverLevelAccessor, MobSpawnType.CHUNK_GENERATION, new BlockPos(d, (double)blockPos.getY(), e), serverLevelAccessor.getRandom())) continue;
                        try {
                            entity = spawnerData.type.create(serverLevelAccessor.getLevel());
                        } catch (Exception exception) {
                            LOGGER.warn("Failed to create mob", (Throwable)exception);
                            continue;
                        }
                        ((Entity)entity).moveTo(d, blockPos.getY(), e, random.nextFloat() * 360.0f, 0.0f);
                        if (entity instanceof Mob && (mob = (Mob)entity).checkSpawnRules(serverLevelAccessor, MobSpawnType.CHUNK_GENERATION) && mob.checkSpawnObstruction(serverLevelAccessor)) {
                            spawnGroupData = mob.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.CHUNK_GENERATION, spawnGroupData, null);
                            serverLevelAccessor.addFreshEntity(mob);
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

    private static BlockPos getTopNonCollidingPos(LevelReader levelReader, EntityType<?> entityType, int i, int j) {
        Vec3i blockPos;
        int k = levelReader.getHeight(SpawnPlacements.getHeightmapType(entityType), i, j);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, k, j);
        if (levelReader.dimensionType().hasCeiling()) {
            do {
                mutableBlockPos.move(Direction.DOWN);
            } while (!levelReader.getBlockState(mutableBlockPos).isAir());
            do {
                mutableBlockPos.move(Direction.DOWN);
            } while (levelReader.getBlockState(mutableBlockPos).isAir() && mutableBlockPos.getY() > 0);
        }
        if (SpawnPlacements.getPlacementType(entityType) == SpawnPlacements.Type.ON_GROUND && levelReader.getBlockState((BlockPos)(blockPos = mutableBlockPos.below())).isPathfindable(levelReader, (BlockPos)blockPos, PathComputationType.LAND)) {
            return blockPos;
        }
        return mutableBlockPos.immutable();
    }

    @FunctionalInterface
    public static interface ChunkGetter {
        public void query(long var1, Consumer<LevelChunk> var3);
    }

    @FunctionalInterface
    public static interface AfterSpawnCallback {
        public void run(Mob var1, ChunkAccess var2);
    }

    @FunctionalInterface
    public static interface SpawnPredicate {
        public boolean test(EntityType<?> var1, BlockPos var2, ChunkAccess var3);
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
            double d;
            this.lastCheckedPos = blockPos;
            this.lastCheckedType = entityType;
            Biome biome = NaturalSpawner.getRoughBiome(blockPos, chunkAccess);
            Biome.MobSpawnCost mobSpawnCost = biome.getMobSpawnCost(entityType);
            if (mobSpawnCost == null) {
                this.lastCharge = 0.0;
                return true;
            }
            this.lastCharge = d = mobSpawnCost.getCharge();
            double e = this.spawnPotential.getPotentialEnergyChange(blockPos, d);
            return e <= mobSpawnCost.getEnergyBudget();
        }

        private void afterSpawn(Mob mob, ChunkAccess chunkAccess) {
            Biome biome;
            Biome.MobSpawnCost mobSpawnCost;
            EntityType<?> entityType = mob.getType();
            BlockPos blockPos = mob.blockPosition();
            double d = blockPos.equals(this.lastCheckedPos) && entityType == this.lastCheckedType ? this.lastCharge : ((mobSpawnCost = (biome = NaturalSpawner.getRoughBiome(blockPos, chunkAccess)).getMobSpawnCost(entityType)) != null ? mobSpawnCost.getCharge() : 0.0);
            this.spawnPotential.addCharge(blockPos, d);
            this.mobCategoryCounts.addTo(entityType.getCategory(), 1);
        }

        @Environment(value=EnvType.CLIENT)
        public int getSpawnableChunkCount() {
            return this.spawnableChunkCount;
        }

        public Object2IntMap<MobCategory> getMobCategoryCounts() {
            return this.unmodifiableMobCategoryCounts;
        }

        private boolean canSpawnForCategory(MobCategory mobCategory) {
            int i = mobCategory.getMaxInstancesPerChunk() * this.spawnableChunkCount / MAGIC_NUMBER;
            return this.mobCategoryCounts.getInt(mobCategory) < i;
        }
    }
}

