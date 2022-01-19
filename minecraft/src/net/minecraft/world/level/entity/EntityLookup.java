package net.minecraft.world.level.entity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class EntityLookup<T extends EntityAccess> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Int2ObjectMap<T> byId = new Int2ObjectLinkedOpenHashMap<>();
	private final Map<UUID, T> byUuid = Maps.<UUID, T>newHashMap();

	public <U extends T> void getEntities(EntityTypeTest<T, U> entityTypeTest, Consumer<U> consumer) {
		for (T entityAccess : this.byId.values()) {
			U entityAccess2 = (U)entityTypeTest.tryCast(entityAccess);
			if (entityAccess2 != null) {
				consumer.accept(entityAccess2);
			}
		}
	}

	public Iterable<T> getAllEntities() {
		return Iterables.unmodifiableIterable(this.byId.values());
	}

	public void add(T entityAccess) {
		UUID uUID = entityAccess.getUUID();
		if (this.byUuid.containsKey(uUID)) {
			LOGGER.warn("Duplicate entity UUID {}: {}", uUID, entityAccess);
		} else {
			this.byUuid.put(uUID, entityAccess);
			this.byId.put(entityAccess.getId(), entityAccess);
		}
	}

	public void remove(T entityAccess) {
		this.byUuid.remove(entityAccess.getUUID());
		this.byId.remove(entityAccess.getId());
	}

	@Nullable
	public T getEntity(int i) {
		return this.byId.get(i);
	}

	@Nullable
	public T getEntity(UUID uUID) {
		return (T)this.byUuid.get(uUID);
	}

	public int count() {
		return this.byUuid.size();
	}
}
