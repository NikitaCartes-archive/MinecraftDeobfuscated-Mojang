package net.minecraft.world.level.entity;

import javax.annotation.Nullable;

public interface EntityTypeTest<B, T extends B> {
	static <B, T extends B> EntityTypeTest<B, T> forClass(Class<T> class_) {
		return new EntityTypeTest<B, T>() {
			@Nullable
			@Override
			public T tryCast(B object) {
				return (T)(class_.isInstance(object) ? object : null);
			}

			@Override
			public Class<? extends B> getBaseClass() {
				return class_;
			}
		};
	}

	static <B, T extends B> EntityTypeTest<B, T> forExactClass(Class<T> class_) {
		return new EntityTypeTest<B, T>() {
			@Nullable
			@Override
			public T tryCast(B object) {
				return (T)(class_.equals(object.getClass()) ? object : null);
			}

			@Override
			public Class<? extends B> getBaseClass() {
				return class_;
			}
		};
	}

	@Nullable
	T tryCast(B object);

	Class<? extends B> getBaseClass();
}
