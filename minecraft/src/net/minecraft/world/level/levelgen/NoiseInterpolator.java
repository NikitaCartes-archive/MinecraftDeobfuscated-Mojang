package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public class NoiseInterpolator {
	private double[][] slice0;
	private double[][] slice1;
	private final int cellCountY;
	private final int cellCountZ;
	private final int cellNoiseMinY;
	private final NoiseInterpolator.NoiseColumnFiller noiseColumnFiller;
	private double noise000;
	private double noise001;
	private double noise100;
	private double noise101;
	private double noise010;
	private double noise011;
	private double noise110;
	private double noise111;
	private double valueXZ00;
	private double valueXZ10;
	private double valueXZ01;
	private double valueXZ11;
	private double valueZ0;
	private double valueZ1;
	private final int firstCellXInChunk;
	private final int firstCellZInChunk;

	public NoiseInterpolator(int i, int j, int k, ChunkPos chunkPos, int l, NoiseInterpolator.NoiseColumnFiller noiseColumnFiller) {
		this.cellCountY = j;
		this.cellCountZ = k;
		this.cellNoiseMinY = l;
		this.noiseColumnFiller = noiseColumnFiller;
		this.slice0 = allocateSlice(j, k);
		this.slice1 = allocateSlice(j, k);
		this.firstCellXInChunk = chunkPos.x * i;
		this.firstCellZInChunk = chunkPos.z * k;
	}

	private static double[][] allocateSlice(int i, int j) {
		int k = j + 1;
		int l = i + 1;
		double[][] ds = new double[k][l];

		for (int m = 0; m < k; m++) {
			ds[m] = new double[l];
		}

		return ds;
	}

	public void initializeForFirstCellX() {
		this.fillSlice(this.slice0, this.firstCellXInChunk);
	}

	public void advanceCellX(int i) {
		this.fillSlice(this.slice1, this.firstCellXInChunk + i + 1);
	}

	private void fillSlice(double[][] ds, int i) {
		for (int j = 0; j < this.cellCountZ + 1; j++) {
			int k = this.firstCellZInChunk + j;
			this.noiseColumnFiller.fillNoiseColumn(ds[j], i, k, this.cellNoiseMinY, this.cellCountY);
		}
	}

	public void selectCellYZ(int i, int j) {
		this.noise000 = this.slice0[j][i];
		this.noise001 = this.slice0[j + 1][i];
		this.noise100 = this.slice1[j][i];
		this.noise101 = this.slice1[j + 1][i];
		this.noise010 = this.slice0[j][i + 1];
		this.noise011 = this.slice0[j + 1][i + 1];
		this.noise110 = this.slice1[j][i + 1];
		this.noise111 = this.slice1[j + 1][i + 1];
	}

	public void updateForY(double d) {
		this.valueXZ00 = Mth.lerp(d, this.noise000, this.noise010);
		this.valueXZ10 = Mth.lerp(d, this.noise100, this.noise110);
		this.valueXZ01 = Mth.lerp(d, this.noise001, this.noise011);
		this.valueXZ11 = Mth.lerp(d, this.noise101, this.noise111);
	}

	public void updateForX(double d) {
		this.valueZ0 = Mth.lerp(d, this.valueXZ00, this.valueXZ10);
		this.valueZ1 = Mth.lerp(d, this.valueXZ01, this.valueXZ11);
	}

	public double calculateValue(double d) {
		return Mth.lerp(d, this.valueZ0, this.valueZ1);
	}

	public void swapSlices() {
		double[][] ds = this.slice0;
		this.slice0 = this.slice1;
		this.slice1 = ds;
	}

	@FunctionalInterface
	public interface NoiseColumnFiller {
		void fillNoiseColumn(double[] ds, int i, int j, int k, int l);
	}
}
