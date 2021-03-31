package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableInt;

public final class SkyLightEngine extends LayerLightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
	private static final Direction[] DIRECTIONS = Direction.values();
	private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

	public SkyLightEngine(LightChunkGetter lightChunkGetter) {
		super(lightChunkGetter, LightLayer.SKY, new SkyLightSectionStorage(lightChunkGetter));
	}

	@Override
	protected int computeLevelFromNeighbor(long l, long m, int i) {
		if (m == Long.MAX_VALUE || l == Long.MAX_VALUE) {
			return 15;
		} else if (i >= 15) {
			return i;
		} else {
			MutableInt mutableInt = new MutableInt();
			BlockState blockState = this.getStateAndOpacity(m, mutableInt);
			if (mutableInt.getValue() >= 15) {
				return 15;
			} else {
				int j = BlockPos.getX(l);
				int k = BlockPos.getY(l);
				int n = BlockPos.getZ(l);
				int o = BlockPos.getX(m);
				int p = BlockPos.getY(m);
				int q = BlockPos.getZ(m);
				int r = Integer.signum(o - j);
				int s = Integer.signum(p - k);
				int t = Integer.signum(q - n);
				Direction direction = Direction.fromNormal(r, s, t);
				if (direction == null) {
					throw new IllegalStateException(String.format("Light was spread in illegal direction %d, %d, %d", r, s, t));
				} else {
					BlockState blockState2 = this.getStateAndOpacity(l, null);
					VoxelShape voxelShape = this.getShape(blockState2, l, direction);
					VoxelShape voxelShape2 = this.getShape(blockState, m, direction.getOpposite());
					if (Shapes.faceShapeOccludes(voxelShape, voxelShape2)) {
						return 15;
					} else {
						boolean bl = j == o && n == q;
						boolean bl2 = bl && k > p;
						return bl2 && i == 0 && mutableInt.getValue() == 0 ? 0 : i + Math.max(1, mutableInt.getValue());
					}
				}
			}
		}
	}

	@Override
	protected void checkNeighborsAfterUpdate(long l, int i, boolean bl) {
		long m = SectionPos.blockToSection(l);
		int j = BlockPos.getY(l);
		int k = SectionPos.sectionRelative(j);
		int n = SectionPos.blockToSectionCoord(j);
		int o;
		if (k != 0) {
			o = 0;
		} else {
			int p = 0;

			while (!this.storage.storingLightForSection(SectionPos.offset(m, 0, -p - 1, 0)) && this.storage.hasSectionsBelow(n - p - 1)) {
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

				int q = this.computeLevelFromNeighbor(o, l, k);
				if (j > q) {
					j = q;
				}

				if (j == 0) {
					return j;
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
