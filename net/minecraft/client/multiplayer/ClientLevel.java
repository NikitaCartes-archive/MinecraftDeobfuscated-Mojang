/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ClientLevel
extends Level {
    private final Int2ObjectMap<Entity> entitiesById = new Int2ObjectOpenHashMap<Entity>();
    private final ClientPacketListener connection;
    private final LevelRenderer levelRenderer;
    private final ClientLevelData clientLevelData;
    private final DimensionSpecialEffects effects;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final List<AbstractClientPlayer> players = Lists.newArrayList();
    private Scoreboard scoreboard = new Scoreboard();
    private final Map<String, MapItemSavedData> mapData = Maps.newHashMap();
    private int skyFlashTime;
    private final Object2ObjectArrayMap<ColorResolver, BlockTintCache> tintCaches = Util.make(new Object2ObjectArrayMap(3), object2ObjectArrayMap -> {
        object2ObjectArrayMap.put(BiomeColors.GRASS_COLOR_RESOLVER, new BlockTintCache());
        object2ObjectArrayMap.put(BiomeColors.FOLIAGE_COLOR_RESOLVER, new BlockTintCache());
        object2ObjectArrayMap.put(BiomeColors.WATER_COLOR_RESOLVER, new BlockTintCache());
    });
    private final ClientChunkCache chunkSource;

    public ClientLevel(ClientPacketListener clientPacketListener, ClientLevelData clientLevelData, ResourceKey<Level> resourceKey, ResourceKey<DimensionType> resourceKey2, DimensionType dimensionType, int i, Supplier<ProfilerFiller> supplier, LevelRenderer levelRenderer, boolean bl, long l) {
        super(clientLevelData, resourceKey, resourceKey2, dimensionType, supplier, true, bl, l);
        this.chunkSource = new ClientChunkCache(this, i);
        this.clientLevelData = clientLevelData;
        this.connection = clientPacketListener;
        this.levelRenderer = levelRenderer;
        this.effects = DimensionSpecialEffects.forType(clientPacketListener.registryAccess().dimensionTypes().getResourceKey(dimensionType));
        this.setDefaultSpawnPos(new BlockPos(8, 64, 8));
        this.updateSkyBrightness();
        this.prepareWeather();
    }

    public DimensionSpecialEffects effects() {
        return this.effects;
    }

    public void tick(BooleanSupplier booleanSupplier) {
        this.getWorldBorder().tick();
        this.tickTime();
        this.getProfiler().push("blocks");
        this.chunkSource.tick(booleanSupplier);
        this.getProfiler().pop();
    }

    private void tickTime() {
        this.setGameTime(this.levelData.getGameTime() + 1L);
        if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            this.setDayTime(this.levelData.getDayTime() + 1L);
        }
    }

    public void setGameTime(long l) {
        this.clientLevelData.setGameTime(l);
    }

    public void setDayTime(long l) {
        if (l < 0L) {
            l = -l;
            this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        } else {
            this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, null);
        }
        this.clientLevelData.setDayTime(l);
    }

    public Iterable<Entity> entitiesForRendering() {
        return this.entitiesById.values();
    }

    public void tickEntities() {
        ProfilerFiller profilerFiller = this.getProfiler();
        profilerFiller.push("entities");
        Iterator objectIterator = this.entitiesById.int2ObjectEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Int2ObjectMap.Entry entry = (Int2ObjectMap.Entry)objectIterator.next();
            Entity entity = (Entity)entry.getValue();
            if (entity.isPassenger()) continue;
            profilerFiller.push("tick");
            if (!entity.removed) {
                this.guardEntityTick(this::tickNonPassenger, entity);
            }
            profilerFiller.pop();
            profilerFiller.push("remove");
            if (entity.removed) {
                objectIterator.remove();
                this.onEntityRemoved(entity);
            }
            profilerFiller.pop();
        }
        this.tickBlockEntities();
        profilerFiller.pop();
    }

    public void tickNonPassenger(Entity entity) {
        if (!(entity instanceof Player) && !this.getChunkSource().isEntityTickingChunk(entity)) {
            this.updateChunkPos(entity);
            return;
        }
        entity.setPosAndOldPos(entity.getX(), entity.getY(), entity.getZ());
        entity.yRotO = entity.yRot;
        entity.xRotO = entity.xRot;
        if (entity.inChunk || entity.isSpectator()) {
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

    private void updateChunkPos(Entity entity) {
        if (!entity.checkAndResetUpdateChunkPos()) {
            return;
        }
        this.getProfiler().push("chunkCheck");
        int i = Mth.floor(entity.getX() / 16.0);
        int j = Mth.floor(entity.getY() / 16.0);
        int k = Mth.floor(entity.getZ() / 16.0);
        if (!entity.inChunk || entity.xChunk != i || entity.yChunk != j || entity.zChunk != k) {
            if (entity.inChunk && this.hasChunk(entity.xChunk, entity.zChunk)) {
                this.getChunk(entity.xChunk, entity.zChunk).removeEntity(entity, entity.yChunk);
            }
            if (entity.checkAndResetForcedChunkAdditionFlag() || this.hasChunk(i, k)) {
                this.getChunk(i, k).addEntity(entity);
            } else {
                if (entity.inChunk) {
                    LOGGER.warn("Entity {} left loaded chunk area", (Object)entity);
                }
                entity.inChunk = false;
            }
        }
        this.getProfiler().pop();
    }

    public void unload(LevelChunk levelChunk) {
        this.blockEntitiesToUnload.addAll(levelChunk.getBlockEntities().values());
        this.chunkSource.getLightEngine().enableLightSources(levelChunk.getPos(), false);
    }

    public void onChunkLoaded(int i, int j) {
        this.tintCaches.forEach((colorResolver, blockTintCache) -> blockTintCache.invalidateForChunk(i, j));
    }

    public void clearTintCaches() {
        this.tintCaches.forEach((colorResolver, blockTintCache) -> blockTintCache.invalidateAll());
    }

    @Override
    public boolean hasChunk(int i, int j) {
        return true;
    }

    public int getEntityCount() {
        return this.entitiesById.size();
    }

    public void addPlayer(int i, AbstractClientPlayer abstractClientPlayer) {
        this.addEntity(i, abstractClientPlayer);
        this.players.add(abstractClientPlayer);
    }

    public void putNonPlayerEntity(int i, Entity entity) {
        this.addEntity(i, entity);
    }

    private void addEntity(int i, Entity entity) {
        this.removeEntity(i);
        this.entitiesById.put(i, entity);
        this.getChunkSource().getChunk(Mth.floor(entity.getX() / 16.0), Mth.floor(entity.getZ() / 16.0), ChunkStatus.FULL, true).addEntity(entity);
    }

    public void removeEntity(int i) {
        Entity entity = (Entity)this.entitiesById.remove(i);
        if (entity != null) {
            entity.remove();
            this.onEntityRemoved(entity);
        }
    }

    private void onEntityRemoved(Entity entity) {
        entity.unRide();
        if (entity.inChunk) {
            this.getChunk(entity.xChunk, entity.zChunk).removeEntity(entity);
        }
        this.players.remove(entity);
    }

    public void reAddEntitiesToChunk(LevelChunk levelChunk) {
        for (Int2ObjectMap.Entry entry : this.entitiesById.int2ObjectEntrySet()) {
            Entity entity = (Entity)entry.getValue();
            int i = Mth.floor(entity.getX() / 16.0);
            int j = Mth.floor(entity.getZ() / 16.0);
            if (i != levelChunk.getPos().x || j != levelChunk.getPos().z) continue;
            levelChunk.addEntity(entity);
        }
    }

    @Override
    @Nullable
    public Entity getEntity(int i) {
        return (Entity)this.entitiesById.get(i);
    }

    public void setKnownState(BlockPos blockPos, BlockState blockState) {
        this.setBlock(blockPos, blockState, 19);
    }

    @Override
    public void disconnect() {
        this.connection.getConnection().disconnect(new TranslatableComponent("multiplayer.status.quitting"));
    }

    public void animateTick(int i, int j, int k) {
        int l = 32;
        Random random = new Random();
        boolean bl = false;
        if (this.minecraft.gameMode.getPlayerMode() == GameType.CREATIVE) {
            for (ItemStack itemStack : this.minecraft.player.getHandSlots()) {
                if (itemStack.getItem() != Blocks.BARRIER.asItem()) continue;
                bl = true;
                break;
            }
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int m = 0; m < 667; ++m) {
            this.doAnimateTick(i, j, k, 16, random, bl, mutableBlockPos);
            this.doAnimateTick(i, j, k, 32, random, bl, mutableBlockPos);
        }
    }

    public void doAnimateTick(int i, int j, int k, int l, Random random, boolean bl, BlockPos.MutableBlockPos mutableBlockPos) {
        int m = i + this.random.nextInt(l) - this.random.nextInt(l);
        int n = j + this.random.nextInt(l) - this.random.nextInt(l);
        int o = k + this.random.nextInt(l) - this.random.nextInt(l);
        mutableBlockPos.set(m, n, o);
        BlockState blockState = this.getBlockState(mutableBlockPos);
        blockState.getBlock().animateTick(blockState, this, mutableBlockPos, random);
        FluidState fluidState = this.getFluidState(mutableBlockPos);
        if (!fluidState.isEmpty()) {
            fluidState.animateTick(this, mutableBlockPos, random);
            ParticleOptions particleOptions = fluidState.getDripParticle();
            if (particleOptions != null && this.random.nextInt(10) == 0) {
                boolean bl2 = blockState.isFaceSturdy(this, mutableBlockPos, Direction.DOWN);
                Vec3i blockPos = mutableBlockPos.below();
                this.trySpawnDripParticles((BlockPos)blockPos, this.getBlockState((BlockPos)blockPos), particleOptions, bl2);
            }
        }
        if (bl && blockState.is(Blocks.BARRIER)) {
            this.addParticle(ParticleTypes.BARRIER, (double)m + 0.5, (double)n + 0.5, (double)o + 0.5, 0.0, 0.0, 0.0);
        }
        if (!blockState.isCollisionShapeFullBlock(this, mutableBlockPos)) {
            this.getBiome(mutableBlockPos).getAmbientParticle().ifPresent(ambientParticleSettings -> {
                if (ambientParticleSettings.canSpawn(this.random)) {
                    this.addParticle(ambientParticleSettings.getOptions(), (double)mutableBlockPos.getX() + this.random.nextDouble(), (double)mutableBlockPos.getY() + this.random.nextDouble(), (double)mutableBlockPos.getZ() + this.random.nextDouble(), 0.0, 0.0, 0.0);
                }
            });
        }
    }

    private void trySpawnDripParticles(BlockPos blockPos, BlockState blockState, ParticleOptions particleOptions, boolean bl) {
        if (!blockState.getFluidState().isEmpty()) {
            return;
        }
        VoxelShape voxelShape = blockState.getCollisionShape(this, blockPos);
        double d = voxelShape.max(Direction.Axis.Y);
        if (d < 1.0) {
            if (bl) {
                this.spawnFluidParticle(blockPos.getX(), blockPos.getX() + 1, blockPos.getZ(), blockPos.getZ() + 1, (double)(blockPos.getY() + 1) - 0.05, particleOptions);
            }
        } else if (!blockState.is(BlockTags.IMPERMEABLE)) {
            double e = voxelShape.min(Direction.Axis.Y);
            if (e > 0.0) {
                this.spawnParticle(blockPos, particleOptions, voxelShape, (double)blockPos.getY() + e - 0.05);
            } else {
                BlockPos blockPos2 = blockPos.below();
                BlockState blockState2 = this.getBlockState(blockPos2);
                VoxelShape voxelShape2 = blockState2.getCollisionShape(this, blockPos2);
                double f = voxelShape2.max(Direction.Axis.Y);
                if (f < 1.0 && blockState2.getFluidState().isEmpty()) {
                    this.spawnParticle(blockPos, particleOptions, voxelShape, (double)blockPos.getY() - 0.05);
                }
            }
        }
    }

    private void spawnParticle(BlockPos blockPos, ParticleOptions particleOptions, VoxelShape voxelShape, double d) {
        this.spawnFluidParticle((double)blockPos.getX() + voxelShape.min(Direction.Axis.X), (double)blockPos.getX() + voxelShape.max(Direction.Axis.X), (double)blockPos.getZ() + voxelShape.min(Direction.Axis.Z), (double)blockPos.getZ() + voxelShape.max(Direction.Axis.Z), d, particleOptions);
    }

    private void spawnFluidParticle(double d, double e, double f, double g, double h, ParticleOptions particleOptions) {
        this.addParticle(particleOptions, Mth.lerp(this.random.nextDouble(), d, e), h, Mth.lerp(this.random.nextDouble(), f, g), 0.0, 0.0, 0.0);
    }

    public void removeAllPendingEntityRemovals() {
        Iterator objectIterator = this.entitiesById.int2ObjectEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Int2ObjectMap.Entry entry = (Int2ObjectMap.Entry)objectIterator.next();
            Entity entity = (Entity)entry.getValue();
            if (!entity.removed) continue;
            objectIterator.remove();
            this.onEntityRemoved(entity);
        }
    }

    @Override
    public CrashReportCategory fillReportDetails(CrashReport crashReport) {
        CrashReportCategory crashReportCategory = super.fillReportDetails(crashReport);
        crashReportCategory.setDetail("Server brand", () -> this.minecraft.player.getServerBrand());
        crashReportCategory.setDetail("Server type", () -> this.minecraft.getSingleplayerServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server");
        return crashReportCategory;
    }

    @Override
    public void playSound(@Nullable Player player, double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h) {
        if (player == this.minecraft.player) {
            this.playLocalSound(d, e, f, soundEvent, soundSource, g, h, false);
        }
    }

    @Override
    public void playSound(@Nullable Player player, Entity entity, SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
        if (player == this.minecraft.player) {
            this.minecraft.getSoundManager().play(new EntityBoundSoundInstance(soundEvent, soundSource, entity));
        }
    }

    public void playLocalSound(BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float g, boolean bl) {
        this.playLocalSound((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, soundEvent, soundSource, f, g, bl);
    }

    @Override
    public void playLocalSound(double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h, boolean bl) {
        double i = this.minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(d, e, f);
        SimpleSoundInstance simpleSoundInstance = new SimpleSoundInstance(soundEvent, soundSource, g, h, d, e, f);
        if (bl && i > 100.0) {
            double j = Math.sqrt(i) / 40.0;
            this.minecraft.getSoundManager().playDelayed(simpleSoundInstance, (int)(j * 20.0));
        } else {
            this.minecraft.getSoundManager().play(simpleSoundInstance);
        }
    }

    @Override
    public void createFireworks(double d, double e, double f, double g, double h, double i, @Nullable CompoundTag compoundTag) {
        this.minecraft.particleEngine.add(new FireworkParticles.Starter(this, d, e, f, g, h, i, this.minecraft.particleEngine, compoundTag));
    }

    @Override
    public void sendPacketToServer(Packet<?> packet) {
        this.connection.send(packet);
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.connection.getRecipeManager();
    }

    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public TickList<Block> getBlockTicks() {
        return EmptyTickList.empty();
    }

    @Override
    public TickList<Fluid> getLiquidTicks() {
        return EmptyTickList.empty();
    }

    @Override
    public ClientChunkCache getChunkSource() {
        return this.chunkSource;
    }

    @Override
    @Nullable
    public MapItemSavedData getMapData(String string) {
        return this.mapData.get(string);
    }

    @Override
    public void setMapData(MapItemSavedData mapItemSavedData) {
        this.mapData.put(mapItemSavedData.getId(), mapItemSavedData);
    }

    @Override
    public int getFreeMapId() {
        return 0;
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public TagManager getTagManager() {
        return this.connection.getTags();
    }

    @Override
    public void sendBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState2, int i) {
        this.levelRenderer.blockChanged(this, blockPos, blockState, blockState2, i);
    }

    @Override
    public void setBlocksDirty(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
        this.levelRenderer.setBlockDirty(blockPos, blockState, blockState2);
    }

    public void setSectionDirtyWithNeighbors(int i, int j, int k) {
        this.levelRenderer.setSectionDirtyWithNeighbors(i, j, k);
    }

    @Override
    public void destroyBlockProgress(int i, BlockPos blockPos, int j) {
        this.levelRenderer.destroyBlockProgress(i, blockPos, j);
    }

    @Override
    public void globalLevelEvent(int i, BlockPos blockPos, int j) {
        this.levelRenderer.globalLevelEvent(i, blockPos, j);
    }

    @Override
    public void levelEvent(@Nullable Player player, int i, BlockPos blockPos, int j) {
        try {
            this.levelRenderer.levelEvent(player, i, blockPos, j);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Playing level event");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Level event being played");
            crashReportCategory.setDetail("Block coordinates", CrashReportCategory.formatLocation(blockPos));
            crashReportCategory.setDetail("Event source", player);
            crashReportCategory.setDetail("Event type", i);
            crashReportCategory.setDetail("Event data", j);
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public void addParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {
        this.levelRenderer.addParticle(particleOptions, particleOptions.getType().getOverrideLimiter(), d, e, f, g, h, i);
    }

    @Override
    public void addParticle(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
        this.levelRenderer.addParticle(particleOptions, particleOptions.getType().getOverrideLimiter() || bl, d, e, f, g, h, i);
    }

    @Override
    public void addAlwaysVisibleParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {
        this.levelRenderer.addParticle(particleOptions, false, true, d, e, f, g, h, i);
    }

    @Override
    public void addAlwaysVisibleParticle(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
        this.levelRenderer.addParticle(particleOptions, particleOptions.getType().getOverrideLimiter() || bl, true, d, e, f, g, h, i);
    }

    public List<AbstractClientPlayer> players() {
        return this.players;
    }

    @Override
    public Biome getUncachedNoiseBiome(int i, int j, int k) {
        return Biomes.PLAINS;
    }

    public float getSkyDarken(float f) {
        float g = this.getTimeOfDay(f);
        float h = 1.0f - (Mth.cos(g * ((float)Math.PI * 2)) * 2.0f + 0.2f);
        h = Mth.clamp(h, 0.0f, 1.0f);
        h = 1.0f - h;
        h = (float)((double)h * (1.0 - (double)(this.getRainLevel(f) * 5.0f) / 16.0));
        h = (float)((double)h * (1.0 - (double)(this.getThunderLevel(f) * 5.0f) / 16.0));
        return h * 0.8f + 0.2f;
    }

    public Vec3 getSkyColor(BlockPos blockPos, float f) {
        float o;
        float n;
        float g = this.getTimeOfDay(f);
        float h = Mth.cos(g * ((float)Math.PI * 2)) * 2.0f + 0.5f;
        h = Mth.clamp(h, 0.0f, 1.0f);
        Biome biome = this.getBiome(blockPos);
        int i = biome.getSkyColor();
        float j = (float)(i >> 16 & 0xFF) / 255.0f;
        float k = (float)(i >> 8 & 0xFF) / 255.0f;
        float l = (float)(i & 0xFF) / 255.0f;
        j *= h;
        k *= h;
        l *= h;
        float m = this.getRainLevel(f);
        if (m > 0.0f) {
            n = (j * 0.3f + k * 0.59f + l * 0.11f) * 0.6f;
            o = 1.0f - m * 0.75f;
            j = j * o + n * (1.0f - o);
            k = k * o + n * (1.0f - o);
            l = l * o + n * (1.0f - o);
        }
        if ((n = this.getThunderLevel(f)) > 0.0f) {
            o = (j * 0.3f + k * 0.59f + l * 0.11f) * 0.2f;
            float p = 1.0f - n * 0.75f;
            j = j * p + o * (1.0f - p);
            k = k * p + o * (1.0f - p);
            l = l * p + o * (1.0f - p);
        }
        if (this.skyFlashTime > 0) {
            o = (float)this.skyFlashTime - f;
            if (o > 1.0f) {
                o = 1.0f;
            }
            j = j * (1.0f - (o *= 0.45f)) + 0.8f * o;
            k = k * (1.0f - o) + 0.8f * o;
            l = l * (1.0f - o) + 1.0f * o;
        }
        return new Vec3(j, k, l);
    }

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

    public float getStarBrightness(float f) {
        float g = this.getTimeOfDay(f);
        float h = 1.0f - (Mth.cos(g * ((float)Math.PI * 2)) * 2.0f + 0.25f);
        h = Mth.clamp(h, 0.0f, 1.0f);
        return h * h * 0.5f;
    }

    public int getSkyFlashTime() {
        return this.skyFlashTime;
    }

    @Override
    public void setSkyFlashTime(int i) {
        this.skyFlashTime = i;
    }

    @Override
    public float getShade(Direction direction, boolean bl) {
        boolean bl2 = this.effects().constantAmbientLight();
        if (!bl) {
            return bl2 ? 0.9f : 1.0f;
        }
        switch (direction) {
            case DOWN: {
                return bl2 ? 0.9f : 0.5f;
            }
            case UP: {
                return bl2 ? 0.9f : 1.0f;
            }
            case NORTH: 
            case SOUTH: {
                return 0.8f;
            }
            case WEST: 
            case EAST: {
                return 0.6f;
            }
        }
        return 1.0f;
    }

    @Override
    public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        BlockTintCache blockTintCache = this.tintCaches.get(colorResolver);
        return blockTintCache.getColor(blockPos, () -> this.calculateBlockTint(blockPos, colorResolver));
    }

    public int calculateBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        int i = Minecraft.getInstance().options.biomeBlendRadius;
        if (i == 0) {
            return colorResolver.getColor(this.getBiome(blockPos), blockPos.getX(), blockPos.getZ());
        }
        int j = (i * 2 + 1) * (i * 2 + 1);
        int k = 0;
        int l = 0;
        int m = 0;
        Cursor3D cursor3D = new Cursor3D(blockPos.getX() - i, blockPos.getY(), blockPos.getZ() - i, blockPos.getX() + i, blockPos.getY(), blockPos.getZ() + i);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        while (cursor3D.advance()) {
            mutableBlockPos.set(cursor3D.nextX(), cursor3D.nextY(), cursor3D.nextZ());
            int n = colorResolver.getColor(this.getBiome(mutableBlockPos), mutableBlockPos.getX(), mutableBlockPos.getZ());
            k += (n & 0xFF0000) >> 16;
            l += (n & 0xFF00) >> 8;
            m += n & 0xFF;
        }
        return (k / j & 0xFF) << 16 | (l / j & 0xFF) << 8 | m / j & 0xFF;
    }

    public BlockPos getSharedSpawnPos() {
        BlockPos blockPos = new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn());
        if (!this.getWorldBorder().isWithinBounds(blockPos)) {
            blockPos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ()));
        }
        return blockPos;
    }

    public void setDefaultSpawnPos(BlockPos blockPos) {
        this.levelData.setSpawn(blockPos);
    }

    public String toString() {
        return "ClientLevel";
    }

    @Override
    public ClientLevelData getLevelData() {
        return this.clientLevelData;
    }

    @Override
    public /* synthetic */ LevelData getLevelData() {
        return this.getLevelData();
    }

    @Override
    public /* synthetic */ ChunkSource getChunkSource() {
        return this.getChunkSource();
    }

    @Environment(value=EnvType.CLIENT)
    public static class ClientLevelData
    implements WritableLevelData {
        private final boolean hardcore;
        private final GameRules gameRules;
        private final boolean isFlat;
        private int xSpawn;
        private int ySpawn;
        private int zSpawn;
        private long gameTime;
        private long dayTime;
        private boolean raining;
        private Difficulty difficulty;
        private boolean difficultyLocked;

        public ClientLevelData(Difficulty difficulty, boolean bl, boolean bl2) {
            this.difficulty = difficulty;
            this.hardcore = bl;
            this.isFlat = bl2;
            this.gameRules = new GameRules();
        }

        @Override
        public int getXSpawn() {
            return this.xSpawn;
        }

        @Override
        public int getYSpawn() {
            return this.ySpawn;
        }

        @Override
        public int getZSpawn() {
            return this.zSpawn;
        }

        @Override
        public long getGameTime() {
            return this.gameTime;
        }

        @Override
        public long getDayTime() {
            return this.dayTime;
        }

        @Override
        public void setXSpawn(int i) {
            this.xSpawn = i;
        }

        @Override
        public void setYSpawn(int i) {
            this.ySpawn = i;
        }

        @Override
        public void setZSpawn(int i) {
            this.zSpawn = i;
        }

        public void setGameTime(long l) {
            this.gameTime = l;
        }

        public void setDayTime(long l) {
            this.dayTime = l;
        }

        @Override
        public void setSpawn(BlockPos blockPos) {
            this.xSpawn = blockPos.getX();
            this.ySpawn = blockPos.getY();
            this.zSpawn = blockPos.getZ();
        }

        @Override
        public boolean isThundering() {
            return false;
        }

        @Override
        public boolean isRaining() {
            return this.raining;
        }

        @Override
        public void setRaining(boolean bl) {
            this.raining = bl;
        }

        @Override
        public boolean isHardcore() {
            return this.hardcore;
        }

        @Override
        public GameRules getGameRules() {
            return this.gameRules;
        }

        @Override
        public Difficulty getDifficulty() {
            return this.difficulty;
        }

        @Override
        public boolean isDifficultyLocked() {
            return this.difficultyLocked;
        }

        @Override
        public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
            WritableLevelData.super.fillCrashReportCategory(crashReportCategory);
        }

        public void setDifficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
        }

        public void setDifficultyLocked(boolean bl) {
            this.difficultyLocked = bl;
        }

        public double getHorizonHeight() {
            if (this.isFlat) {
                return 0.0;
            }
            return 63.0;
        }

        public double getClearColorScale() {
            if (this.isFlat) {
                return 1.0;
            }
            return 0.03125;
        }
    }
}

