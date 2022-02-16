package net.minecraft.world.level.levelgen;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.Arrays;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.slf4j.Logger;

public final class DensityFunctions {
	private DensityFunctions() {
	}

	public static DensityFunction interpolated(DensityFunction densityFunction) {
		return new DensityFunctions.Marker(DensityFunctions.Marker.Type.Interpolated, densityFunction);
	}

	public static DensityFunction flatCache(DensityFunction densityFunction) {
		return new DensityFunctions.Marker(DensityFunctions.Marker.Type.FlatCache, densityFunction);
	}

	public static DensityFunction cache2d(DensityFunction densityFunction) {
		return new DensityFunctions.Marker(DensityFunctions.Marker.Type.Cache2D, densityFunction);
	}

	public static DensityFunction cacheOnce(DensityFunction densityFunction) {
		return new DensityFunctions.Marker(DensityFunctions.Marker.Type.CacheOnce, densityFunction);
	}

	public static DensityFunction cacheAllInCell(DensityFunction densityFunction) {
		return new DensityFunctions.Marker(DensityFunctions.Marker.Type.CacheAllInCell, densityFunction);
	}

	public static DensityFunction mappedNoise(NormalNoise normalNoise, @Deprecated double d, double e, double f, double g) {
		return mapFromUnitTo(new DensityFunctions.Noise(normalNoise, d, e), f, g);
	}

	public static DensityFunction mappedNoise(NormalNoise normalNoise, double d, double e, double f) {
		return mapFromUnitTo(new DensityFunctions.Noise(normalNoise, 1.0, d), e, f);
	}

	public static DensityFunction mappedNoise(NormalNoise normalNoise, double d, double e) {
		return mapFromUnitTo(new DensityFunctions.Noise(normalNoise, 1.0, 1.0), d, e);
	}

	public static DensityFunction shiftedNoise2d(DensityFunction densityFunction, DensityFunction densityFunction2, double d, NormalNoise normalNoise) {
		return new DensityFunctions.ShiftedNoise(densityFunction, zero(), densityFunction2, d, 0.0, normalNoise);
	}

	public static DensityFunction noise(NormalNoise normalNoise) {
		return new DensityFunctions.Noise(normalNoise);
	}

	public static DensityFunction noise(NormalNoise normalNoise, double d, double e) {
		return new DensityFunctions.Noise(normalNoise, d, e);
	}

	public static DensityFunction noise(NormalNoise normalNoise, double d) {
		return new DensityFunctions.Noise(normalNoise, d);
	}

	public static DensityFunction rangeChoice(
		DensityFunction densityFunction, double d, double e, DensityFunction densityFunction2, DensityFunction densityFunction3
	) {
		return new DensityFunctions.RangeChoice(densityFunction, d, e, densityFunction2, densityFunction3);
	}

	public static DensityFunction shiftA(NormalNoise normalNoise) {
		return new DensityFunctions.ShiftA(normalNoise);
	}

	public static DensityFunction shiftB(NormalNoise normalNoise) {
		return new DensityFunctions.ShiftB(normalNoise);
	}

	public static DensityFunction shift0(NormalNoise normalNoise) {
		return new DensityFunctions.Shift0(normalNoise);
	}

	public static DensityFunction shift1(NormalNoise normalNoise) {
		return new DensityFunctions.Shift1(normalNoise);
	}

	public static DensityFunction shift2(NormalNoise normalNoise) {
		return new DensityFunctions.Shift2(normalNoise);
	}

	public static DensityFunction blendDensity(DensityFunction densityFunction) {
		return new DensityFunctions.BlendDensity(densityFunction);
	}

	public static DensityFunction endIslands(long l) {
		return new DensityFunctions.EndIslandDensityFunction(l);
	}

	public static DensityFunction weirdScaledSampler(
		DensityFunction densityFunction, NormalNoise normalNoise, Double2DoubleFunction double2DoubleFunction, double d
	) {
		return new DensityFunctions.WeirdScaledSampler(densityFunction, normalNoise, double2DoubleFunction, d);
	}

	public static DensityFunction slide(NoiseSettings noiseSettings, DensityFunction densityFunction) {
		return new DensityFunctions.Slide(noiseSettings, densityFunction);
	}

	public static DensityFunction add(DensityFunction densityFunction, DensityFunction densityFunction2) {
		return DensityFunctions.Ap2.create(DensityFunctions.Ap2.Type.ADD, densityFunction, densityFunction2);
	}

	public static DensityFunction mul(DensityFunction densityFunction, DensityFunction densityFunction2) {
		return DensityFunctions.Ap2.create(DensityFunctions.Ap2.Type.MUL, densityFunction, densityFunction2);
	}

	public static DensityFunction min(DensityFunction densityFunction, DensityFunction densityFunction2) {
		return DensityFunctions.Ap2.create(DensityFunctions.Ap2.Type.MIN, densityFunction, densityFunction2);
	}

	public static DensityFunction max(DensityFunction densityFunction, DensityFunction densityFunction2) {
		return DensityFunctions.Ap2.create(DensityFunctions.Ap2.Type.MAX, densityFunction, densityFunction2);
	}

	public static DensityFunction terrainShaperSpline(
		DensityFunction densityFunction,
		DensityFunction densityFunction2,
		DensityFunction densityFunction3,
		ToFloatFunction<TerrainShaper.Point> toFloatFunction,
		double d,
		double e
	) {
		return new DensityFunctions.TerrainShaperSpline(densityFunction, densityFunction2, densityFunction3, toFloatFunction, d, e);
	}

	public static DensityFunction zero() {
		return DensityFunctions.Constant.ZERO;
	}

	public static DensityFunction constant(double d) {
		return new DensityFunctions.Constant(d);
	}

	public static DensityFunction yClampedGradient(int i, int j, double d, double e) {
		return new DensityFunctions.YClampedGradient(i, j, d, e);
	}

	protected static DensityFunction map(DensityFunction densityFunction, DensityFunctions.Mapped.Type type) {
		return new DensityFunctions.Mapped(type, densityFunction, 0.0, 0.0).mapAll(densityFunctionx -> densityFunctionx);
	}

	private static DensityFunction mapFromUnitTo(DensityFunction densityFunction, double d, double e) {
		double f = (d + e) * 0.5;
		double g = (e - d) * 0.5;
		return add(constant(f), mul(constant(g), densityFunction));
	}

	public static DensityFunction blendAlpha() {
		return DensityFunctions.BlendAlpha.INSTANCE;
	}

	public static DensityFunction blendOffset() {
		return DensityFunctions.BlendOffset.INSTANCE;
	}

	public static DensityFunction lerp(DensityFunction densityFunction, DensityFunction densityFunction2, DensityFunction densityFunction3) {
		DensityFunction densityFunction4 = cacheOnce(densityFunction);
		DensityFunction densityFunction5 = add(mul(densityFunction4, constant(-1.0)), constant(1.0));
		return add(mul(densityFunction2, densityFunction5), mul(densityFunction3, densityFunction4));
	}

	static record Ap2(DensityFunctions.Ap2.Type type, DensityFunction f1, DensityFunction f2, double minValue, double maxValue) implements DensityFunction {
		private static final Logger LOGGER = LogUtils.getLogger();

		public static DensityFunction create(DensityFunctions.Ap2.Type type, DensityFunction densityFunction, DensityFunction densityFunction2) {
			double d = densityFunction.minValue();
			double e = densityFunction2.minValue();
			double f = densityFunction.maxValue();
			double g = densityFunction2.maxValue();
			if (type == DensityFunctions.Ap2.Type.MIN || type == DensityFunctions.Ap2.Type.MAX) {
				boolean bl = d >= g;
				boolean bl2 = e >= f;
				if (bl || bl2) {
					LOGGER.warn("Creating a " + type + " function between two non-overlapping inputs: " + densityFunction + " and " + densityFunction2);
					if (type == DensityFunctions.Ap2.Type.MIN) {
						return bl2 ? densityFunction : densityFunction2;
					} else {
						return bl2 ? densityFunction2 : densityFunction;
					}
				}
			}
			double h = switch (type) {
				case ADD -> d + e;
				case MAX -> Math.max(d, e);
				case MIN -> Math.min(d, e);
				case MUL -> d > 0.0 && e > 0.0 ? d * e : (f < 0.0 && g < 0.0 ? f * g : Math.min(d * g, f * e));
			};

			double i = switch (type) {
				case ADD -> f + g;
				case MAX -> Math.max(f, g);
				case MIN -> Math.min(f, g);
				case MUL -> d > 0.0 && e > 0.0 ? f * g : (f < 0.0 && g < 0.0 ? d * e : Math.max(d * e, f * g));
			};
			if (type == DensityFunctions.Ap2.Type.MUL || type == DensityFunctions.Ap2.Type.ADD) {
				if (densityFunction instanceof DensityFunctions.Constant constant) {
					return new DensityFunctions.MulOrAdd(
						type == DensityFunctions.Ap2.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL, densityFunction2, h, i, constant.value
					);
				}

				if (densityFunction2 instanceof DensityFunctions.Constant constant) {
					return new DensityFunctions.MulOrAdd(
						type == DensityFunctions.Ap2.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL, densityFunction, h, i, constant.value
					);
				}
			}

			return new DensityFunctions.Ap2(type, densityFunction, densityFunction2, h, i);
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			double d = this.f1.compute(functionContext);

			return switch (this.type) {
				case ADD -> d + this.f2.compute(functionContext);
				case MAX -> d > this.f2.maxValue() ? d : Math.max(d, this.f2.compute(functionContext));
				case MIN -> d < this.f2.minValue() ? d : Math.min(d, this.f2.compute(functionContext));
				case MUL -> d == 0.0 ? 0.0 : d * this.f2.compute(functionContext);
			};
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			this.f1.fillArray(ds, contextProvider);
			switch (this.type) {
				case ADD:
					double[] es = new double[ds.length];
					this.f2.fillArray(es, contextProvider);

					for (int i = 0; i < ds.length; i++) {
						ds[i] += es[i];
					}
					break;
				case MAX:
					double e = this.f2.maxValue();

					for (int k = 0; k < ds.length; k++) {
						double f = ds[k];
						ds[k] = f > e ? f : Math.max(f, this.f2.compute(contextProvider.forIndex(k)));
					}
					break;
				case MIN:
					double e = this.f2.minValue();

					for (int k = 0; k < ds.length; k++) {
						double f = ds[k];
						ds[k] = f < e ? f : Math.min(f, this.f2.compute(contextProvider.forIndex(k)));
					}
					break;
				case MUL:
					for (int j = 0; j < ds.length; j++) {
						double d = ds[j];
						ds[j] = d == 0.0 ? 0.0 : d * this.f2.compute(contextProvider.forIndex(j));
					}
			}
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return (DensityFunction)visitor.apply(create(this.type, this.f1.mapAll(visitor), this.f2.mapAll(visitor)));
		}

		static enum Type {
			ADD,
			MUL,
			MIN,
			MAX;
		}
	}

	protected static enum BlendAlpha implements DensityFunction.SimpleFunction {
		INSTANCE;

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return 1.0;
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			Arrays.fill(ds, 1.0);
		}

		@Override
		public double minValue() {
			return 1.0;
		}

		@Override
		public double maxValue() {
			return 1.0;
		}
	}

	static record BlendDensity(DensityFunction input) implements DensityFunctions.TransformerWithContext {
		@Override
		public double transform(DensityFunction.FunctionContext functionContext, double d) {
			return functionContext.getBlender().blendDensity(functionContext, d);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return (DensityFunction)visitor.apply(new DensityFunctions.BlendDensity(this.input.mapAll(visitor)));
		}

		@Override
		public double minValue() {
			return Double.NEGATIVE_INFINITY;
		}

		@Override
		public double maxValue() {
			return Double.POSITIVE_INFINITY;
		}
	}

	protected static enum BlendOffset implements DensityFunction.SimpleFunction {
		INSTANCE;

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return 0.0;
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			Arrays.fill(ds, 0.0);
		}

		@Override
		public double minValue() {
			return 0.0;
		}

		@Override
		public double maxValue() {
			return 0.0;
		}
	}

	protected static record Clamp(DensityFunction input, double minValue, double maxValue) implements DensityFunctions.PureTransformer {
		@Override
		public double transform(double d) {
			return Mth.clamp(d, this.minValue, this.maxValue);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return new DensityFunctions.Clamp(this.input.mapAll(visitor), this.minValue, this.maxValue);
		}
	}

	static record Constant(double value) implements DensityFunction.SimpleFunction {
		static final DensityFunctions.Constant ZERO = new DensityFunctions.Constant(0.0);

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.value;
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			Arrays.fill(ds, this.value);
		}

		@Override
		public double minValue() {
			return this.value;
		}

		@Override
		public double maxValue() {
			return this.value;
		}
	}

	static final class EndIslandDensityFunction implements DensityFunction.SimpleFunction {
		final SimplexNoise islandNoise;

		public EndIslandDensityFunction(long l) {
			RandomSource randomSource = new LegacyRandomSource(l);
			randomSource.consumeCount(17292);
			this.islandNoise = new SimplexNoise(randomSource);
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return ((double)TheEndBiomeSource.getHeightValue(this.islandNoise, functionContext.blockX() / 8, functionContext.blockZ() / 8) - 8.0) / 128.0;
		}

		@Override
		public double minValue() {
			return -0.84375;
		}

		@Override
		public double maxValue() {
			return 0.5625;
		}
	}

	protected static record Mapped(DensityFunctions.Mapped.Type type, DensityFunction input, double minValue, double maxValue)
		implements DensityFunctions.PureTransformer {
		private static double transform(DensityFunctions.Mapped.Type type, double d) {
			return switch (type) {
				case ABS -> Math.abs(d);
				case SQUARE -> d * d;
				case CUBE -> d * d * d;
				case HALF_NEGATIVE -> d > 0.0 ? d : d * 0.5;
				case QUARTER_NEGATIVE -> d > 0.0 ? d : d * 0.25;
				case SQUEEZE -> {
					double e = Mth.clamp(d, -1.0, 1.0);
					yield e / 2.0 - e * e * e / 24.0;
				}
			};
		}

		@Override
		public double transform(double d) {
			return transform(this.type, d);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			DensityFunction densityFunction = this.input.mapAll(visitor);
			double d = densityFunction.minValue();
			double e = transform(this.type, d);
			double f = transform(this.type, densityFunction.maxValue());
			return this.type != DensityFunctions.Mapped.Type.ABS && this.type != DensityFunctions.Mapped.Type.SQUARE
				? new DensityFunctions.Mapped(this.type, densityFunction, e, f)
				: new DensityFunctions.Mapped(this.type, densityFunction, Math.max(0.0, d), Math.max(e, f));
		}

		static enum Type {
			ABS,
			SQUARE,
			CUBE,
			HALF_NEGATIVE,
			QUARTER_NEGATIVE,
			SQUEEZE;
		}
	}

	protected static record Marker(DensityFunctions.Marker.Type type, DensityFunction function) implements DensityFunction {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.function.compute(functionContext);
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			this.function.fillArray(ds, contextProvider);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return (DensityFunction)visitor.apply(new DensityFunctions.Marker(this.type, this.function.mapAll(visitor)));
		}

		@Override
		public double minValue() {
			return this.function.minValue();
		}

		@Override
		public double maxValue() {
			return this.function.maxValue();
		}

		static enum Type {
			Interpolated,
			FlatCache,
			Cache2D,
			CacheOnce,
			CacheAllInCell;
		}
	}

	static record MulOrAdd(DensityFunctions.MulOrAdd.Type type, DensityFunction input, double minValue, double maxValue, double argument)
		implements DensityFunctions.PureTransformer {
		@Override
		public double transform(double d) {
			return switch (this.type) {
				case MUL -> d * this.argument;
				case ADD -> d + this.argument;
			};
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			DensityFunction densityFunction = this.input.mapAll(visitor);
			double d = densityFunction.minValue();
			double e = densityFunction.maxValue();
			double f;
			double g;
			if (this.type == DensityFunctions.MulOrAdd.Type.ADD) {
				f = d + this.argument;
				g = e + this.argument;
			} else if (this.argument >= 0.0) {
				f = d * this.argument;
				g = e * this.argument;
			} else {
				f = e * this.argument;
				g = d * this.argument;
			}

			return new DensityFunctions.MulOrAdd(this.type, densityFunction, f, g, this.argument);
		}

		static enum Type {
			MUL,
			ADD;
		}
	}

	static record Noise(NormalNoise noise, @Deprecated double xzScale, double yScale) implements DensityFunction.SimpleFunction {
		public Noise(NormalNoise normalNoise, double d) {
			this(normalNoise, 1.0, d);
		}

		public Noise(NormalNoise normalNoise) {
			this(normalNoise, 1.0, 1.0);
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.noise
				.getValue((double)functionContext.blockX() * this.xzScale, (double)functionContext.blockY() * this.yScale, (double)functionContext.blockZ() * this.xzScale);
		}

		@Override
		public double minValue() {
			return -this.maxValue();
		}

		@Override
		public double maxValue() {
			return this.noise.maxValue();
		}
	}

	interface PureTransformer extends DensityFunction {
		DensityFunction input();

		@Override
		default double compute(DensityFunction.FunctionContext functionContext) {
			return this.transform(this.input().compute(functionContext));
		}

		@Override
		default void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			this.input().fillArray(ds, contextProvider);

			for (int i = 0; i < ds.length; i++) {
				ds[i] = this.transform(ds[i]);
			}
		}

		double transform(double d);
	}

	static record RangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange)
		implements DensityFunction {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			double d = this.input.compute(functionContext);
			return d >= this.minInclusive && d < this.maxExclusive ? this.whenInRange.compute(functionContext) : this.whenOutOfRange.compute(functionContext);
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			this.input.fillArray(ds, contextProvider);

			for (int i = 0; i < ds.length; i++) {
				double d = ds[i];
				if (d >= this.minInclusive && d < this.maxExclusive) {
					ds[i] = this.whenInRange.compute(contextProvider.forIndex(i));
				} else {
					ds[i] = this.whenOutOfRange.compute(contextProvider.forIndex(i));
				}
			}
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return (DensityFunction)visitor.apply(
				new DensityFunctions.RangeChoice(
					this.input.mapAll(visitor), this.minInclusive, this.maxExclusive, this.whenInRange.mapAll(visitor), this.whenOutOfRange.mapAll(visitor)
				)
			);
		}

		@Override
		public double minValue() {
			return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue());
		}

		@Override
		public double maxValue() {
			return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue());
		}
	}

	static record Shift0(NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.compute((double)functionContext.blockX(), (double)functionContext.blockY(), (double)functionContext.blockZ());
		}
	}

	static record Shift1(NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.compute((double)functionContext.blockY(), (double)functionContext.blockZ(), (double)functionContext.blockX());
		}
	}

	static record Shift2(NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.compute((double)functionContext.blockZ(), (double)functionContext.blockX(), (double)functionContext.blockY());
		}
	}

	static record ShiftA(NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.compute((double)functionContext.blockX(), 0.0, (double)functionContext.blockZ());
		}
	}

	static record ShiftB(NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.compute((double)functionContext.blockZ(), (double)functionContext.blockX(), 0.0);
		}
	}

	interface ShiftNoise extends DensityFunction.SimpleFunction {
		NormalNoise offsetNoise();

		@Override
		default double minValue() {
			return -this.maxValue();
		}

		@Override
		default double maxValue() {
			return this.offsetNoise().maxValue() * 4.0;
		}

		default double compute(double d, double e, double f) {
			return this.offsetNoise().getValue(d * 0.25, e * 0.25, f * 0.25) * 4.0;
		}
	}

	static record ShiftedNoise(DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale, NormalNoise noise)
		implements DensityFunction {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			double d = (double)functionContext.blockX() * this.xzScale + this.shiftX.compute(functionContext);
			double e = (double)functionContext.blockY() * this.yScale + this.shiftY.compute(functionContext);
			double f = (double)functionContext.blockZ() * this.xzScale + this.shiftZ.compute(functionContext);
			return this.noise.getValue(d, e, f);
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			contextProvider.fillAllDirectly(ds, this);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return (DensityFunction)visitor.apply(
				new DensityFunctions.ShiftedNoise(
					this.shiftX.mapAll(visitor), this.shiftY.mapAll(visitor), this.shiftZ.mapAll(visitor), this.xzScale, this.yScale, this.noise
				)
			);
		}

		@Override
		public double minValue() {
			return -this.maxValue();
		}

		@Override
		public double maxValue() {
			return this.noise.maxValue();
		}
	}

	static record Slide(NoiseSettings settings, DensityFunction input) implements DensityFunctions.TransformerWithContext {
		@Override
		public double transform(DensityFunction.FunctionContext functionContext, double d) {
			return NoiseRouterData.applySlide(this.settings, d, (double)functionContext.blockY());
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return (DensityFunction)visitor.apply(new DensityFunctions.Slide(this.settings, this.input.mapAll(visitor)));
		}

		@Override
		public double minValue() {
			return Math.min(this.input.minValue(), Math.min(this.settings.bottomSlideSettings().target(), this.settings.topSlideSettings().target()));
		}

		@Override
		public double maxValue() {
			return Math.max(this.input.maxValue(), Math.max(this.settings.bottomSlideSettings().target(), this.settings.topSlideSettings().target()));
		}
	}

	static record TerrainShaperSpline(
		DensityFunction continentalness,
		DensityFunction erosion,
		DensityFunction weirdness,
		ToFloatFunction<TerrainShaper.Point> spline,
		double minValue,
		double maxValue
	) implements DensityFunction {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return Mth.clamp(
				(double)this.spline
					.apply(
						TerrainShaper.makePoint(
							(float)this.continentalness.compute(functionContext), (float)this.erosion.compute(functionContext), (float)this.weirdness.compute(functionContext)
						)
					),
				this.minValue,
				this.maxValue
			);
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			for (int i = 0; i < ds.length; i++) {
				ds[i] = this.compute(contextProvider.forIndex(i));
			}
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return (DensityFunction)visitor.apply(
				new DensityFunctions.TerrainShaperSpline(
					this.continentalness.mapAll(visitor), this.erosion.mapAll(visitor), this.weirdness.mapAll(visitor), this.spline, this.minValue, this.maxValue
				)
			);
		}
	}

	interface TransformerWithContext extends DensityFunction {
		DensityFunction input();

		@Override
		default double compute(DensityFunction.FunctionContext functionContext) {
			return this.transform(functionContext, this.input().compute(functionContext));
		}

		@Override
		default void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			this.input().fillArray(ds, contextProvider);

			for (int i = 0; i < ds.length; i++) {
				ds[i] = this.transform(contextProvider.forIndex(i), ds[i]);
			}
		}

		double transform(DensityFunction.FunctionContext functionContext, double d);
	}

	static record WeirdScaledSampler(DensityFunction input, NormalNoise noise, Double2DoubleFunction rarityValueMapper, double maxRarity)
		implements DensityFunctions.TransformerWithContext {
		@Override
		public double transform(DensityFunction.FunctionContext functionContext, double d) {
			double e = this.rarityValueMapper.get(d);
			return e * Math.abs(this.noise.getValue((double)functionContext.blockX() / e, (double)functionContext.blockY() / e, (double)functionContext.blockZ() / e));
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			this.input.mapAll(visitor);
			return (DensityFunction)visitor.apply(
				new DensityFunctions.WeirdScaledSampler(this.input.mapAll(visitor), this.noise, this.rarityValueMapper, this.maxRarity)
			);
		}

		@Override
		public double minValue() {
			return 0.0;
		}

		@Override
		public double maxValue() {
			return this.maxRarity * this.noise.maxValue();
		}
	}

	static record YClampedGradient(int fromY, int toY, double fromValue, double toValue) implements DensityFunction.SimpleFunction {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return Mth.clampedMap((double)functionContext.blockY(), (double)this.fromY, (double)this.toY, this.fromValue, this.toValue);
		}

		@Override
		public double minValue() {
			return Math.min(this.fromValue, this.toValue);
		}

		@Override
		public double maxValue() {
			return Math.max(this.fromValue, this.toValue);
		}
	}
}
