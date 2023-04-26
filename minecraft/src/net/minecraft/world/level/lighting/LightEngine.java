package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class LightEngine<M extends DataLayerStorageMap<M>, S extends LayerLightSectionStorage<M>> implements LayerLightEventListener {
	public static final int MAX_LEVEL = 15;
	protected static final int MIN_OPACITY = 1;
	protected static final long PULL_LIGHT_IN_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(1);
	private static final int MIN_QUEUE_SIZE = 512;
	protected static final Direction[] PROPAGATION_DIRECTIONS = Direction.values();
	protected final LightChunkGetter chunkSource;
	protected final S storage;
	private final LongSet blockNodesToCheck = new LongOpenHashSet(512, 0.5F) {
		@Override
		protected void rehash(int i) {
			if (i > 512) {
				super.rehash(i);
			}
		}
	};
	private final LongArrayFIFOQueue decreaseQueue = new LongArrayFIFOQueue();
	private final LongArrayFIFOQueue increaseQueue = new LongArrayFIFOQueue();
	private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
	private static final int CACHE_SIZE = 2;
	private final long[] lastChunkPos = new long[2];
	private final LightChunk[] lastChunk = new LightChunk[2];

	protected LightEngine(LightChunkGetter lightChunkGetter, S layerLightSectionStorage) {
		this.chunkSource = lightChunkGetter;
		this.storage = layerLightSectionStorage;
		this.clearChunkCache();
	}

	public static boolean hasDifferentLightProperties(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockState blockState2) {
		return blockState2 == blockState
			? false
			: blockState2.getLightBlock(blockGetter, blockPos) != blockState.getLightBlock(blockGetter, blockPos)
				|| blockState2.getLightEmission() != blockState.getLightEmission()
				|| blockState2.useShapeForLightOcclusion()
				|| blockState.useShapeForLightOcclusion();
	}

	public static int getLightBlockInto(
		BlockGetter blockGetter, BlockState blockState, BlockPos blockPos, BlockState blockState2, BlockPos blockPos2, Direction direction, int i
	) {
		boolean bl = isEmptyShape(blockState);
		boolean bl2 = isEmptyShape(blockState2);
		if (bl && bl2) {
			return i;
		} else {
			VoxelShape voxelShape = bl ? Shapes.empty() : blockState.getOcclusionShape(blockGetter, blockPos);
			VoxelShape voxelShape2 = bl2 ? Shapes.empty() : blockState2.getOcclusionShape(blockGetter, blockPos2);
			return Shapes.mergedFaceOccludes(voxelShape, voxelShape2, direction) ? 16 : i;
		}
	}

	public static VoxelShape getOcclusionShape(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction) {
		return isEmptyShape(blockState) ? Shapes.empty() : blockState.getFaceOcclusionShape(blockGetter, blockPos, direction);
	}

	protected static boolean isEmptyShape(BlockState blockState) {
		return !blockState.canOcclude() || !blockState.useShapeForLightOcclusion();
	}

	protected BlockState getState(BlockPos blockPos) {
		int i = SectionPos.blockToSectionCoord(blockPos.getX());
		int j = SectionPos.blockToSectionCoord(blockPos.getZ());
		LightChunk lightChunk = this.getChunk(i, j);
		return lightChunk == null ? Blocks.BEDROCK.defaultBlockState() : lightChunk.getBlockState(blockPos);
	}

	protected int getOpacity(BlockState blockState, BlockPos blockPos) {
		return Math.max(1, blockState.getLightBlock(this.chunkSource.getLevel(), blockPos));
	}

	protected boolean shapeOccludes(long l, BlockState blockState, long m, BlockState blockState2, Direction direction) {
		VoxelShape voxelShape = this.getOcclusionShape(blockState, l, direction);
		VoxelShape voxelShape2 = this.getOcclusionShape(blockState2, m, direction.getOpposite());
		return Shapes.faceShapeOccludes(voxelShape, voxelShape2);
	}

	protected VoxelShape getOcclusionShape(BlockState blockState, long l, Direction direction) {
		return getOcclusionShape(this.chunkSource.getLevel(), this.mutablePos.set(l), blockState, direction);
	}

	@Nullable
	protected LightChunk getChunk(int i, int j) {
		long l = ChunkPos.asLong(i, j);

		for (int k = 0; k < 2; k++) {
			if (l == this.lastChunkPos[k]) {
				return this.lastChunk[k];
			}
		}

		LightChunk lightChunk = this.chunkSource.getChunkForLighting(i, j);

		for (int m = 1; m > 0; m--) {
			this.lastChunkPos[m] = this.lastChunkPos[m - 1];
			this.lastChunk[m] = this.lastChunk[m - 1];
		}

		this.lastChunkPos[0] = l;
		this.lastChunk[0] = lightChunk;
		return lightChunk;
	}

	private void clearChunkCache() {
		Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
		Arrays.fill(this.lastChunk, null);
	}

	@Override
	public void checkBlock(BlockPos blockPos) {
		this.blockNodesToCheck.add(blockPos.asLong());
	}

	public void queueSectionData(long l, @Nullable DataLayer dataLayer) {
		this.storage.queueSectionData(l, dataLayer);
	}

	public void retainData(ChunkPos chunkPos, boolean bl) {
		this.storage.retainData(SectionPos.getZeroNode(chunkPos.x, chunkPos.z), bl);
	}

	@Override
	public void updateSectionStatus(SectionPos sectionPos, boolean bl) {
		this.storage.updateSectionStatus(sectionPos.asLong(), bl);
	}

	@Override
	public void setLightEnabled(ChunkPos chunkPos, boolean bl) {
		this.storage.setLightEnabled(SectionPos.getZeroNode(chunkPos.x, chunkPos.z), bl);
	}

	protected void clearQueuedSectionBlocks(long l) {
		if (!this.blockNodesToCheck.isEmpty()) {
			if (this.blockNodesToCheck.size() < 8192) {
				this.blockNodesToCheck.removeIf(mx -> SectionPos.blockToSection(mx) == l);
			} else {
				int i = SectionPos.sectionToBlockCoord(SectionPos.x(l));
				int j = SectionPos.sectionToBlockCoord(SectionPos.y(l));
				int k = SectionPos.sectionToBlockCoord(SectionPos.z(l));

				for (int m = 0; m < 16; m++) {
					for (int n = 0; n < 16; n++) {
						for (int o = 0; o < 16; o++) {
							long p = BlockPos.asLong(i + m, j + n, k + o);
							this.blockNodesToCheck.remove(p);
						}
					}
				}
			}
		}
	}

	@Override
	public int runLightUpdates() {
		this.storage.markNewInconsistencies(this);
		LongIterator longIterator = this.blockNodesToCheck.iterator();

		while (longIterator.hasNext()) {
			this.checkNode(longIterator.nextLong());
		}

		this.blockNodesToCheck.clear();
		int i = 0;
		i += this.propagateDecreases();
		i += this.propagateIncreases();
		this.clearChunkCache();
		this.storage.swapSectionMap();
		return i;
	}

	private int propagateIncreases() {
		int i;
		for (i = 0; !this.increaseQueue.isEmpty(); i++) {
			long l = this.increaseQueue.dequeueLong();
			long m = this.increaseQueue.dequeueLong();
			int j = this.storage.getStoredLevel(l);
			int k = LightEngine.QueueEntry.getFromLevel(m);
			if (LightEngine.QueueEntry.isIncreaseFromEmission(m) && j < k) {
				this.storage.setStoredLevel(l, k);
				j = k;
			}

			if (j == k) {
				this.propagateIncrease(l, m, j);
			}
		}

		return i;
	}

	private int propagateDecreases() {
		int i;
		for (i = 0; !this.decreaseQueue.isEmpty(); i++) {
			long l = this.decreaseQueue.dequeueLong();
			long m = this.decreaseQueue.dequeueLong();
			this.propagateDecrease(l, m);
		}

		return i;
	}

	protected void enqueueDecrease(long l, long m) {
		this.decreaseQueue.enqueue(l);
		this.decreaseQueue.enqueue(m);
	}

	protected void enqueueIncrease(long l, long m) {
		this.increaseQueue.enqueue(l);
		this.increaseQueue.enqueue(m);
	}

	@Override
	public boolean hasLightWork() {
		return this.storage.hasInconsistencies() || !this.blockNodesToCheck.isEmpty() || !this.decreaseQueue.isEmpty() || !this.increaseQueue.isEmpty();
	}

	@Nullable
	@Override
	public DataLayer getDataLayerData(SectionPos sectionPos) {
		return this.storage.getDataLayerData(sectionPos.asLong());
	}

	@Override
	public int getLightValue(BlockPos blockPos) {
		return this.storage.getLightValue(blockPos.asLong());
	}

	public String getDebugData(long l) {
		return this.getDebugSectionType(l).display();
	}

	public LayerLightSectionStorage.SectionType getDebugSectionType(long l) {
		return this.storage.getDebugSectionType(l);
	}

	protected abstract void checkNode(long l);

	protected abstract void propagateIncrease(long l, long m, int i);

	protected abstract void propagateDecrease(long l, long m);

	public static class QueueEntry {
		private static final int FROM_LEVEL_BITS = 4;
		private static final int DIRECTION_BITS = 6;
		private static final long LEVEL_MASK = 15L;
		private static final long DIRECTIONS_MASK = 1008L;
		private static final long FLAG_FROM_EMPTY_SHAPE = 1024L;
		private static final long FLAG_INCREASE_FROM_EMISSION = 2048L;

		public static long decreaseSkipOneDirection(int i, Direction direction) {
			long l = withoutDirection(1008L, direction);
			return withLevel(l, i);
		}

		public static long decreaseAllDirections(int i) {
			return withLevel(1008L, i);
		}

		public static long increaseLightFromEmission(int i, boolean bl) {
			long l = 1008L;
			l |= 2048L;
			if (bl) {
				l |= 1024L;
			}

			return withLevel(l, i);
		}

		public static long increaseSkipOneDirection(int i, boolean bl, Direction direction) {
			long l = withoutDirection(1008L, direction);
			if (bl) {
				l |= 1024L;
			}

			return withLevel(l, i);
		}

		public static long increaseOnlyOneDirection(int i, boolean bl, Direction direction) {
			long l = 0L;
			if (bl) {
				l |= 1024L;
			}

			l = withDirection(l, direction);
			return withLevel(l, i);
		}

		public static long increaseSkySourceInDirections(boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
			long l = withLevel(0L, 15);
			if (bl) {
				l = withDirection(l, Direction.DOWN);
			}

			if (bl2) {
				l = withDirection(l, Direction.NORTH);
			}

			if (bl3) {
				l = withDirection(l, Direction.SOUTH);
			}

			if (bl4) {
				l = withDirection(l, Direction.WEST);
			}

			if (bl5) {
				l = withDirection(l, Direction.EAST);
			}

			return l;
		}

		public static int getFromLevel(long l) {
			return (int)(l & 15L);
		}

		public static boolean isFromEmptyShape(long l) {
			return (l & 1024L) != 0L;
		}

		public static boolean isIncreaseFromEmission(long l) {
			return (l & 2048L) != 0L;
		}

		public static boolean shouldPropagateInDirection(long l, Direction direction) {
			return (l & 1L << direction.ordinal() + 4) != 0L;
		}

		private static long withLevel(long l, int i) {
			return l & -16L | (long)i & 15L;
		}

		private static long withDirection(long l, Direction direction) {
			return l | 1L << direction.ordinal() + 4;
		}

		private static long withoutDirection(long l, Direction direction) {
			return l & ~(1L << direction.ordinal() + 4);
		}
	}
}
