package net.minecraft.world.level.lighting;

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
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class LayerLightEngine<M extends DataLayerStorageMap<M>, S extends LayerLightSectionStorage<M>>
	extends DynamicGraphMinFixedPoint
	implements LayerLightEventListener {
	private static final Direction[] DIRECTIONS = Direction.values();
	protected final LightChunkGetter chunkSource;
	protected final S storage;
	private boolean runningLightUpdates;
	protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	private static final int CACHE_SIZE = 2;
	private final long[] lastChunkPos = new long[2];
	private final BlockGetter[] lastChunk = new BlockGetter[2];

	public LayerLightEngine(LightChunkGetter lightChunkGetter, S layerLightSectionStorage) {
		super(16, 256, 8192);
		this.chunkSource = lightChunkGetter;
		this.storage = layerLightSectionStorage;
		this.clearCache();
	}

	@Override
	protected void checkNode(long l) {
		this.storage.runAllUpdates();
		if (this.storage.storingLightForSection(SectionPos.blockToSection(l))) {
			super.checkNode(l);
		}
	}

	@Nullable
	private BlockGetter getChunk(int i, int j) {
		long l = ChunkPos.asLong(i, j);

		for (int k = 0; k < 2; k++) {
			if (l == this.lastChunkPos[k]) {
				return this.lastChunk[k];
			}
		}

		BlockGetter blockGetter = this.chunkSource.getChunkForLighting(i, j);

		for (int m = 1; m > 0; m--) {
			this.lastChunkPos[m] = this.lastChunkPos[m - 1];
			this.lastChunk[m] = this.lastChunk[m - 1];
		}

		this.lastChunkPos[0] = l;
		this.lastChunk[0] = blockGetter;
		return blockGetter;
	}

	private void clearCache() {
		Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
		Arrays.fill(this.lastChunk, null);
	}

	protected BlockState getState(BlockPos blockPos) {
		int i = SectionPos.blockToSectionCoord(blockPos.getX());
		int j = SectionPos.blockToSectionCoord(blockPos.getZ());
		BlockGetter blockGetter = this.getChunk(i, j);
		return blockGetter == null ? Blocks.BEDROCK.defaultBlockState() : blockGetter.getBlockState(blockPos);
	}

	protected int getOpacity(BlockState blockState, BlockPos blockPos) {
		return blockState.getLightBlock(this.chunkSource.getLevel(), blockPos);
	}

	protected boolean shapeOccludes(long l, BlockState blockState, long m, BlockState blockState2, Direction direction) {
		VoxelShape voxelShape = this.getShape(blockState, l, direction);
		VoxelShape voxelShape2 = this.getShape(blockState2, m, direction.getOpposite());
		return Shapes.faceShapeOccludes(voxelShape, voxelShape2);
	}

	private VoxelShape getShape(BlockState blockState, long l, Direction direction) {
		return blockState.canOcclude() && blockState.useShapeForLightOcclusion()
			? blockState.getFaceOcclusionShape(this.chunkSource.getLevel(), this.pos.set(l), direction)
			: Shapes.empty();
	}

	public static int getLightBlockInto(
		BlockGetter blockGetter, BlockState blockState, BlockPos blockPos, BlockState blockState2, BlockPos blockPos2, Direction direction, int i
	) {
		boolean bl = blockState.canOcclude() && blockState.useShapeForLightOcclusion();
		boolean bl2 = blockState2.canOcclude() && blockState2.useShapeForLightOcclusion();
		if (!bl && !bl2) {
			return i;
		} else {
			VoxelShape voxelShape = bl ? blockState.getOcclusionShape(blockGetter, blockPos) : Shapes.empty();
			VoxelShape voxelShape2 = bl2 ? blockState2.getOcclusionShape(blockGetter, blockPos2) : Shapes.empty();
			return Shapes.mergedFaceOccludes(voxelShape, voxelShape2, direction) ? 16 : i;
		}
	}

	@Nullable
	protected static Direction getDirection(long l, long m) {
		int i = BlockPos.getX(m) - BlockPos.getX(l);
		int j = BlockPos.getY(m) - BlockPos.getY(l);
		int k = BlockPos.getZ(m) - BlockPos.getZ(l);
		return Direction.fromDelta(i, j, k);
	}

	@Override
	protected int getComputedLevel(long l, long m, int i) {
		return 0;
	}

	@Override
	protected int getLevel(long l) {
		return this.isSource(l) ? 0 : 15 - this.storage.getStoredLevel(l);
	}

	protected int getLevel(DataLayer dataLayer, long l) {
		return 15
			- dataLayer.get(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)));
	}

	@Override
	protected void setLevel(long l, int i) {
		this.storage.setStoredLevel(l, Math.min(15, 15 - i));
	}

	@Override
	protected int computeLevelFromNeighbor(long l, long m, int i) {
		return 0;
	}

	@Override
	public boolean hasLightWork() {
		return this.hasWork() || this.storage.hasWork() || this.storage.hasInconsistencies();
	}

	@Override
	public int runUpdates(int i, boolean bl, boolean bl2) {
		if (!this.runningLightUpdates) {
			if (this.storage.hasWork()) {
				i = this.storage.runUpdates(i);
				if (i == 0) {
					return i;
				}
			}

			this.storage.markNewInconsistencies(this, bl, bl2);
		}

		this.runningLightUpdates = true;
		if (this.hasWork()) {
			i = this.runUpdates(i);
			this.clearCache();
			if (i == 0) {
				return i;
			}
		}

		this.runningLightUpdates = false;
		this.storage.swapSectionMap();
		return i;
	}

	protected void queueSectionData(long l, @Nullable DataLayer dataLayer, boolean bl) {
		this.storage.queueSectionData(l, dataLayer, bl);
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
		return this.storage.getLevel(l) + "";
	}

	@Override
	public void checkBlock(BlockPos blockPos) {
		long l = blockPos.asLong();
		this.checkNode(l);

		for (Direction direction : DIRECTIONS) {
			this.checkNode(BlockPos.offset(l, direction));
		}
	}

	@Override
	public void onBlockEmissionIncrease(BlockPos blockPos, int i) {
	}

	@Override
	public void updateSectionStatus(SectionPos sectionPos, boolean bl) {
		this.storage.updateSectionStatus(sectionPos.asLong(), bl);
	}

	@Override
	public void enableLightSources(ChunkPos chunkPos, boolean bl) {
		long l = SectionPos.getZeroNode(SectionPos.asLong(chunkPos.x, 0, chunkPos.z));
		this.storage.enableLightSources(l, bl);
	}

	public void retainData(ChunkPos chunkPos, boolean bl) {
		long l = SectionPos.getZeroNode(SectionPos.asLong(chunkPos.x, 0, chunkPos.z));
		this.storage.retainData(l, bl);
	}
}
