/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.syncher;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SynchedEntityData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Object2IntMap<Class<? extends Entity>> ENTITY_ID_POOL = new Object2IntOpenHashMap<Class<? extends Entity>>();
    private static final int EOF_MARKER = 255;
    private static final int MAX_ID_VALUE = 254;
    private final Entity entity;
    private final Int2ObjectMap<DataItem<?>> itemsById = new Int2ObjectOpenHashMap();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private boolean isEmpty = true;
    private boolean isDirty;

    public SynchedEntityData(Entity entity) {
        this.entity = entity;
    }

    public static <T> EntityDataAccessor<T> defineId(Class<? extends Entity> class_, EntityDataSerializer<T> entityDataSerializer) {
        int i;
        if (LOGGER.isDebugEnabled()) {
            try {
                Class<?> class2 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
                if (!class2.equals(class_)) {
                    LOGGER.debug("defineId called for: {} from {}", class_, class2, new RuntimeException());
                }
            } catch (ClassNotFoundException class2) {
                // empty catch block
            }
        }
        if (ENTITY_ID_POOL.containsKey(class_)) {
            i = ENTITY_ID_POOL.getInt(class_) + 1;
        } else {
            int j = 0;
            Class<? extends Entity> class3 = class_;
            while (class3 != Entity.class) {
                if (!ENTITY_ID_POOL.containsKey(class3 = class3.getSuperclass())) continue;
                j = ENTITY_ID_POOL.getInt(class3) + 1;
                break;
            }
            i = j;
        }
        if (i > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
        }
        ENTITY_ID_POOL.put(class_, i);
        return entityDataSerializer.createAccessor(i);
    }

    public <T> void define(EntityDataAccessor<T> entityDataAccessor, T object) {
        int i = entityDataAccessor.getId();
        if (i > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
        }
        if (this.itemsById.containsKey(i)) {
            throw new IllegalArgumentException("Duplicate id value for " + i + "!");
        }
        if (EntityDataSerializers.getSerializedId(entityDataAccessor.getSerializer()) < 0) {
            throw new IllegalArgumentException("Unregistered serializer " + entityDataAccessor.getSerializer() + " for " + i + "!");
        }
        this.createDataItem(entityDataAccessor, object);
    }

    private <T> void createDataItem(EntityDataAccessor<T> entityDataAccessor, T object) {
        DataItem<T> dataItem = new DataItem<T>(entityDataAccessor, object);
        this.lock.writeLock().lock();
        this.itemsById.put(entityDataAccessor.getId(), (DataItem<?>)dataItem);
        this.isEmpty = false;
        this.lock.writeLock().unlock();
    }

    private <T> DataItem<T> getItem(EntityDataAccessor<T> entityDataAccessor) {
        DataItem dataItem;
        this.lock.readLock().lock();
        try {
            dataItem = (DataItem)this.itemsById.get(entityDataAccessor.getId());
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Getting synched entity data");
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
        DataItem<T> dataItem = this.getItem(entityDataAccessor);
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

    public static void pack(@Nullable List<DataItem<?>> list, FriendlyByteBuf friendlyByteBuf) {
        if (list != null) {
            for (DataItem<?> dataItem : list) {
                SynchedEntityData.writeDataItem(friendlyByteBuf, dataItem);
            }
        }
        friendlyByteBuf.writeByte(255);
    }

    @Nullable
    public List<DataItem<?>> packDirty() {
        ArrayList list = null;
        if (this.isDirty) {
            this.lock.readLock().lock();
            for (DataItem dataItem : this.itemsById.values()) {
                if (!dataItem.isDirty()) continue;
                dataItem.setDirty(false);
                if (list == null) {
                    list = Lists.newArrayList();
                }
                list.add(dataItem.copy());
            }
            this.lock.readLock().unlock();
        }
        this.isDirty = false;
        return list;
    }

    @Nullable
    public List<DataItem<?>> getAll() {
        ArrayList list = null;
        this.lock.readLock().lock();
        for (DataItem dataItem : this.itemsById.values()) {
            if (list == null) {
                list = Lists.newArrayList();
            }
            list.add(dataItem.copy());
        }
        this.lock.readLock().unlock();
        return list;
    }

    private static <T> void writeDataItem(FriendlyByteBuf friendlyByteBuf, DataItem<T> dataItem) {
        EntityDataAccessor<T> entityDataAccessor = dataItem.getAccessor();
        int i = EntityDataSerializers.getSerializedId(entityDataAccessor.getSerializer());
        if (i < 0) {
            throw new EncoderException("Unknown serializer type " + entityDataAccessor.getSerializer());
        }
        friendlyByteBuf.writeByte(entityDataAccessor.getId());
        friendlyByteBuf.writeVarInt(i);
        entityDataAccessor.getSerializer().write(friendlyByteBuf, dataItem.getValue());
    }

    @Nullable
    public static List<DataItem<?>> unpack(FriendlyByteBuf friendlyByteBuf) {
        short i;
        ArrayList<DataItem<?>> list = null;
        while ((i = friendlyByteBuf.readUnsignedByte()) != 255) {
            int j;
            EntityDataSerializer<?> entityDataSerializer;
            if (list == null) {
                list = Lists.newArrayList();
            }
            if ((entityDataSerializer = EntityDataSerializers.getSerializer(j = friendlyByteBuf.readVarInt())) == null) {
                throw new DecoderException("Unknown serializer type " + j);
            }
            list.add(SynchedEntityData.genericHelper(friendlyByteBuf, i, entityDataSerializer));
        }
        return list;
    }

    private static <T> DataItem<T> genericHelper(FriendlyByteBuf friendlyByteBuf, int i, EntityDataSerializer<T> entityDataSerializer) {
        return new DataItem<T>(entityDataSerializer.createAccessor(i), entityDataSerializer.read(friendlyByteBuf));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void assignValues(List<DataItem<?>> list) {
        this.lock.writeLock().lock();
        try {
            for (DataItem<?> dataItem : list) {
                DataItem dataItem2 = (DataItem)this.itemsById.get(dataItem.getAccessor().getId());
                if (dataItem2 == null) continue;
                this.assignValue(dataItem2, dataItem);
                this.entity.onSyncedDataUpdated(dataItem.getAccessor());
            }
        } finally {
            this.lock.writeLock().unlock();
        }
        this.isDirty = true;
    }

    private <T> void assignValue(DataItem<T> dataItem, DataItem<?> dataItem2) {
        if (!Objects.equals(dataItem2.accessor.getSerializer(), dataItem.accessor.getSerializer())) {
            throw new IllegalStateException(String.format("Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)", dataItem.accessor.getId(), this.entity, dataItem.value, dataItem.value.getClass(), dataItem2.value, dataItem2.value.getClass()));
        }
        dataItem.setValue(dataItem2.getValue());
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }

    public void clearDirty() {
        this.isDirty = false;
        this.lock.readLock().lock();
        for (DataItem dataItem : this.itemsById.values()) {
            dataItem.setDirty(false);
        }
        this.lock.readLock().unlock();
    }

    public static class DataItem<T> {
        final EntityDataAccessor<T> accessor;
        T value;
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

        public DataItem<T> copy() {
            return new DataItem<T>(this.accessor, this.accessor.getSerializer().copy(this.value));
        }
    }
}

