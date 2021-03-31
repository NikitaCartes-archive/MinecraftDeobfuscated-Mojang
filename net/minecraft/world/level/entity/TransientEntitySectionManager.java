/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.entity;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraft.world.level.entity.Visibility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransientEntitySectionManager<T extends EntityAccess> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final LevelCallback<T> callbacks;
    private final EntityLookup<T> entityStorage;
    private final EntitySectionStorage<T> sectionStorage;
    private final LongSet tickingChunks = new LongOpenHashSet();
    private final LevelEntityGetter<T> entityGetter;

    public TransientEntitySectionManager(Class<T> class_, LevelCallback<T> levelCallback) {
        this.entityStorage = new EntityLookup();
        this.sectionStorage = new EntitySectionStorage<T>(class_, l -> this.tickingChunks.contains(l) ? Visibility.TICKING : Visibility.TRACKED);
        this.callbacks = levelCallback;
        this.entityGetter = new LevelEntityGetterAdapter<T>(this.entityStorage, this.sectionStorage);
    }

    public void startTicking(ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        this.tickingChunks.add(l);
        this.sectionStorage.getExistingSectionsInChunk(l).forEach(entitySection -> {
            Visibility visibility = entitySection.updateChunkStatus(Visibility.TICKING);
            if (!visibility.isTicking()) {
                entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this.callbacks::onTickingStart);
            }
        });
    }

    public void stopTicking(ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        this.tickingChunks.remove(l);
        this.sectionStorage.getExistingSectionsInChunk(l).forEach(entitySection -> {
            Visibility visibility = entitySection.updateChunkStatus(Visibility.TRACKED);
            if (visibility.isTicking()) {
                entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this.callbacks::onTickingEnd);
            }
        });
    }

    public LevelEntityGetter<T> getEntityGetter() {
        return this.entityGetter;
    }

    public void addEntity(T entityAccess) {
        this.entityStorage.add(entityAccess);
        long l = SectionPos.asLong(entityAccess.blockPosition());
        EntitySection<T> entitySection = this.sectionStorage.getOrCreateSection(l);
        entitySection.add(entityAccess);
        entityAccess.setLevelCallback(new Callback(this, (EntityAccess)entityAccess, l, entitySection));
        this.callbacks.onCreated(entityAccess);
        this.callbacks.onTrackingStart(entityAccess);
        if (entityAccess.isAlwaysTicking() || entitySection.getStatus().isTicking()) {
            this.callbacks.onTickingStart(entityAccess);
        }
    }

    @VisibleForDebug
    public int count() {
        return this.entityStorage.count();
    }

    private void removeSectionIfEmpty(long l, EntitySection<T> entitySection) {
        if (entitySection.isEmpty()) {
            this.sectionStorage.remove(l);
        }
    }

    @VisibleForDebug
    public String gatherStats() {
        return this.entityStorage.count() + "," + this.sectionStorage.count() + "," + this.tickingChunks.size();
    }

    static class Callback
    implements EntityInLevelCallback {
        private final T entity;
        private long currentSectionKey;
        private EntitySection<T> currentSection;
        final /* synthetic */ TransientEntitySectionManager field_27285;

        /*
         * WARNING - Possible parameter corruption
         * WARNING - void declaration
         */
        private Callback(T entityAccess, long l, EntitySection<T> entitySection) {
            void var3_3;
            this.field_27285 = transientEntitySectionManager;
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
                this.field_27285.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
                EntitySection entitySection = this.field_27285.sectionStorage.getOrCreateSection(l);
                entitySection.add(this.entity);
                this.currentSection = entitySection;
                this.currentSectionKey = l;
                if (!this.entity.isAlwaysTicking()) {
                    boolean bl = visibility.isTicking();
                    boolean bl2 = entitySection.getStatus().isTicking();
                    if (bl && !bl2) {
                        this.field_27285.callbacks.onTickingEnd(this.entity);
                    } else if (!bl && bl2) {
                        this.field_27285.callbacks.onTickingStart(this.entity);
                    }
                }
            }
        }

        @Override
        public void onRemove(Entity.RemovalReason removalReason) {
            Visibility visibility;
            if (!this.currentSection.remove(this.entity)) {
                LOGGER.warn("Entity {} wasn't found in section {} (destroying due to {})", this.entity, (Object)SectionPos.of(this.currentSectionKey), (Object)removalReason);
            }
            if ((visibility = this.currentSection.getStatus()).isTicking() || this.entity.isAlwaysTicking()) {
                this.field_27285.callbacks.onTickingEnd(this.entity);
            }
            this.field_27285.callbacks.onTrackingEnd(this.entity);
            this.field_27285.callbacks.onDestroyed(this.entity);
            this.field_27285.entityStorage.remove(this.entity);
            this.entity.setLevelCallback(NULL);
            this.field_27285.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
        }
    }
}

