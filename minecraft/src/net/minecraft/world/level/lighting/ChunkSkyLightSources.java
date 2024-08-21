package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChunkSkyLightSources {
	private static final int SIZE = 16;
	public static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;
	private final int minY;
	private final BitStorage heightmap;
	private final BlockPos.MutableBlockPos mutablePos1 = new BlockPos.MutableBlockPos();
	private final BlockPos.MutableBlockPos mutablePos2 = new BlockPos.MutableBlockPos();

	public ChunkSkyLightSources(LevelHeightAccessor levelHeightAccessor) {
		this.minY = levelHeightAccessor.getMinY() - 1;
		int i = levelHeightAccessor.getMaxY() + 1;
		int j = Mth.ceillog2(i - this.minY + 1);
		this.heightmap = new SimpleBitStorage(j, 256);
	}

	public void fillFrom(ChunkAccess chunkAccess) {
		int i = chunkAccess.getHighestFilledSectionIndex();
		if (i == -1) {
			this.fill(this.minY);
		} else {
			for (int j = 0; j < 16; j++) {
				for (int k = 0; k < 16; k++) {
					int l = Math.max(this.findLowestSourceY(chunkAccess, i, k, j), this.minY);
					this.set(index(k, j), l);
				}
			}
		}
	}

	private int findLowestSourceY(ChunkAccess chunkAccess, int i, int j, int k) {
		int l = SectionPos.sectionToBlockCoord(chunkAccess.getSectionYFromSectionIndex(i) + 1);
		BlockPos.MutableBlockPos mutableBlockPos = this.mutablePos1.set(j, l, k);
		BlockPos.MutableBlockPos mutableBlockPos2 = this.mutablePos2.setWithOffset(mutableBlockPos, Direction.DOWN);
		BlockState blockState = Blocks.AIR.defaultBlockState();

		for (int m = i; m >= 0; m--) {
			LevelChunkSection levelChunkSection = chunkAccess.getSection(m);
			if (levelChunkSection.hasOnlyAir()) {
				blockState = Blocks.AIR.defaultBlockState();
				int n = chunkAccess.getSectionYFromSectionIndex(m);
				mutableBlockPos.setY(SectionPos.sectionToBlockCoord(n));
				mutableBlockPos2.setY(mutableBlockPos.getY() - 1);
			} else {
				for (int n = 15; n >= 0; n--) {
					BlockState blockState2 = levelChunkSection.getBlockState(j, n, k);
					if (isEdgeOccluded(blockState, blockState2)) {
						return mutableBlockPos.getY();
					}

					blockState = blockState2;
					mutableBlockPos.set(mutableBlockPos2);
					mutableBlockPos2.move(Direction.DOWN);
				}
			}
		}

		return this.minY;
	}

	public boolean update(BlockGetter blockGetter, int i, int j, int k) {
		int l = j + 1;
		int m = index(i, k);
		int n = this.get(m);
		if (l < n) {
			return false;
		} else {
			BlockPos blockPos = this.mutablePos1.set(i, j + 1, k);
			BlockState blockState = blockGetter.getBlockState(blockPos);
			BlockPos blockPos2 = this.mutablePos2.set(i, j, k);
			BlockState blockState2 = blockGetter.getBlockState(blockPos2);
			if (this.updateEdge(blockGetter, m, n, blockPos, blockState, blockPos2, blockState2)) {
				return true;
			} else {
				BlockPos blockPos3 = this.mutablePos1.set(i, j - 1, k);
				BlockState blockState3 = blockGetter.getBlockState(blockPos3);
				return this.updateEdge(blockGetter, m, n, blockPos2, blockState2, blockPos3, blockState3);
			}
		}
	}

	private boolean updateEdge(BlockGetter blockGetter, int i, int j, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
		int k = blockPos.getY();
		if (isEdgeOccluded(blockState, blockState2)) {
			if (k > j) {
				this.set(i, k);
				return true;
			}
		} else if (k == j) {
			this.set(i, this.findLowestSourceBelow(blockGetter, blockPos2, blockState2));
			return true;
		}

		return false;
	}

	private int findLowestSourceBelow(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		BlockPos.MutableBlockPos mutableBlockPos = this.mutablePos1.set(blockPos);
		BlockPos.MutableBlockPos mutableBlockPos2 = this.mutablePos2.setWithOffset(blockPos, Direction.DOWN);
		BlockState blockState2 = blockState;

		while (mutableBlockPos2.getY() >= this.minY) {
			BlockState blockState3 = blockGetter.getBlockState(mutableBlockPos2);
			if (isEdgeOccluded(blockState2, blockState3)) {
				return mutableBlockPos.getY();
			}

			blockState2 = blockState3;
			mutableBlockPos.set(mutableBlockPos2);
			mutableBlockPos2.move(Direction.DOWN);
		}

		return this.minY;
	}

	private static boolean isEdgeOccluded(BlockState blockState, BlockState blockState2) {
		if (blockState2.getLightBlock() != 0) {
			return true;
		} else {
			VoxelShape voxelShape = LightEngine.getOcclusionShape(blockState, Direction.DOWN);
			VoxelShape voxelShape2 = LightEngine.getOcclusionShape(blockState2, Direction.UP);
			return Shapes.faceShapeOccludes(voxelShape, voxelShape2);
		}
	}

	public int getLowestSourceY(int i, int j) {
		int k = this.get(index(i, j));
		return this.extendSourcesBelowWorld(k);
	}

	public int getHighestLowestSourceY() {
		int i = Integer.MIN_VALUE;

		for (int j = 0; j < this.heightmap.getSize(); j++) {
			int k = this.heightmap.get(j);
			if (k > i) {
				i = k;
			}
		}

		return this.extendSourcesBelowWorld(i + this.minY);
	}

	private void fill(int i) {
		int j = i - this.minY;

		for (int k = 0; k < this.heightmap.getSize(); k++) {
			this.heightmap.set(k, j);
		}
	}

	private void set(int i, int j) {
		this.heightmap.set(i, j - this.minY);
	}

	private int get(int i) {
		return this.heightmap.get(i) + this.minY;
	}

	private int extendSourcesBelowWorld(int i) {
		return i == this.minY ? Integer.MIN_VALUE : i;
	}

	private static int index(int i, int j) {
		return i + j * 16;
	}
}
