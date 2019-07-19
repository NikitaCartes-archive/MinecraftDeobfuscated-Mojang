package net.minecraft.network.syncher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SynchedEntityData {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<Class<? extends Entity>, Integer> ENTITY_ID_POOL = Maps.<Class<? extends Entity>, Integer>newHashMap();
	private final Entity entity;
	private final Map<Integer, SynchedEntityData.DataItem<?>> itemsById = Maps.<Integer, SynchedEntityData.DataItem<?>>newHashMap();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private boolean isEmpty = true;
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
			i = (Integer)ENTITY_ID_POOL.get(class_) + 1;
		} else {
			int j = 0;
			Class<?> class3 = class_;

			while (class3 != Entity.class) {
				class3 = class3.getSuperclass();
				if (ENTITY_ID_POOL.containsKey(class3)) {
					j = (Integer)ENTITY_ID_POOL.get(class3) + 1;
					break;
				}
			}

			i = j;
		}

		if (i > 254) {
			throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + 254 + ")");
		} else {
			ENTITY_ID_POOL.put(class_, i);
			return entityDataSerializer.createAccessor(i);
		}
	}

	public <T> void define(EntityDataAccessor<T> entityDataAccessor, T object) {
		int i = entityDataAccessor.getId();
		if (i > 254) {
			throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + 254 + ")");
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
		this.isEmpty = false;
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

	public static void pack(List<SynchedEntityData.DataItem<?>> list, FriendlyByteBuf friendlyByteBuf) throws IOException {
		if (list != null) {
			int i = 0;

			for (int j = list.size(); i < j; i++) {
				writeDataItem(friendlyByteBuf, (SynchedEntityData.DataItem)list.get(i));
			}
		}

		friendlyByteBuf.writeByte(255);
	}

	@Nullable
	public List<SynchedEntityData.DataItem<?>> packDirty() {
		List<SynchedEntityData.DataItem<?>> list = null;
		if (this.isDirty) {
			this.lock.readLock().lock();

			for (SynchedEntityData.DataItem<?> dataItem : this.itemsById.values()) {
				if (dataItem.isDirty()) {
					dataItem.setDirty(false);
					if (list == null) {
						list = Lists.<SynchedEntityData.DataItem<?>>newArrayList();
					}

					list.add(dataItem.copy());
				}
			}

			this.lock.readLock().unlock();
		}

		this.isDirty = false;
		return list;
	}

	public void packAll(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.lock.readLock().lock();

		for (SynchedEntityData.DataItem<?> dataItem : this.itemsById.values()) {
			writeDataItem(friendlyByteBuf, dataItem);
		}

		this.lock.readLock().unlock();
		friendlyByteBuf.writeByte(255);
	}

	@Nullable
	public List<SynchedEntityData.DataItem<?>> getAll() {
		List<SynchedEntityData.DataItem<?>> list = null;
		this.lock.readLock().lock();

		for (SynchedEntityData.DataItem<?> dataItem : this.itemsById.values()) {
			if (list == null) {
				list = Lists.<SynchedEntityData.DataItem<?>>newArrayList();
			}

			list.add(dataItem.copy());
		}

		this.lock.readLock().unlock();
		return list;
	}

	private static <T> void writeDataItem(FriendlyByteBuf friendlyByteBuf, SynchedEntityData.DataItem<T> dataItem) throws IOException {
		EntityDataAccessor<T> entityDataAccessor = dataItem.getAccessor();
		int i = EntityDataSerializers.getSerializedId(entityDataAccessor.getSerializer());
		if (i < 0) {
			throw new EncoderException("Unknown serializer type " + entityDataAccessor.getSerializer());
		} else {
			friendlyByteBuf.writeByte(entityDataAccessor.getId());
			friendlyByteBuf.writeVarInt(i);
			entityDataAccessor.getSerializer().write(friendlyByteBuf, dataItem.getValue());
		}
	}

	@Nullable
	public static List<SynchedEntityData.DataItem<?>> unpack(FriendlyByteBuf friendlyByteBuf) throws IOException {
		List<SynchedEntityData.DataItem<?>> list = null;

		int i;
		while ((i = friendlyByteBuf.readUnsignedByte()) != 255) {
			if (list == null) {
				list = Lists.<SynchedEntityData.DataItem<?>>newArrayList();
			}

			int j = friendlyByteBuf.readVarInt();
			EntityDataSerializer<?> entityDataSerializer = EntityDataSerializers.getSerializer(j);
			if (entityDataSerializer == null) {
				throw new DecoderException("Unknown serializer type " + j);
			}

			list.add(genericHelper(friendlyByteBuf, i, entityDataSerializer));
		}

		return list;
	}

	private static <T> SynchedEntityData.DataItem<T> genericHelper(FriendlyByteBuf friendlyByteBuf, int i, EntityDataSerializer<T> entityDataSerializer) {
		return new SynchedEntityData.DataItem<>(entityDataSerializer.createAccessor(i), entityDataSerializer.read(friendlyByteBuf));
	}

	@Environment(EnvType.CLIENT)
	public void assignValues(List<SynchedEntityData.DataItem<?>> list) {
		this.lock.writeLock().lock();

		for (SynchedEntityData.DataItem<?> dataItem : list) {
			SynchedEntityData.DataItem<?> dataItem2 = (SynchedEntityData.DataItem<?>)this.itemsById.get(dataItem.getAccessor().getId());
			if (dataItem2 != null) {
				this.assignValue(dataItem2, dataItem);
				this.entity.onSyncedDataUpdated(dataItem.getAccessor());
			}
		}

		this.lock.writeLock().unlock();
		this.isDirty = true;
	}

	@Environment(EnvType.CLIENT)
	private <T> void assignValue(SynchedEntityData.DataItem<T> dataItem, SynchedEntityData.DataItem<?> dataItem2) {
		if (!Objects.equals(dataItem2.accessor.getSerializer(), dataItem.accessor.getSerializer())) {
			throw new IllegalStateException(
				String.format(
					"Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)",
					dataItem.accessor.getId(),
					this.entity,
					dataItem.value,
					dataItem.value.getClass(),
					dataItem2.value,
					dataItem2.value.getClass()
				)
			);
		} else {
			dataItem.setValue((T)dataItem2.getValue());
		}
	}

	public boolean isEmpty() {
		return this.isEmpty;
	}

	public void clearDirty() {
		this.isDirty = false;
		this.lock.readLock().lock();

		for (SynchedEntityData.DataItem<?> dataItem : this.itemsById.values()) {
			dataItem.setDirty(false);
		}

		this.lock.readLock().unlock();
	}

	public static class DataItem<T> {
		private final EntityDataAccessor<T> accessor;
		private T value;
		private boolean dirty;

		public DataItem(EntityDataAccessor<T> entityDataAccessor, T object) {
			this.accessor = entityDataAccessor;
			this.value = object;
			this.dirty = true;
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

		public SynchedEntityData.DataItem<T> copy() {
			return new SynchedEntityData.DataItem<>(this.accessor, this.accessor.getSerializer().copy(this.value));
		}
	}
}
