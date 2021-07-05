/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraft.world.level.entity.Visibility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PersistentEntitySectionManager<T extends EntityAccess>
implements AutoCloseable {
    static final Logger LOGGER = LogManager.getLogger();
    final Set<UUID> knownUuids = Sets.newHashSet();
    final LevelCallback<T> callbacks;
    private final EntityPersistentStorage<T> permanentStorage;
    private final EntityLookup<T> visibleEntityStorage;
    final EntitySectionStorage<T> sectionStorage;
    private final LevelEntityGetter<T> entityGetter;
    private final Long2ObjectMap<Visibility> chunkVisibility = new Long2ObjectOpenHashMap<Visibility>();
    private final Long2ObjectMap<ChunkLoadStatus> chunkLoadStatuses = new Long2ObjectOpenHashMap<ChunkLoadStatus>();
    private final LongSet chunksToUnload = new LongOpenHashSet();
    private final Queue<ChunkEntities<T>> loadingInbox = Queues.newConcurrentLinkedQueue();

    public PersistentEntitySectionManager(Class<T> class_, LevelCallback<T> levelCallback, EntityPersistentStorage<T> entityPersistentStorage) {
        this.visibleEntityStorage = new EntityLookup();
        this.sectionStorage = new EntitySectionStorage<T>(class_, this.chunkVisibility);
        this.chunkVisibility.defaultReturnValue(Visibility.HIDDEN);
        this.chunkLoadStatuses.defaultReturnValue(ChunkLoadStatus.FRESH);
        this.callbacks = levelCallback;
        this.permanentStorage = entityPersistentStorage;
        this.entityGetter = new LevelEntityGetterAdapter<T>(this.visibleEntityStorage, this.sectionStorage);
    }

    void removeSectionIfEmpty(long l, EntitySection<T> entitySection) {
        if (entitySection.isEmpty()) {
            this.sectionStorage.remove(l);
        }
    }

    private boolean addEntityUuid(T entityAccess) {
        if (!this.knownUuids.add(entityAccess.getUUID())) {
            LOGGER.warn("UUID of added entity already exists: {}", (Object)entityAccess);
            return false;
        }
        return true;
    }

    public boolean addNewEntity(T entityAccess) {
        return this.addEntity(entityAccess, false);
    }

    private boolean addEntity(T entityAccess, boolean bl) {
        Visibility visibility;
        if (!this.addEntityUuid(entityAccess)) {
            return false;
        }
        long l = SectionPos.asLong(entityAccess.blockPosition());
        EntitySection<T> entitySection = this.sectionStorage.getOrCreateSection(l);
        entitySection.add(entityAccess);
        entityAccess.setLevelCallback(new Callback(this, entityAccess, l, entitySection));
        if (!bl) {
            this.callbacks.onCreated(entityAccess);
        }
        if ((visibility = PersistentEntitySectionManager.getEffectiveStatus(entityAccess, entitySection.getStatus())).isAccessible()) {
            this.startTracking(entityAccess);
        }
        if (visibility.isTicking()) {
            this.startTicking(entityAccess);
        }
        return true;
    }

    static <T extends EntityAccess> Visibility getEffectiveStatus(T entityAccess, Visibility visibility) {
        return entityAccess.isAlwaysTicking() ? Visibility.TICKING : visibility;
    }

    public void addLegacyChunkEntities(Stream<T> stream) {
        stream.forEach(entityAccess -> this.addEntity(entityAccess, true));
    }

    public void addWorldGenChunkEntities(Stream<T> stream) {
        stream.forEach(entityAccess -> this.addEntity(entityAccess, false));
    }

    void startTicking(T entityAccess) {
        this.callbacks.onTickingStart(entityAccess);
    }

    void stopTicking(T entityAccess) {
        this.callbacks.onTickingEnd(entityAccess);
    }

    void startTracking(T entityAccess) {
        this.visibleEntityStorage.add(entityAccess);
        this.callbacks.onTrackingStart(entityAccess);
    }

    void stopTracking(T entityAccess) {
        this.callbacks.onTrackingEnd(entityAccess);
        this.visibleEntityStorage.remove(entityAccess);
    }

    public void updateChunkStatus(ChunkPos chunkPos, ChunkHolder.FullChunkStatus fullChunkStatus) {
        Visibility visibility = Visibility.fromFullChunkStatus(fullChunkStatus);
        this.updateChunkStatus(chunkPos, visibility);
    }

    public void updateChunkStatus(ChunkPos chunkPos, Visibility visibility) {
        long l = chunkPos.toLong();
        if (visibility == Visibility.HIDDEN) {
            this.chunkVisibility.remove(l);
            this.chunksToUnload.add(l);
        } else {
            this.chunkVisibility.put(l, visibility);
            this.chunksToUnload.remove(l);
            this.ensureChunkQueuedForLoad(l);
        }
        this.sectionStorage.getExistingSectionsInChunk(l).forEach(entitySection -> {
            Visibility visibility2 = entitySection.updateChunkStatus(visibility);
            boolean bl = visibility2.isAccessible();
            boolean bl2 = visibility.isAccessible();
            boolean bl3 = visibility2.isTicking();
            boolean bl4 = visibility.isTicking();
            if (bl3 && !bl4) {
                entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this::stopTicking);
            }
            if (bl && !bl2) {
                entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this::stopTracking);
            } else if (!bl && bl2) {
                entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this::startTracking);
            }
            if (!bl3 && bl4) {
                entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this::startTicking);
            }
        });
    }

    private void ensureChunkQueuedForLoad(long l) {
        ChunkLoadStatus chunkLoadStatus = (ChunkLoadStatus)((Object)this.chunkLoadStatuses.get(l));
        if (chunkLoadStatus == ChunkLoadStatus.FRESH) {
            this.requestChunkLoad(l);
        }
    }

    private boolean storeChunkSections(long l, Consumer<T> consumer) {
        ChunkLoadStatus chunkLoadStatus = (ChunkLoadStatus)((Object)this.chunkLoadStatuses.get(l));
        if (chunkLoadStatus == ChunkLoadStatus.PENDING) {
            return false;
        }
        List<T> list = this.sectionStorage.getExistingSectionsInChunk(l).flatMap(entitySection -> entitySection.getEntities().filter(EntityAccess::shouldBeSaved)).collect(Collectors.toList());
        if (list.isEmpty()) {
            if (chunkLoadStatus == ChunkLoadStatus.LOADED) {
                this.permanentStorage.storeEntities(new ChunkEntities(new ChunkPos(l), ImmutableList.of()));
            }
            return true;
        }
        if (chunkLoadStatus == ChunkLoadStatus.FRESH) {
            this.requestChunkLoad(l);
            return false;
        }
        this.permanentStorage.storeEntities(new ChunkEntities(new ChunkPos(l), list));
        list.forEach(consumer);
        return true;
    }

    private void requestChunkLoad(long l) {
        this.chunkLoadStatuses.put(l, ChunkLoadStatus.PENDING);
        ChunkPos chunkPos = new ChunkPos(l);
        ((CompletableFuture)this.permanentStorage.loadEntities(chunkPos).thenAccept(this.loadingInbox::add)).exceptionally(throwable -> {
            LOGGER.error("Failed to read chunk {}", (Object)chunkPos, throwable);
            return null;
        });
    }

    private boolean processChunkUnload(long l) {
        boolean bl = this.storeChunkSections(l, entityAccess -> entityAccess.getPassengersAndSelf().forEach(this::unloadEntity));
        if (!bl) {
            return false;
        }
        this.chunkLoadStatuses.remove(l);
        return true;
    }

    private void unloadEntity(EntityAccess entityAccess) {
        entityAccess.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        entityAccess.setLevelCallback(EntityInLevelCallback.NULL);
    }

    private void processUnloads() {
        this.chunksToUnload.removeIf(l -> {
            if (this.chunkVisibility.get(l) != Visibility.HIDDEN) {
                return true;
            }
            return this.processChunkUnload(l);
        });
    }

    private void processPendingLoads() {
        ChunkEntities<T> chunkEntities;
        while ((chunkEntities = this.loadingInbox.poll()) != null) {
            chunkEntities.getEntities().forEach(entityAccess -> this.addEntity(entityAccess, true));
            this.chunkLoadStatuses.put(chunkEntities.getPos().toLong(), ChunkLoadStatus.LOADED);
        }
    }

    public void tick() {
        this.processPendingLoads();
        this.processUnloads();
    }

    private LongSet getAllChunksToSave() {
        LongSet longSet = this.sectionStorage.getAllChunksWithExistingSections();
        for (Long2ObjectMap.Entry entry : Long2ObjectMaps.fastIterable(this.chunkLoadStatuses)) {
            if (entry.getValue() != ChunkLoadStatus.LOADED) continue;
            longSet.add(entry.getLongKey());
        }
        return longSet;
    }

    public void autoSave() {
        this.getAllChunksToSave().forEach(l -> {
            boolean bl;
            boolean bl2 = bl = this.chunkVisibility.get(l) == Visibility.HIDDEN;
            if (bl) {
                this.processChunkUnload(l);
            } else {
                this.storeChunkSections(l, entityAccess -> {});
            }
        });
    }

    public void saveAll() {
        LongSet longSet = this.getAllChunksToSave();
        while (!longSet.isEmpty()) {
            this.permanentStorage.flush(false);
            this.processPendingLoads();
            longSet.removeIf(l -> {
                boolean bl = this.chunkVisibility.get(l) == Visibility.HIDDEN;
                return bl ? this.processChunkUnload(l) : this.storeChunkSections(l, entityAccess -> {});
            });
        }
        this.permanentStorage.flush(true);
    }

    @Override
    public void close() throws IOException {
        this.saveAll();
        this.permanentStorage.close();
    }

    public boolean isLoaded(UUID uUID) {
        return this.knownUuids.contains(uUID);
    }

    public LevelEntityGetter<T> getEntityGetter() {
        return this.entityGetter;
    }

    public boolean isPositionTicking(BlockPos blockPos) {
        return ((Visibility)((Object)this.chunkVisibility.get(ChunkPos.asLong(blockPos)))).isTicking();
    }

    public boolean isPositionTicking(ChunkPos chunkPos) {
        return ((Visibility)((Object)this.chunkVisibility.get(chunkPos.toLong()))).isTicking();
    }

    public boolean areEntitiesLoaded(long l) {
        return this.chunkLoadStatuses.get(l) == ChunkLoadStatus.LOADED;
    }

    public void dumpSections(Writer writer) throws IOException {
        CsvOutput csvOutput = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("visibility").addColumn("load_status").addColumn("entity_count").build(writer);
        this.sectionStorage.getAllChunksWithExistingSections().forEach(l2 -> {
            ChunkLoadStatus chunkLoadStatus = (ChunkLoadStatus)((Object)((Object)this.chunkLoadStatuses.get(l2)));
            this.sectionStorage.getExistingSectionPositionsInChunk(l2).forEach(l -> {
                EntitySection<T> entitySection = this.sectionStorage.getSection(l);
                if (entitySection != null) {
                    try {
                        csvOutput.writeRow(new Object[]{SectionPos.x(l), SectionPos.y(l), SectionPos.z(l), entitySection.getStatus(), chunkLoadStatus, entitySection.size()});
                    } catch (IOException iOException) {
                        throw new UncheckedIOException(iOException);
                    }
                }
            });
        });
    }

    @VisibleForDebug
    public String gatherStats() {
        return this.knownUuids.size() + "," + this.visibleEntityStorage.count() + "," + this.sectionStorage.count() + "," + this.chunkLoadStatuses.size() + "," + this.chunkVisibility.size() + "," + this.loadingInbox.size() + "," + this.chunksToUnload.size();
    }

    static enum ChunkLoadStatus {
        FRESH,
        PENDING,
        LOADED;

    }

    static class Callback
    implements EntityInLevelCallback {
        private final T entity;
        private long currentSectionKey;
        private EntitySection<T> currentSection;
        final /* synthetic */ PersistentEntitySectionManager field_27271;

        /*
         * WARNING - Possible parameter corruption
         * WARNING - void declaration
         */
        Callback(T entityAccess, long l, EntitySection<T> entitySection) {
            void var3_3;
            this.field_27271 = persistentEntitySectionManager;
            this.entity = entityAccess;
            this.currentSectionKey = var3_3;
            this.currentSection = entitySection;
        }

        @Override
        public void onMove() {
            BlockPos blockPos = this.entity.blockPosition();
            long l = SectionPos.asLong(blockPos);
            if (l != this.currentSectionKey) {
                Visibility visibility = this.currentSection.getStatus();
                if (!this.currentSection.remove(this.entity)) {
                    LOGGER.warn("Entity {} wasn't found in section {} (moving to {})", this.entity, (Object)SectionPos.of(this.currentSectionKey), (Object)l);
                }
                this.field_27271.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
                EntitySection entitySection = this.field_27271.sectionStorage.getOrCreateSection(l);
                entitySection.add(this.entity);
                this.currentSection = entitySection;
                this.currentSectionKey = l;
                this.updateStatus(visibility, entitySection.getStatus());
            }
        }

        private void updateStatus(Visibility visibility, Visibility visibility2) {
            Visibility visibility4;
            Visibility visibility3 = PersistentEntitySectionManager.getEffectiveStatus(this.entity, visibility);
            if (visibility3 == (visibility4 = PersistentEntitySectionManager.getEffectiveStatus(this.entity, visibility2))) {
                return;
            }
            boolean bl = visibility3.isAccessible();
            boolean bl2 = visibility4.isAccessible();
            if (bl && !bl2) {
                this.field_27271.stopTracking(this.entity);
            } else if (!bl && bl2) {
                this.field_27271.startTracking(this.entity);
            }
            boolean bl3 = visibility3.isTicking();
            boolean bl4 = visibility4.isTicking();
            if (bl3 && !bl4) {
                this.field_27271.stopTicking(this.entity);
            } else if (!bl3 && bl4) {
                this.field_27271.startTicking(this.entity);
            }
        }

        @Override
        public void onRemove(Entity.RemovalReason removalReason) {
            Visibility visibility;
            if (!this.currentSection.remove(this.entity)) {
                LOGGER.warn("Entity {} wasn't found in section {} (destroying due to {})", this.entity, (Object)SectionPos.of(this.currentSectionKey), (Object)removalReason);
            }
            if ((visibility = PersistentEntitySectionManager.getEffectiveStatus(this.entity, this.currentSection.getStatus())).isTicking()) {
                this.field_27271.stopTicking(this.entity);
            }
            if (visibility.isAccessible()) {
                this.field_27271.stopTracking(this.entity);
            }
            if (removalReason.shouldDestroy()) {
                this.field_27271.callbacks.onDestroyed(this.entity);
            }
            this.field_27271.knownUuids.remove(this.entity.getUUID());
            this.entity.setLevelCallback(NULL);
            this.field_27271.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
        }
    }
}

