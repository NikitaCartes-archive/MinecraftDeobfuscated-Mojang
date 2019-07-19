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
		this(i, j, k, i, j, k, 0, 0, 0);
	}

	public BitSetDiscreteVoxelShape(int i, int j, int k, int l, int m, int n, int o, int p, int q) {
		super(i, j, k);
		this.storage = new BitSet(i * j * k);
		this.xMin = l;
		this.yMin = m;
		this.zMin = n;
		this.xMax = o;
		this.yMax = p;
		this.zMax = q;
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

	@Override
	public void setFull(int i, int j, int k, boolean bl, boolean bl2) {
		this.storage.set(this.getIndex(i, j, k), bl2);
		if (bl && bl2) {
			this.xMin = Math.min(this.xMin, i);
			this.yMin = Math.min(this.yMin, j);
			this.zMin = Math.min(this.zMin, k);
			this.xMax = Math.max(this.xMax, i + 1);
			this.yMax = Math.max(this.yMax, j + 1);
			this.zMax = Math.max(this.zMax, k + 1);
		}
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

	@Override
	protected boolean isZStripFull(int i, int j, int k, int l) {
		if (k < 0 || l < 0 || i < 0) {
			return false;
		} else {
			return k < this.xSize && l < this.ySize && j <= this.zSize ? this.storage.nextClearBit(this.getIndex(k, l, i)) >= this.getIndex(k, l, j) : false;
		}
	}

	@Override
	protected void setZStrip(int i, int j, int k, int l, boolean bl) {
		this.storage.set(this.getIndex(k, l, i), this.getIndex(k, l, j), bl);
	}

	static BitSetDiscreteVoxelShape join(
		DiscreteVoxelShape discreteVoxelShape,
		DiscreteVoxelShape discreteVoxelShape2,
		IndexMerger indexMerger,
		IndexMerger indexMerger2,
		IndexMerger indexMerger3,
		BooleanOp booleanOp
	) {
		BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape(
			indexMerger.getList().size() - 1, indexMerger2.getList().size() - 1, indexMerger3.getList().size() - 1
		);
		int[] is = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
		indexMerger.forMergedIndexes((i, j, k) -> {
			boolean[] bls = new boolean[]{false};
			boolean bl = indexMerger2.forMergedIndexes((l, m, n) -> {
				boolean[] bls2 = new boolean[]{false};
				boolean blx = indexMerger3.forMergedIndexes((o, p, q) -> {
					boolean blxx = booleanOp.apply(discreteVoxelShape.isFullWide(i, l, o), discreteVoxelShape2.isFullWide(j, m, p));
					if (blxx) {
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

				return blx;
			});
			if (bls[0]) {
				is[0] = Math.min(is[0], k);
				is[3] = Math.max(is[3], k);
			}

			return bl;
		});
		bitSetDiscreteVoxelShape.xMin = is[0];
		bitSetDiscreteVoxelShape.yMin = is[1];
		bitSetDiscreteVoxelShape.zMin = is[2];
		bitSetDiscreteVoxelShape.xMax = is[3] + 1;
		bitSetDiscreteVoxelShape.yMax = is[4] + 1;
		bitSetDiscreteVoxelShape.zMax = is[5] + 1;
		return bitSetDiscreteVoxelShape;
	}
}
