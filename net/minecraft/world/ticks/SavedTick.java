/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.Hash;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

public record SavedTick<T>(T type, BlockPos pos, int delay, TickPriority priority) {
    private static final String TAG_ID = "i";
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";
    private static final String TAG_DELAY = "t";
    private static final String TAG_PRIORITY = "p";
    public static final Hash.Strategy<SavedTick<?>> UNIQUE_TICK_HASH = new Hash.Strategy<SavedTick<?>>(){

        @Override
        public int hashCode(SavedTick<?> savedTick) {
            return 31 * savedTick.pos().hashCode() + savedTick.type().hashCode();
        }

        @Override
        public boolean equals(@Nullable SavedTick<?> savedTick, @Nullable SavedTick<?> savedTick2) {
            if (savedTick == savedTick2) {
                return true;
            }
            if (savedTick == null || savedTick2 == null) {
                return false;
            }
            return savedTick.type() == savedTick2.type() && savedTick.pos().equals(savedTick2.pos());
        }

        @Override
        public /* synthetic */ boolean equals(@Nullable Object object, @Nullable Object object2) {
            return this.equals((SavedTick)object, (SavedTick)object2);
        }

        @Override
        public /* synthetic */ int hashCode(Object object) {
            return this.hashCode((SavedTick)object);
        }
    };

    public static <T> void loadTickList(ListTag listTag, Function<String, Optional<T>> function, ChunkPos chunkPos, Consumer<SavedTick<T>> consumer) {
        long l = chunkPos.toLong();
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            SavedTick.loadTick(compoundTag, function).ifPresent(savedTick -> {
                if (ChunkPos.asLong(savedTick.pos()) == l) {
                    consumer.accept((SavedTick)savedTick);
                }
            });
        }
    }

    public static <T> Optional<SavedTick<T>> loadTick(CompoundTag compoundTag, Function<String, Optional<T>> function) {
        return function.apply(compoundTag.getString(TAG_ID)).map(object -> {
            BlockPos blockPos = new BlockPos(compoundTag.getInt(TAG_X), compoundTag.getInt(TAG_Y), compoundTag.getInt(TAG_Z));
            return new SavedTick<Object>(object, blockPos, compoundTag.getInt(TAG_DELAY), TickPriority.byValue(compoundTag.getInt(TAG_PRIORITY)));
        });
    }

    private static CompoundTag saveTick(String string, BlockPos blockPos, int i, TickPriority tickPriority) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString(TAG_ID, string);
        compoundTag.putInt(TAG_X, blockPos.getX());
        compoundTag.putInt(TAG_Y, blockPos.getY());
        compoundTag.putInt(TAG_Z, blockPos.getZ());
        compoundTag.putInt(TAG_DELAY, i);
        compoundTag.putInt(TAG_PRIORITY, tickPriority.getValue());
        return compoundTag;
    }

    public static <T> CompoundTag saveTick(ScheduledTick<T> scheduledTick, Function<T, String> function, long l) {
        return SavedTick.saveTick(function.apply(scheduledTick.type()), scheduledTick.pos(), (int)(scheduledTick.triggerTick() - l), scheduledTick.priority());
    }

    public CompoundTag save(Function<T, String> function) {
        return SavedTick.saveTick(function.apply(this.type), this.pos, this.delay, this.priority);
    }

    public ScheduledTick<T> unpack(long l, long m) {
        return new ScheduledTick<T>(this.type, this.pos, l + (long)this.delay, this.priority, m);
    }

    public static <T> SavedTick<T> probe(T object, BlockPos blockPos) {
        return new SavedTick<T>(object, blockPos, 0, TickPriority.NORMAL);
    }
}

