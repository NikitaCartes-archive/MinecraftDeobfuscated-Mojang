package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;

public class ProtoTickList<T> implements TickList<T> {
	protected final Predicate<T> ignore;
	private final ChunkPos chunkPos;
	private final ShortList[] toBeTicked = new ShortList[16];

	public ProtoTickList(Predicate<T> predicate, ChunkPos chunkPos) {
		this(predicate, chunkPos, new ListTag());
	}

	public ProtoTickList(Predicate<T> predicate, ChunkPos chunkPos, ListTag listTag) {
		this.ignore = predicate;
		this.chunkPos = chunkPos;

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
					BlockPos blockPos = ProtoChunk.unpackOffsetCoordinates(short_, i, this.chunkPos);
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
		ChunkAccess.getOrCreateOffsetList(this.toBeTicked, blockPos.getY() >> 4).add(ProtoChunk.packOffsetCoordinates(blockPos));
	}

	@Override
	public boolean willTickThisTick(BlockPos blockPos, T object) {
		return false;
	}
}
