/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.entity;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.stream.Stream;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class EntitySection<T extends EntityAccess> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ClassInstanceMultiMap<T> storage;
    private Visibility chunkStatus;

    public EntitySection(Class<T> class_, Visibility visibility) {
        this.chunkStatus = visibility;
        this.storage = new ClassInstanceMultiMap<T>(class_);
    }

    public void add(T entityAccess) {
        this.storage.add(entityAccess);
    }

    public boolean remove(T entityAccess) {
        return this.storage.remove(entityAccess);
    }

    public AbortableIterationConsumer.Continuation getEntities(AABB aABB, AbortableIterationConsumer<T> abortableIterationConsumer) {
        for (EntityAccess entityAccess : this.storage) {
            if (!entityAccess.getBoundingBox().intersects(aABB) || !abortableIterationConsumer.accept(entityAccess).shouldAbort()) continue;
            return AbortableIterationConsumer.Continuation.ABORT;
        }
        return AbortableIterationConsumer.Continuation.CONTINUE;
    }

    public <U extends T> AbortableIterationConsumer.Continuation getEntities(EntityTypeTest<T, U> entityTypeTest, AABB aABB, AbortableIterationConsumer<? super U> abortableIterationConsumer) {
        Collection<T> collection = this.storage.find(entityTypeTest.getBaseClass());
        if (collection.isEmpty()) {
            return AbortableIterationConsumer.Continuation.CONTINUE;
        }
        for (EntityAccess entityAccess : collection) {
            EntityAccess entityAccess2 = (EntityAccess)entityTypeTest.tryCast(entityAccess);
            if (entityAccess2 == null || !entityAccess.getBoundingBox().intersects(aABB) || !abortableIterationConsumer.accept(entityAccess2).shouldAbort()) continue;
            return AbortableIterationConsumer.Continuation.ABORT;
        }
        return AbortableIterationConsumer.Continuation.CONTINUE;
    }

    public boolean isEmpty() {
        return this.storage.isEmpty();
    }

    public Stream<T> getEntities() {
        return this.storage.stream();
    }

    public Visibility getStatus() {
        return this.chunkStatus;
    }

    public Visibility updateChunkStatus(Visibility visibility) {
        Visibility visibility2 = this.chunkStatus;
        this.chunkStatus = visibility;
        return visibility2;
    }

    @VisibleForDebug
    public int size() {
        return this.storage.size();
    }
}

