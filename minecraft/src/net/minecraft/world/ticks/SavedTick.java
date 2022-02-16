package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.Hash.Strategy;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;

public record SavedTick<T>(T type, BlockPos pos, int delay, TickPriority priority) {
	private static final String TAG_ID = "i";
	private static final String TAG_X = "x";
	private static final String TAG_Y = "y";
	private static final String TAG_Z = "z";
	private static final String TAG_DELAY = "t";
	private static final String TAG_PRIORITY = "p";
	public static final Strategy<SavedTick<?>> UNIQUE_TICK_HASH = new Strategy<SavedTick<?>>() {
		public int hashCode(SavedTick<?> savedTick) {
			return 31 * savedTick.pos().hashCode() + savedTick.type().hashCode();
		}

		public boolean equals(@Nullable SavedTick<?> savedTick, @Nullable SavedTick<?> savedTick2) {
			if (savedTick == savedTick2) {
				return true;
			} else {
				return savedTick != null && savedTick2 != null ? savedTick.type() == savedTick2.type() && savedTick.pos().equals(savedTick2.pos()) : false;
			}
		}
	};

	public static <T> void loadTickList(ListTag listTag, Function<String, Optional<T>> function, ChunkPos chunkPos, Consumer<SavedTick<T>> consumer) {
		long l = chunkPos.toLong();

		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			loadTick(compoundTag, function).ifPresent(savedTick -> {
				if (ChunkPos.asLong(savedTick.pos()) == l) {
					consumer.accept(savedTick);
				}
			});
		}
	}

	public static <T> Optional<SavedTick<T>> loadTick(CompoundTag compoundTag, Function<String, Optional<T>> function) {
		return ((Optional)function.apply(compoundTag.getString("i"))).map(object -> {
			BlockPos blockPos = new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z"));
			return new SavedTick<>(object, blockPos, compoundTag.getInt("t"), TickPriority.byValue(compoundTag.getInt("p")));
		});
	}

	private static CompoundTag saveTick(String string, BlockPos blockPos, int i, TickPriority tickPriority) {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("i", string);
		compoundTag.putInt("x", blockPos.getX());
		compoundTag.putInt("y", blockPos.getY());
		compoundTag.putInt("z", blockPos.getZ());
		compoundTag.putInt("t", i);
		compoundTag.putInt("p", tickPriority.getValue());
		return compoundTag;
	}

	public static <T> CompoundTag saveTick(ScheduledTick<T> scheduledTick, Function<T, String> function, long l) {
		return saveTick((String)function.apply(scheduledTick.type()), scheduledTick.pos(), (int)(scheduledTick.triggerTick() - l), scheduledTick.priority());
	}

	public CompoundTag save(Function<T, String> function) {
		return saveTick((String)function.apply(this.type), this.pos, this.delay, this.priority);
	}

	public ScheduledTick<T> unpack(long l, long m) {
		return new ScheduledTick<>(this.type, this.pos, l + (long)this.delay, this.priority, m);
	}

	public static <T> SavedTick<T> probe(T object, BlockPos blockPos) {
		return new SavedTick<>(object, blockPos, 0, TickPriority.NORMAL);
	}
}
