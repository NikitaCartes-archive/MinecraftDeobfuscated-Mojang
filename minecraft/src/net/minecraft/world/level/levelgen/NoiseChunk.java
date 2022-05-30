package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.material.MaterialRuleList;

public class NoiseChunk implements DensityFunction.ContextProvider, DensityFunction.FunctionContext {
	private final NoiseSettings noiseSettings;
	final int cellCountXZ;
	final int cellCountY;
	final int cellNoiseMinY;
	private final int firstCellX;
	private final int firstCellZ;
	final int firstNoiseX;
	final int firstNoiseZ;
	final List<NoiseChunk.NoiseInterpolator> interpolators;
	final List<NoiseChunk.CacheAllInCell> cellCaches;
	private final Map<DensityFunction, DensityFunction> wrapped = new HashMap();
	private final Long2IntMap preliminarySurfaceLevel = new Long2IntOpenHashMap();
	private final Aquifer aquifer;
	private final DensityFunction initialDensityNoJaggedness;
	private final NoiseChunk.BlockStateFiller blockStateRule;
	private final Blender blender;
	private final NoiseChunk.FlatCache blendAlpha;
	private final NoiseChunk.FlatCache blendOffset;
	private final DensityFunctions.BeardifierOrMarker beardifier;
	private long lastBlendingDataPos = ChunkPos.INVALID_CHUNK_POS;
	private Blender.BlendingOutput lastBlendingOutput = new Blender.BlendingOutput(1.0, 0.0);
	final int noiseSizeXZ;
	final int cellWidth;
	final int cellHeight;
	boolean interpolating;
	boolean fillingCell;
	private int cellStartBlockX;
	int cellStartBlockY;
	private int cellStartBlockZ;
	int inCellX;
	int inCellY;
	int inCellZ;
	long interpolationCounter;
	long arrayInterpolationCounter;
	int arrayIndex;
	private final DensityFunction.ContextProvider sliceFillingContextProvider = new DensityFunction.ContextProvider() {
		@Override
		public DensityFunction.FunctionContext forIndex(int i) {
			NoiseChunk.this.cellStartBlockY = (i + NoiseChunk.this.cellNoiseMinY) * NoiseChunk.this.cellHeight;
			NoiseChunk.this.interpolationCounter++;
			NoiseChunk.this.inCellY = 0;
			NoiseChunk.this.arrayIndex = i;
			return NoiseChunk.this;
		}

		@Override
		public void fillAllDirectly(double[] ds, DensityFunction densityFunction) {
			for (int i = 0; i < NoiseChunk.this.cellCountY + 1; i++) {
				NoiseChunk.this.cellStartBlockY = (i + NoiseChunk.this.cellNoiseMinY) * NoiseChunk.this.cellHeight;
				NoiseChunk.this.interpolationCounter++;
				NoiseChunk.this.inCellY = 0;
				NoiseChunk.this.arrayIndex = i;
				ds[i] = densityFunction.compute(NoiseChunk.this);
			}
		}
	};

	public static NoiseChunk forChunk(
		ChunkAccess chunkAccess,
		RandomState randomState,
		DensityFunctions.BeardifierOrMarker beardifierOrMarker,
		NoiseGeneratorSettings noiseGeneratorSettings,
		Aquifer.FluidPicker fluidPicker,
		Blender blender
	) {
		NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings().clampToHeightAccessor(chunkAccess);
		ChunkPos chunkPos = chunkAccess.getPos();
		int i = 16 / noiseSettings.getCellWidth();
		return new NoiseChunk(
			i, randomState, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), noiseSettings, beardifierOrMarker, noiseGeneratorSettings, fluidPicker, blender
		);
	}

	public NoiseChunk(
		int i,
		RandomState randomState,
		int j,
		int k,
		NoiseSettings noiseSettings,
		DensityFunctions.BeardifierOrMarker beardifierOrMarker,
		NoiseGeneratorSettings noiseGeneratorSettings,
		Aquifer.FluidPicker fluidPicker,
		Blender blender
	) {
		this.noiseSettings = noiseSettings;
		this.cellWidth = noiseSettings.getCellWidth();
		this.cellHeight = noiseSettings.getCellHeight();
		this.cellCountXZ = i;
		this.cellCountY = Mth.intFloorDiv(noiseSettings.height(), this.cellHeight);
		this.cellNoiseMinY = Mth.intFloorDiv(noiseSettings.minY(), this.cellHeight);
		this.firstCellX = Math.floorDiv(j, this.cellWidth);
		this.firstCellZ = Math.floorDiv(k, this.cellWidth);
		this.interpolators = Lists.<NoiseChunk.NoiseInterpolator>newArrayList();
		this.cellCaches = Lists.<NoiseChunk.CacheAllInCell>newArrayList();
		this.firstNoiseX = QuartPos.fromBlock(j);
		this.firstNoiseZ = QuartPos.fromBlock(k);
		this.noiseSizeXZ = QuartPos.fromBlock(i * this.cellWidth);
		this.blender = blender;
		this.beardifier = beardifierOrMarker;
		this.blendAlpha = new NoiseChunk.FlatCache(new NoiseChunk.BlendAlpha(), false);
		this.blendOffset = new NoiseChunk.FlatCache(new NoiseChunk.BlendOffset(), false);

		for (int l = 0; l <= this.noiseSizeXZ; l++) {
			int m = this.firstNoiseX + l;
			int n = QuartPos.toBlock(m);

			for (int o = 0; o <= this.noiseSizeXZ; o++) {
				int p = this.firstNoiseZ + o;
				int q = QuartPos.toBlock(p);
				Blender.BlendingOutput blendingOutput = blender.blendOffsetAndFactor(n, q);
				this.blendAlpha.values[l][o] = blendingOutput.alpha();
				this.blendOffset.values[l][o] = blendingOutput.blendingOffset();
			}
		}

		NoiseRouter noiseRouter = randomState.router();
		NoiseRouter noiseRouter2 = noiseRouter.mapAll(this::wrap);
		if (!noiseGeneratorSettings.isAquifersEnabled()) {
			this.aquifer = Aquifer.createDisabled(fluidPicker);
		} else {
			int n = SectionPos.blockToSectionCoord(j);
			int o = SectionPos.blockToSectionCoord(k);
			this.aquifer = Aquifer.create(this, new ChunkPos(n, o), noiseRouter2, randomState.aquiferRandom(), noiseSettings.minY(), noiseSettings.height(), fluidPicker);
		}

		Builder<NoiseChunk.BlockStateFiller> builder = ImmutableList.builder();
		DensityFunction densityFunction = DensityFunctions.cacheAllInCell(
				DensityFunctions.add(noiseRouter2.finalDensity(), DensityFunctions.BeardifierMarker.INSTANCE)
			)
			.mapAll(this::wrap);
		builder.add(functionContext -> this.aquifer.computeSubstance(functionContext, densityFunction.compute(functionContext)));
		if (noiseGeneratorSettings.oreVeinsEnabled()) {
			builder.add(OreVeinifier.create(noiseRouter2.veinToggle(), noiseRouter2.veinRidged(), noiseRouter2.veinGap(), randomState.oreRandom()));
		}

		this.blockStateRule = new MaterialRuleList(builder.build());
		this.initialDensityNoJaggedness = noiseRouter2.initialDensityWithoutJaggedness();
	}

	protected Climate.Sampler cachedClimateSampler(NoiseRouter noiseRouter, List<Climate.ParameterPoint> list) {
		return new Climate.Sampler(
			noiseRouter.temperature().mapAll(this::wrap),
			noiseRouter.vegetation().mapAll(this::wrap),
			noiseRouter.continents().mapAll(this::wrap),
			noiseRouter.erosion().mapAll(this::wrap),
			noiseRouter.depth().mapAll(this::wrap),
			noiseRouter.ridges().mapAll(this::wrap),
			list
		);
	}

	@Nullable
	protected BlockState getInterpolatedState() {
		return this.blockStateRule.calculate(this);
	}

	@Override
	public int blockX() {
		return this.cellStartBlockX + this.inCellX;
	}

	@Override
	public int blockY() {
		return this.cellStartBlockY + this.inCellY;
	}

	@Override
	public int blockZ() {
		return this.cellStartBlockZ + this.inCellZ;
	}

	public int preliminarySurfaceLevel(int i, int j) {
		int k = QuartPos.toBlock(QuartPos.fromBlock(i));
		int l = QuartPos.toBlock(QuartPos.fromBlock(j));
		return this.preliminarySurfaceLevel.computeIfAbsent(ColumnPos.asLong(k, l), this::computePreliminarySurfaceLevel);
	}

	private int computePreliminarySurfaceLevel(long l) {
		int i = ColumnPos.getX(l);
		int j = ColumnPos.getZ(l);
		int k = this.noiseSettings.minY();

		for (int m = k + this.noiseSettings.height(); m >= k; m -= this.cellHeight) {
			if (this.initialDensityNoJaggedness.compute(new DensityFunction.SinglePointContext(i, m, j)) > 0.390625) {
				return m;
			}
		}

		return Integer.MAX_VALUE;
	}

	@Override
	public Blender getBlender() {
		return this.blender;
	}

	private void fillSlice(boolean bl, int i) {
		this.cellStartBlockX = i * this.cellWidth;
		this.inCellX = 0;

		for (int j = 0; j < this.cellCountXZ + 1; j++) {
			int k = this.firstCellZ + j;
			this.cellStartBlockZ = k * this.cellWidth;
			this.inCellZ = 0;
			this.arrayInterpolationCounter++;

			for (NoiseChunk.NoiseInterpolator noiseInterpolator : this.interpolators) {
				double[] ds = (bl ? noiseInterpolator.slice0 : noiseInterpolator.slice1)[j];
				noiseInterpolator.fillArray(ds, this.sliceFillingContextProvider);
			}
		}

		this.arrayInterpolationCounter++;
	}

	public void initializeForFirstCellX() {
		if (this.interpolating) {
			throw new IllegalStateException("Staring interpolation twice");
		} else {
			this.interpolating = true;
			this.interpolationCounter = 0L;
			this.fillSlice(true, this.firstCellX);
		}
	}

	public void advanceCellX(int i) {
		this.fillSlice(false, this.firstCellX + i + 1);
		this.cellStartBlockX = (this.firstCellX + i) * this.cellWidth;
	}

	public NoiseChunk forIndex(int i) {
		int j = Math.floorMod(i, this.cellWidth);
		int k = Math.floorDiv(i, this.cellWidth);
		int l = Math.floorMod(k, this.cellWidth);
		int m = this.cellHeight - 1 - Math.floorDiv(k, this.cellWidth);
		this.inCellX = l;
		this.inCellY = m;
		this.inCellZ = j;
		this.arrayIndex = i;
		return this;
	}

	@Override
	public void fillAllDirectly(double[] ds, DensityFunction densityFunction) {
		this.arrayIndex = 0;

		for (int i = this.cellHeight - 1; i >= 0; i--) {
			this.inCellY = i;

			for (int j = 0; j < this.cellWidth; j++) {
				this.inCellX = j;

				for (int k = 0; k < this.cellWidth; k++) {
					this.inCellZ = k;
					ds[this.arrayIndex++] = densityFunction.compute(this);
				}
			}
		}
	}

	public void selectCellYZ(int i, int j) {
		this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.selectCellYZ(i, j));
		this.fillingCell = true;
		this.cellStartBlockY = (i + this.cellNoiseMinY) * this.cellHeight;
		this.cellStartBlockZ = (this.firstCellZ + j) * this.cellWidth;
		this.arrayInterpolationCounter++;

		for (NoiseChunk.CacheAllInCell cacheAllInCell : this.cellCaches) {
			cacheAllInCell.noiseFiller.fillArray(cacheAllInCell.values, this);
		}

		this.arrayInterpolationCounter++;
		this.fillingCell = false;
	}

	public void updateForY(int i, double d) {
		this.inCellY = i - this.cellStartBlockY;
		this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.updateForY(d));
	}

	public void updateForX(int i, double d) {
		this.inCellX = i - this.cellStartBlockX;
		this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.updateForX(d));
	}

	public void updateForZ(int i, double d) {
		this.inCellZ = i - this.cellStartBlockZ;
		this.interpolationCounter++;
		this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.updateForZ(d));
	}

	public void stopInterpolation() {
		if (!this.interpolating) {
			throw new IllegalStateException("Staring interpolation twice");
		} else {
			this.interpolating = false;
		}
	}

	public void swapSlices() {
		this.interpolators.forEach(NoiseChunk.NoiseInterpolator::swapSlices);
	}

	public Aquifer aquifer() {
		return this.aquifer;
	}

	protected int cellWidth() {
		return this.cellWidth;
	}

	protected int cellHeight() {
		return this.cellHeight;
	}

	Blender.BlendingOutput getOrComputeBlendingOutput(int i, int j) {
		long l = ChunkPos.asLong(i, j);
		if (this.lastBlendingDataPos == l) {
			return this.lastBlendingOutput;
		} else {
			this.lastBlendingDataPos = l;
			Blender.BlendingOutput blendingOutput = this.blender.blendOffsetAndFactor(i, j);
			this.lastBlendingOutput = blendingOutput;
			return blendingOutput;
		}
	}

	protected DensityFunction wrap(DensityFunction densityFunction) {
		return (DensityFunction)this.wrapped.computeIfAbsent(densityFunction, this::wrapNew);
	}

	private DensityFunction wrapNew(DensityFunction densityFunction) {
		if (densityFunction instanceof DensityFunctions.Marker marker) {
			return (DensityFunction)(switch (marker.type()) {
				case Interpolated -> new NoiseChunk.NoiseInterpolator(marker.wrapped());
				case FlatCache -> new NoiseChunk.FlatCache(marker.wrapped(), true);
				case Cache2D -> new NoiseChunk.Cache2D(marker.wrapped());
				case CacheOnce -> new NoiseChunk.CacheOnce(marker.wrapped());
				case CacheAllInCell -> new NoiseChunk.CacheAllInCell(marker.wrapped());
			});
		} else {
			if (this.blender != Blender.empty()) {
				if (densityFunction == DensityFunctions.BlendAlpha.INSTANCE) {
					return this.blendAlpha;
				}

				if (densityFunction == DensityFunctions.BlendOffset.INSTANCE) {
					return this.blendOffset;
				}
			}

			if (densityFunction == DensityFunctions.BeardifierMarker.INSTANCE) {
				return this.beardifier;
			} else {
				return densityFunction instanceof DensityFunctions.HolderHolder holderHolder ? holderHolder.function().value() : densityFunction;
			}
		}
	}

	class BlendAlpha implements NoiseChunk.NoiseChunkDensityFunction {
		@Override
		public DensityFunction wrapped() {
			return DensityFunctions.BlendAlpha.INSTANCE;
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return this.wrapped().mapAll(visitor);
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return NoiseChunk.this.getOrComputeBlendingOutput(functionContext.blockX(), functionContext.blockZ()).alpha();
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			contextProvider.fillAllDirectly(ds, this);
		}

		@Override
		public double minValue() {
			return 0.0;
		}

		@Override
		public double maxValue() {
			return 1.0;
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return DensityFunctions.BlendAlpha.CODEC;
		}
	}

	class BlendOffset implements NoiseChunk.NoiseChunkDensityFunction {
		@Override
		public DensityFunction wrapped() {
			return DensityFunctions.BlendOffset.INSTANCE;
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return this.wrapped().mapAll(visitor);
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return NoiseChunk.this.getOrComputeBlendingOutput(functionContext.blockX(), functionContext.blockZ()).blendingOffset();
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			contextProvider.fillAllDirectly(ds, this);
		}

		@Override
		public double minValue() {
			return Double.NEGATIVE_INFINITY;
		}

		@Override
		public double maxValue() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return DensityFunctions.BlendOffset.CODEC;
		}
	}

	@FunctionalInterface
	public interface BlockStateFiller {
		@Nullable
		BlockState calculate(DensityFunction.FunctionContext functionContext);
	}

	static class Cache2D implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
		private final DensityFunction function;
		private long lastPos2D = ChunkPos.INVALID_CHUNK_POS;
		private double lastValue;

		Cache2D(DensityFunction densityFunction) {
			this.function = densityFunction;
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			int i = functionContext.blockX();
			int j = functionContext.blockZ();
			long l = ChunkPos.asLong(i, j);
			if (this.lastPos2D == l) {
				return this.lastValue;
			} else {
				this.lastPos2D = l;
				double d = this.function.compute(functionContext);
				this.lastValue = d;
				return d;
			}
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			this.function.fillArray(ds, contextProvider);
		}

		@Override
		public DensityFunction wrapped() {
			return this.function;
		}

		@Override
		public DensityFunctions.Marker.Type type() {
			return DensityFunctions.Marker.Type.Cache2D;
		}
	}

	class CacheAllInCell implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
		final DensityFunction noiseFiller;
		final double[] values;

		CacheAllInCell(DensityFunction densityFunction) {
			this.noiseFiller = densityFunction;
			this.values = new double[NoiseChunk.this.cellWidth * NoiseChunk.this.cellWidth * NoiseChunk.this.cellHeight];
			NoiseChunk.this.cellCaches.add(this);
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			if (functionContext != NoiseChunk.this) {
				return this.noiseFiller.compute(functionContext);
			} else if (!NoiseChunk.this.interpolating) {
				throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
			} else {
				int i = NoiseChunk.this.inCellX;
				int j = NoiseChunk.this.inCellY;
				int k = NoiseChunk.this.inCellZ;
				return i >= 0 && j >= 0 && k >= 0 && i < NoiseChunk.this.cellWidth && j < NoiseChunk.this.cellHeight && k < NoiseChunk.this.cellWidth
					? this.values[((NoiseChunk.this.cellHeight - 1 - j) * NoiseChunk.this.cellWidth + i) * NoiseChunk.this.cellWidth + k]
					: this.noiseFiller.compute(functionContext);
			}
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			contextProvider.fillAllDirectly(ds, this);
		}

		@Override
		public DensityFunction wrapped() {
			return this.noiseFiller;
		}

		@Override
		public DensityFunctions.Marker.Type type() {
			return DensityFunctions.Marker.Type.CacheAllInCell;
		}
	}

	class CacheOnce implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
		private final DensityFunction function;
		private long lastCounter;
		private long lastArrayCounter;
		private double lastValue;
		@Nullable
		private double[] lastArray;

		CacheOnce(DensityFunction densityFunction) {
			this.function = densityFunction;
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			if (functionContext != NoiseChunk.this) {
				return this.function.compute(functionContext);
			} else if (this.lastArray != null && this.lastArrayCounter == NoiseChunk.this.arrayInterpolationCounter) {
				return this.lastArray[NoiseChunk.this.arrayIndex];
			} else if (this.lastCounter == NoiseChunk.this.interpolationCounter) {
				return this.lastValue;
			} else {
				this.lastCounter = NoiseChunk.this.interpolationCounter;
				double d = this.function.compute(functionContext);
				this.lastValue = d;
				return d;
			}
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			if (this.lastArray != null && this.lastArrayCounter == NoiseChunk.this.arrayInterpolationCounter) {
				System.arraycopy(this.lastArray, 0, ds, 0, ds.length);
			} else {
				this.wrapped().fillArray(ds, contextProvider);
				if (this.lastArray != null && this.lastArray.length == ds.length) {
					System.arraycopy(ds, 0, this.lastArray, 0, ds.length);
				} else {
					this.lastArray = (double[])ds.clone();
				}

				this.lastArrayCounter = NoiseChunk.this.arrayInterpolationCounter;
			}
		}

		@Override
		public DensityFunction wrapped() {
			return this.function;
		}

		@Override
		public DensityFunctions.Marker.Type type() {
			return DensityFunctions.Marker.Type.CacheOnce;
		}
	}

	class FlatCache implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
		private final DensityFunction noiseFiller;
		final double[][] values;

		FlatCache(DensityFunction densityFunction, boolean bl) {
			this.noiseFiller = densityFunction;
			this.values = new double[NoiseChunk.this.noiseSizeXZ + 1][NoiseChunk.this.noiseSizeXZ + 1];
			if (bl) {
				for (int i = 0; i <= NoiseChunk.this.noiseSizeXZ; i++) {
					int j = NoiseChunk.this.firstNoiseX + i;
					int k = QuartPos.toBlock(j);

					for (int l = 0; l <= NoiseChunk.this.noiseSizeXZ; l++) {
						int m = NoiseChunk.this.firstNoiseZ + l;
						int n = QuartPos.toBlock(m);
						this.values[i][l] = densityFunction.compute(new DensityFunction.SinglePointContext(k, 0, n));
					}
				}
			}
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			int i = QuartPos.fromBlock(functionContext.blockX());
			int j = QuartPos.fromBlock(functionContext.blockZ());
			int k = i - NoiseChunk.this.firstNoiseX;
			int l = j - NoiseChunk.this.firstNoiseZ;
			int m = this.values.length;
			return k >= 0 && l >= 0 && k < m && l < m ? this.values[k][l] : this.noiseFiller.compute(functionContext);
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			contextProvider.fillAllDirectly(ds, this);
		}

		@Override
		public DensityFunction wrapped() {
			return this.noiseFiller;
		}

		@Override
		public DensityFunctions.Marker.Type type() {
			return DensityFunctions.Marker.Type.FlatCache;
		}
	}

	interface NoiseChunkDensityFunction extends DensityFunction {
		DensityFunction wrapped();

		@Override
		default double minValue() {
			return this.wrapped().minValue();
		}

		@Override
		default double maxValue() {
			return this.wrapped().maxValue();
		}
	}

	public class NoiseInterpolator implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
		double[][] slice0;
		double[][] slice1;
		private final DensityFunction noiseFiller;
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
		private double value;

		NoiseInterpolator(DensityFunction densityFunction) {
			this.noiseFiller = densityFunction;
			this.slice0 = this.allocateSlice(NoiseChunk.this.cellCountY, NoiseChunk.this.cellCountXZ);
			this.slice1 = this.allocateSlice(NoiseChunk.this.cellCountY, NoiseChunk.this.cellCountXZ);
			NoiseChunk.this.interpolators.add(this);
		}

		private double[][] allocateSlice(int i, int j) {
			int k = j + 1;
			int l = i + 1;
			double[][] ds = new double[k][l];

			for (int m = 0; m < k; m++) {
				ds[m] = new double[l];
			}

			return ds;
		}

		void selectCellYZ(int i, int j) {
			this.noise000 = this.slice0[j][i];
			this.noise001 = this.slice0[j + 1][i];
			this.noise100 = this.slice1[j][i];
			this.noise101 = this.slice1[j + 1][i];
			this.noise010 = this.slice0[j][i + 1];
			this.noise011 = this.slice0[j + 1][i + 1];
			this.noise110 = this.slice1[j][i + 1];
			this.noise111 = this.slice1[j + 1][i + 1];
		}

		void updateForY(double d) {
			this.valueXZ00 = Mth.lerp(d, this.noise000, this.noise010);
			this.valueXZ10 = Mth.lerp(d, this.noise100, this.noise110);
			this.valueXZ01 = Mth.lerp(d, this.noise001, this.noise011);
			this.valueXZ11 = Mth.lerp(d, this.noise101, this.noise111);
		}

		void updateForX(double d) {
			this.valueZ0 = Mth.lerp(d, this.valueXZ00, this.valueXZ10);
			this.valueZ1 = Mth.lerp(d, this.valueXZ01, this.valueXZ11);
		}

		void updateForZ(double d) {
			this.value = Mth.lerp(d, this.valueZ0, this.valueZ1);
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			if (functionContext != NoiseChunk.this) {
				return this.noiseFiller.compute(functionContext);
			} else if (!NoiseChunk.this.interpolating) {
				throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
			} else {
				return NoiseChunk.this.fillingCell
					? Mth.lerp3(
						(double)NoiseChunk.this.inCellX / (double)NoiseChunk.this.cellWidth,
						(double)NoiseChunk.this.inCellY / (double)NoiseChunk.this.cellHeight,
						(double)NoiseChunk.this.inCellZ / (double)NoiseChunk.this.cellWidth,
						this.noise000,
						this.noise100,
						this.noise010,
						this.noise110,
						this.noise001,
						this.noise101,
						this.noise011,
						this.noise111
					)
					: this.value;
			}
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			if (NoiseChunk.this.fillingCell) {
				contextProvider.fillAllDirectly(ds, this);
			} else {
				this.wrapped().fillArray(ds, contextProvider);
			}
		}

		@Override
		public DensityFunction wrapped() {
			return this.noiseFiller;
		}

		private void swapSlices() {
			double[][] ds = this.slice0;
			this.slice0 = this.slice1;
			this.slice1 = ds;
		}

		@Override
		public DensityFunctions.Marker.Type type() {
			return DensityFunctions.Marker.Type.Interpolated;
		}
	}
}
