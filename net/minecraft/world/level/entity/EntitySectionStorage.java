/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.SectionPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class EntitySectionStorage<T extends EntityAccess> {
    private final Class<T> entityClass;
    private final Long2ObjectFunction<Visibility> intialSectionVisibility;
    private final Long2ObjectMap<EntitySection<T>> sections = new Long2ObjectOpenHashMap<EntitySection<T>>();
    private final LongSortedSet sectionIds = new LongAVLTreeSet();

    public EntitySectionStorage(Class<T> class_, Long2ObjectFunction<Visibility> long2ObjectFunction) {
        this.entityClass = class_;
        this.intialSectionVisibility = long2ObjectFunction;
    }

    public void forEachAccessibleNonEmptySection(AABB aABB, AbortableIterationConsumer<EntitySection<T>> abortableIterationConsumer) {
        int i = 2;
        int j = SectionPos.posToSectionCoord(aABB.minX - 2.0);
        int k = SectionPos.posToSectionCoord(aABB.minY - 4.0);
        int l = SectionPos.posToSectionCoord(aABB.minZ - 2.0);
        int m = SectionPos.posToSectionCoord(aABB.maxX + 2.0);
        int n = SectionPos.posToSectionCoord(aABB.maxY + 0.0);
        int o = SectionPos.posToSectionCoord(aABB.maxZ + 2.0);
        for (int p = j; p <= m; ++p) {
            long q = SectionPos.asLong(p, 0, 0);
            long r = SectionPos.asLong(p, -1, -1);
            LongBidirectionalIterator longIterator = this.sectionIds.subSet(q, r + 1L).iterator();
            while (longIterator.hasNext()) {
                EntitySection entitySection;
                long s = longIterator.nextLong();
                int t = SectionPos.y(s);
                int u = SectionPos.z(s);
                if (t < k || t > n || u < l || u > o || (entitySection = (EntitySection)this.sections.get(s)) == null || entitySection.isEmpty() || !entitySection.getStatus().isAccessible() || !abortableIterationConsumer.accept(entitySection).shouldAbort()) continue;
                return;
            }
        }
    }

    public LongStream getExistingSectionPositionsInChunk(long l) {
        int j;
        int i = ChunkPos.getX(l);
        LongSortedSet longSortedSet = this.getChunkSections(i, j = ChunkPos.getZ(l));
        if (longSortedSet.isEmpty()) {
            return LongStream.empty();
        }
        LongBidirectionalIterator ofLong = longSortedSet.iterator();
        return StreamSupport.longStream(Spliterators.spliteratorUnknownSize(ofLong, 1301), false);
    }

    private LongSortedSet getChunkSections(int i, int j) {
        long l = SectionPos.asLong(i, 0, j);
        long m = SectionPos.asLong(i, -1, j);
        return this.sectionIds.subSet(l, m + 1L);
    }

    public Stream<EntitySection<T>> getExistingSectionsInChunk(long l) {
        return this.getExistingSectionPositionsInChunk(l).mapToObj(this.sections::get).filter(Objects::nonNull);
    }

    private static long getChunkKeyFromSectionKey(long l) {
        return ChunkPos.asLong(SectionPos.x(l), SectionPos.z(l));
    }

    public EntitySection<T> getOrCreateSection(long l) {
        return this.sections.computeIfAbsent(l, this::createSection);
    }

    @Nullable
    public EntitySection<T> getSection(long l) {
        return (EntitySection)this.sections.get(l);
    }

    private EntitySection<T> createSection(long l) {
        long m = EntitySectionStorage.getChunkKeyFromSectionKey(l);
        Visibility visibility = this.intialSectionVisibility.get(m);
        this.sectionIds.add(l);
        return new EntitySection<T>(this.entityClass, visibility);
    }

    public LongSet getAllChunksWithExistingSections() {
        LongOpenHashSet longSet = new LongOpenHashSet();
        this.sections.keySet().forEach(l -> longSet.add(EntitySectionStorage.getChunkKeyFromSectionKey(l)));
        return longSet;
    }

    public void getEntities(AABB aABB, AbortableIterationConsumer<T> abortableIterationConsumer) {
        this.forEachAccessibleNonEmptySection(aABB, entitySection -> entitySection.getEntities(aABB, abortableIterationConsumer));
    }

    public <U extends T> void getEntities(EntityTypeTest<T, U> entityTypeTest, AABB aABB, AbortableIterationConsumer<U> abortableIterationConsumer) {
        this.forEachAccessibleNonEmptySection(aABB, entitySection -> entitySection.getEntities(entityTypeTest, aABB, abortableIterationConsumer));
    }

    public void remove(long l) {
        this.sections.remove(l);
        this.sectionIds.remove(l);
    }

    @VisibleForDebug
    public int count() {
        return this.sectionIds.size();
    }
}

