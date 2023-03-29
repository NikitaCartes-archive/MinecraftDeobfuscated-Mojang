package net.minecraft.world.level.lighting;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public final class SkyLightEngine extends LayerLightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
	private static final Direction[] DIRECTIONS = Direction.values();
	private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

	public SkyLightEngine(LightChunkGetter lightChunkGetter) {
		super(lightChunkGetter, new SkyLightSectionStorage(lightChunkGetter));
	}

	@Override
	protected int computeLevelFromNeighbor(long l, long m, int i) {
		if (this.isSource(m) || this.isSource(l)) {
			return 15;
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
					throw new IllegalStateException(
						String.format(
							Locale.ROOT,
							"Light was spread in illegal direction. From %d, %d, %d to %d, %d, %d",
							BlockPos.getX(l),
							BlockPos.getY(l),
							BlockPos.getZ(l),
							BlockPos.getX(m),
							BlockPos.getY(m),
							BlockPos.getZ(m)
						)
					);
				} else {
					this.pos.set(l);
					BlockState blockState2 = this.getState(this.pos);
					if (this.shapeOccludes(l, blockState2, m, blockState, direction)) {
						return 15;
					} else {
						boolean bl = direction == Direction.DOWN;
						return bl && i == 0 && j == 0 ? 0 : i + Math.max(1, j);
					}
				}
			}
		}
	}

	@Override
	protected void checkNeighborsAfterUpdate(long l, int i, boolean bl) {
		if (!bl || i < this.levelCount - 2) {
			long m = SectionPos.blockToSection(l);
			int j = BlockPos.getY(l);
			int k = SectionPos.sectionRelative(j);
			int n = SectionPos.blockToSectionCoord(j);
			int o;
			if (k != 0) {
				o = 0;
			} else {
				int p = 0;

				while (!this.storage.storingLightForSection(SectionPos.offset(m, 0, -p - 1, 0)) && this.storage.hasLightDataAtOrBelow(n - p - 1)) {
					p++;
				}

				o = p;
			}

			long q = BlockPos.offset(l, 0, -1 - o * 16, 0);
			long r = SectionPos.blockToSection(q);
			if (m == r || this.storage.storingLightForSection(r)) {
				this.checkNeighbor(l, q, i, bl);
			}

			long s = BlockPos.offset(l, Direction.UP);
			long t = SectionPos.blockToSection(s);
			if (m == t || this.storage.storingLightForSection(t)) {
				this.checkNeighbor(l, s, i, bl);
			}

			for (Direction direction : HORIZONTALS) {
				int u = 0;

				do {
					long v = BlockPos.offset(l, direction.getStepX(), -u, direction.getStepZ());
					long w = SectionPos.blockToSection(v);
					if (m == w) {
						this.checkNeighbor(l, v, i, bl);
						break;
					}

					if (this.storage.storingLightForSection(w)) {
						long x = BlockPos.offset(l, 0, -u, 0);
						this.checkNeighbor(x, v, i, bl);
					}
				} while (++u > o * 16);
			}
		}
	}

	@Override
	protected int getComputedLevel(long l, long m, int i) {
		int j = i;
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

				int k;
				if (dataLayer2 != null) {
					k = this.getLevel(dataLayer2, o);
				} else {
					if (direction == Direction.DOWN) {
						continue;
					}

					k = 15 - this.storage.getLightValue(o, true);
				}

				if (k + 1 < j || k == 0 && direction == Direction.UP) {
					int q = this.computeLevelFromNeighbor(o, l, k);
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
	protected void checkNode(long l) {
		this.storage.runAllUpdates();
		long m = SectionPos.blockToSection(l);
		if (this.storage.storingLightForSection(m)) {
			super.checkNode(l);
		} else {
			for (l = BlockPos.getFlatIndex(l); !this.storage.storingLightForSection(m) && !this.storage.isAboveData(m); l = BlockPos.offset(l, 0, 16, 0)) {
				m = SectionPos.offset(m, Direction.UP);
			}

			if (this.storage.storingLightForSection(m)) {
				super.checkNode(l);
			}
		}
	}

	@Override
	public String getDebugData(long l) {
		return super.getDebugData(l) + (this.storage.isAboveData(l) ? "*" : "");
	}
}
