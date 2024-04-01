package net.minecraft.world.level.entity;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.phys.AABB;

public interface LevelEntityGetter<T extends EntityAccess> {
	LevelEntityGetter<?> EMPTY = new LevelEntityGetter<EntityAccess>() {
		@Nullable
		@Override
		public EntityAccess get(int i) {
			return null;
		}

		@Nullable
		@Override
		public EntityAccess get(UUID uUID) {
			return null;
		}

		@Override
		public Iterable<EntityAccess> getAll() {
			return List.of();
		}

		@Override
		public <U extends EntityAccess> void get(EntityTypeTest<EntityAccess, U> entityTypeTest, AbortableIterationConsumer<U> abortableIterationConsumer) {
		}

		@Override
		public void get(AABB aABB, Consumer<EntityAccess> consumer) {
		}

		@Override
		public <U extends EntityAccess> void get(EntityTypeTest<EntityAccess, U> entityTypeTest, AABB aABB, AbortableIterationConsumer<U> abortableIterationConsumer) {
		}
	};

	static <T extends EntityAccess> LevelEntityGetter<T> empty() {
		return (LevelEntityGetter<T>)EMPTY;
	}

	@Nullable
	T get(int i);

	@Nullable
	T get(UUID uUID);

	Iterable<T> getAll();

	<U extends T> void get(EntityTypeTest<T, U> entityTypeTest, AbortableIterationConsumer<U> abortableIterationConsumer);

	void get(AABB aABB, Consumer<T> consumer);

	<U extends T> void get(EntityTypeTest<T, U> entityTypeTest, AABB aABB, AbortableIterationConsumer<U> abortableIterationConsumer);
}
