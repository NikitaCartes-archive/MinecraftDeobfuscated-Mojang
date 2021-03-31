package net.minecraft.world.level;

import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public class ChunkPos {
	public static final long INVALID_CHUNK_POS = asLong(1875016, 1875016);
	private static final long COORD_BITS = 32L;
	private static final long COORD_MASK = 4294967295L;
	private static final int REGION_BITS = 5;
	private static final int REGION_MASK = 31;
	public final int x;
	public final int z;
	private static final int HASH_A = 1664525;
	private static final int HASH_C = 1013904223;
	private static final int HASH_Z_XOR = -559038737;

	public ChunkPos(int i, int j) {
		this.x = i;
		this.z = j;
	}

	public ChunkPos(BlockPos blockPos) {
		this.x = SectionPos.blockToSectionCoord(blockPos.getX());
		this.z = SectionPos.blockToSectionCoord(blockPos.getZ());
	}

	public ChunkPos(long l) {
		this.x = (int)l;
		this.z = (int)(l >> 32);
	}

	public long toLong() {
		return asLong(this.x, this.z);
	}

	public static long asLong(int i, int j) {
		return (long)i & 4294967295L | ((long)j & 4294967295L) << 32;
	}

	public static int getX(long l) {
		return (int)(l & 4294967295L);
	}

	public static int getZ(long l) {
		return (int)(l >>> 32 & 4294967295L);
	}

	public int hashCode() {
		int i = 1664525 * this.x + 1013904223;
		int j = 1664525 * (this.z ^ -559038737) + 1013904223;
		return i ^ j;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof ChunkPos)) {
			return false;
		} else {
			ChunkPos chunkPos = (ChunkPos)object;
			return this.x == chunkPos.x && this.z == chunkPos.z;
		}
	}

	public int getMiddleBlockX() {
		return this.getBlockX(8);
	}

	public int getMiddleBlockZ() {
		return this.getBlockZ(8);
	}

	public int getMinBlockX() {
		return SectionPos.sectionToBlockCoord(this.x);
	}

	public int getMinBlockZ() {
		return SectionPos.sectionToBlockCoord(this.z);
	}

	public int getMaxBlockX() {
		return this.getBlockX(15);
	}

	public int getMaxBlockZ() {
		return this.getBlockZ(15);
	}

	public int getRegionX() {
		return this.x >> 5;
	}

	public int getRegionZ() {
		return this.z >> 5;
	}

	public int getRegionLocalX() {
		return this.x & 31;
	}

	public int getRegionLocalZ() {
		return this.z & 31;
	}

	public BlockPos getBlockAt(int i, int j, int k) {
		return new BlockPos(this.getBlockX(i), j, this.getBlockZ(k));
	}

	public int getBlockX(int i) {
		return SectionPos.sectionToBlockCoord(this.x, i);
	}

	public int getBlockZ(int i) {
		return SectionPos.sectionToBlockCoord(this.z, i);
	}

	public BlockPos getMiddleBlockPosition(int i) {
		return new BlockPos(this.getMiddleBlockX(), i, this.getMiddleBlockZ());
	}

	public String toString() {
		return "[" + this.x + ", " + this.z + "]";
	}

	public BlockPos getWorldPosition() {
		return new BlockPos(this.getMinBlockX(), 0, this.getMinBlockZ());
	}

	public int getChessboardDistance(ChunkPos chunkPos) {
		return Math.max(Math.abs(this.x - chunkPos.x), Math.abs(this.z - chunkPos.z));
	}

	public static Stream<ChunkPos> rangeClosed(ChunkPos chunkPos, int i) {
		return rangeClosed(new ChunkPos(chunkPos.x - i, chunkPos.z - i), new ChunkPos(chunkPos.x + i, chunkPos.z + i));
	}

	public static Stream<ChunkPos> rangeClosed(ChunkPos chunkPos, ChunkPos chunkPos2) {
		int i = Math.abs(chunkPos.x - chunkPos2.x) + 1;
		int j = Math.abs(chunkPos.z - chunkPos2.z) + 1;
		final int k = chunkPos.x < chunkPos2.x ? 1 : -1;
		final int l = chunkPos.z < chunkPos2.z ? 1 : -1;
		return StreamSupport.stream(new AbstractSpliterator<ChunkPos>((long)(i * j), 64) {
			@Nullable
			private ChunkPos pos;

			public boolean tryAdvance(Consumer<? super ChunkPos> consumer) {
				if (this.pos == null) {
					this.pos = chunkPos;
				} else {
					int i = this.pos.x;
					int j = this.pos.z;
					if (i == chunkPos2.x) {
						if (j == chunkPos2.z) {
							return false;
						}

						this.pos = new ChunkPos(chunkPos.x, j + l);
					} else {
						this.pos = new ChunkPos(i + k, j);
					}
				}

				consumer.accept(this.pos);
				return true;
			}
		}, false);
	}
}
