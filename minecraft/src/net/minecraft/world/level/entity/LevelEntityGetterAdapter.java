package net.minecraft.world.level.entity;

import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
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
