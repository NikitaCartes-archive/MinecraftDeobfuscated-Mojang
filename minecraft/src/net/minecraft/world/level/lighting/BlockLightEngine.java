package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableInt;

public final class BlockLightEngine extends LayerLightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
	private static final Direction[] DIRECTIONS = Direction.values();
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	public BlockLightEngine(LightChunkGetter lightChunkGetter) {
		super(lightChunkGetter, LightLayer.BLOCK, new BlockLightSectionStorage(lightChunkGetter));
	}

	private int getLightEmission(long l) {
		int i = BlockPos.getX(l);
		int j = BlockPos.getY(l);
		int k = BlockPos.getZ(l);
		BlockGetter blockGetter = this.chunkSource.getChunkForLighting(i >> 4, k >> 4);
		return blockGetter != null ? blockGetter.getLightEmission(this.pos.set(i, j, k)) : 0;
	}

	@Override
	protected int computeLevelFromNeighbor(long l, long m, int i) {
		if (m == Long.MAX_VALUE) {
			return 15;
		} else if (l == Long.MAX_VALUE) {
			return i + 15 - this.getLightEmission(m);
		} else if (i >= 15) {
			return i;
		} else {
			int j = Integer.signum(BlockPos.getX(m) - BlockPos.getX(l));
			int k = Integer.signum(BlockPos.getY(m) - BlockPos.getY(l));
			int n = Integer.signum(BlockPos.getZ(m) - BlockPos.getZ(l));
			Direction direction = Direction.fromNormal(j, k, n);
			if (direction == null) {
				return 15;
			} else {
				MutableInt mutableInt = new MutableInt();
				BlockState blockState = this.getStateAndOpacity(m, mutableInt);
				if (mutableInt.getValue() >= 15) {
					return 15;
				} else {
					BlockState blockState2 = this.getStateAndOpacity(l, null);
					VoxelShape voxelShape = this.getShape(blockState2, l, direction);
					VoxelShape voxelShape2 = this.getShape(blockState, m, direction.getOpposite());
					return Shapes.faceShapeOccludes(voxelShape, voxelShape2) ? 15 : i + Math.max(1, mutableInt.getValue());
				}
			}
		}
	}

	@Override
	protected void checkNeighborsAfterUpdate(long l, int i, boolean bl) {
		long m = SectionPos.blockToSection(l);

		for (Direction direction : DIRECTIONS) {
			long n = BlockPos.offset(l, direction);
			long o = SectionPos.blockToSection(n);
			if (m == o || this.storage.storingLightForSection(o)) {
				this.checkNeighbor(l, n, i, bl);
			}
		}
	}

	@Override
	protected int getComputedLevel(long l, long m, int i) {
		int j = i;
		if (Long.MAX_VALUE != m) {
			int k = this.computeLevelFromNeighbor(Long.MAX_VALUE, l, 0);
			if (i > k) {
				j = k;
			}

			if (j == 0) {
				return j;
			}
		}

		long n = SectionPos.blockToSection(l);
		DataLayer dataLayer = this.storage.getDataLayer(n, true);

		for (Direction direction : DIRECTIONS) {
			long o = BlockPos.offset(l, direction);
			if (o != m) {
				long p = SectionPos.blockToSection(o);
				DataLayer dataLayer2;
				if (n == p) {
					dataLayer2 = dataLayer;
				} else {
					dataLayer2 = this.storage.getDataLayer(p, true);
				}

				if (dataLayer2 != null) {
					int q = this.computeLevelFromNeighbor(o, l, this.getLevel(dataLayer2, o));
					if (j > q) {
						j = q;
					}

					if (j == 0) {
						return j;
					}
				}
			}
		}

		return j;
	}

	@Override
	public void onBlockEmissionIncrease(BlockPos blockPos, int i) {
		this.storage.runAllUpdates();
		this.checkEdge(Long.MAX_VALUE, blockPos.asLong(), 15 - i, true);
	}
}
