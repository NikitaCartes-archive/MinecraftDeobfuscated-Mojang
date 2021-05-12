package net.minecraft.core;

import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;

public class SectionPos extends Vec3i {
	public static final int SECTION_BITS = 4;
	public static final int SECTION_SIZE = 16;
	private static final int SECTION_MASK = 15;
	public static final int SECTION_HALF_SIZE = 8;
	public static final int SECTION_MAX_INDEX = 15;
	private static final int PACKED_X_LENGTH = 22;
	private static final int PACKED_Y_LENGTH = 20;
	private static final int PACKED_Z_LENGTH = 22;
	private static final long PACKED_X_MASK = 4194303L;
	private static final long PACKED_Y_MASK = 1048575L;
	private static final long PACKED_Z_MASK = 4194303L;
	private static final int Y_OFFSET = 0;
	private static final int Z_OFFSET = 20;
	private static final int X_OFFSET = 42;
	private static final int RELATIVE_X_SHIFT = 8;
	private static final int RELATIVE_Y_SHIFT = 0;
	private static final int RELATIVE_Z_SHIFT = 4;

	SectionPos(int i, int j, int k) {
		super(i, j, k);
	}

	public static SectionPos of(int i, int j, int k) {
		return new SectionPos(i, j, k);
	}

	public static SectionPos of(BlockPos blockPos) {
		return new SectionPos(blockToSectionCoord(blockPos.getX()), blockToSectionCoord(blockPos.getY()), blockToSectionCoord(blockPos.getZ()));
	}

	public static SectionPos of(ChunkPos chunkPos, int i) {
		return new SectionPos(chunkPos.x, i, chunkPos.z);
	}

	public static SectionPos of(Entity entity) {
		return new SectionPos(blockToSectionCoord(entity.getBlockX()), blockToSectionCoord(entity.getBlockY()), blockToSectionCoord(entity.getBlockZ()));
	}

	public static SectionPos of(long l) {
		return new SectionPos(x(l), y(l), z(l));
	}

	public static SectionPos bottomOf(ChunkAccess chunkAccess) {
		return of(chunkAccess.getPos(), chunkAccess.getMinSection());
	}

	public static long offset(long l, Direction direction) {
		return offset(l, direction.getStepX(), direction.getStepY(), direction.getStepZ());
	}

	public static long offset(long l, int i, int j, int k) {
		return asLong(x(l) + i, y(l) + j, z(l) + k);
	}

	public static int posToSectionCoord(double d) {
		return blockToSectionCoord(Mth.floor(d));
	}

	public static int blockToSectionCoord(int i) {
		return i >> 4;
	}

	public static int sectionRelative(int i) {
		return i & 15;
	}

	public static short sectionRelativePos(BlockPos blockPos) {
		int i = sectionRelative(blockPos.getX());
		int j = sectionRelative(blockPos.getY());
		int k = sectionRelative(blockPos.getZ());
		return (short)(i << 8 | k << 4 | j << 0);
	}

	public static int sectionRelativeX(short s) {
		return s >>> 8 & 15;
	}

	public static int sectionRelativeY(short s) {
		return s >>> 0 & 15;
	}

	public static int sectionRelativeZ(short s) {
		return s >>> 4 & 15;
	}

	public int relativeToBlockX(short s) {
		return this.minBlockX() + sectionRelativeX(s);
	}

	public int relativeToBlockY(short s) {
		return this.minBlockY() + sectionRelativeY(s);
	}

	public int relativeToBlockZ(short s) {
		return this.minBlockZ() + sectionRelativeZ(s);
	}

	public BlockPos relativeToBlockPos(short s) {
		return new BlockPos(this.relativeToBlockX(s), this.relativeToBlockY(s), this.relativeToBlockZ(s));
	}

	public static int sectionToBlockCoord(int i) {
		return i << 4;
	}

	public static int sectionToBlockCoord(int i, int j) {
		return sectionToBlockCoord(i) + j;
	}

	public static int x(long l) {
		return (int)(l << 0 >> 42);
	}

	public static int y(long l) {
		return (int)(l << 44 >> 44);
	}

	public static int z(long l) {
		return (int)(l << 22 >> 42);
	}

	public int x() {
		return this.getX();
	}

	public int y() {
		return this.getY();
	}

	public int z() {
		return this.getZ();
	}

	public int minBlockX() {
		return sectionToBlockCoord(this.x());
	}

	public int minBlockY() {
		return sectionToBlockCoord(this.y());
	}

	public int minBlockZ() {
		return sectionToBlockCoord(this.z());
	}

	public int maxBlockX() {
		return sectionToBlockCoord(this.x(), 15);
	}

	public int maxBlockY() {
		return sectionToBlockCoord(this.y(), 15);
	}

	public int maxBlockZ() {
		return sectionToBlockCoord(this.z(), 15);
	}

	public static long blockToSection(long l) {
		return asLong(blockToSectionCoord(BlockPos.getX(l)), blockToSectionCoord(BlockPos.getY(l)), blockToSectionCoord(BlockPos.getZ(l)));
	}

	public static long getZeroNode(long l) {
		return l & -1048576L;
	}

	public BlockPos origin() {
		return new BlockPos(sectionToBlockCoord(this.x()), sectionToBlockCoord(this.y()), sectionToBlockCoord(this.z()));
	}

	public BlockPos center() {
		int i = 8;
		return this.origin().offset(8, 8, 8);
	}

	public ChunkPos chunk() {
		return new ChunkPos(this.x(), this.z());
	}

	public static long asLong(BlockPos blockPos) {
		return asLong(blockToSectionCoord(blockPos.getX()), blockToSectionCoord(blockPos.getY()), blockToSectionCoord(blockPos.getZ()));
	}

	public static long asLong(int i, int j, int k) {
		long l = 0L;
		l |= ((long)i & 4194303L) << 42;
		l |= ((long)j & 1048575L) << 0;
		return l | ((long)k & 4194303L) << 20;
	}

	public long asLong() {
		return asLong(this.x(), this.y(), this.z());
	}

	public SectionPos offset(int i, int j, int k) {
		return i == 0 && j == 0 && k == 0 ? this : new SectionPos(this.x() + i, this.y() + j, this.z() + k);
	}

	public Stream<BlockPos> blocksInside() {
		return BlockPos.betweenClosedStream(this.minBlockX(), this.minBlockY(), this.minBlockZ(), this.maxBlockX(), this.maxBlockY(), this.maxBlockZ());
	}

	public static Stream<SectionPos> cube(SectionPos sectionPos, int i) {
		int j = sectionPos.x();
		int k = sectionPos.y();
		int l = sectionPos.z();
		return betweenClosedStream(j - i, k - i, l - i, j + i, k + i, l + i);
	}

	public static Stream<SectionPos> aroundChunk(ChunkPos chunkPos, int i, int j, int k) {
		int l = chunkPos.x;
		int m = chunkPos.z;
		return betweenClosedStream(l - i, j, m - i, l + i, k - 1, m + i);
	}

	public static Stream<SectionPos> betweenClosedStream(int i, int j, int k, int l, int m, int n) {
		return StreamSupport.stream(new AbstractSpliterator<SectionPos>((long)((l - i + 1) * (m - j + 1) * (n - k + 1)), 64) {
			final Cursor3D cursor = new Cursor3D(i, j, k, l, m, n);

			public boolean tryAdvance(Consumer<? super SectionPos> consumer) {
				if (this.cursor.advance()) {
					consumer.accept(new SectionPos(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()));
					return true;
				} else {
					return false;
				}
			}
		}, false);
	}
}
