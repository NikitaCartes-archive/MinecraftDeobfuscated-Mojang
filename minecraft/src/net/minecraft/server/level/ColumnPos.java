package net.minecraft.server.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public class ColumnPos {
	private static final long COORD_BITS = 32L;
	private static final long COORD_MASK = 4294967295L;
	private static final int HASH_A = 1664525;
	private static final int HASH_C = 1013904223;
	private static final int HASH_Z_XOR = -559038737;
	public final int x;
	public final int z;

	public ColumnPos(int i, int j) {
		this.x = i;
		this.z = j;
	}

	public ColumnPos(BlockPos blockPos) {
		this.x = blockPos.getX();
		this.z = blockPos.getZ();
	}

	public ChunkPos toChunkPos() {
		return new ChunkPos(SectionPos.blockToSectionCoord(this.x), SectionPos.blockToSectionCoord(this.z));
	}

	public long toLong() {
		return asLong(this.x, this.z);
	}

	public static long asLong(int i, int j) {
		return (long)i & 4294967295L | ((long)j & 4294967295L) << 32;
	}

	public String toString() {
		return "[" + this.x + ", " + this.z + "]";
	}

	public int hashCode() {
		int i = 1664525 * this.x + 1013904223;
		int j = 1664525 * (this.z ^ -559038737) + 1013904223;
		return i ^ j;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof ColumnPos)) {
			return false;
		} else {
			ColumnPos columnPos = (ColumnPos)object;
			return this.x == columnPos.x && this.z == columnPos.z;
		}
	}
}
