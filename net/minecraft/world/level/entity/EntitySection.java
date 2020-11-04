/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.entity;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.Visibility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntitySection<T> {
    protected static final Logger LOGGER = LogManager.getLogger();
    private final ClassInstanceMultiMap<T> storage;
    private Visibility chunkStatus;

    public EntitySection(Class<T> class_, Visibility visibility) {
        this.chunkStatus = visibility;
        this.storage = new ClassInstanceMultiMap<T>(class_);
    }

    public void add(T object) {
        this.storage.add(object);
    }

    public boolean remove(T object) {
        return this.storage.remove(object);
    }

    public void getEntities(Predicate<? super T> predicate, Consumer<T> consumer) {
        for (T object : this.storage) {
            if (!predicate.test(object)) continue;
            consumer.accept(object);
        }
    }

    public <U extends T> void getEntities(EntityTypeTest<T, U> entityTypeTest, Predicate<? super U> predicate, Consumer<? super U> consumer) {
        for (T object : this.storage.find(entityTypeTest.getBaseClass())) {
            U object2 = entityTypeTest.tryCast(object);
            if (object2 == null || !predicate.test(object2)) continue;
            consumer.accept(object2);
        }
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

    public int size() {
        return this.storage.size();
    }
}

