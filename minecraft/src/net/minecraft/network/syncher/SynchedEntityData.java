package net.minecraft.network.syncher;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.ClassTreeIdRegistry;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class SynchedEntityData {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_ID_VALUE = 254;
	static final ClassTreeIdRegistry ID_REGISTRY = new ClassTreeIdRegistry();
	private final SyncedDataHolder entity;
	private final SynchedEntityData.DataItem<?>[] itemsById;
	private boolean isDirty;

	SynchedEntityData(SyncedDataHolder syncedDataHolder, SynchedEntityData.DataItem<?>[] dataItems) {
		this.entity = syncedDataHolder;
		this.itemsById = dataItems;
	}

	public static <T> EntityDataAccessor<T> defineId(Class<? extends SyncedDataHolder> class_, EntityDataSerializer<T> entityDataSerializer) {
		if (LOGGER.isDebugEnabled()) {
			try {
				Class<?> class2 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
				if (!class2.equals(class_)) {
					LOGGER.debug("defineId called for: {} from {}", class_, class2, new RuntimeException());
				}
			} catch (ClassNotFoundException var3) {
			}
		}

		int i = ID_REGISTRY.define(class_);
		if (i > 254) {
			throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
		} else {
			return entityDataSerializer.createAccessor(i);
		}
	}

	private <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> entityDataAccessor) {
		return (SynchedEntityData.DataItem<T>)this.itemsById[entityDataAccessor.id()];
	}

	public <T> T get(EntityDataAccessor<T> entityDataAccessor) {
		return this.getItem(entityDataAccessor).getValue();
	}

	public <T> void set(EntityDataAccessor<T> entityDataAccessor, T object) {
		this.set(entityDataAccessor, object, false);
	}

	public <T> void set(EntityDataAccessor<T> entityDataAccessor, T object, boolean bl) {
		SynchedEntityData.DataItem<T> dataItem = this.getItem(entityDataAccessor);
		if (bl || ObjectUtils.notEqual(object, dataItem.getValue())) {
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
		if (!this.isDirty) {
			return null;
		} else {
			this.isDirty = false;
			List<SynchedEntityData.DataValue<?>> list = new ArrayList();

			for (SynchedEntityData.DataItem<?> dataItem : this.itemsById) {
				if (dataItem.isDirty()) {
					dataItem.setDirty(false);
					list.add(dataItem.value());
				}
			}

			return list;
		}
	}

	@Nullable
	public List<SynchedEntityData.DataValue<?>> getNonDefaultValues() {
		List<SynchedEntityData.DataValue<?>> list = null;

		for (SynchedEntityData.DataItem<?> dataItem : this.itemsById) {
			if (!dataItem.isSetToDefault()) {
				if (list == null) {
					list = new ArrayList();
				}

				list.add(dataItem.value());
			}
		}

		return list;
	}

	public void assignValues(List<SynchedEntityData.DataValue<?>> list) {
		for (SynchedEntityData.DataValue<?> dataValue : list) {
			SynchedEntityData.DataItem<?> dataItem = this.itemsById[dataValue.id];
			this.assignValue(dataItem, dataValue);
			this.entity.onSyncedDataUpdated(dataItem.getAccessor());
		}

		this.entity.onSyncedDataUpdated(list);
	}

	private <T> void assignValue(SynchedEntityData.DataItem<T> dataItem, SynchedEntityData.DataValue<?> dataValue) {
		if (!Objects.equals(dataValue.serializer(), dataItem.accessor.serializer())) {
			throw new IllegalStateException(
				String.format(
					Locale.ROOT,
					"Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)",
					dataItem.accessor.id(),
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

	public static class Builder {
		private final SyncedDataHolder entity;
		private final SynchedEntityData.DataItem<?>[] itemsById;

		public Builder(SyncedDataHolder syncedDataHolder) {
			this.entity = syncedDataHolder;
			this.itemsById = new SynchedEntityData.DataItem[SynchedEntityData.ID_REGISTRY.getCount(syncedDataHolder.getClass())];
		}

		public <T> SynchedEntityData.Builder define(EntityDataAccessor<T> entityDataAccessor, T object) {
			int i = entityDataAccessor.id();
			if (i > this.itemsById.length) {
				throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + this.itemsById.length + ")");
			} else if (this.itemsById[i] != null) {
				throw new IllegalArgumentException("Duplicate id value for " + i + "!");
			} else if (EntityDataSerializers.getSerializedId(entityDataAccessor.serializer()) < 0) {
				throw new IllegalArgumentException("Unregistered serializer " + entityDataAccessor.serializer() + " for " + i + "!");
			} else {
				this.itemsById[entityDataAccessor.id()] = new SynchedEntityData.DataItem<>(entityDataAccessor, object);
				return this;
			}
		}

		public SynchedEntityData build() {
			for (SynchedEntityData.DataItem<?> dataItem : this.itemsById) {
				if (dataItem == null) {
					throw new IllegalStateException("Entity " + this.entity + " did not have all synched data values defined");
				}
			}

			return new SynchedEntityData(this.entity, this.itemsById);
		}
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
			EntityDataSerializer<T> entityDataSerializer = entityDataAccessor.serializer();
			return new SynchedEntityData.DataValue<>(entityDataAccessor.id(), entityDataSerializer, entityDataSerializer.copy(object));
		}

		public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			int i = EntityDataSerializers.getSerializedId(this.serializer);
			if (i < 0) {
				throw new EncoderException("Unknown serializer type " + this.serializer);
			} else {
				registryFriendlyByteBuf.writeByte(this.id);
				registryFriendlyByteBuf.writeVarInt(i);
				this.serializer.codec().encode(registryFriendlyByteBuf, this.value);
			}
		}

		public static SynchedEntityData.DataValue<?> read(RegistryFriendlyByteBuf registryFriendlyByteBuf, int i) {
			int j = registryFriendlyByteBuf.readVarInt();
			EntityDataSerializer<?> entityDataSerializer = EntityDataSerializers.getSerializer(j);
			if (entityDataSerializer == null) {
				throw new DecoderException("Unknown serializer type " + j);
			} else {
				return read(registryFriendlyByteBuf, i, entityDataSerializer);
			}
		}

		private static <T> SynchedEntityData.DataValue<T> read(RegistryFriendlyByteBuf registryFriendlyByteBuf, int i, EntityDataSerializer<T> entityDataSerializer) {
			return new SynchedEntityData.DataValue<>(i, entityDataSerializer, entityDataSerializer.codec().decode(registryFriendlyByteBuf));
		}
	}
}
