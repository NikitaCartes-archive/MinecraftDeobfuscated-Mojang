/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.entity;

import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class LevelEntityGetterAdapter<T extends EntityAccess>
implements LevelEntityGetter<T> {
    private final EntityLookup<T> visibleEntities;
    private final EntitySectionStorage<T> sectionStorage;

    public LevelEntityGetterAdapter(EntityLookup<T> entityLookup, EntitySectionStorage<T> entitySectionStorage) {
        this.visibleEntities = entityLookup;
        this.sectionStorage = entitySectionStorage;
    }

    @Override
    @Nullable
    public T get(int i) {
        return this.visibleEntities.getEntity(i);
    }

    @Override
    @Nullable
    public T get(UUID uUID) {
        return this.visibleEntities.getEntity(uUID);
    }

    @Override
    public Iterable<T> getAll() {
        return this.visibleEntities.getAllEntities();
    }

    @Override
    public <U extends T> void get(EntityTypeTest<T, U> entityTypeTest, Consumer<U> consumer) {
        this.visibleEntities.getEntities(entityTypeTest, consumer);
    }

    @Override
    public void get(AABB aABB, Consumer<T> consumer) {
        this.sectionStorage.getEntities(aABB, consumer);
    }

    @Override
    public <U extends T> void get(EntityTypeTest<T, U> entityTypeTest, AABB aABB, Consumer<U> consumer) {
        this.sectionStorage.getEntities(entityTypeTest, aABB, consumer);
    }
}

