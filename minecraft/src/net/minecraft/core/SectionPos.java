package net.minecraft.core;

import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public class SectionPos extends Vec3i {
	private SectionPos(int i, int j, int k) {
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
		return new SectionPos(
			blockToSectionCoord(Mth.floor(entity.getX())), blockToSectionCoord(Mth.floor(entity.getY())), blockToSectionCoord(Mth.floor(entity.getZ()))
		);
	}

	public static SectionPos of(long l) {
		return new SectionPos(x(l), y(l), z(l));
	}

	public static long offset(long l, Direction direction) {
		return offset(l, direction.getStepX(), direction.getStepY(), direction.getStepZ());
	}

	public static long offset(long l, int i, int j, int k) {
		return asLong(x(l) + i, y(l) + j, z(l) + k);
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
		return (short)(i << 8 | k << 4 | j);
	}

	public static int sectionToBlockCoord(int i) {
		return i << 4;
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
		return this.x() << 4;
	}

	public int minBlockY() {
		return this.y() << 4;
	}

	public int minBlockZ() {
		return this.z() << 4;
	}

	public int maxBlockX() {
		return (this.x() << 4) + 15;
	}

	public int maxBlockY() {
		return (this.y() << 4) + 15;
	}

	public int maxBlockZ() {
		return (this.z() << 4) + 15;
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

	public static long asLong(int i, int j, int k) {
		long l = 0L;
		l |= ((long)i & 4194303L) << 42;
		l |= ((long)j & 1048575L) << 0;
		return l | ((long)k & 4194303L) << 20;
	}

	public long asLong() {
		return asLong(this.x(), this.y(), this.z());
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

	public static Stream<SectionPos> aroundChunk(ChunkPos chunkPos, int i) {
		int j = chunkPos.x;
		int k = chunkPos.z;
		return betweenClosedStream(j - i, 0, k - i, j + i, 15, k + i);
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
