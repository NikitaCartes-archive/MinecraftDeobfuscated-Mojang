/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddGlobalEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagManager;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelConflictException;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.PortalForcer;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerLevel
extends Level {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<Entity> globalEntities = Lists.newArrayList();
    private final Int2ObjectMap<Entity> entitiesById = new Int2ObjectLinkedOpenHashMap<Entity>();
    private final Map<UUID, Entity> entitiesByUuid = Maps.newHashMap();
    private final Queue<Entity> toAddAfterTick = Queues.newArrayDeque();
    private final List<ServerPlayer> players = Lists.newArrayList();
    boolean tickingEntities;
    private final MinecraftServer server;
    private final LevelStorage levelStorage;
    public boolean noSave;
    private boolean allPlayersSleeping;
    private int emptyTime;
    private final PortalForcer portalForcer;
    private final ServerTickList<Block> blockTicks = new ServerTickList<Block>(this, block -> block == null || block.defaultBlockState().isAir(), Registry.BLOCK::getKey, Registry.BLOCK::get, this::tickBlock);
    private final ServerTickList<Fluid> liquidTicks = new ServerTickList<Fluid>(this, fluid -> fluid == null || fluid == Fluids.EMPTY, Registry.FLUID::getKey, Registry.FLUID::get, this::tickLiquid);
    private final Set<PathNavigation> navigations = Sets.newHashSet();
    protected final Raids raids;
    private final ObjectLinkedOpenHashSet<BlockEventData> blockEvents = new ObjectLinkedOpenHashSet();
    private boolean handlingTick;
    @Nullable
    private final WanderingTraderSpawner wanderingTraderSpawner;

    public ServerLevel(MinecraftServer minecraftServer, Executor executor, LevelStorage levelStorage, LevelData levelData, DimensionType dimensionType, ProfilerFiller profilerFiller, ChunkProgressListener chunkProgressListener) {
        super(levelData, dimensionType, (level, dimension) -> new ServerChunkCache((ServerLevel)level, levelStorage.getFolder(), levelStorage.getFixerUpper(), levelStorage.getStructureManager(), executor, dimension.createRandomLevelGenerator(), minecraftServer.getPlayerList().getViewDistance(), chunkProgressListener, () -> minecraftServer.getLevel(DimensionType.OVERWORLD).getDataStorage()), profilerFiller, false);
        this.levelStorage = levelStorage;
        this.server = minecraftServer;
        this.portalForcer = new PortalForcer(this);
        this.updateSkyBrightness();
        this.prepareWeather();
        this.getWorldBorder().setAbsoluteMaxSize(minecraftServer.getAbsoluteMaxWorldSize());
        this.raids = this.getDataStorage().computeIfAbsent(() -> new Raids(this), Raids.getFileId(this.dimension));
        if (!minecraftServer.isSingleplayer()) {
            this.getLevelData().setGameType(minecraftServer.getDefaultGameType());
        }
        this.wanderingTraderSpawner = this.dimension.getType() == DimensionType.OVERWORLD ? new WanderingTraderSpawner(this) : null;
    }

    @Override
    public Biome getUncachedNoiseBiome(int i, int j, int k) {
        return this.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(i, j, k);
    }

    public void tick(BooleanSupplier booleanSupplier) {
        boolean bl4;
        int j;
        ProfilerFiller profilerFiller = this.getProfiler();
        this.handlingTick = true;
        profilerFiller.push("world border");
        this.getWorldBorder().tick();
        profilerFiller.popPush("weather");
        boolean bl = this.isRaining();
        if (this.dimension.isHasSkyLight()) {
            if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
                int i = this.levelData.getClearWeatherTime();
                j = this.levelData.getThunderTime();
                int k = this.levelData.getRainTime();
                boolean bl2 = this.levelData.isThundering();
                boolean bl3 = this.levelData.isRaining();
                if (i > 0) {
                    --i;
                    j = bl2 ? 0 : 1;
                    k = bl3 ? 0 : 1;
                    bl2 = false;
                    bl3 = false;
                } else {
                    if (j > 0) {
                        if (--j == 0) {
                            bl2 = !bl2;
                        }
                    } else {
                        j = bl2 ? this.random.nextInt(12000) + 3600 : this.random.nextInt(168000) + 12000;
                    }
                    if (k > 0) {
                        if (--k == 0) {
                            bl3 = !bl3;
                        }
                    } else {
                        k = bl3 ? this.random.nextInt(12000) + 12000 : this.random.nextInt(168000) + 12000;
                    }
                }
                this.levelData.setThunderTime(j);
                this.levelData.setRainTime(k);
                this.levelData.setClearWeatherTime(i);
                this.levelData.setThundering(bl2);
                this.levelData.setRaining(bl3);
            }
            this.oThunderLevel = this.thunderLevel;
            this.thunderLevel = this.levelData.isThundering() ? (float)((double)this.thunderLevel + 0.01) : (float)((double)this.thunderLevel - 0.01);
            this.thunderLevel = Mth.clamp(this.thunderLevel, 0.0f, 1.0f);
            this.oRainLevel = this.rainLevel;
            this.rainLevel = this.levelData.isRaining() ? (float)((double)this.rainLevel + 0.01) : (float)((double)this.rainLevel - 0.01);
            this.rainLevel = Mth.clamp(this.rainLevel, 0.0f, 1.0f);
        }
        if (this.oRainLevel != this.rainLevel) {
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(7, this.rainLevel), this.dimension.getType());
        }
        if (this.oThunderLevel != this.thunderLevel) {
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(8, this.thunderLevel), this.dimension.getType());
        }
        if (bl != this.isRaining()) {
            if (bl) {
                this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(2, 0.0f));
            } else {
                this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(1, 0.0f));
            }
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(7, this.rainLevel));
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(8, this.thunderLevel));
        }
        if (this.getLevelData().isHardcore() && this.getDifficulty() != Difficulty.HARD) {
            this.getLevelData().setDifficulty(Difficulty.HARD);
        }
        if (this.allPlayersSleeping && this.players.stream().noneMatch(serverPlayer -> !serverPlayer.isSpectator() && !serverPlayer.isSleepingLongEnough())) {
            this.allPlayersSleeping = false;
            if (this.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                long l = this.levelData.getDayTime() + 24000L;
                this.setDayTime(l - l % 24000L);
            }
            this.wakeUpAllPlayers();
            if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
                this.stopWeather();
            }
        }
        this.updateSkyBrightness();
        this.tickTime();
        profilerFiller.popPush("chunkSource");
        this.getChunkSource().tick(booleanSupplier);
        profilerFiller.popPush("tickPending");
        if (this.levelData.getGeneratorType() != LevelType.DEBUG_ALL_BLOCK_STATES) {
            this.blockTicks.tick();
            this.liquidTicks.tick();
        }
        profilerFiller.popPush("raid");
        this.raids.tick();
        if (this.wanderingTraderSpawner != null) {
            this.wanderingTraderSpawner.tick();
        }
        profilerFiller.popPush("blockEvents");
        this.runBlockEvents();
        this.handlingTick = false;
        profilerFiller.popPush("entities");
        boolean bl2 = bl4 = !this.players.isEmpty() || !this.getForcedChunks().isEmpty();
        if (bl4) {
            this.resetEmptyTime();
        }
        if (bl4 || this.emptyTime++ < 300) {
            Entity entity2;
            this.dimension.tick();
            profilerFiller.push("global");
            for (j = 0; j < this.globalEntities.size(); ++j) {
                Entity entity3 = this.globalEntities.get(j);
                this.guardEntityTick(entity -> {
                    ++entity.tickCount;
                    entity.tick();
                }, entity3);
                if (!entity3.removed) continue;
                this.globalEntities.remove(j--);
            }
            profilerFiller.popPush("regular");
            this.tickingEntities = true;
            Iterator objectIterator = this.entitiesById.int2ObjectEntrySet().iterator();
            while (objectIterator.hasNext()) {
                Int2ObjectMap.Entry entry = (Int2ObjectMap.Entry)objectIterator.next();
                Entity entity22 = (Entity)entry.getValue();
                Entity entity3 = entity22.getVehicle();
                if (!this.server.isAnimals() && (entity22 instanceof Animal || entity22 instanceof WaterAnimal)) {
                    entity22.remove();
                }
                if (!this.server.isNpcsEnabled() && entity22 instanceof Npc) {
                    entity22.remove();
                }
                profilerFiller.push("checkDespawn");
                if (!entity22.removed) {
                    entity22.checkDespawn();
                }
                profilerFiller.pop();
                if (entity3 != null) {
                    if (!entity3.removed && entity3.hasPassenger(entity22)) continue;
                    entity22.stopRiding();
                }
                profilerFiller.push("tick");
                if (!entity22.removed && !(entity22 instanceof EnderDragonPart)) {
                    this.guardEntityTick(this::tickNonPassenger, entity22);
                }
                profilerFiller.pop();
                profilerFiller.push("remove");
                if (entity22.removed) {
                    this.removeFromChunk(entity22);
                    objectIterator.remove();
                    this.onEntityRemoved(entity22);
                }
                profilerFiller.pop();
            }
            this.tickingEntities = false;
            while ((entity2 = this.toAddAfterTick.poll()) != null) {
                this.add(entity2);
            }
            profilerFiller.pop();
            this.tickBlockEntities();
        }
        profilerFiller.pop();
    }

    private void wakeUpAllPlayers() {
        this.players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList()).forEach(serverPlayer -> serverPlayer.stopSleepInBed(false, false));
    }

    public void tickChunk(LevelChunk levelChunk, int i) {
        BlockPos blockPos;
        ChunkPos chunkPos = levelChunk.getPos();
        boolean bl = this.isRaining();
        int j = chunkPos.getMinBlockX();
        int k = chunkPos.getMinBlockZ();
        ProfilerFiller profilerFiller = this.getProfiler();
        profilerFiller.push("thunder");
        if (bl && this.isThundering() && this.random.nextInt(100000) == 0 && this.isRainingAt(blockPos = this.findLightingTargetAround(this.getBlockRandomPos(j, 0, k, 15)))) {
            boolean bl2;
            DifficultyInstance difficultyInstance = this.getCurrentDifficultyAt(blockPos);
            boolean bl3 = bl2 = this.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && this.random.nextDouble() < (double)difficultyInstance.getEffectiveDifficulty() * 0.01;
            if (bl2) {
                SkeletonHorse skeletonHorse = EntityType.SKELETON_HORSE.create(this);
                skeletonHorse.setTrap(true);
                skeletonHorse.setAge(0);
                skeletonHorse.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                this.addFreshEntity(skeletonHorse);
            }
            this.addGlobalEntity(new LightningBolt(this, (double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, bl2));
        }
        profilerFiller.popPush("iceandsnow");
        if (this.random.nextInt(16) == 0) {
            blockPos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.getBlockRandomPos(j, 0, k, 15));
            BlockPos blockPos2 = blockPos.below();
            Biome biome = this.getBiome(blockPos);
            if (biome.shouldFreeze(this, blockPos2)) {
                this.setBlockAndUpdate(blockPos2, Blocks.ICE.defaultBlockState());
            }
            if (bl && biome.shouldSnow(this, blockPos)) {
                this.setBlockAndUpdate(blockPos, Blocks.SNOW.defaultBlockState());
            }
            if (bl && this.getBiome(blockPos2).getPrecipitation() == Biome.Precipitation.RAIN) {
                this.getBlockState(blockPos2).getBlock().handleRain(this, blockPos2);
            }
        }
        profilerFiller.popPush("tickBlocks");
        if (i > 0) {
            for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
                if (levelChunkSection == LevelChunk.EMPTY_SECTION || !levelChunkSection.isRandomlyTicking()) continue;
                int l = levelChunkSection.bottomBlockY();
                for (int m = 0; m < i; ++m) {
                    FluidState fluidState;
                    BlockPos blockPos3 = this.getBlockRandomPos(j, l, k, 15);
                    profilerFiller.push("randomTick");
                    BlockState blockState = levelChunkSection.getBlockState(blockPos3.getX() - j, blockPos3.getY() - l, blockPos3.getZ() - k);
                    if (blockState.isRandomlyTicking()) {
                        blockState.randomTick(this, blockPos3, this.random);
                    }
                    if ((fluidState = blockState.getFluidState()).isRandomlyTicking()) {
                        fluidState.randomTick(this, blockPos3, this.random);
                    }
                    profilerFiller.pop();
                }
            }
        }
        profilerFiller.pop();
    }

    protected BlockPos findLightingTargetAround(BlockPos blockPos) {
        BlockPos blockPos2 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos);
        AABB aABB = new AABB(blockPos2, new BlockPos(blockPos2.getX(), this.getMaxBuildHeight(), blockPos2.getZ())).inflate(3.0);
        List<LivingEntity> list = this.getEntitiesOfClass(LivingEntity.class, aABB, livingEntity -> livingEntity != null && livingEntity.isAlive() && this.canSeeSky(livingEntity.getCommandSenderBlockPosition()));
        if (!list.isEmpty()) {
            return list.get(this.random.nextInt(list.size())).getCommandSenderBlockPosition();
        }
        if (blockPos2.getY() == -1) {
            blockPos2 = blockPos2.above(2);
        }
        return blockPos2;
    }

    public boolean isHandlingTick() {
        return this.handlingTick;
    }

    public void updateSleepingPlayerList() {
        this.allPlayersSleeping = false;
        if (!this.players.isEmpty()) {
            int i = 0;
            int j = 0;
            for (ServerPlayer serverPlayer : this.players) {
                if (serverPlayer.isSpectator()) {
                    ++i;
                    continue;
                }
                if (!serverPlayer.isSleeping()) continue;
                ++j;
            }
            this.allPlayersSleeping = j > 0 && j >= this.players.size() - i;
        }
    }

    @Override
    public ServerScoreboard getScoreboard() {
        return this.server.getScoreboard();
    }

    private void stopWeather() {
        this.levelData.setRainTime(0);
        this.levelData.setRaining(false);
        this.levelData.setThunderTime(0);
        this.levelData.setThundering(false);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void validateSpawn() {
        if (this.levelData.getYSpawn() <= 0) {
            this.levelData.setYSpawn(this.getSeaLevel() + 1);
        }
        int i = this.levelData.getXSpawn();
        int j = this.levelData.getZSpawn();
        int k = 0;
        while (this.getTopBlockState(new BlockPos(i, 0, j)).isAir()) {
            i += this.random.nextInt(8) - this.random.nextInt(8);
            j += this.random.nextInt(8) - this.random.nextInt(8);
            if (++k != 10000) continue;
        }
        this.levelData.setXSpawn(i);
        this.levelData.setZSpawn(j);
    }

    public void resetEmptyTime() {
        this.emptyTime = 0;
    }

    private void tickLiquid(TickNextTickData<Fluid> tickNextTickData) {
        FluidState fluidState = this.getFluidState(tickNextTickData.pos);
        if (fluidState.getType() == tickNextTickData.getType()) {
            fluidState.tick(this, tickNextTickData.pos);
        }
    }

    private void tickBlock(TickNextTickData<Block> tickNextTickData) {
        BlockState blockState = this.getBlockState(tickNextTickData.pos);
        if (blockState.getBlock() == tickNextTickData.getType()) {
            blockState.tick(this, tickNextTickData.pos, this.random);
        }
    }

    public void tickNonPassenger(Entity entity) {
        if (!(entity instanceof Player) && !this.getChunkSource().isEntityTickingChunk(entity)) {
            return;
        }
        entity.setPosAndOldPos(entity.getX(), entity.getY(), entity.getZ());
        entity.yRotO = entity.yRot;
        entity.xRotO = entity.xRot;
        if (entity.inChunk) {
            ++entity.tickCount;
            this.getProfiler().push(() -> Registry.ENTITY_TYPE.getKey(entity.getType()).toString());
            entity.tick();
            this.getProfiler().pop();
        }
        this.updateChunkPos(entity);
        if (entity.inChunk) {
            for (Entity entity2 : entity.getPassengers()) {
                this.tickPassenger(entity, entity2);
            }
        }
    }

    public void tickPassenger(Entity entity, Entity entity2) {
        if (entity2.removed || entity2.getVehicle() != entity) {
            entity2.stopRiding();
            return;
        }
        if (!(entity2 instanceof Player) && !this.getChunkSource().isEntityTickingChunk(entity2)) {
            return;
        }
        entity2.setPosAndOldPos(entity2.getX(), entity2.getY(), entity2.getZ());
        entity2.yRotO = entity2.yRot;
        entity2.xRotO = entity2.xRot;
        if (entity2.inChunk) {
            ++entity2.tickCount;
            entity2.rideTick();
        }
        this.updateChunkPos(entity2);
        if (entity2.inChunk) {
            for (Entity entity3 : entity2.getPassengers()) {
                this.tickPassenger(entity2, entity3);
            }
        }
    }

    public void updateChunkPos(Entity entity) {
        this.getProfiler().push("chunkCheck");
        int i = Mth.floor(entity.getX() / 16.0);
        int j = Mth.floor(entity.getY() / 16.0);
        int k = Mth.floor(entity.getZ() / 16.0);
        if (!entity.inChunk || entity.xChunk != i || entity.yChunk != j || entity.zChunk != k) {
            if (entity.inChunk && this.hasChunk(entity.xChunk, entity.zChunk)) {
                this.getChunk(entity.xChunk, entity.zChunk).removeEntity(entity, entity.yChunk);
            }
            if (entity.checkAndResetTeleportedFlag() || this.hasChunk(i, k)) {
                this.getChunk(i, k).addEntity(entity);
            } else {
                entity.inChunk = false;
            }
        }
        this.getProfiler().pop();
    }

    @Override
    public boolean mayInteract(Player player, BlockPos blockPos) {
        return !this.server.isUnderSpawnProtection(this, blockPos, player) && this.getWorldBorder().isWithinBounds(blockPos);
    }

    public void setInitialSpawn(LevelSettings levelSettings) {
        ChunkPos chunkPos;
        if (!this.dimension.mayRespawn()) {
            this.levelData.setSpawn(BlockPos.ZERO.above(this.getChunkSource().getGenerator().getSpawnHeight()));
            return;
        }
        if (this.levelData.getGeneratorType() == LevelType.DEBUG_ALL_BLOCK_STATES) {
            this.levelData.setSpawn(BlockPos.ZERO.above());
            return;
        }
        BiomeSource biomeSource = this.getChunkSource().getGenerator().getBiomeSource();
        List<Biome> list = biomeSource.getPlayerSpawnBiomes();
        Random random = new Random(this.getSeed());
        BlockPos blockPos = biomeSource.findBiomeHorizontal(0, this.getSeaLevel(), 0, 256, list, random);
        ChunkPos chunkPos2 = chunkPos = blockPos == null ? new ChunkPos(0, 0) : new ChunkPos(blockPos);
        if (blockPos == null) {
            LOGGER.warn("Unable to find spawn biome");
        }
        boolean bl = false;
        for (Block block : BlockTags.VALID_SPAWN.getValues()) {
            if (!biomeSource.getSurfaceBlocks().contains(block.defaultBlockState())) continue;
            bl = true;
            break;
        }
        this.levelData.setSpawn(chunkPos.getWorldPosition().offset(8, this.getChunkSource().getGenerator().getSpawnHeight(), 8));
        int i = 0;
        int j = 0;
        int k = 0;
        int l = -1;
        int m = 32;
        for (int n = 0; n < 1024; ++n) {
            BlockPos blockPos2;
            if (i > -16 && i <= 16 && j > -16 && j <= 16 && (blockPos2 = this.dimension.getSpawnPosInChunk(new ChunkPos(chunkPos.x + i, chunkPos.z + j), bl)) != null) {
                this.levelData.setSpawn(blockPos2);
                break;
            }
            if (i == j || i < 0 && i == -j || i > 0 && i == 1 - j) {
                int o = k;
                k = -l;
                l = o;
            }
            i += k;
            j += l;
        }
        if (levelSettings.hasStartingBonusItems()) {
            this.generateBonusItemsNearSpawn();
        }
    }

    protected void generateBonusItemsNearSpawn() {
        ConfiguredFeature<NoneFeatureConfiguration, ?> configuredFeature = Feature.BONUS_CHEST.configured(FeatureConfiguration.NONE);
        configuredFeature.place(this, this.getChunkSource().getGenerator(), this.random, new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn()));
    }

    @Nullable
    public BlockPos getDimensionSpecificSpawn() {
        return this.dimension.getDimensionSpecificSpawn();
    }

    public void save(@Nullable ProgressListener progressListener, boolean bl, boolean bl2) throws LevelConflictException {
        ServerChunkCache serverChunkCache = this.getChunkSource();
        if (bl2) {
            return;
        }
        if (progressListener != null) {
            progressListener.progressStartNoAbort(new TranslatableComponent("menu.savingLevel", new Object[0]));
        }
        this.saveLevelData();
        if (progressListener != null) {
            progressListener.progressStage(new TranslatableComponent("menu.savingChunks", new Object[0]));
        }
        serverChunkCache.save(bl);
    }

    protected void saveLevelData() throws LevelConflictException {
        this.checkSession();
        this.dimension.saveData();
        this.getChunkSource().getDataStorage().save();
    }

    public List<Entity> getEntities(@Nullable EntityType<?> entityType, Predicate<? super Entity> predicate) {
        ArrayList<Entity> list = Lists.newArrayList();
        ServerChunkCache serverChunkCache = this.getChunkSource();
        for (Entity entity : this.entitiesById.values()) {
            if (entityType != null && entity.getType() != entityType || !serverChunkCache.hasChunk(Mth.floor(entity.getX()) >> 4, Mth.floor(entity.getZ()) >> 4) || !predicate.test(entity)) continue;
            list.add(entity);
        }
        return list;
    }

    public List<EnderDragon> getDragons() {
        ArrayList<EnderDragon> list = Lists.newArrayList();
        for (Entity entity : this.entitiesById.values()) {
            if (!(entity instanceof EnderDragon) || !entity.isAlive()) continue;
            list.add((EnderDragon)entity);
        }
        return list;
    }

    public List<ServerPlayer> getPlayers(Predicate<? super ServerPlayer> predicate) {
        ArrayList<ServerPlayer> list = Lists.newArrayList();
        for (ServerPlayer serverPlayer : this.players) {
            if (!predicate.test(serverPlayer)) continue;
            list.add(serverPlayer);
        }
        return list;
    }

    @Nullable
    public ServerPlayer getRandomPlayer() {
        List<ServerPlayer> list = this.getPlayers(LivingEntity::isAlive);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(this.random.nextInt(list.size()));
    }

    public Object2IntMap<MobCategory> getMobCategoryCounts() {
        Object2IntOpenHashMap<MobCategory> object2IntMap = new Object2IntOpenHashMap<MobCategory>();
        for (Entity entity : this.entitiesById.values()) {
            MobCategory mobCategory;
            Mob mob;
            if (entity instanceof Mob && ((mob = (Mob)entity).isPersistenceRequired() || mob.requiresCustomPersistence()) || (mobCategory = entity.getType().getCategory()) == MobCategory.MISC || !this.getChunkSource().isInAccessibleChunk(entity)) continue;
            object2IntMap.mergeInt(mobCategory, 1, Integer::sum);
        }
        return object2IntMap;
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        return this.addEntity(entity);
    }

    public boolean addWithUUID(Entity entity) {
        return this.addEntity(entity);
    }

    public void addFromAnotherDimension(Entity entity) {
        boolean bl = entity.forcedLoading;
        entity.forcedLoading = true;
        this.addWithUUID(entity);
        entity.forcedLoading = bl;
        this.updateChunkPos(entity);
    }

    public void addDuringCommandTeleport(ServerPlayer serverPlayer) {
        this.addPlayer(serverPlayer);
        this.updateChunkPos(serverPlayer);
    }

    public void addDuringPortalTeleport(ServerPlayer serverPlayer) {
        this.addPlayer(serverPlayer);
        this.updateChunkPos(serverPlayer);
    }

    public void addNewPlayer(ServerPlayer serverPlayer) {
        this.addPlayer(serverPlayer);
    }

    public void addRespawnedPlayer(ServerPlayer serverPlayer) {
        this.addPlayer(serverPlayer);
    }

    private void addPlayer(ServerPlayer serverPlayer) {
        Entity entity = this.entitiesByUuid.get(serverPlayer.getUUID());
        if (entity != null) {
            LOGGER.warn("Force-added player with duplicate UUID {}", (Object)serverPlayer.getUUID().toString());
            entity.unRide();
            this.removePlayerImmediately((ServerPlayer)entity);
        }
        this.players.add(serverPlayer);
        this.updateSleepingPlayerList();
        ChunkAccess chunkAccess = this.getChunk(Mth.floor(serverPlayer.getX() / 16.0), Mth.floor(serverPlayer.getZ() / 16.0), ChunkStatus.FULL, true);
        if (chunkAccess instanceof LevelChunk) {
            chunkAccess.addEntity(serverPlayer);
        }
        this.add(serverPlayer);
    }

    private boolean addEntity(Entity entity) {
        if (entity.removed) {
            LOGGER.warn("Tried to add entity {} but it was marked as removed already", (Object)EntityType.getKey(entity.getType()));
            return false;
        }
        if (this.isUUIDUsed(entity)) {
            return false;
        }
        ChunkAccess chunkAccess = this.getChunk(Mth.floor(entity.getX() / 16.0), Mth.floor(entity.getZ() / 16.0), ChunkStatus.FULL, entity.forcedLoading);
        if (!(chunkAccess instanceof LevelChunk)) {
            return false;
        }
        chunkAccess.addEntity(entity);
        this.add(entity);
        return true;
    }

    public boolean loadFromChunk(Entity entity) {
        if (this.isUUIDUsed(entity)) {
            return false;
        }
        this.add(entity);
        return true;
    }

    private boolean isUUIDUsed(Entity entity) {
        Entity entity2 = this.entitiesByUuid.get(entity.getUUID());
        if (entity2 == null) {
            return false;
        }
        LOGGER.warn("Keeping entity {} that already exists with UUID {}", (Object)EntityType.getKey(entity2.getType()), (Object)entity.getUUID().toString());
        return true;
    }

    public void unload(LevelChunk levelChunk) {
        this.blockEntitiesToUnload.addAll(levelChunk.getBlockEntities().values());
        for (ClassInstanceMultiMap<Entity> classInstanceMultiMap : levelChunk.getEntitySections()) {
            for (Entity entity : classInstanceMultiMap) {
                if (entity instanceof ServerPlayer) continue;
                if (this.tickingEntities) {
                    throw Util.pauseInIde(new IllegalStateException("Removing entity while ticking!"));
                }
                this.entitiesById.remove(entity.getId());
                this.onEntityRemoved(entity);
            }
        }
    }

    public void onEntityRemoved(Entity entity) {
        if (entity instanceof EnderDragon) {
            for (EnderDragonPart enderDragonPart : ((EnderDragon)entity).getSubEntities()) {
                enderDragonPart.remove();
            }
        }
        this.entitiesByUuid.remove(entity.getUUID());
        this.getChunkSource().removeEntity(entity);
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            this.players.remove(serverPlayer);
        }
        this.getScoreboard().entityRemoved(entity);
        if (entity instanceof Mob) {
            this.navigations.remove(((Mob)entity).getNavigation());
        }
    }

    private void add(Entity entity) {
        if (this.tickingEntities) {
            this.toAddAfterTick.add(entity);
        } else {
            this.entitiesById.put(entity.getId(), entity);
            if (entity instanceof EnderDragon) {
                for (EnderDragonPart enderDragonPart : ((EnderDragon)entity).getSubEntities()) {
                    this.entitiesById.put(enderDragonPart.getId(), (Entity)enderDragonPart);
                }
            }
            this.entitiesByUuid.put(entity.getUUID(), entity);
            this.getChunkSource().addEntity(entity);
            if (entity instanceof Mob) {
                this.navigations.add(((Mob)entity).getNavigation());
            }
        }
    }

    public void despawn(Entity entity) {
        if (this.tickingEntities) {
            throw Util.pauseInIde(new IllegalStateException("Removing entity while ticking!"));
        }
        this.removeFromChunk(entity);
        this.entitiesById.remove(entity.getId());
        this.onEntityRemoved(entity);
    }

    private void removeFromChunk(Entity entity) {
        ChunkAccess chunkAccess = this.getChunk(entity.xChunk, entity.zChunk, ChunkStatus.FULL, false);
        if (chunkAccess instanceof LevelChunk) {
            ((LevelChunk)chunkAccess).removeEntity(entity);
        }
    }

    public void removePlayerImmediately(ServerPlayer serverPlayer) {
        serverPlayer.remove();
        this.despawn(serverPlayer);
        this.updateSleepingPlayerList();
    }

    public void addGlobalEntity(LightningBolt lightningBolt) {
        this.globalEntities.add(lightningBolt);
        this.server.getPlayerList().broadcast(null, lightningBolt.getX(), lightningBolt.getY(), lightningBolt.getZ(), 512.0, this.dimension.getType(), new ClientboundAddGlobalEntityPacket(lightningBolt));
    }

    @Override
    public void destroyBlockProgress(int i, BlockPos blockPos, int j) {
        for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
            double f;
            double e;
            double d;
            if (serverPlayer == null || serverPlayer.level != this || serverPlayer.getId() == i || !((d = (double)blockPos.getX() - serverPlayer.getX()) * d + (e = (double)blockPos.getY() - serverPlayer.getY()) * e + (f = (double)blockPos.getZ() - serverPlayer.getZ()) * f < 1024.0)) continue;
            serverPlayer.connection.send(new ClientboundBlockDestructionPacket(i, blockPos, j));
        }
    }

    @Override
    public void playSound(@Nullable Player player, double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h) {
        this.server.getPlayerList().broadcast(player, d, e, f, g > 1.0f ? (double)(16.0f * g) : 16.0, this.dimension.getType(), new ClientboundSoundPacket(soundEvent, soundSource, d, e, f, g, h));
    }

    @Override
    public void playSound(@Nullable Player player, Entity entity, SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
        this.server.getPlayerList().broadcast(player, entity.getX(), entity.getY(), entity.getZ(), f > 1.0f ? (double)(16.0f * f) : 16.0, this.dimension.getType(), new ClientboundSoundEntityPacket(soundEvent, soundSource, entity, f, g));
    }

    @Override
    public void globalLevelEvent(int i, BlockPos blockPos, int j) {
        this.server.getPlayerList().broadcastAll(new ClientboundLevelEventPacket(i, blockPos, j, true));
    }

    @Override
    public void levelEvent(@Nullable Player player, int i, BlockPos blockPos, int j) {
        this.server.getPlayerList().broadcast(player, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 64.0, this.dimension.getType(), new ClientboundLevelEventPacket(i, blockPos, j, false));
    }

    @Override
    public void sendBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState2, int i) {
        this.getChunkSource().blockChanged(blockPos);
        VoxelShape voxelShape = blockState.getCollisionShape(this, blockPos);
        VoxelShape voxelShape2 = blockState2.getCollisionShape(this, blockPos);
        if (!Shapes.joinIsNotEmpty(voxelShape, voxelShape2, BooleanOp.NOT_SAME)) {
            return;
        }
        for (PathNavigation pathNavigation : this.navigations) {
            if (pathNavigation.hasDelayedRecomputation()) continue;
            pathNavigation.recomputePath(blockPos);
        }
    }

    @Override
    public void broadcastEntityEvent(Entity entity, byte b) {
        this.getChunkSource().broadcastAndSend(entity, new ClientboundEntityEventPacket(entity, b));
    }

    @Override
    public ServerChunkCache getChunkSource() {
        return (ServerChunkCache)super.getChunkSource();
    }

    @Override
    public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damageSource, double d, double e, double f, float g, boolean bl, Explosion.BlockInteraction blockInteraction) {
        Explosion explosion = new Explosion(this, entity, d, e, f, g, bl, blockInteraction);
        if (damageSource != null) {
            explosion.setDamageSource(damageSource);
        }
        explosion.explode();
        explosion.finalizeExplosion(false);
        if (blockInteraction == Explosion.BlockInteraction.NONE) {
            explosion.clearToBlow();
        }
        for (ServerPlayer serverPlayer : this.players) {
            if (!(serverPlayer.distanceToSqr(d, e, f) < 4096.0)) continue;
            serverPlayer.connection.send(new ClientboundExplodePacket(d, e, f, g, explosion.getToBlow(), explosion.getHitPlayers().get(serverPlayer)));
        }
        return explosion;
    }

    @Override
    public void blockEvent(BlockPos blockPos, Block block, int i, int j) {
        this.blockEvents.add(new BlockEventData(blockPos, block, i, j));
    }

    private void runBlockEvents() {
        while (!this.blockEvents.isEmpty()) {
            BlockEventData blockEventData = this.blockEvents.removeFirst();
            if (!this.doBlockEvent(blockEventData)) continue;
            this.server.getPlayerList().broadcast(null, blockEventData.getPos().getX(), blockEventData.getPos().getY(), blockEventData.getPos().getZ(), 64.0, this.dimension.getType(), new ClientboundBlockEventPacket(blockEventData.getPos(), blockEventData.getBlock(), blockEventData.getParamA(), blockEventData.getParamB()));
        }
    }

    private boolean doBlockEvent(BlockEventData blockEventData) {
        BlockState blockState = this.getBlockState(blockEventData.getPos());
        if (blockState.getBlock() == blockEventData.getBlock()) {
            return blockState.triggerEvent(this, blockEventData.getPos(), blockEventData.getParamA(), blockEventData.getParamB());
        }
        return false;
    }

    public ServerTickList<Block> getBlockTicks() {
        return this.blockTicks;
    }

    public ServerTickList<Fluid> getLiquidTicks() {
        return this.liquidTicks;
    }

    @Override
    @NotNull
    public MinecraftServer getServer() {
        return this.server;
    }

    public PortalForcer getPortalForcer() {
        return this.portalForcer;
    }

    public StructureManager getStructureManager() {
        return this.levelStorage.getStructureManager();
    }

    public <T extends ParticleOptions> int sendParticles(T particleOptions, double d, double e, double f, int i, double g, double h, double j, double k) {
        ClientboundLevelParticlesPacket clientboundLevelParticlesPacket = new ClientboundLevelParticlesPacket(particleOptions, false, (float)d, (float)e, (float)f, (float)g, (float)h, (float)j, (float)k, i);
        int l = 0;
        for (int m = 0; m < this.players.size(); ++m) {
            ServerPlayer serverPlayer = this.players.get(m);
            if (!this.sendParticles(serverPlayer, false, d, e, f, clientboundLevelParticlesPacket)) continue;
            ++l;
        }
        return l;
    }

    public <T extends ParticleOptions> boolean sendParticles(ServerPlayer serverPlayer, T particleOptions, boolean bl, double d, double e, double f, int i, double g, double h, double j, double k) {
        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(particleOptions, bl, (float)d, (float)e, (float)f, (float)g, (float)h, (float)j, (float)k, i);
        return this.sendParticles(serverPlayer, bl, d, e, f, packet);
    }

    private boolean sendParticles(ServerPlayer serverPlayer, boolean bl, double d, double e, double f, Packet<?> packet) {
        if (serverPlayer.getLevel() != this) {
            return false;
        }
        BlockPos blockPos = serverPlayer.getCommandSenderBlockPosition();
        if (blockPos.closerThan(new Vec3(d, e, f), bl ? 512.0 : 32.0)) {
            serverPlayer.connection.send(packet);
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public Entity getEntity(int i) {
        return (Entity)this.entitiesById.get(i);
    }

    @Nullable
    public Entity getEntity(UUID uUID) {
        return this.entitiesByUuid.get(uUID);
    }

    @Nullable
    public BlockPos findNearestMapFeature(String string, BlockPos blockPos, int i, boolean bl) {
        return this.getChunkSource().getGenerator().findNearestMapFeature(this, string, blockPos, i, bl);
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.server.getRecipeManager();
    }

    @Override
    public TagManager getTagManager() {
        return this.server.getTags();
    }

    @Override
    public void setGameTime(long l) {
        super.setGameTime(l);
        this.levelData.getScheduledEvents().tick(this.server, l);
    }

    @Override
    public boolean noSave() {
        return this.noSave;
    }

    public void checkSession() throws LevelConflictException {
        this.levelStorage.checkSession();
    }

    public LevelStorage getLevelStorage() {
        return this.levelStorage;
    }

    public DimensionDataStorage getDataStorage() {
        return this.getChunkSource().getDataStorage();
    }

    @Override
    @Nullable
    public MapItemSavedData getMapData(String string) {
        return this.getServer().getLevel(DimensionType.OVERWORLD).getDataStorage().get(() -> new MapItemSavedData(string), string);
    }

    @Override
    public void setMapData(MapItemSavedData mapItemSavedData) {
        this.getServer().getLevel(DimensionType.OVERWORLD).getDataStorage().set(mapItemSavedData);
    }

    @Override
    public int getFreeMapId() {
        return this.getServer().getLevel(DimensionType.OVERWORLD).getDataStorage().computeIfAbsent(MapIndex::new, "idcounts").getFreeAuxValueForMap();
    }

    @Override
    public void setSpawnPos(BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(new BlockPos(this.levelData.getXSpawn(), 0, this.levelData.getZSpawn()));
        super.setSpawnPos(blockPos);
        this.getChunkSource().removeRegionTicket(TicketType.START, chunkPos, 11, Unit.INSTANCE);
        this.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(blockPos), 11, Unit.INSTANCE);
    }

    public LongSet getForcedChunks() {
        ForcedChunksSavedData forcedChunksSavedData = this.getDataStorage().get(ForcedChunksSavedData::new, "chunks");
        return forcedChunksSavedData != null ? LongSets.unmodifiable(forcedChunksSavedData.getChunks()) : LongSets.EMPTY_SET;
    }

    public boolean setChunkForced(int i, int j, boolean bl) {
        boolean bl2;
        ForcedChunksSavedData forcedChunksSavedData = this.getDataStorage().computeIfAbsent(ForcedChunksSavedData::new, "chunks");
        ChunkPos chunkPos = new ChunkPos(i, j);
        long l = chunkPos.toLong();
        if (bl) {
            bl2 = forcedChunksSavedData.getChunks().add(l);
            if (bl2) {
                this.getChunk(i, j);
            }
        } else {
            bl2 = forcedChunksSavedData.getChunks().remove(l);
        }
        forcedChunksSavedData.setDirty(bl2);
        if (bl2) {
            this.getChunkSource().updateChunkForced(chunkPos, bl);
        }
        return bl2;
    }

    public List<ServerPlayer> players() {
        return this.players;
    }

    @Override
    public void onBlockStateChange(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
        Optional<PoiType> optional2;
        Optional<PoiType> optional = PoiType.forState(blockState);
        if (Objects.equals(optional, optional2 = PoiType.forState(blockState2))) {
            return;
        }
        BlockPos blockPos2 = blockPos.immutable();
        optional.ifPresent(poiType -> this.getServer().execute(() -> {
            this.getPoiManager().remove(blockPos2);
            DebugPackets.sendPoiRemovedPacket(this, blockPos2);
        }));
        optional2.ifPresent(poiType -> this.getServer().execute(() -> {
            this.getPoiManager().add(blockPos2, (PoiType)poiType);
            DebugPackets.sendPoiAddedPacket(this, blockPos2);
        }));
    }

    public PoiManager getPoiManager() {
        return this.getChunkSource().getPoiManager();
    }

    public boolean isVillage(BlockPos blockPos) {
        return this.closeToVillage(blockPos, 1);
    }

    public boolean isVillage(SectionPos sectionPos) {
        return this.isVillage(sectionPos.center());
    }

    public boolean closeToVillage(BlockPos blockPos, int i) {
        if (i > 6) {
            return false;
        }
        return this.sectionsToVillage(SectionPos.of(blockPos)) <= i;
    }

    public int sectionsToVillage(SectionPos sectionPos) {
        return this.getPoiManager().sectionsToVillage(sectionPos);
    }

    public Raids getRaids() {
        return this.raids;
    }

    @Nullable
    public Raid getRaidAt(BlockPos blockPos) {
        return this.raids.getNearbyRaid(blockPos, 9216);
    }

    public boolean isRaided(BlockPos blockPos) {
        return this.getRaidAt(blockPos) != null;
    }

    public void onReputationEvent(ReputationEventType reputationEventType, Entity entity, ReputationEventHandler reputationEventHandler) {
        reputationEventHandler.onReputationEventFrom(reputationEventType, entity);
    }

    public void saveDebugReport(Path path) throws IOException {
        ChunkMap chunkMap = this.getChunkSource().chunkMap;
        try (BufferedWriter writer = Files.newBufferedWriter(path.resolve("stats.txt"), new OpenOption[0]);){
            writer.write(String.format("spawning_chunks: %d\n", chunkMap.getDistanceManager().getNaturalSpawnChunkCount()));
            for (Object2IntMap.Entry entry : this.getMobCategoryCounts().object2IntEntrySet()) {
                writer.write(String.format("spawn_count.%s: %d\n", ((MobCategory)((Object)entry.getKey())).getName(), entry.getIntValue()));
            }
            writer.write(String.format("entities: %d\n", this.entitiesById.size()));
            writer.write(String.format("block_entities: %d\n", this.blockEntityList.size()));
            writer.write(String.format("block_ticks: %d\n", ((ServerTickList)this.getBlockTicks()).size()));
            writer.write(String.format("fluid_ticks: %d\n", ((ServerTickList)this.getLiquidTicks()).size()));
            writer.write("distance_manager: " + chunkMap.getDistanceManager().getDebugStatus() + "\n");
            writer.write(String.format("pending_tasks: %d\n", this.getChunkSource().getPendingTasksCount()));
        }
        CrashReport crashReport = new CrashReport("Level dump", new Exception("dummy"));
        this.fillReportDetails(crashReport);
        BufferedWriter writer2 = Files.newBufferedWriter(path.resolve("example_crash.txt"), new OpenOption[0]);
        Object object = null;
        try {
            writer2.write(crashReport.getFriendlyReport());
        } catch (Throwable throwable) {
            object = throwable;
            throw throwable;
        } finally {
            if (writer2 != null) {
                if (object != null) {
                    try {
                        ((Writer)writer2).close();
                    } catch (Throwable throwable) {
                        ((Throwable)object).addSuppressed(throwable);
                    }
                } else {
                    ((Writer)writer2).close();
                }
            }
        }
        Path path2 = path.resolve("chunks.csv");
        Throwable throwable = null;
        try (BufferedWriter writer3 = Files.newBufferedWriter(path2, new OpenOption[0]);){
            chunkMap.dumpChunks(writer3);
        } catch (Throwable throwable2) {
            Throwable throwable3 = throwable2;
            throw throwable2;
        }
        Path path3 = path.resolve("entities.csv");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path3, new OpenOption[0]);){
            ServerLevel.dumpEntities(bufferedWriter, this.entitiesById.values());
        }
        Path path4 = path.resolve("global_entities.csv");
        try (BufferedWriter writer5 = Files.newBufferedWriter(path4, new OpenOption[0]);){
            ServerLevel.dumpEntities(writer5, this.globalEntities);
        }
        Path path5 = path.resolve("block_entities.csv");
        try (BufferedWriter writer6 = Files.newBufferedWriter(path5, new OpenOption[0]);){
            this.dumpBlockEntities(writer6);
        }
    }

    private static void dumpEntities(Writer writer, Iterable<Entity> iterable) throws IOException {
        CsvOutput csvOutput = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("uuid").addColumn("type").addColumn("alive").addColumn("display_name").addColumn("custom_name").build(writer);
        for (Entity entity : iterable) {
            Component component = entity.getCustomName();
            Component component2 = entity.getDisplayName();
            csvOutput.writeRow(entity.getX(), entity.getY(), entity.getZ(), entity.getUUID(), Registry.ENTITY_TYPE.getKey(entity.getType()), entity.isAlive(), component2.getString(), component != null ? component.getString() : null);
        }
    }

    private void dumpBlockEntities(Writer writer) throws IOException {
        CsvOutput csvOutput = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("type").build(writer);
        for (BlockEntity blockEntity : this.blockEntityList) {
            BlockPos blockPos = blockEntity.getBlockPos();
            csvOutput.writeRow(blockPos.getX(), blockPos.getY(), blockPos.getZ(), Registry.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()));
        }
    }

    @VisibleForTesting
    public void clearBlockEvents(BoundingBox boundingBox) {
        this.blockEvents.removeIf(blockEventData -> boundingBox.isInside(blockEventData.getPos()));
    }

    @Override
    public /* synthetic */ Scoreboard getScoreboard() {
        return this.getScoreboard();
    }

    @Override
    public /* synthetic */ ChunkSource getChunkSource() {
        return this.getChunkSource();
    }

    public /* synthetic */ TickList getLiquidTicks() {
        return this.getLiquidTicks();
    }

    public /* synthetic */ TickList getBlockTicks() {
        return this.getBlockTicks();
    }
}

