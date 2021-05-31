package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;

public class ProtoTickList<T> implements TickList<T> {
	protected final Predicate<T> ignore;
	private final ChunkPos chunkPos;
	private final ShortList[] toBeTicked;
	private LevelHeightAccessor levelHeightAccessor;

	public ProtoTickList(Predicate<T> predicate, ChunkPos chunkPos, LevelHeightAccessor levelHeightAccessor) {
		this(predicate, chunkPos, new ListTag(), levelHeightAccessor);
	}

	public ProtoTickList(Predicate<T> predicate, ChunkPos chunkPos, ListTag listTag, LevelHeightAccessor levelHeightAccessor) {
		this.ignore = predicate;
		this.chunkPos = chunkPos;
		this.levelHeightAccessor = levelHeightAccessor;
		this.toBeTicked = new ShortList[levelHeightAccessor.getSectionsCount()];

		for (int i = 0; i < listTag.size(); i++) {
			ListTag listTag2 = listTag.getList(i);

			for (int j = 0; j < listTag2.size(); j++) {
				ChunkAccess.getOrCreateOffsetList(this.toBeTicked, i).add(listTag2.getShort(j));
			}
		}
	}

	public ListTag save() {
		return ChunkSerializer.packOffsets(this.toBeTicked);
	}

	public void copyOut(TickList<T> tickList, Function<BlockPos, T> function) {
		for (int i = 0; i < this.toBeTicked.length; i++) {
			if (this.toBeTicked[i] != null) {
				for (Short short_ : this.toBeTicked[i]) {
					BlockPos blockPos = ProtoChunk.unpackOffsetCoordinates(short_, this.levelHeightAccessor.getSectionYFromSectionIndex(i), this.chunkPos);
					tickList.scheduleTick(blockPos, (T)function.apply(blockPos), 0);
				}

				this.toBeTicked[i].clear();
			}
		}
	}

	@Override
	public boolean hasScheduledTick(BlockPos blockPos, T object) {
		return false;
	}

	@Override
	public void scheduleTick(BlockPos blockPos, T object, int i, TickPriority tickPriority) {
		int j = this.levelHeightAccessor.getSectionIndex(blockPos.getY());
		if (j >= 0 && j < this.levelHeightAccessor.getSectionsCount()) {
			ChunkAccess.getOrCreateOffsetList(this.toBeTicked, j).add(ProtoChunk.packOffsetCoordinates(blockPos));
		}
	}

	@Override
	public boolean willTickThisTick(BlockPos blockPos, T object) {
		return false;
	}

	@Override
	public int size() {
		return Stream.of(this.toBeTicked).filter(Objects::nonNull).mapToInt(List::size).sum();
	}
}
