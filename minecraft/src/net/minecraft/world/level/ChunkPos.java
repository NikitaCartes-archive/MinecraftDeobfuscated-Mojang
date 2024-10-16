package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ChunkPos {
	public static final Codec<ChunkPos> CODEC = Codec.INT_STREAM
		.<ChunkPos>comapFlatMap(
			intStream -> Util.fixedSize(intStream, 2).map(is -> new ChunkPos(is[0], is[1])), chunkPos -> IntStream.of(new int[]{chunkPos.x, chunkPos.z})
		)
		.stable();
	public static final StreamCodec<ByteBuf, ChunkPos> STREAM_CODEC = new StreamCodec<ByteBuf, ChunkPos>() {
		public ChunkPos decode(ByteBuf byteBuf) {
			return FriendlyByteBuf.readChunkPos(byteBuf);
		}

		public void encode(ByteBuf byteBuf, ChunkPos chunkPos) {
			FriendlyByteBuf.writeChunkPos(byteBuf, chunkPos);
		}
	};
	private static final int SAFETY_MARGIN = 1056;
	public static final long INVALID_CHUNK_POS = asLong(1875066, 1875066);
	private static final int SAFETY_MARGIN_CHUNKS = (32 + ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FULL).accumulatedDependencies().size() + 1) * 2;
	public static final int MAX_COORDINATE_VALUE = SectionPos.blockToSectionCoord(BlockPos.MAX_HORIZONTAL_COORDINATE) - SAFETY_MARGIN_CHUNKS;
	public static final ChunkPos ZERO = new ChunkPos(0, 0);
	private static final long COORD_BITS = 32L;
	private static final long COORD_MASK = 4294967295L;
	private static final int REGION_BITS = 5;
	public static final int REGION_SIZE = 32;
	private static final int REGION_MASK = 31;
	public static final int REGION_MAX_INDEX = 31;
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

	public static ChunkPos minFromRegion(int i, int j) {
		return new ChunkPos(i << 5, j << 5);
	}

	public static ChunkPos maxFromRegion(int i, int j) {
		return new ChunkPos((i << 5) + 31, (j << 5) + 31);
	}

	public long toLong() {
		return asLong(this.x, this.z);
	}

	public static long asLong(int i, int j) {
		return (long)i & 4294967295L | ((long)j & 4294967295L) << 32;
	}

	public static long asLong(BlockPos blockPos) {
		return asLong(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
	}

	public static int getX(long l) {
		return (int)(l & 4294967295L);
	}

	public static int getZ(long l) {
		return (int)(l >>> 32 & 4294967295L);
	}

	public int hashCode() {
		return hash(this.x, this.z);
	}

	public static int hash(int i, int j) {
		int k = 1664525 * i + 1013904223;
		int l = 1664525 * (j ^ -559038737) + 1013904223;
		return k ^ l;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof ChunkPos chunkPos) ? false : this.x == chunkPos.x && this.z == chunkPos.z;
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
		return this.getChessboardDistance(chunkPos.x, chunkPos.z);
	}

	public int getChessboardDistance(int i, int j) {
		return Math.max(Math.abs(this.x - i), Math.abs(this.z - j));
	}

	public int distanceSquared(ChunkPos chunkPos) {
		return this.distanceSquared(chunkPos.x, chunkPos.z);
	}

	public int distanceSquared(long l) {
		return this.distanceSquared(getX(l), getZ(l));
	}

	private int distanceSquared(int i, int j) {
		int k = i - this.x;
		int l = j - this.z;
		return k * k + l * l;
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
