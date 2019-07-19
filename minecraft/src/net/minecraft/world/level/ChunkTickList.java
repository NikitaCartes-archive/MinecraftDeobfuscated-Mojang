package net.minecraft.world.level;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

public class ChunkTickList<T> implements TickList<T> {
	private final Set<TickNextTickData<T>> ticks;
	private final Function<T, ResourceLocation> toId;

	public ChunkTickList(Function<T, ResourceLocation> function, List<TickNextTickData<T>> list) {
		this(function, Sets.<TickNextTickData<T>>newHashSet(list));
	}

	private ChunkTickList(Function<T, ResourceLocation> function, Set<TickNextTickData<T>> set) {
		this.ticks = set;
		this.toId = function;
	}

	@Override
	public boolean hasScheduledTick(BlockPos blockPos, T object) {
		return false;
	}

	@Override
	public void scheduleTick(BlockPos blockPos, T object, int i, TickPriority tickPriority) {
		this.ticks.add(new TickNextTickData(blockPos, object, (long)i, tickPriority));
	}

	@Override
	public boolean willTickThisTick(BlockPos blockPos, T object) {
		return false;
	}

	@Override
	public void addAll(Stream<TickNextTickData<T>> stream) {
		stream.forEach(this.ticks::add);
	}

	public Stream<TickNextTickData<T>> ticks() {
		return this.ticks.stream();
	}

	public ListTag save(long l) {
		return ServerTickList.saveTickList(this.toId, this.ticks, l);
	}

	public static <T> ChunkTickList<T> create(ListTag listTag, Function<T, ResourceLocation> function, Function<ResourceLocation, T> function2) {
		Set<TickNextTickData<T>> set = Sets.<TickNextTickData<T>>newHashSet();

		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			T object = (T)function2.apply(new ResourceLocation(compoundTag.getString("i")));
			if (object != null) {
				set.add(
					new TickNextTickData(
						new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z")),
						object,
						(long)compoundTag.getInt("t"),
						TickPriority.byValue(compoundTag.getInt("p"))
					)
				);
			}
		}

		return new ChunkTickList<>(function, set);
	}
}
