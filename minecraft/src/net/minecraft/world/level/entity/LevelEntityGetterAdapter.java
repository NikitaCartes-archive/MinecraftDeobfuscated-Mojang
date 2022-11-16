package net.minecraft.world.level.entity;

import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.phys.AABB;

public class LevelEntityGetterAdapter<T extends EntityAccess> implements LevelEntityGetter<T> {
	private final EntityLookup<T> visibleEntities;
	private final EntitySectionStorage<T> sectionStorage;

	public LevelEntityGetterAdapter(EntityLookup<T> entityLookup, EntitySectionStorage<T> entitySectionStorage) {
		this.visibleEntities = entityLookup;
		this.sectionStorage = entitySectionStorage;
	}

	@Nullable
	@Override
	public T get(int i) {
		return this.visibleEntities.getEntity(i);
	}

	@Nullable
	@Override
	public T get(UUID uUID) {
		return this.visibleEntities.getEntity(uUID);
	}

	@Override
	public Iterable<T> getAll() {
		return this.visibleEntities.getAllEntities();
	}

	@Override
	public <U extends T> void get(EntityTypeTest<T, U> entityTypeTest, AbortableIterationConsumer<U> abortableIterationConsumer) {
		this.visibleEntities.getEntities(entityTypeTest, abortableIterationConsumer);
	}

	@Override
	public void get(AABB aABB, Consumer<T> consumer) {
		this.sectionStorage.getEntities(aABB, AbortableIterationConsumer.forConsumer(consumer));
	}

	@Override
	public <U extends T> void get(EntityTypeTest<T, U> entityTypeTest, AABB aABB, AbortableIterationConsumer<U> abortableIterationConsumer) {
		this.sectionStorage.getEntities(entityTypeTest, aABB, abortableIterationConsumer);
	}
}
