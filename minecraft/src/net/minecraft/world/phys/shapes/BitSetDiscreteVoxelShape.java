package net.minecraft.world.phys.shapes;

import java.util.BitSet;
import net.minecraft.core.Direction;

public final class BitSetDiscreteVoxelShape extends DiscreteVoxelShape {
	private final BitSet storage;
	private int xMin;
	private int yMin;
	private int zMin;
	private int xMax;
	private int yMax;
	private int zMax;

	public BitSetDiscreteVoxelShape(int i, int j, int k) {
		super(i, j, k);
		this.storage = new BitSet(i * j * k);
		this.xMin = i;
		this.yMin = j;
		this.zMin = k;
	}

	public static BitSetDiscreteVoxelShape withFilledBounds(int i, int j, int k, int l, int m, int n, int o, int p, int q) {
		BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape(i, j, k);
		bitSetDiscreteVoxelShape.xMin = l;
		bitSetDiscreteVoxelShape.yMin = m;
		bitSetDiscreteVoxelShape.zMin = n;
		bitSetDiscreteVoxelShape.xMax = o;
		bitSetDiscreteVoxelShape.yMax = p;
		bitSetDiscreteVoxelShape.zMax = q;

		for (int r = l; r < o; r++) {
			for (int s = m; s < p; s++) {
				for (int t = n; t < q; t++) {
					bitSetDiscreteVoxelShape.fillUpdateBounds(r, s, t, false);
				}
			}
		}

		return bitSetDiscreteVoxelShape;
	}

	public BitSetDiscreteVoxelShape(DiscreteVoxelShape discreteVoxelShape) {
		super(discreteVoxelShape.xSize, discreteVoxelShape.ySize, discreteVoxelShape.zSize);
		if (discreteVoxelShape instanceof BitSetDiscreteVoxelShape) {
			this.storage = (BitSet)((BitSetDiscreteVoxelShape)discreteVoxelShape).storage.clone();
		} else {
			this.storage = new BitSet(this.xSize * this.ySize * this.zSize);

			for (int i = 0; i < this.xSize; i++) {
				for (int j = 0; j < this.ySize; j++) {
					for (int k = 0; k < this.zSize; k++) {
						if (discreteVoxelShape.isFull(i, j, k)) {
							this.storage.set(this.getIndex(i, j, k));
						}
					}
				}
			}
		}

		this.xMin = discreteVoxelShape.firstFull(Direction.Axis.X);
		this.yMin = discreteVoxelShape.firstFull(Direction.Axis.Y);
		this.zMin = discreteVoxelShape.firstFull(Direction.Axis.Z);
		this.xMax = discreteVoxelShape.lastFull(Direction.Axis.X);
		this.yMax = discreteVoxelShape.lastFull(Direction.Axis.Y);
		this.zMax = discreteVoxelShape.lastFull(Direction.Axis.Z);
	}

	protected int getIndex(int i, int j, int k) {
		return (i * this.ySize + j) * this.zSize + k;
	}

	@Override
	public boolean isFull(int i, int j, int k) {
		return this.storage.get(this.getIndex(i, j, k));
	}

	private void fillUpdateBounds(int i, int j, int k, boolean bl) {
		this.storage.set(this.getIndex(i, j, k));
		if (bl) {
			this.xMin = Math.min(this.xMin, i);
			this.yMin = Math.min(this.yMin, j);
			this.zMin = Math.min(this.zMin, k);
			this.xMax = Math.max(this.xMax, i + 1);
			this.yMax = Math.max(this.yMax, j + 1);
			this.zMax = Math.max(this.zMax, k + 1);
		}
	}

	@Override
	public void fill(int i, int j, int k) {
		this.fillUpdateBounds(i, j, k, true);
	}

	@Override
	public boolean isEmpty() {
		return this.storage.isEmpty();
	}

	@Override
	public int firstFull(Direction.Axis axis) {
		return axis.choose(this.xMin, this.yMin, this.zMin);
	}

	@Override
	public int lastFull(Direction.Axis axis) {
		return axis.choose(this.xMax, this.yMax, this.zMax);
	}

	static BitSetDiscreteVoxelShape join(
		DiscreteVoxelShape discreteVoxelShape,
		DiscreteVoxelShape discreteVoxelShape2,
		IndexMerger indexMerger,
		IndexMerger indexMerger2,
		IndexMerger indexMerger3,
		BooleanOp booleanOp
	) {
		BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape(indexMerger.size() - 1, indexMerger2.size() - 1, indexMerger3.size() - 1);
		int[] is = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
		indexMerger.forMergedIndexes((i, j, k) -> {
			boolean[] bls = new boolean[]{false};
			indexMerger2.forMergedIndexes((l, m, n) -> {
				boolean[] bls2 = new boolean[]{false};
				indexMerger3.forMergedIndexes((o, p, q) -> {
					if (booleanOp.apply(discreteVoxelShape.isFullWide(i, l, o), discreteVoxelShape2.isFullWide(j, m, p))) {
						bitSetDiscreteVoxelShape.storage.set(bitSetDiscreteVoxelShape.getIndex(k, n, q));
						is[2] = Math.min(is[2], q);
						is[5] = Math.max(is[5], q);
						bls2[0] = true;
					}

					return true;
				});
				if (bls2[0]) {
					is[1] = Math.min(is[1], n);
					is[4] = Math.max(is[4], n);
					bls[0] = true;
				}

				return true;
			});
			if (bls[0]) {
				is[0] = Math.min(is[0], k);
				is[3] = Math.max(is[3], k);
			}

			return true;
		});
		bitSetDiscreteVoxelShape.xMin = is[0];
		bitSetDiscreteVoxelShape.yMin = is[1];
		bitSetDiscreteVoxelShape.zMin = is[2];
		bitSetDiscreteVoxelShape.xMax = is[3] + 1;
		bitSetDiscreteVoxelShape.yMax = is[4] + 1;
		bitSetDiscreteVoxelShape.zMax = is[5] + 1;
		return bitSetDiscreteVoxelShape;
	}

	protected static void forAllBoxes(DiscreteVoxelShape discreteVoxelShape, DiscreteVoxelShape.IntLineConsumer intLineConsumer, boolean bl) {
		BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape(discreteVoxelShape);

		for (int i = 0; i < bitSetDiscreteVoxelShape.ySize; i++) {
			for (int j = 0; j < bitSetDiscreteVoxelShape.xSize; j++) {
				int k = -1;

				for (int l = 0; l <= bitSetDiscreteVoxelShape.zSize; l++) {
					if (bitSetDiscreteVoxelShape.isFullWide(j, i, l)) {
						if (bl) {
							if (k == -1) {
								k = l;
							}
						} else {
							intLineConsumer.consume(j, i, l, j + 1, i + 1, l + 1);
						}
					} else if (k != -1) {
						int m = j;
						int n = i;
						bitSetDiscreteVoxelShape.clearZStrip(k, l, j, i);

						while (bitSetDiscreteVoxelShape.isZStripFull(k, l, m + 1, i)) {
							bitSetDiscreteVoxelShape.clearZStrip(k, l, m + 1, i);
							m++;
						}

						while (bitSetDiscreteVoxelShape.isXZRectangleFull(j, m + 1, k, l, n + 1)) {
							for (int o = j; o <= m; o++) {
								bitSetDiscreteVoxelShape.clearZStrip(k, l, o, n + 1);
							}

							n++;
						}

						intLineConsumer.consume(j, i, k, m + 1, n + 1, l);
						k = -1;
					}
				}
			}
		}
	}

	private boolean isZStripFull(int i, int j, int k, int l) {
		return k < this.xSize && l < this.ySize ? this.storage.nextClearBit(this.getIndex(k, l, i)) >= this.getIndex(k, l, j) : false;
	}

	private boolean isXZRectangleFull(int i, int j, int k, int l, int m) {
		for (int n = i; n < j; n++) {
			if (!this.isZStripFull(k, l, n, m)) {
				return false;
			}
		}

		return true;
	}

	private void clearZStrip(int i, int j, int k, int l) {
		this.storage.clear(this.getIndex(k, l, i), this.getIndex(k, l, j));
	}

	public boolean isInterior(int i, int j, int k) {
		boolean bl = i > 0 && i < this.xSize - 1 && j > 0 && j < this.ySize - 1 && k > 0 && k < this.zSize - 1;
		return bl
			&& this.isFull(i, j, k)
			&& this.isFull(i - 1, j, k)
			&& this.isFull(i + 1, j, k)
			&& this.isFull(i, j - 1, k)
			&& this.isFull(i, j + 1, k)
			&& this.isFull(i, j, k - 1)
			&& this.isFull(i, j, k + 1);
	}
}
