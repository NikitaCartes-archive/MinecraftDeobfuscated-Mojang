package net.minecraft.network.syncher;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class SynchedEntityData {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Object2IntMap<Class<? extends Entity>> ENTITY_ID_POOL = new Object2IntOpenHashMap<>();
	private static final int MAX_ID_VALUE = 254;
	private final Entity entity;
	private final Int2ObjectMap<SynchedEntityData.DataItem<?>> itemsById = new Int2ObjectOpenHashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private boolean isDirty;

	public SynchedEntityData(Entity entity) {
		this.entity = entity;
	}

	public static <T> EntityDataAccessor<T> defineId(Class<? extends Entity> class_, EntityDataSerializer<T> entityDataSerializer) {
		if (LOGGER.isDebugEnabled()) {
			try {
				Class<?> class2 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
				if (!class2.equals(class_)) {
					LOGGER.debug("defineId called for: {} from {}", class_, class2, new RuntimeException());
				}
			} catch (ClassNotFoundException var5) {
			}
		}

		int i;
		if (ENTITY_ID_POOL.containsKey(class_)) {
			i = ENTITY_ID_POOL.getInt(class_) + 1;
		} else {
			int j = 0;
			Class<?> class3 = class_;

			while (class3 != Entity.class) {
				class3 = class3.getSuperclass();
				if (ENTITY_ID_POOL.containsKey(class3)) {
					j = ENTITY_ID_POOL.getInt(class3) + 1;
					break;
				}
			}

			i = j;
		}

		if (i > 254) {
			throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
		} else {
			ENTITY_ID_POOL.put(class_, i);
			return entityDataSerializer.createAccessor(i);
		}
	}

	public <T> void define(EntityDataAccessor<T> entityDataAccessor, T object) {
		int i = entityDataAccessor.getId();
		if (i > 254) {
			throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
		} else if (this.itemsById.containsKey(i)) {
			throw new IllegalArgumentException("Duplicate id value for " + i + "!");
		} else if (EntityDataSerializers.getSerializedId(entityDataAccessor.getSerializer()) < 0) {
			throw new IllegalArgumentException("Unregistered serializer " + entityDataAccessor.getSerializer() + " for " + i + "!");
		} else {
			this.createDataItem(entityDataAccessor, object);
		}
	}

	private <T> void createDataItem(EntityDataAccessor<T> entityDataAccessor, T object) {
		SynchedEntityData.DataItem<T> dataItem = new SynchedEntityData.DataItem<>(entityDataAccessor, object);
		this.lock.writeLock().lock();
		this.itemsById.put(entityDataAccessor.getId(), dataItem);
		this.lock.writeLock().unlock();
	}

	private <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> entityDataAccessor) {
		this.lock.readLock().lock();

		SynchedEntityData.DataItem<T> dataItem;
		try {
			dataItem = (SynchedEntityData.DataItem<T>)this.itemsById.get(entityDataAccessor.getId());
		} catch (Throwable var9) {
			CrashReport crashReport = CrashReport.forThrowable(var9, "Getting synched entity data");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Synched entity data");
			crashReportCategory.setDetail("Data ID", entityDataAccessor);
			throw new ReportedException(crashReport);
		} finally {
			this.lock.readLock().unlock();
		}

		return dataItem;
	}

	public <T> T get(EntityDataAccessor<T> entityDataAccessor) {
		return this.getItem(entityDataAccessor).getValue();
	}

	public <T> void set(EntityDataAccessor<T> entityDataAccessor, T object) {
		SynchedEntityData.DataItem<T> dataItem = this.getItem(entityDataAccessor);
		if (ObjectUtils.notEqual(object, dataItem.getValue())) {
			dataItem.setValue(object);
			this.entity.onSyncedDataUpdated(entityDataAccessor);
			dataItem.setDirty(true);
			this.isDirty = true;
		}
	}

	public boolean isDirty() {
		return this.isDirty;
	}

	@Nullable
	public List<SynchedEntityData.DataValue<?>> packDirty() {
		List<SynchedEntityData.DataValue<?>> list = null;
		if (this.isDirty) {
			this.lock.readLock().lock();

			for (SynchedEntityData.DataItem<?> dataItem : this.itemsById.values()) {
				if (dataItem.isDirty()) {
					dataItem.setDirty(false);
					if (list == null) {
						list = new ArrayList();
					}

					list.add(dataItem.value());
				}
			}

			this.lock.readLock().unlock();
		}

		this.isDirty = false;
		return list;
	}

	@Nullable
	public List<SynchedEntityData.DataValue<?>> getNonDefaultValues() {
		List<SynchedEntityData.DataValue<?>> list = null;
		this.lock.readLock().lock();

		for (SynchedEntityData.DataItem<?> dataItem : this.itemsById.values()) {
			if (!dataItem.isSetToDefault()) {
				if (list == null) {
					list = new ArrayList();
				}

				list.add(dataItem.value());
			}
		}

		this.lock.readLock().unlock();
		return list;
	}

	public void assignValues(List<SynchedEntityData.DataValue<?>> list) {
		this.lock.writeLock().lock();

		try {
			for (SynchedEntityData.DataValue<?> dataValue : list) {
				SynchedEntityData.DataItem<?> dataItem = this.itemsById.get(dataValue.id);
				if (dataItem != null) {
					this.assignValue(dataItem, dataValue);
					this.entity.onSyncedDataUpdated(dataItem.getAccessor());
				}
			}
		} finally {
			this.lock.writeLock().unlock();
		}

		this.entity.onSyncedDataUpdated(list);
	}

	private <T> void assignValue(SynchedEntityData.DataItem<T> dataItem, SynchedEntityData.DataValue<?> dataValue) {
		if (!Objects.equals(dataValue.serializer(), dataItem.accessor.getSerializer())) {
			throw new IllegalStateException(
				String.format(
					Locale.ROOT,
					"Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)",
					dataItem.accessor.getId(),
					this.entity,
					dataItem.value,
					dataItem.value.getClass(),
					dataValue.value,
					dataValue.value.getClass()
				)
			);
		} else {
			dataItem.setValue((T)dataValue.value);
		}
	}

	public boolean isEmpty() {
		return this.itemsById.isEmpty();
	}

	public static class DataItem<T> {
		final EntityDataAccessor<T> accessor;
		T value;
		private final T initialValue;
		private boolean dirty;

		public DataItem(EntityDataAccessor<T> entityDataAccessor, T object) {
			this.accessor = entityDataAccessor;
			this.initialValue = object;
			this.value = object;
		}

		public EntityDataAccessor<T> getAccessor() {
			return this.accessor;
		}

		public void setValue(T object) {
			this.value = object;
		}

		public T getValue() {
			return this.value;
		}

		public boolean isDirty() {
			return this.dirty;
		}

		public void setDirty(boolean bl) {
			this.dirty = bl;
		}

		public boolean isSetToDefault() {
			return this.initialValue.equals(this.value);
		}

		public SynchedEntityData.DataValue<T> value() {
			return SynchedEntityData.DataValue.create(this.accessor, this.value);
		}
	}

	public static record DataValue<T>(int id, EntityDataSerializer<T> serializer, T value) {

		public static <T> SynchedEntityData.DataValue<T> create(EntityDataAccessor<T> entityDataAccessor, T object) {
			EntityDataSerializer<T> entityDataSerializer = entityDataAccessor.getSerializer();
			return new SynchedEntityData.DataValue<>(entityDataAccessor.getId(), entityDataSerializer, entityDataSerializer.copy(object));
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			int i = EntityDataSerializers.getSerializedId(this.serializer);
			if (i < 0) {
				throw new EncoderException("Unknown serializer type " + this.serializer);
			} else {
				friendlyByteBuf.writeByte(this.id);
				friendlyByteBuf.writeVarInt(i);
				this.serializer.write(friendlyByteBuf, this.value);
			}
		}

		public static SynchedEntityData.DataValue<?> read(FriendlyByteBuf friendlyByteBuf, int i) {
			int j = friendlyByteBuf.readVarInt();
			EntityDataSerializer<?> entityDataSerializer = EntityDataSerializers.getSerializer(j);
			if (entityDataSerializer == null) {
				throw new DecoderException("Unknown serializer type " + j);
			} else {
				return read(friendlyByteBuf, i, entityDataSerializer);
			}
		}

		private static <T> SynchedEntityData.DataValue<T> read(FriendlyByteBuf friendlyByteBuf, int i, EntityDataSerializer<T> entityDataSerializer) {
			return new SynchedEntityData.DataValue<>(i, entityDataSerializer, entityDataSerializer.read(friendlyByteBuf));
		}
	}
}
