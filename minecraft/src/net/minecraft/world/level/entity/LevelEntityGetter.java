package net.minecraft.world.level.entity;

import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.world.phys.AABB;

public interface LevelEntityGetter<T extends EntityAccess> {
	@Nullable
	T get(int i);

	@Nullable
	T get(UUID uUID);

	Iterable<T> getAll();

	<U extends T> void get(EntityTypeTest<T, U> entityTypeTest, Consumer<U> consumer);

	void get(AABB aABB, Consumer<T> consumer);

	<U extends T> void get(EntityTypeTest<T, U> entityTypeTest, AABB aABB, Consumer<U> consumer);
}
