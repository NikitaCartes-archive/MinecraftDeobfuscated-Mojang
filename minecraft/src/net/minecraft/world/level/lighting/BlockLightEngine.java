package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public final class BlockLightEngine extends LayerLightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
	private static final Direction[] DIRECTIONS = Direction.values();
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	public BlockLightEngine(LightChunkGetter lightChunkGetter) {
		this(lightChunkGetter, new BlockLightSectionStorage(lightChunkGetter));
	}

	@VisibleForTesting
	public BlockLightEngine(LightChunkGetter lightChunkGetter, BlockLightSectionStorage blockLightSectionStorage) {
		super(lightChunkGetter, blockLightSectionStorage);
	}

	private int getLightEmission(long l) {
		return this.getState(this.pos.set(l)).getLightEmission();
	}

	@Override
	protected int computeLevelFromNeighbor(long l, long m, int i) {
		if (this.isSource(m)) {
			return 15;
		} else if (this.isSource(l)) {
			return i + 15 - this.getLightEmission(m);
		} else if (i >= 14) {
			return 15;
		} else {
			this.pos.set(m);
			BlockState blockState = this.getState(this.pos);
			int j = this.getOpacity(blockState, this.pos);
			if (j >= 15) {
				return 15;
			} else {
				Direction direction = getDirection(l, m);
				if (direction == null) {
					return 15;
				} else {
					this.pos.set(l);
					BlockState blockState2 = this.getState(this.pos);
					return this.shapeOccludes(l, blockState2, m, blockState, direction) ? 15 : i + Math.max(1, j);
				}
			}
		}
	}

	@Override
	protected void checkNeighborsAfterUpdate(long l, int i, boolean bl) {
		if (!bl || i < this.levelCount - 2) {
			long m = SectionPos.blockToSection(l);

			for (Direction direction : DIRECTIONS) {
				long n = BlockPos.offset(l, direction);
				long o = SectionPos.blockToSection(n);
				if (m == o || this.storage.storingLightForSection(o)) {
					this.checkNeighbor(l, n, i, bl);
				}
			}
		}
	}

	@Override
	protected int getComputedLevel(long l, long m, int i) {
		int j = i;
		if (!this.isSource(m)) {
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
					int q = this.getLevel(dataLayer2, o);
					if (q + 1 < j) {
						int r = this.computeLevelFromNeighbor(o, l, q);
						if (j > r) {
							j = r;
						}

						if (j == 0) {
							return j;
						}
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
