/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.PotentialCalculator;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class NaturalSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MIN_SPAWN_DISTANCE = 24;
    public static final int SPAWN_DISTANCE_CHUNK = 8;
    public static final int SPAWN_DISTANCE_BLOCK = 128;
    static final int MAGIC_NUMBER = (int)Math.pow(17.0, 2.0);
    private static final MobCategory[] SPAWNING_CATEGORIES = (MobCategory[])Stream.of(MobCategory.values()).filter(mobCategory -> mobCategory != MobCategory.MISC).toArray(MobCategory[]::new);

    private NaturalSpawner() {
    }

    public static SpawnState createState(int i, Iterable<Entity> iterable, ChunkGetter chunkGetter, LocalMobCapCalculator localMobCapCalculator) {
        PotentialCalculator potentialCalculator = new PotentialCalculator();
        Object2IntOpenHashMap<MobCategory> object2IntOpenHashMap = new Object2IntOpenHashMap<MobCategory>();
        for (Entity entity : iterable) {
            MobCategory mobCategory;
            Mob mob;
            if (entity instanceof Mob && ((mob = (Mob)entity).isPersistenceRequired() || mob.requiresCustomPersistence()) || (mobCategory = entity.getType().getCategory()) == MobCategory.MISC) continue;
            BlockPos blockPos = entity.blockPosition();
            chunkGetter.query(ChunkPos.asLong(blockPos), levelChunk -> {
                MobSpawnSettings.MobSpawnCost mobSpawnCost = NaturalSpawner.getRoughBiome(blockPos, levelChunk).getMobSettings().getMobSpawnCost(entity.getType());
                if (mobSpawnCost != null) {
                    potentialCalculator.addCharge(entity.blockPosition(), mobSpawnCost.getCharge());
                }
                if (entity instanceof Mob) {
                    localMobCapCalculator.addMob(levelChunk.getPos(), mobCategory);
                }
                object2IntOpenHashMap.addTo(mobCategory, 1);
            });
        }
        return new SpawnState(i, object2IntOpenHashMap, potentialCalculator, localMobCapCalculator);
    }

    static Biome getRoughBiome(BlockPos blockPos, ChunkAccess chunkAccess) {
        return chunkAccess.getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getY()), QuartPos.fromBlock(blockPos.getZ())).value();
    }

    public static void spawnForChunk(ServerLevel serverLevel, LevelChunk levelChunk, SpawnState spawnState, boolean bl, boolean bl2, boolean bl3) {
        serverLevel.getProfiler().push("spawner");
        for (MobCategory mobCategory : SPAWNING_CATEGORIES) {
            if (!bl && mobCategory.isFriendly() || !bl2 && !mobCategory.isFriendly() || !bl3 && mobCategory.isPersistent() || !spawnState.canSpawnForCategory(mobCategory, levelChunk.getPos())) continue;
            NaturalSpawner.spawnCategoryForChunk(mobCategory, serverLevel, levelChunk, spawnState::canSpawn, spawnState::afterSpawn);
        }
        serverLevel.getProfiler().pop();
    }

    public static void spawnCategoryForChunk(MobCategory mobCategory, ServerLevel serverLevel, LevelChunk levelChunk, SpawnPredicate spawnPredicate, AfterSpawnCallback afterSpawnCallback) {
        BlockPos blockPos = NaturalSpawner.getRandomPosWithin(serverLevel, levelChunk);
        if (blockPos.getY() < serverLevel.getMinBuildHeight() + 1) {
            return;
        }
        NaturalSpawner.spawnCategoryForPosition(mobCategory, serverLevel, levelChunk, blockPos, spawnPredicate, afterSpawnCallback);
    }

    @VisibleForDebug
    public static void spawnCategoryForPosition(MobCategory mobCategory, ServerLevel serverLevel, BlockPos blockPos2) {
        NaturalSpawner.spawnCategoryForPosition(mobCategory, serverLevel, serverLevel.getChunk(blockPos2), blockPos2, (entityType, blockPos, chunkAccess) -> true, (mob, chunkAccess) -> {});
    }

    public static void spawnCategoryForPosition(MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, SpawnPredicate spawnPredicate, AfterSpawnCallback afterSpawnCallback) {
        StructureManager structureManager = serverLevel.structureManager();
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
            MobSpawnSettings.SpawnerData spawnerData = null;
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
                    Optional<MobSpawnSettings.SpawnerData> optional = NaturalSpawner.getRandomSpawnMobAt(serverLevel, structureManager, chunkGenerator, mobCategory, serverLevel.random, mutableBlockPos);
                    if (optional.isEmpty()) continue block0;
                    spawnerData = optional.get();
                    o = spawnerData.minCount + serverLevel.random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
                }
                if (!NaturalSpawner.isValidSpawnPostitionForType(serverLevel, mobCategory, structureManager, chunkGenerator, spawnerData, mutableBlockPos, f) || !spawnPredicate.test(spawnerData.type, mutableBlockPos, chunkAccess)) continue;
                Mob mob = NaturalSpawner.getMobForSpawn(serverLevel, spawnerData.type);
                if (mob == null) {
                    return;
                }
                mob.moveTo(d, i, e, serverLevel.random.nextFloat() * 360.0f, 0.0f);
                if (!NaturalSpawner.isValidPositionForMob(serverLevel, mob, f)) continue;
                spawnGroupData = mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.NATURAL, spawnGroupData, null);
                ++p;
                serverLevel.addFreshEntityWithPassengers(mob);
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
        if (serverLevel.getSharedSpawnPos().closerToCenterThan(new Vec3((double)mutableBlockPos.getX() + 0.5, mutableBlockPos.getY(), (double)mutableBlockPos.getZ() + 0.5), 24.0)) {
            return false;
        }
        return Objects.equals(new ChunkPos(mutableBlockPos), chunkAccess.getPos()) || serverLevel.isNaturalSpawningAllowed(mutableBlockPos);
    }

    private static boolean isValidSpawnPostitionForType(ServerLevel serverLevel, MobCategory mobCategory, StructureManager structureManager, ChunkGenerator chunkGenerator, MobSpawnSettings.SpawnerData spawnerData, BlockPos.MutableBlockPos mutableBlockPos, double d) {
        EntityType<?> entityType = spawnerData.type;
        if (entityType.getCategory() == MobCategory.MISC) {
            return false;
        }
        if (!entityType.canSpawnFarFromPlayer() && d > (double)(entityType.getCategory().getDespawnDistance() * entityType.getCategory().getDespawnDistance())) {
            return false;
        }
        if (!entityType.canSummon() || !NaturalSpawner.canSpawnMobAt(serverLevel, structureManager, chunkGenerator, mobCategory, spawnerData, mutableBlockPos)) {
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
            LOGGER.warn("Failed to create mob", exception);
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

    private static Optional<MobSpawnSettings.SpawnerData> getRandomSpawnMobAt(ServerLevel serverLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, Random random, BlockPos blockPos) {
        Holder<Biome> holder = serverLevel.getBiome(blockPos);
        if (mobCategory == MobCategory.WATER_AMBIENT && holder.is(BiomeTags.REDUCED_WATER_AMBIENT_SPAWNS) && random.nextFloat() < 0.98f) {
            return Optional.empty();
        }
        return NaturalSpawner.mobsAt(serverLevel, structureManager, chunkGenerator, mobCategory, blockPos, holder).getRandom(random);
    }

    private static boolean canSpawnMobAt(ServerLevel serverLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, MobSpawnSettings.SpawnerData spawnerData, BlockPos blockPos) {
        return NaturalSpawner.mobsAt(serverLevel, structureManager, chunkGenerator, mobCategory, blockPos, null).unwrap().contains(spawnerData);
    }

    private static WeightedRandomList<MobSpawnSettings.SpawnerData> mobsAt(ServerLevel serverLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, BlockPos blockPos, @Nullable Holder<Biome> holder) {
        if (NaturalSpawner.isInNetherFortressBounds(blockPos, serverLevel, mobCategory, structureManager)) {
            return NetherFortressStructure.FORTRESS_ENEMIES;
        }
        return chunkGenerator.getMobsAt(holder != null ? holder : serverLevel.getBiome(blockPos), structureManager, mobCategory, blockPos);
    }

    public static boolean isInNetherFortressBounds(BlockPos blockPos, ServerLevel serverLevel, MobCategory mobCategory, StructureManager structureManager) {
        if (mobCategory != MobCategory.MONSTER || !serverLevel.getBlockState(blockPos.below()).is(Blocks.NETHER_BRICKS)) {
            return false;
        }
        Structure structure = structureManager.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY).get(BuiltinStructures.FORTRESS);
        if (structure == null) {
            return false;
        }
        return structureManager.getStructureAt(blockPos, structure).isValid();
    }

    private static BlockPos getRandomPosWithin(Level level, LevelChunk levelChunk) {
        ChunkPos chunkPos = levelChunk.getPos();
        int i = chunkPos.getMinBlockX() + level.random.nextInt(16);
        int j = chunkPos.getMinBlockZ() + level.random.nextInt(16);
        int k = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i, j) + 1;
        int l = Mth.randomBetweenInclusive(level.random, level.getMinBuildHeight(), k);
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
                return fluidState.is(FluidTags.WATER) && !levelReader.getBlockState(blockPos2).isRedstoneConductor(levelReader, blockPos2);
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

    public static void spawnMobsForChunkGeneration(ServerLevelAccessor serverLevelAccessor, Holder<Biome> holder, ChunkPos chunkPos, Random random) {
        MobSpawnSettings mobSpawnSettings = holder.value().getMobSettings();
        WeightedRandomList<MobSpawnSettings.SpawnerData> weightedRandomList = mobSpawnSettings.getMobs(MobCategory.CREATURE);
        if (weightedRandomList.isEmpty()) {
            return;
        }
        int i = chunkPos.getMinBlockX();
        int j = chunkPos.getMinBlockZ();
        while (random.nextFloat() < mobSpawnSettings.getCreatureProbability()) {
            Optional<MobSpawnSettings.SpawnerData> optional = weightedRandomList.getRandom(random);
            if (!optional.isPresent()) continue;
            MobSpawnSettings.SpawnerData spawnerData = optional.get();
            int k = spawnerData.minCount + random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
            SpawnGroupData spawnGroupData = null;
            int l = i + random.nextInt(16);
            int m = j + random.nextInt(16);
            int n = l;
            int o = m;
            for (int p = 0; p < k; ++p) {
                boolean bl = false;
                for (int q = 0; !bl && q < 4; ++q) {
                    BlockPos blockPos = NaturalSpawner.getTopNonCollidingPos(serverLevelAccessor, spawnerData.type, l, m);
                    if (spawnerData.type.canSummon() && NaturalSpawner.isSpawnPositionOk(SpawnPlacements.getPlacementType(spawnerData.type), serverLevelAccessor, blockPos, spawnerData.type)) {
                        Mob mob;
                        Object entity;
                        float f = spawnerData.type.getWidth();
                        double d = Mth.clamp((double)l, (double)i + (double)f, (double)i + 16.0 - (double)f);
                        double e = Mth.clamp((double)m, (double)j + (double)f, (double)j + 16.0 - (double)f);
                        if (!serverLevelAccessor.noCollision(spawnerData.type.getAABB(d, blockPos.getY(), e)) || !SpawnPlacements.checkSpawnRules(spawnerData.type, serverLevelAccessor, MobSpawnType.CHUNK_GENERATION, new BlockPos(d, (double)blockPos.getY(), e), serverLevelAccessor.getRandom())) continue;
                        try {
                            entity = spawnerData.type.create(serverLevelAccessor.getLevel());
                        } catch (Exception exception) {
                            LOGGER.warn("Failed to create mob", exception);
                            continue;
                        }
                        ((Entity)entity).moveTo(d, blockPos.getY(), e, random.nextFloat() * 360.0f, 0.0f);
                        if (entity instanceof Mob && (mob = (Mob)entity).checkSpawnRules(serverLevelAccessor, MobSpawnType.CHUNK_GENERATION) && mob.checkSpawnObstruction(serverLevelAccessor)) {
                            spawnGroupData = mob.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.CHUNK_GENERATION, spawnGroupData, null);
                            serverLevelAccessor.addFreshEntityWithPassengers(mob);
                            bl = true;
                        }
                    }
                    l += random.nextInt(5) - random.nextInt(5);
                    m += random.nextInt(5) - random.nextInt(5);
                    while (l < i || l >= i + 16 || m < j || m >= j + 16) {
                        l = n + random.nextInt(5) - random.nextInt(5);
                        m = o + random.nextInt(5) - random.nextInt(5);
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
            } while (levelReader.getBlockState(mutableBlockPos).isAir() && mutableBlockPos.getY() > levelReader.getMinBuildHeight());
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

        SpawnState(int i, Object2IntOpenHashMap<MobCategory> object2IntOpenHashMap, PotentialCalculator potentialCalculator, LocalMobCapCalculator localMobCapCalculator) {
            this.spawnableChunkCount = i;
            this.mobCategoryCounts = object2IntOpenHashMap;
            this.spawnPotential = potentialCalculator;
            this.localMobCapCalculator = localMobCapCalculator;
            this.unmodifiableMobCategoryCounts = Object2IntMaps.unmodifiable(object2IntOpenHashMap);
        }

        private boolean canSpawn(EntityType<?> entityType, BlockPos blockPos, ChunkAccess chunkAccess) {
            double d;
            this.lastCheckedPos = blockPos;
            this.lastCheckedType = entityType;
            MobSpawnSettings.MobSpawnCost mobSpawnCost = NaturalSpawner.getRoughBiome(blockPos, chunkAccess).getMobSettings().getMobSpawnCost(entityType);
            if (mobSpawnCost == null) {
                this.lastCharge = 0.0;
                return true;
            }
            this.lastCharge = d = mobSpawnCost.getCharge();
            double e = this.spawnPotential.getPotentialEnergyChange(blockPos, d);
            return e <= mobSpawnCost.getEnergyBudget();
        }

        private void afterSpawn(Mob mob, ChunkAccess chunkAccess) {
            MobSpawnSettings.MobSpawnCost mobSpawnCost;
            EntityType<?> entityType = mob.getType();
            BlockPos blockPos = mob.blockPosition();
            double d = blockPos.equals(this.lastCheckedPos) && entityType == this.lastCheckedType ? this.lastCharge : ((mobSpawnCost = NaturalSpawner.getRoughBiome(blockPos, chunkAccess).getMobSettings().getMobSpawnCost(entityType)) != null ? mobSpawnCost.getCharge() : 0.0);
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
            int i = mobCategory.getMaxInstancesPerChunk() * this.spawnableChunkCount / MAGIC_NUMBER;
            if (this.mobCategoryCounts.getInt(mobCategory) >= i) {
                return false;
            }
            return this.localMobCapCalculator.canSpawn(mobCategory, chunkPos);
        }
    }

    @FunctionalInterface
    public static interface SpawnPredicate {
        public boolean test(EntityType<?> var1, BlockPos var2, ChunkAccess var3);
    }

    @FunctionalInterface
    public static interface AfterSpawnCallback {
        public void run(Mob var1, ChunkAccess var2);
    }
}

