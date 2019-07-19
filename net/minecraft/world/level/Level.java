/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockMaterialPredicate;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.jetbrains.annotations.Nullable;

public abstract class Level
implements BlockAndBiomeGetter,
LevelAccessor,
AutoCloseable {
    protected static final Logger LOGGER = LogManager.getLogger();
    private static final Direction[] DIRECTIONS = Direction.values();
    public final List<BlockEntity> blockEntityList = Lists.newArrayList();
    public final List<BlockEntity> tickableBlockEntities = Lists.newArrayList();
    protected final List<BlockEntity> pendingBlockEntities = Lists.newArrayList();
    protected final List<BlockEntity> blockEntitiesToUnload = Lists.newArrayList();
    private final long cloudColor = 0xFFFFFFL;
    private final Thread thread;
    private int skyDarken;
    protected int randValue = new Random().nextInt();
    protected final int addend = 1013904223;
    protected float oRainLevel;
    protected float rainLevel;
    protected float oThunderLevel;
    protected float thunderLevel;
    private int skyFlashTime;
    public final Random random = new Random();
    public final Dimension dimension;
    protected final ChunkSource chunkSource;
    protected final LevelData levelData;
    private final ProfilerFiller profiler;
    public final boolean isClientSide;
    protected boolean updatingBlockEntities;
    private final WorldBorder worldBorder;

    protected Level(LevelData levelData, DimensionType dimensionType, BiFunction<Level, Dimension, ChunkSource> biFunction, ProfilerFiller profilerFiller, boolean bl) {
        this.profiler = profilerFiller;
        this.levelData = levelData;
        this.dimension = dimensionType.create(this);
        this.chunkSource = biFunction.apply(this, this.dimension);
        this.isClientSide = bl;
        this.worldBorder = this.dimension.createWorldBorder();
        this.thread = Thread.currentThread();
    }

    @Override
    public Biome getBiome(BlockPos blockPos) {
        ChunkSource chunkSource = this.getChunkSource();
        LevelChunk levelChunk = chunkSource.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4, false);
        if (levelChunk != null) {
            return levelChunk.getBiome(blockPos);
        }
        ChunkGenerator<?> chunkGenerator = this.getChunkSource().getGenerator();
        if (chunkGenerator == null) {
            return Biomes.PLAINS;
        }
        return chunkGenerator.getBiomeSource().getBiome(blockPos);
    }

    @Override
    public boolean isClientSide() {
        return this.isClientSide;
    }

    @Nullable
    public MinecraftServer getServer() {
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public void validateSpawn() {
        this.setSpawnPos(new BlockPos(8, 64, 8));
    }

    public BlockState getTopBlockState(BlockPos blockPos) {
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), this.getSeaLevel(), blockPos.getZ());
        while (!this.isEmptyBlock(blockPos2.above())) {
            blockPos2 = blockPos2.above();
        }
        return this.getBlockState(blockPos2);
    }

    public static boolean isInWorldBounds(BlockPos blockPos) {
        return !Level.isOutsideBuildHeight(blockPos) && blockPos.getX() >= -30000000 && blockPos.getZ() >= -30000000 && blockPos.getX() < 30000000 && blockPos.getZ() < 30000000;
    }

    public static boolean isOutsideBuildHeight(BlockPos blockPos) {
        return Level.isOutsideBuildHeight(blockPos.getY());
    }

    public static boolean isOutsideBuildHeight(int i) {
        return i < 0 || i >= 256;
    }

    public LevelChunk getChunkAt(BlockPos blockPos) {
        return this.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    @Override
    public LevelChunk getChunk(int i, int j) {
        return (LevelChunk)this.getChunk(i, j, ChunkStatus.FULL);
    }

    @Override
    public ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl) {
        ChunkAccess chunkAccess = this.chunkSource.getChunk(i, j, chunkStatus, bl);
        if (chunkAccess == null && bl) {
            throw new IllegalStateException("Should always be able to create a chunk!");
        }
        return chunkAccess;
    }

    @Override
    public boolean setBlock(BlockPos blockPos, BlockState blockState, int i) {
        if (Level.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        if (!this.isClientSide && this.levelData.getGeneratorType() == LevelType.DEBUG_ALL_BLOCK_STATES) {
            return false;
        }
        LevelChunk levelChunk = this.getChunkAt(blockPos);
        Block block = blockState.getBlock();
        BlockState blockState2 = levelChunk.setBlockState(blockPos, blockState, (i & 0x40) != 0);
        if (blockState2 != null) {
            BlockState blockState3 = this.getBlockState(blockPos);
            if (blockState3 != blockState2 && (blockState3.getLightBlock(this, blockPos) != blockState2.getLightBlock(this, blockPos) || blockState3.getLightEmission() != blockState2.getLightEmission() || blockState3.useShapeForLightOcclusion() || blockState2.useShapeForLightOcclusion())) {
                this.profiler.push("queueCheckLight");
                this.getChunkSource().getLightEngine().checkBlock(blockPos);
                this.profiler.pop();
            }
            if (blockState3 == blockState) {
                if (blockState2 != blockState3) {
                    this.setBlocksDirty(blockPos, blockState2, blockState3);
                }
                if ((i & 2) != 0 && (!this.isClientSide || (i & 4) == 0) && (this.isClientSide || levelChunk.getFullStatus() != null && levelChunk.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING))) {
                    this.sendBlockUpdated(blockPos, blockState2, blockState, i);
                }
                if (!this.isClientSide && (i & 1) != 0) {
                    this.blockUpdated(blockPos, blockState2.getBlock());
                    if (blockState.hasAnalogOutputSignal()) {
                        this.updateNeighbourForOutputSignal(blockPos, block);
                    }
                }
                if ((i & 0x10) == 0) {
                    int j = i & 0xFFFFFFFE;
                    blockState2.updateIndirectNeighbourShapes(this, blockPos, j);
                    blockState.updateNeighbourShapes(this, blockPos, j);
                    blockState.updateIndirectNeighbourShapes(this, blockPos, j);
                }
                this.onBlockStateChange(blockPos, blockState2, blockState3);
            }
            return true;
        }
        return false;
    }

    public void onBlockStateChange(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
    }

    @Override
    public boolean removeBlock(BlockPos blockPos, boolean bl) {
        FluidState fluidState = this.getFluidState(blockPos);
        return this.setBlock(blockPos, fluidState.createLegacyBlock(), 3 | (bl ? 64 : 0));
    }

    @Override
    public boolean destroyBlock(BlockPos blockPos, boolean bl) {
        BlockState blockState = this.getBlockState(blockPos);
        if (blockState.isAir()) {
            return false;
        }
        FluidState fluidState = this.getFluidState(blockPos);
        this.levelEvent(2001, blockPos, Block.getId(blockState));
        if (bl) {
            BlockEntity blockEntity = blockState.getBlock().isEntityBlock() ? this.getBlockEntity(blockPos) : null;
            Block.dropResources(blockState, this, blockPos, blockEntity);
        }
        return this.setBlock(blockPos, fluidState.createLegacyBlock(), 3);
    }

    public boolean setBlockAndUpdate(BlockPos blockPos, BlockState blockState) {
        return this.setBlock(blockPos, blockState, 3);
    }

    public abstract void sendBlockUpdated(BlockPos var1, BlockState var2, BlockState var3, int var4);

    @Override
    public void blockUpdated(BlockPos blockPos, Block block) {
        if (this.levelData.getGeneratorType() != LevelType.DEBUG_ALL_BLOCK_STATES) {
            this.updateNeighborsAt(blockPos, block);
        }
    }

    public void setBlocksDirty(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
    }

    public void updateNeighborsAt(BlockPos blockPos, Block block) {
        this.neighborChanged(blockPos.west(), block, blockPos);
        this.neighborChanged(blockPos.east(), block, blockPos);
        this.neighborChanged(blockPos.below(), block, blockPos);
        this.neighborChanged(blockPos.above(), block, blockPos);
        this.neighborChanged(blockPos.north(), block, blockPos);
        this.neighborChanged(blockPos.south(), block, blockPos);
    }

    public void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, Direction direction) {
        if (direction != Direction.WEST) {
            this.neighborChanged(blockPos.west(), block, blockPos);
        }
        if (direction != Direction.EAST) {
            this.neighborChanged(blockPos.east(), block, blockPos);
        }
        if (direction != Direction.DOWN) {
            this.neighborChanged(blockPos.below(), block, blockPos);
        }
        if (direction != Direction.UP) {
            this.neighborChanged(blockPos.above(), block, blockPos);
        }
        if (direction != Direction.NORTH) {
            this.neighborChanged(blockPos.north(), block, blockPos);
        }
        if (direction != Direction.SOUTH) {
            this.neighborChanged(blockPos.south(), block, blockPos);
        }
    }

    public void neighborChanged(BlockPos blockPos, Block block, BlockPos blockPos2) {
        if (this.isClientSide) {
            return;
        }
        BlockState blockState = this.getBlockState(blockPos);
        try {
            blockState.neighborChanged(this, blockPos, block, blockPos2, false);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception while updating neighbours");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being updated");
            crashReportCategory.setDetail("Source block type", () -> {
                try {
                    return String.format("ID #%s (%s // %s)", Registry.BLOCK.getKey(block), block.getDescriptionId(), block.getClass().getCanonicalName());
                } catch (Throwable throwable) {
                    return "ID #" + Registry.BLOCK.getKey(block);
                }
            });
            CrashReportCategory.populateBlockDetails(crashReportCategory, blockPos, blockState);
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public int getRawBrightness(BlockPos blockPos, int i) {
        if (blockPos.getX() < -30000000 || blockPos.getZ() < -30000000 || blockPos.getX() >= 30000000 || blockPos.getZ() >= 30000000) {
            return 15;
        }
        if (blockPos.getY() < 0) {
            return 0;
        }
        if (blockPos.getY() >= 256) {
            blockPos = new BlockPos(blockPos.getX(), 255, blockPos.getZ());
        }
        return this.getChunkAt(blockPos).getRawBrightness(blockPos, i);
    }

    @Override
    public int getHeight(Heightmap.Types types, int i, int j) {
        int k = i < -30000000 || j < -30000000 || i >= 30000000 || j >= 30000000 ? this.getSeaLevel() + 1 : (this.hasChunk(i >> 4, j >> 4) ? this.getChunk(i >> 4, j >> 4).getHeight(types, i & 0xF, j & 0xF) + 1 : 0);
        return k;
    }

    @Override
    public int getBrightness(LightLayer lightLayer, BlockPos blockPos) {
        return this.getChunkSource().getLightEngine().getLayerListener(lightLayer).getLightValue(blockPos);
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        if (Level.isOutsideBuildHeight(blockPos)) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        LevelChunk levelChunk = this.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        return levelChunk.getBlockState(blockPos);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        if (Level.isOutsideBuildHeight(blockPos)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        LevelChunk levelChunk = this.getChunkAt(blockPos);
        return levelChunk.getFluidState(blockPos);
    }

    public boolean isDay() {
        return this.skyDarken < 4;
    }

    @Override
    public void playSound(@Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
        this.playSound(player, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, soundEvent, soundSource, f, g);
    }

    public abstract void playSound(@Nullable Player var1, double var2, double var4, double var6, SoundEvent var8, SoundSource var9, float var10, float var11);

    public abstract void playSound(@Nullable Player var1, Entity var2, SoundEvent var3, SoundSource var4, float var5, float var6);

    public void playLocalSound(double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h, boolean bl) {
    }

    @Override
    public void addParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {
    }

    @Environment(value=EnvType.CLIENT)
    public void addParticle(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
    }

    public void addAlwaysVisibleParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {
    }

    public void addAlwaysVisibleParticle(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
    }

    @Environment(value=EnvType.CLIENT)
    public float getSkyDarken(float f) {
        float g = this.getTimeOfDay(f);
        float h = 1.0f - (Mth.cos(g * ((float)Math.PI * 2)) * 2.0f + 0.2f);
        h = Mth.clamp(h, 0.0f, 1.0f);
        h = 1.0f - h;
        h = (float)((double)h * (1.0 - (double)(this.getRainLevel(f) * 5.0f) / 16.0));
        h = (float)((double)h * (1.0 - (double)(this.getThunderLevel(f) * 5.0f) / 16.0));
        return h * 0.8f + 0.2f;
    }

    @Environment(value=EnvType.CLIENT)
    public Vec3 getSkyColor(BlockPos blockPos, float f) {
        float p;
        float o;
        float g = this.getTimeOfDay(f);
        float h = Mth.cos(g * ((float)Math.PI * 2)) * 2.0f + 0.5f;
        h = Mth.clamp(h, 0.0f, 1.0f);
        Biome biome = this.getBiome(blockPos);
        float i = biome.getTemperature(blockPos);
        int j = biome.getSkyColor(i);
        float k = (float)(j >> 16 & 0xFF) / 255.0f;
        float l = (float)(j >> 8 & 0xFF) / 255.0f;
        float m = (float)(j & 0xFF) / 255.0f;
        k *= h;
        l *= h;
        m *= h;
        float n = this.getRainLevel(f);
        if (n > 0.0f) {
            o = (k * 0.3f + l * 0.59f + m * 0.11f) * 0.6f;
            p = 1.0f - n * 0.75f;
            k = k * p + o * (1.0f - p);
            l = l * p + o * (1.0f - p);
            m = m * p + o * (1.0f - p);
        }
        if ((o = this.getThunderLevel(f)) > 0.0f) {
            p = (k * 0.3f + l * 0.59f + m * 0.11f) * 0.2f;
            float q = 1.0f - o * 0.75f;
            k = k * q + p * (1.0f - q);
            l = l * q + p * (1.0f - q);
            m = m * q + p * (1.0f - q);
        }
        if (this.skyFlashTime > 0) {
            p = (float)this.skyFlashTime - f;
            if (p > 1.0f) {
                p = 1.0f;
            }
            k = k * (1.0f - (p *= 0.45f)) + 0.8f * p;
            l = l * (1.0f - p) + 0.8f * p;
            m = m * (1.0f - p) + 1.0f * p;
        }
        return new Vec3(k, l, m);
    }

    public float getSunAngle(float f) {
        float g = this.getTimeOfDay(f);
        return g * ((float)Math.PI * 2);
    }

    @Environment(value=EnvType.CLIENT)
    public Vec3 getCloudColor(float f) {
        float n;
        float m;
        float g = this.getTimeOfDay(f);
        float h = Mth.cos(g * ((float)Math.PI * 2)) * 2.0f + 0.5f;
        h = Mth.clamp(h, 0.0f, 1.0f);
        float i = 1.0f;
        float j = 1.0f;
        float k = 1.0f;
        float l = this.getRainLevel(f);
        if (l > 0.0f) {
            m = (i * 0.3f + j * 0.59f + k * 0.11f) * 0.6f;
            n = 1.0f - l * 0.95f;
            i = i * n + m * (1.0f - n);
            j = j * n + m * (1.0f - n);
            k = k * n + m * (1.0f - n);
        }
        i *= h * 0.9f + 0.1f;
        j *= h * 0.9f + 0.1f;
        k *= h * 0.85f + 0.15f;
        m = this.getThunderLevel(f);
        if (m > 0.0f) {
            n = (i * 0.3f + j * 0.59f + k * 0.11f) * 0.2f;
            float o = 1.0f - m * 0.95f;
            i = i * o + n * (1.0f - o);
            j = j * o + n * (1.0f - o);
            k = k * o + n * (1.0f - o);
        }
        return new Vec3(i, j, k);
    }

    @Environment(value=EnvType.CLIENT)
    public Vec3 getFogColor(float f) {
        float g = this.getTimeOfDay(f);
        return this.dimension.getFogColor(g, f);
    }

    @Environment(value=EnvType.CLIENT)
    public float getStarBrightness(float f) {
        float g = this.getTimeOfDay(f);
        float h = 1.0f - (Mth.cos(g * ((float)Math.PI * 2)) * 2.0f + 0.25f);
        h = Mth.clamp(h, 0.0f, 1.0f);
        return h * h * 0.5f;
    }

    public boolean addBlockEntity(BlockEntity blockEntity) {
        boolean bl;
        if (this.updatingBlockEntities) {
            Supplier[] supplierArray = new Supplier[2];
            supplierArray[0] = () -> Registry.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
            supplierArray[1] = blockEntity::getBlockPos;
            LOGGER.error("Adding block entity while ticking: {} @ {}", supplierArray);
        }
        if ((bl = this.blockEntityList.add(blockEntity)) && blockEntity instanceof TickableBlockEntity) {
            this.tickableBlockEntities.add(blockEntity);
        }
        if (this.isClientSide) {
            BlockPos blockPos = blockEntity.getBlockPos();
            BlockState blockState = this.getBlockState(blockPos);
            this.sendBlockUpdated(blockPos, blockState, blockState, 2);
        }
        return bl;
    }

    public void addAllPendingBlockEntities(Collection<BlockEntity> collection) {
        if (this.updatingBlockEntities) {
            this.pendingBlockEntities.addAll(collection);
        } else {
            for (BlockEntity blockEntity : collection) {
                this.addBlockEntity(blockEntity);
            }
        }
    }

    public void tickBlockEntities() {
        ProfilerFiller profilerFiller = this.getProfiler();
        profilerFiller.push("blockEntities");
        if (!this.blockEntitiesToUnload.isEmpty()) {
            this.tickableBlockEntities.removeAll(this.blockEntitiesToUnload);
            this.blockEntityList.removeAll(this.blockEntitiesToUnload);
            this.blockEntitiesToUnload.clear();
        }
        this.updatingBlockEntities = true;
        Iterator<BlockEntity> iterator = this.tickableBlockEntities.iterator();
        while (iterator.hasNext()) {
            BlockPos blockPos;
            BlockEntity blockEntity = iterator.next();
            if (!blockEntity.isRemoved() && blockEntity.hasLevel() && this.chunkSource.isTickingChunk(blockPos = blockEntity.getBlockPos()) && this.getWorldBorder().isWithinBounds(blockPos)) {
                try {
                    profilerFiller.push(() -> String.valueOf(BlockEntityType.getKey(blockEntity.getType())));
                    if (blockEntity.getType().isValid(this.getBlockState(blockPos).getBlock())) {
                        ((TickableBlockEntity)((Object)blockEntity)).tick();
                    } else {
                        blockEntity.logInvalidState();
                    }
                    profilerFiller.pop();
                } catch (Throwable throwable) {
                    CrashReport crashReport = CrashReport.forThrowable(throwable, "Ticking block entity");
                    CrashReportCategory crashReportCategory = crashReport.addCategory("Block entity being ticked");
                    blockEntity.fillCrashReportCategory(crashReportCategory);
                    throw new ReportedException(crashReport);
                }
            }
            if (!blockEntity.isRemoved()) continue;
            iterator.remove();
            this.blockEntityList.remove(blockEntity);
            if (!this.hasChunkAt(blockEntity.getBlockPos())) continue;
            this.getChunkAt(blockEntity.getBlockPos()).removeBlockEntity(blockEntity.getBlockPos());
        }
        this.updatingBlockEntities = false;
        profilerFiller.popPush("pendingBlockEntities");
        if (!this.pendingBlockEntities.isEmpty()) {
            for (int i = 0; i < this.pendingBlockEntities.size(); ++i) {
                BlockEntity blockEntity2 = this.pendingBlockEntities.get(i);
                if (blockEntity2.isRemoved()) continue;
                if (!this.blockEntityList.contains(blockEntity2)) {
                    this.addBlockEntity(blockEntity2);
                }
                if (!this.hasChunkAt(blockEntity2.getBlockPos())) continue;
                LevelChunk levelChunk = this.getChunkAt(blockEntity2.getBlockPos());
                BlockState blockState = levelChunk.getBlockState(blockEntity2.getBlockPos());
                levelChunk.setBlockEntity(blockEntity2.getBlockPos(), blockEntity2);
                this.sendBlockUpdated(blockEntity2.getBlockPos(), blockState, blockState, 3);
            }
            this.pendingBlockEntities.clear();
        }
        profilerFiller.pop();
    }

    public void guardEntityTick(Consumer<Entity> consumer, Entity entity) {
        try {
            consumer.accept(entity);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Ticking entity");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being ticked");
            entity.fillCrashReportCategory(crashReportCategory);
            throw new ReportedException(crashReport);
        }
    }

    public boolean containsAnyBlocks(AABB aABB) {
        int i = Mth.floor(aABB.minX);
        int j = Mth.ceil(aABB.maxX);
        int k = Mth.floor(aABB.minY);
        int l = Mth.ceil(aABB.maxY);
        int m = Mth.floor(aABB.minZ);
        int n = Mth.ceil(aABB.maxZ);
        try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire();){
            for (int o = i; o < j; ++o) {
                for (int p = k; p < l; ++p) {
                    for (int q = m; q < n; ++q) {
                        BlockState blockState = this.getBlockState(pooledMutableBlockPos.set(o, p, q));
                        if (blockState.isAir()) continue;
                        boolean bl = true;
                        return bl;
                    }
                }
            }
        }
        return false;
    }

    public boolean containsFireBlock(AABB aABB) {
        int n;
        int i = Mth.floor(aABB.minX);
        int j = Mth.ceil(aABB.maxX);
        int k = Mth.floor(aABB.minY);
        int l = Mth.ceil(aABB.maxY);
        int m = Mth.floor(aABB.minZ);
        if (this.hasChunksAt(i, k, m, j, l, n = Mth.ceil(aABB.maxZ))) {
            try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire();){
                for (int o = i; o < j; ++o) {
                    for (int p = k; p < l; ++p) {
                        for (int q = m; q < n; ++q) {
                            Block block = this.getBlockState(pooledMutableBlockPos.set(o, p, q)).getBlock();
                            if (block != Blocks.FIRE && block != Blocks.LAVA) continue;
                            boolean bl = true;
                            return bl;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public BlockState containsBlock(AABB aABB, Block block) {
        int n;
        int i = Mth.floor(aABB.minX);
        int j = Mth.ceil(aABB.maxX);
        int k = Mth.floor(aABB.minY);
        int l = Mth.ceil(aABB.maxY);
        int m = Mth.floor(aABB.minZ);
        if (this.hasChunksAt(i, k, m, j, l, n = Mth.ceil(aABB.maxZ))) {
            try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire();){
                for (int o = i; o < j; ++o) {
                    for (int p = k; p < l; ++p) {
                        for (int q = m; q < n; ++q) {
                            BlockState blockState = this.getBlockState(pooledMutableBlockPos.set(o, p, q));
                            if (blockState.getBlock() != block) continue;
                            BlockState blockState2 = blockState;
                            return blockState2;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean containsMaterial(AABB aABB, Material material) {
        int i = Mth.floor(aABB.minX);
        int j = Mth.ceil(aABB.maxX);
        int k = Mth.floor(aABB.minY);
        int l = Mth.ceil(aABB.maxY);
        int m = Mth.floor(aABB.minZ);
        int n = Mth.ceil(aABB.maxZ);
        BlockMaterialPredicate blockMaterialPredicate = BlockMaterialPredicate.forMaterial(material);
        return BlockPos.betweenClosedStream(i, k, m, j - 1, l - 1, n - 1).anyMatch(blockPos -> blockMaterialPredicate.test(this.getBlockState((BlockPos)blockPos)));
    }

    public Explosion explode(@Nullable Entity entity, double d, double e, double f, float g, Explosion.BlockInteraction blockInteraction) {
        return this.explode(entity, null, d, e, f, g, false, blockInteraction);
    }

    public Explosion explode(@Nullable Entity entity, double d, double e, double f, float g, boolean bl, Explosion.BlockInteraction blockInteraction) {
        return this.explode(entity, null, d, e, f, g, bl, blockInteraction);
    }

    public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damageSource, double d, double e, double f, float g, boolean bl, Explosion.BlockInteraction blockInteraction) {
        Explosion explosion = new Explosion(this, entity, d, e, f, g, bl, blockInteraction);
        if (damageSource != null) {
            explosion.setDamageSource(damageSource);
        }
        explosion.explode();
        explosion.finalizeExplosion(true);
        return explosion;
    }

    public boolean extinguishFire(@Nullable Player player, BlockPos blockPos, Direction direction) {
        if (this.getBlockState(blockPos = blockPos.relative(direction)).getBlock() == Blocks.FIRE) {
            this.levelEvent(player, 1009, blockPos, 0);
            this.removeBlock(blockPos, false);
            return true;
        }
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    public String gatherChunkSourceStats() {
        return this.chunkSource.gatherStats();
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        if (Level.isOutsideBuildHeight(blockPos)) {
            return null;
        }
        if (!this.isClientSide && Thread.currentThread() != this.thread) {
            return null;
        }
        BlockEntity blockEntity = null;
        if (this.updatingBlockEntities) {
            blockEntity = this.getPendingBlockEntityAt(blockPos);
        }
        if (blockEntity == null) {
            blockEntity = this.getChunkAt(blockPos).getBlockEntity(blockPos, LevelChunk.EntityCreationType.IMMEDIATE);
        }
        if (blockEntity == null) {
            blockEntity = this.getPendingBlockEntityAt(blockPos);
        }
        return blockEntity;
    }

    @Nullable
    private BlockEntity getPendingBlockEntityAt(BlockPos blockPos) {
        for (int i = 0; i < this.pendingBlockEntities.size(); ++i) {
            BlockEntity blockEntity = this.pendingBlockEntities.get(i);
            if (blockEntity.isRemoved() || !blockEntity.getBlockPos().equals(blockPos)) continue;
            return blockEntity;
        }
        return null;
    }

    public void setBlockEntity(BlockPos blockPos, @Nullable BlockEntity blockEntity) {
        if (Level.isOutsideBuildHeight(blockPos)) {
            return;
        }
        if (blockEntity != null && !blockEntity.isRemoved()) {
            if (this.updatingBlockEntities) {
                blockEntity.setPosition(blockPos);
                Iterator<BlockEntity> iterator = this.pendingBlockEntities.iterator();
                while (iterator.hasNext()) {
                    BlockEntity blockEntity2 = iterator.next();
                    if (!blockEntity2.getBlockPos().equals(blockPos)) continue;
                    blockEntity2.setRemoved();
                    iterator.remove();
                }
                this.pendingBlockEntities.add(blockEntity);
            } else {
                this.getChunkAt(blockPos).setBlockEntity(blockPos, blockEntity);
                this.addBlockEntity(blockEntity);
            }
        }
    }

    public void removeBlockEntity(BlockPos blockPos) {
        BlockEntity blockEntity = this.getBlockEntity(blockPos);
        if (blockEntity != null && this.updatingBlockEntities) {
            blockEntity.setRemoved();
            this.pendingBlockEntities.remove(blockEntity);
        } else {
            if (blockEntity != null) {
                this.pendingBlockEntities.remove(blockEntity);
                this.blockEntityList.remove(blockEntity);
                this.tickableBlockEntities.remove(blockEntity);
            }
            this.getChunkAt(blockPos).removeBlockEntity(blockPos);
        }
    }

    public boolean isLoaded(BlockPos blockPos) {
        if (Level.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        return this.chunkSource.hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    public boolean loadedAndEntityCanStandOn(BlockPos blockPos, Entity entity) {
        if (Level.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        ChunkAccess chunkAccess = this.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunkAccess == null) {
            return false;
        }
        return chunkAccess.getBlockState(blockPos).entityCanStandOn(this, blockPos, entity);
    }

    public void updateSkyBrightness() {
        double d = 1.0 - (double)(this.getRainLevel(1.0f) * 5.0f) / 16.0;
        double e = 1.0 - (double)(this.getThunderLevel(1.0f) * 5.0f) / 16.0;
        double f = 0.5 + 2.0 * Mth.clamp((double)Mth.cos(this.getTimeOfDay(1.0f) * ((float)Math.PI * 2)), -0.25, 0.25);
        this.skyDarken = (int)((1.0 - f * d * e) * 11.0);
    }

    public void setSpawnSettings(boolean bl, boolean bl2) {
        this.getChunkSource().setSpawnSettings(bl, bl2);
    }

    protected void prepareWeather() {
        if (this.levelData.isRaining()) {
            this.rainLevel = 1.0f;
            if (this.levelData.isThundering()) {
                this.thunderLevel = 1.0f;
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.chunkSource.close();
    }

    @Override
    public ChunkStatus statusForCollisions() {
        return ChunkStatus.FULL;
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB aABB, @Nullable Predicate<? super Entity> predicate) {
        ArrayList<Entity> list = Lists.newArrayList();
        int i = Mth.floor((aABB.minX - 2.0) / 16.0);
        int j = Mth.floor((aABB.maxX + 2.0) / 16.0);
        int k = Mth.floor((aABB.minZ - 2.0) / 16.0);
        int l = Mth.floor((aABB.maxZ + 2.0) / 16.0);
        for (int m = i; m <= j; ++m) {
            for (int n = k; n <= l; ++n) {
                LevelChunk levelChunk = this.getChunkSource().getChunk(m, n, false);
                if (levelChunk == null) continue;
                levelChunk.getEntities(entity, aABB, list, predicate);
            }
        }
        return list;
    }

    public List<Entity> getEntities(@Nullable EntityType<?> entityType, AABB aABB, Predicate<? super Entity> predicate) {
        int i = Mth.floor((aABB.minX - 2.0) / 16.0);
        int j = Mth.ceil((aABB.maxX + 2.0) / 16.0);
        int k = Mth.floor((aABB.minZ - 2.0) / 16.0);
        int l = Mth.ceil((aABB.maxZ + 2.0) / 16.0);
        ArrayList<Entity> list = Lists.newArrayList();
        for (int m = i; m < j; ++m) {
            for (int n = k; n < l; ++n) {
                LevelChunk levelChunk = this.getChunkSource().getChunk(m, n, false);
                if (levelChunk == null) continue;
                levelChunk.getEntities(entityType, aABB, list, predicate);
            }
        }
        return list;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> class_, AABB aABB, @Nullable Predicate<? super T> predicate) {
        int i = Mth.floor((aABB.minX - 2.0) / 16.0);
        int j = Mth.ceil((aABB.maxX + 2.0) / 16.0);
        int k = Mth.floor((aABB.minZ - 2.0) / 16.0);
        int l = Mth.ceil((aABB.maxZ + 2.0) / 16.0);
        ArrayList list = Lists.newArrayList();
        ChunkSource chunkSource = this.getChunkSource();
        for (int m = i; m < j; ++m) {
            for (int n = k; n < l; ++n) {
                LevelChunk levelChunk = chunkSource.getChunk(m, n, false);
                if (levelChunk == null) continue;
                levelChunk.getEntitiesOfClass(class_, aABB, list, predicate);
            }
        }
        return list;
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> class_, AABB aABB, @Nullable Predicate<? super T> predicate) {
        int i = Mth.floor((aABB.minX - 2.0) / 16.0);
        int j = Mth.ceil((aABB.maxX + 2.0) / 16.0);
        int k = Mth.floor((aABB.minZ - 2.0) / 16.0);
        int l = Mth.ceil((aABB.maxZ + 2.0) / 16.0);
        ArrayList list = Lists.newArrayList();
        ChunkSource chunkSource = this.getChunkSource();
        for (int m = i; m < j; ++m) {
            for (int n = k; n < l; ++n) {
                LevelChunk levelChunk = chunkSource.getChunkNow(m, n);
                if (levelChunk == null) continue;
                levelChunk.getEntitiesOfClass(class_, aABB, list, predicate);
            }
        }
        return list;
    }

    @Nullable
    public abstract Entity getEntity(int var1);

    public void blockEntityChanged(BlockPos blockPos, BlockEntity blockEntity) {
        if (this.hasChunkAt(blockPos)) {
            this.getChunkAt(blockPos).markUnsaved();
        }
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public Level getLevel() {
        return this;
    }

    public LevelType getGeneratorType() {
        return this.levelData.getGeneratorType();
    }

    public int getDirectSignalTo(BlockPos blockPos) {
        int i = 0;
        if ((i = Math.max(i, this.getDirectSignal(blockPos.below(), Direction.DOWN))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getDirectSignal(blockPos.above(), Direction.UP))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getDirectSignal(blockPos.north(), Direction.NORTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getDirectSignal(blockPos.south(), Direction.SOUTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getDirectSignal(blockPos.west(), Direction.WEST))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, this.getDirectSignal(blockPos.east(), Direction.EAST))) >= 15) {
            return i;
        }
        return i;
    }

    public boolean hasSignal(BlockPos blockPos, Direction direction) {
        return this.getSignal(blockPos, direction) > 0;
    }

    public int getSignal(BlockPos blockPos, Direction direction) {
        BlockState blockState = this.getBlockState(blockPos);
        if (blockState.isRedstoneConductor(this, blockPos)) {
            return this.getDirectSignalTo(blockPos);
        }
        return blockState.getSignal(this, blockPos, direction);
    }

    public boolean hasNeighborSignal(BlockPos blockPos) {
        if (this.getSignal(blockPos.below(), Direction.DOWN) > 0) {
            return true;
        }
        if (this.getSignal(blockPos.above(), Direction.UP) > 0) {
            return true;
        }
        if (this.getSignal(blockPos.north(), Direction.NORTH) > 0) {
            return true;
        }
        if (this.getSignal(blockPos.south(), Direction.SOUTH) > 0) {
            return true;
        }
        if (this.getSignal(blockPos.west(), Direction.WEST) > 0) {
            return true;
        }
        return this.getSignal(blockPos.east(), Direction.EAST) > 0;
    }

    public int getBestNeighborSignal(BlockPos blockPos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = this.getSignal(blockPos.relative(direction), direction);
            if (j >= 15) {
                return 15;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    @Environment(value=EnvType.CLIENT)
    public void disconnect() {
    }

    public void setGameTime(long l) {
        this.levelData.setGameTime(l);
    }

    @Override
    public long getSeed() {
        return this.levelData.getSeed();
    }

    public long getGameTime() {
        return this.levelData.getGameTime();
    }

    public long getDayTime() {
        return this.levelData.getDayTime();
    }

    public void setDayTime(long l) {
        this.levelData.setDayTime(l);
    }

    protected void tickTime() {
        this.setGameTime(this.levelData.getGameTime() + 1L);
        if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            this.setDayTime(this.levelData.getDayTime() + 1L);
        }
    }

    @Override
    public BlockPos getSharedSpawnPos() {
        BlockPos blockPos = new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn());
        if (!this.getWorldBorder().isWithinBounds(blockPos)) {
            blockPos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ()));
        }
        return blockPos;
    }

    public void setSpawnPos(BlockPos blockPos) {
        this.levelData.setSpawn(blockPos);
    }

    public boolean mayInteract(Player player, BlockPos blockPos) {
        return true;
    }

    public void broadcastEntityEvent(Entity entity, byte b) {
    }

    @Override
    public ChunkSource getChunkSource() {
        return this.chunkSource;
    }

    public void blockEvent(BlockPos blockPos, Block block, int i, int j) {
        this.getBlockState(blockPos).triggerEvent(this, blockPos, i, j);
    }

    @Override
    public LevelData getLevelData() {
        return this.levelData;
    }

    public GameRules getGameRules() {
        return this.levelData.getGameRules();
    }

    public float getThunderLevel(float f) {
        return Mth.lerp(f, this.oThunderLevel, this.thunderLevel) * this.getRainLevel(f);
    }

    @Environment(value=EnvType.CLIENT)
    public void setThunderLevel(float f) {
        this.oThunderLevel = f;
        this.thunderLevel = f;
    }

    public float getRainLevel(float f) {
        return Mth.lerp(f, this.oRainLevel, this.rainLevel);
    }

    @Environment(value=EnvType.CLIENT)
    public void setRainLevel(float f) {
        this.oRainLevel = f;
        this.rainLevel = f;
    }

    public boolean isThundering() {
        if (!this.dimension.isHasSkyLight() || this.dimension.isHasCeiling()) {
            return false;
        }
        return (double)this.getThunderLevel(1.0f) > 0.9;
    }

    public boolean isRaining() {
        return (double)this.getRainLevel(1.0f) > 0.2;
    }

    public boolean isRainingAt(BlockPos blockPos) {
        if (!this.isRaining()) {
            return false;
        }
        if (!this.canSeeSky(blockPos)) {
            return false;
        }
        if (this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > blockPos.getY()) {
            return false;
        }
        return this.getBiome(blockPos).getPrecipitation() == Biome.Precipitation.RAIN;
    }

    public boolean isHumidAt(BlockPos blockPos) {
        Biome biome = this.getBiome(blockPos);
        return biome.isHumid();
    }

    @Nullable
    public abstract MapItemSavedData getMapData(String var1);

    public abstract void setMapData(MapItemSavedData var1);

    public abstract int getFreeMapId();

    public void globalLevelEvent(int i, BlockPos blockPos, int j) {
    }

    public int getHeight() {
        return this.dimension.isHasCeiling() ? 128 : 256;
    }

    @Environment(value=EnvType.CLIENT)
    public double getHorizonHeight() {
        if (this.levelData.getGeneratorType() == LevelType.FLAT) {
            return 0.0;
        }
        return 63.0;
    }

    public CrashReportCategory fillReportDetails(CrashReport crashReport) {
        CrashReportCategory crashReportCategory = crashReport.addCategory("Affected level", 1);
        crashReportCategory.setDetail("All players", () -> this.players().size() + " total; " + this.players());
        crashReportCategory.setDetail("Chunk stats", this.chunkSource::gatherStats);
        crashReportCategory.setDetail("Level dimension", () -> this.dimension.getType().toString());
        try {
            this.levelData.fillCrashReportCategory(crashReportCategory);
        } catch (Throwable throwable) {
            crashReportCategory.setDetailError("Level Data Unobtainable", throwable);
        }
        return crashReportCategory;
    }

    public abstract void destroyBlockProgress(int var1, BlockPos var2, int var3);

    @Environment(value=EnvType.CLIENT)
    public void createFireworks(double d, double e, double f, double g, double h, double i, @Nullable CompoundTag compoundTag) {
    }

    public abstract Scoreboard getScoreboard();

    public void updateNeighbourForOutputSignal(BlockPos blockPos, Block block) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!this.hasChunkAt(blockPos2)) continue;
            BlockState blockState = this.getBlockState(blockPos2);
            if (blockState.getBlock() == Blocks.COMPARATOR) {
                blockState.neighborChanged(this, blockPos2, block, blockPos, false);
                continue;
            }
            if (!blockState.isRedstoneConductor(this, blockPos2) || (blockState = this.getBlockState(blockPos2 = blockPos2.relative(direction))).getBlock() != Blocks.COMPARATOR) continue;
            blockState.neighborChanged(this, blockPos2, block, blockPos, false);
        }
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos blockPos) {
        long l = 0L;
        float f = 0.0f;
        if (this.hasChunkAt(blockPos)) {
            f = this.getMoonBrightness();
            l = this.getChunkAt(blockPos).getInhabitedTime();
        }
        return new DifficultyInstance(this.getDifficulty(), this.getDayTime(), l, f);
    }

    @Override
    public int getSkyDarken() {
        return this.skyDarken;
    }

    @Environment(value=EnvType.CLIENT)
    public int getSkyFlashTime() {
        return this.skyFlashTime;
    }

    public void setSkyFlashTime(int i) {
        this.skyFlashTime = i;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }

    public void sendPacketToServer(Packet<?> packet) {
        throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
    }

    @Nullable
    public BlockPos findNearestMapFeature(String string, BlockPos blockPos, int i, boolean bl) {
        return null;
    }

    @Override
    public Dimension getDimension() {
        return this.dimension;
    }

    @Override
    public Random getRandom() {
        return this.random;
    }

    @Override
    public boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate) {
        return predicate.test(this.getBlockState(blockPos));
    }

    public abstract RecipeManager getRecipeManager();

    public abstract TagManager getTagManager();

    public BlockPos getBlockRandomPos(int i, int j, int k, int l) {
        this.randValue = this.randValue * 3 + 1013904223;
        int m = this.randValue >> 2;
        return new BlockPos(i + (m & 0xF), j + (m >> 16 & l), k + (m >> 8 & 0xF));
    }

    public boolean noSave() {
        return false;
    }

    public ProfilerFiller getProfiler() {
        return this.profiler;
    }

    @Override
    public BlockPos getHeightmapPos(Heightmap.Types types, BlockPos blockPos) {
        return new BlockPos(blockPos.getX(), this.getHeight(types, blockPos.getX(), blockPos.getZ()), blockPos.getZ());
    }

    @Override
    public /* synthetic */ ChunkAccess getChunk(int i, int j) {
        return this.getChunk(i, j);
    }
}

