package net.minecraft.world.level.entity;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.stream.Stream;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class EntitySection<T extends EntityAccess> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ClassInstanceMultiMap<T> storage;
	private Visibility chunkStatus;

	public EntitySection(Class<T> class_, Visibility visibility) {
		this.chunkStatus = visibility;
		this.storage = new ClassInstanceMultiMap<>(class_);
	}

	public void add(T entityAccess) {
		this.storage.add(entityAccess);
	}

	public boolean remove(T entityAccess) {
		return this.storage.remove(entityAccess);
	}

	public AbortableIterationConsumer.Continuation getEntities(AABB aABB, AbortableIterationConsumer<T> abortableIterationConsumer) {
		for (T entityAccess : this.storage) {
			if (entityAccess.getBoundingBox().intersects(aABB) && abortableIterationConsumer.accept(entityAccess).shouldAbort()) {
				return AbortableIterationConsumer.Continuation.ABORT;
			}
		}

		return AbortableIterationConsumer.Continuation.CONTINUE;
	}

	public <U extends T> AbortableIterationConsumer.Continuation getEntities(
		EntityTypeTest<T, U> entityTypeTest, AABB aABB, AbortableIterationConsumer<? super U> abortableIterationConsumer
	) {
		Collection<? extends T> collection = this.storage.find(entityTypeTest.getBaseClass());
		if (collection.isEmpty()) {
			return AbortableIterationConsumer.Continuation.CONTINUE;
		} else {
			for (T entityAccess : collection) {
				U entityAccess2 = (U)entityTypeTest.tryCast(entityAccess);
				if (entityAccess2 != null && entityAccess.getBoundingBox().intersects(aABB) && abortableIterationConsumer.accept(entityAccess2).shouldAbort()) {
					return AbortableIterationConsumer.Continuation.ABORT;
				}
			}

			return AbortableIterationConsumer.Continuation.CONTINUE;
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

	@VisibleForDebug
	public int size() {
		return this.storage.size();
	}
}
