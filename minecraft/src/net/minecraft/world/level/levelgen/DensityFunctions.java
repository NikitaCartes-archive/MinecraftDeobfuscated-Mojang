package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.slf4j.Logger;

public final class DensityFunctions {
	private static final Codec<DensityFunction> CODEC = Registry.DENSITY_FUNCTION_TYPES
		.byNameCodec()
		.dispatch(densityFunction -> densityFunction.codec().codec(), Function.identity());
	protected static final double MAX_REASONABLE_NOISE_VALUE = 1000000.0;
	static final Codec<Double> NOISE_VALUE_CODEC = Codec.doubleRange(-1000000.0, 1000000.0);
	public static final Codec<DensityFunction> DIRECT_CODEC = Codec.either(NOISE_VALUE_CODEC, CODEC)
		.xmap(
			either -> either.map(DensityFunctions::constant, Function.identity()),
			densityFunction -> densityFunction instanceof DensityFunctions.Constant constant ? Either.left(constant.value()) : Either.right(densityFunction)
		);

	public static Codec<? extends DensityFunction> bootstrap(Registry<Codec<? extends DensityFunction>> registry) {
		register(registry, "blend_alpha", DensityFunctions.BlendAlpha.CODEC);
		register(registry, "blend_offset", DensityFunctions.BlendOffset.CODEC);
		register(registry, "beardifier", DensityFunctions.BeardifierMarker.CODEC);
		register(registry, "old_blended_noise", BlendedNoise.CODEC);

		for (DensityFunctions.Marker.Type type : DensityFunctions.Marker.Type.values()) {
			register(registry, type.getSerializedName(), type.codec);
		}

		register(registry, "noise", DensityFunctions.Noise.CODEC);
		register(registry, "end_islands", DensityFunctions.EndIslandDensityFunction.CODEC);
		register(registry, "weird_scaled_sampler", DensityFunctions.WeirdScaledSampler.CODEC);
		register(registry, "shifted_noise", DensityFunctions.ShiftedNoise.CODEC);
		register(registry, "range_choice", DensityFunctions.RangeChoice.CODEC);
		register(registry, "shift_a", DensityFunctions.ShiftA.CODEC);
		register(registry, "shift_b", DensityFunctions.ShiftB.CODEC);
		register(registry, "shift", DensityFunctions.Shift.CODEC);
		register(registry, "blend_density", DensityFunctions.BlendDensity.CODEC);
		register(registry, "clamp", DensityFunctions.Clamp.CODEC);

		for (DensityFunctions.Mapped.Type type2 : DensityFunctions.Mapped.Type.values()) {
			register(registry, type2.getSerializedName(), type2.codec);
		}

		for (DensityFunctions.TwoArgumentSimpleFunction.Type type3 : DensityFunctions.TwoArgumentSimpleFunction.Type.values()) {
			register(registry, type3.getSerializedName(), type3.codec);
		}

		register(registry, "spline", DensityFunctions.Spline.CODEC);
		register(registry, "constant", DensityFunctions.Constant.CODEC);
		return register(registry, "y_clamped_gradient", DensityFunctions.YClampedGradient.CODEC);
	}

	private static Codec<? extends DensityFunction> register(
		Registry<Codec<? extends DensityFunction>> registry, String string, KeyDispatchDataCodec<? extends DensityFunction> keyDispatchDataCodec
	) {
		return Registry.register(registry, string, keyDispatchDataCodec.codec());
	}

	static <A, O> KeyDispatchDataCodec<O> singleArgumentCodec(Codec<A> codec, Function<A, O> function, Function<O, A> function2) {
		return KeyDispatchDataCodec.of(codec.fieldOf("argument").xmap(function, function2));
	}

	static <O> KeyDispatchDataCodec<O> singleFunctionArgumentCodec(Function<DensityFunction, O> function, Function<O, DensityFunction> function2) {
		return singleArgumentCodec(DensityFunction.HOLDER_HELPER_CODEC, function, function2);
	}

	static <O> KeyDispatchDataCodec<O> doubleFunctionArgumentCodec(
		BiFunction<DensityFunction, DensityFunction, O> biFunction, Function<O, DensityFunction> function, Function<O, DensityFunction> function2
	) {
		return KeyDispatchDataCodec.of(
			RecordCodecBuilder.mapCodec(
				instance -> instance.group(
							DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument1").forGetter(function),
							DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument2").forGetter(function2)
						)
						.apply(instance, biFunction)
			)
		);
	}

	static <O> KeyDispatchDataCodec<O> makeCodec(MapCodec<O> mapCodec) {
		return KeyDispatchDataCodec.of(mapCodec);
	}

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

	public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> holder, @Deprecated double d, double e, double f, double g) {
		return mapFromUnitTo(new DensityFunctions.Noise(new DensityFunction.NoiseHolder(holder), d, e), f, g);
	}

	public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> holder, double d, double e, double f) {
		return mappedNoise(holder, 1.0, d, e, f);
	}

	public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> holder, double d, double e) {
		return mappedNoise(holder, 1.0, 1.0, d, e);
	}

	public static DensityFunction shiftedNoise2d(
		DensityFunction densityFunction, DensityFunction densityFunction2, double d, Holder<NormalNoise.NoiseParameters> holder
	) {
		return new DensityFunctions.ShiftedNoise(densityFunction, zero(), densityFunction2, d, 0.0, new DensityFunction.NoiseHolder(holder));
	}

	public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> holder) {
		return noise(holder, 1.0, 1.0);
	}

	public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> holder, double d, double e) {
		return new DensityFunctions.Noise(new DensityFunction.NoiseHolder(holder), d, e);
	}

	public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> holder, double d) {
		return noise(holder, 1.0, d);
	}

	public static DensityFunction rangeChoice(
		DensityFunction densityFunction, double d, double e, DensityFunction densityFunction2, DensityFunction densityFunction3
	) {
		return new DensityFunctions.RangeChoice(densityFunction, d, e, densityFunction2, densityFunction3);
	}

	public static DensityFunction shiftA(Holder<NormalNoise.NoiseParameters> holder) {
		return new DensityFunctions.ShiftA(new DensityFunction.NoiseHolder(holder));
	}

	public static DensityFunction shiftB(Holder<NormalNoise.NoiseParameters> holder) {
		return new DensityFunctions.ShiftB(new DensityFunction.NoiseHolder(holder));
	}

	public static DensityFunction shift(Holder<NormalNoise.NoiseParameters> holder) {
		return new DensityFunctions.Shift(new DensityFunction.NoiseHolder(holder));
	}

	public static DensityFunction blendDensity(DensityFunction densityFunction) {
		return new DensityFunctions.BlendDensity(densityFunction);
	}

	public static DensityFunction endIslands(long l) {
		return new DensityFunctions.EndIslandDensityFunction(l);
	}

	public static DensityFunction weirdScaledSampler(
		DensityFunction densityFunction, Holder<NormalNoise.NoiseParameters> holder, DensityFunctions.WeirdScaledSampler.RarityValueMapper rarityValueMapper
	) {
		return new DensityFunctions.WeirdScaledSampler(densityFunction, new DensityFunction.NoiseHolder(holder), rarityValueMapper);
	}

	public static DensityFunction add(DensityFunction densityFunction, DensityFunction densityFunction2) {
		return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.ADD, densityFunction, densityFunction2);
	}

	public static DensityFunction mul(DensityFunction densityFunction, DensityFunction densityFunction2) {
		return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL, densityFunction, densityFunction2);
	}

	public static DensityFunction min(DensityFunction densityFunction, DensityFunction densityFunction2) {
		return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MIN, densityFunction, densityFunction2);
	}

	public static DensityFunction max(DensityFunction densityFunction, DensityFunction densityFunction2) {
		return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MAX, densityFunction, densityFunction2);
	}

	public static DensityFunction spline(CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> cubicSpline) {
		return new DensityFunctions.Spline(cubicSpline);
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

	public static DensityFunction map(DensityFunction densityFunction, DensityFunctions.Mapped.Type type) {
		return DensityFunctions.Mapped.create(type, densityFunction);
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
		if (densityFunction2 instanceof DensityFunctions.Constant constant) {
			return lerp(densityFunction, constant.value, densityFunction3);
		} else {
			DensityFunction densityFunction4 = cacheOnce(densityFunction);
			DensityFunction densityFunction5 = add(mul(densityFunction4, constant(-1.0)), constant(1.0));
			return add(mul(densityFunction2, densityFunction5), mul(densityFunction3, densityFunction4));
		}
	}

	public static DensityFunction lerp(DensityFunction densityFunction, double d, DensityFunction densityFunction2) {
		return add(mul(densityFunction, add(densityFunction2, constant(-d))), constant(d));
	}

	static record Ap2(DensityFunctions.TwoArgumentSimpleFunction.Type type, DensityFunction argument1, DensityFunction argument2, double minValue, double maxValue)
		implements DensityFunctions.TwoArgumentSimpleFunction {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			double d = this.argument1.compute(functionContext);

			return switch (this.type) {
				case ADD -> d + this.argument2.compute(functionContext);
				case MAX -> d > this.argument2.maxValue() ? d : Math.max(d, this.argument2.compute(functionContext));
				case MIN -> d < this.argument2.minValue() ? d : Math.min(d, this.argument2.compute(functionContext));
				case MUL -> d == 0.0 ? 0.0 : d * this.argument2.compute(functionContext);
			};
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			this.argument1.fillArray(ds, contextProvider);
			switch (this.type) {
				case ADD:
					double[] es = new double[ds.length];
					this.argument2.fillArray(es, contextProvider);

					for (int i = 0; i < ds.length; i++) {
						ds[i] += es[i];
					}
					break;
				case MAX:
					double e = this.argument2.maxValue();

					for (int k = 0; k < ds.length; k++) {
						double f = ds[k];
						ds[k] = f > e ? f : Math.max(f, this.argument2.compute(contextProvider.forIndex(k)));
					}
					break;
				case MIN:
					double e = this.argument2.minValue();

					for (int k = 0; k < ds.length; k++) {
						double f = ds[k];
						ds[k] = f < e ? f : Math.min(f, this.argument2.compute(contextProvider.forIndex(k)));
					}
					break;
				case MUL:
					for (int j = 0; j < ds.length; j++) {
						double d = ds[j];
						ds[j] = d == 0.0 ? 0.0 : d * this.argument2.compute(contextProvider.forIndex(j));
					}
			}
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return visitor.apply(DensityFunctions.TwoArgumentSimpleFunction.create(this.type, this.argument1.mapAll(visitor), this.argument2.mapAll(visitor)));
		}
	}

	protected static enum BeardifierMarker implements DensityFunctions.BeardifierOrMarker {
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

	public interface BeardifierOrMarker extends DensityFunction.SimpleFunction {
		KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(DensityFunctions.BeardifierMarker.INSTANCE));

		@Override
		default KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static enum BlendAlpha implements DensityFunction.SimpleFunction {
		INSTANCE;

		public static final KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

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

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	static record BlendDensity(DensityFunction input) implements DensityFunctions.TransformerWithContext {
		static final KeyDispatchDataCodec<DensityFunctions.BlendDensity> CODEC = DensityFunctions.singleFunctionArgumentCodec(
			DensityFunctions.BlendDensity::new, DensityFunctions.BlendDensity::input
		);

		@Override
		public double transform(DensityFunction.FunctionContext functionContext, double d) {
			return functionContext.getBlender().blendDensity(functionContext, d);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return visitor.apply(new DensityFunctions.BlendDensity(this.input.mapAll(visitor)));
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
			return CODEC;
		}
	}

	protected static enum BlendOffset implements DensityFunction.SimpleFunction {
		INSTANCE;

		public static final KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

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

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static record Clamp(DensityFunction input, double minValue, double maxValue) implements DensityFunctions.PureTransformer {
		private static final MapCodec<DensityFunctions.Clamp> DATA_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						DensityFunction.DIRECT_CODEC.fieldOf("input").forGetter(DensityFunctions.Clamp::input),
						DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min").forGetter(DensityFunctions.Clamp::minValue),
						DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max").forGetter(DensityFunctions.Clamp::maxValue)
					)
					.apply(instance, DensityFunctions.Clamp::new)
		);
		public static final KeyDispatchDataCodec<DensityFunctions.Clamp> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

		@Override
		public double transform(double d) {
			return Mth.clamp(d, this.minValue, this.maxValue);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return new DensityFunctions.Clamp(this.input.mapAll(visitor), this.minValue, this.maxValue);
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	static record Constant(double value) implements DensityFunction.SimpleFunction {
		static final KeyDispatchDataCodec<DensityFunctions.Constant> CODEC = DensityFunctions.singleArgumentCodec(
			DensityFunctions.NOISE_VALUE_CODEC, DensityFunctions.Constant::new, DensityFunctions.Constant::value
		);
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

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static final class EndIslandDensityFunction implements DensityFunction.SimpleFunction {
		public static final KeyDispatchDataCodec<DensityFunctions.EndIslandDensityFunction> CODEC = KeyDispatchDataCodec.of(
			MapCodec.unit(new DensityFunctions.EndIslandDensityFunction(0L))
		);
		private static final float ISLAND_THRESHOLD = -0.9F;
		private final SimplexNoise islandNoise;

		public EndIslandDensityFunction(long l) {
			RandomSource randomSource = new LegacyRandomSource(l);
			randomSource.consumeCount(17292);
			this.islandNoise = new SimplexNoise(randomSource);
		}

		private static float getHeightValue(SimplexNoise simplexNoise, int i, int j) {
			int k = i / 2;
			int l = j / 2;
			int m = i % 2;
			int n = j % 2;
			float f = 100.0F - Mth.sqrt((float)(i * i + j * j)) * 8.0F;
			f = Mth.clamp(f, -100.0F, 80.0F);

			for (int o = -12; o <= 12; o++) {
				for (int p = -12; p <= 12; p++) {
					long q = (long)(k + o);
					long r = (long)(l + p);
					if (q * q + r * r > 4096L && simplexNoise.getValue((double)q, (double)r) < -0.9F) {
						float g = (Mth.abs((float)q) * 3439.0F + Mth.abs((float)r) * 147.0F) % 13.0F + 9.0F;
						float h = (float)(m - o * 2);
						float s = (float)(n - p * 2);
						float t = 100.0F - Mth.sqrt(h * h + s * s) * g;
						t = Mth.clamp(t, -100.0F, 80.0F);
						f = Math.max(f, t);
					}
				}
			}

			return f;
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return ((double)getHeightValue(this.islandNoise, functionContext.blockX() / 8, functionContext.blockZ() / 8) - 8.0) / 128.0;
		}

		@Override
		public double minValue() {
			return -0.84375;
		}

		@Override
		public double maxValue() {
			return 0.5625;
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	@VisibleForDebug
	public static record HolderHolder(Holder<DensityFunction> function) implements DensityFunction {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.function.value().compute(functionContext);
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			this.function.value().fillArray(ds, contextProvider);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return visitor.apply(new DensityFunctions.HolderHolder(new Holder.Direct<>(this.function.value().mapAll(visitor))));
		}

		@Override
		public double minValue() {
			return this.function.value().minValue();
		}

		@Override
		public double maxValue() {
			return this.function.value().maxValue();
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			throw new UnsupportedOperationException("Calling .codec() on HolderHolder");
		}
	}

	protected static record Mapped(DensityFunctions.Mapped.Type type, DensityFunction input, double minValue, double maxValue)
		implements DensityFunctions.PureTransformer {
		public static DensityFunctions.Mapped create(DensityFunctions.Mapped.Type type, DensityFunction densityFunction) {
			double d = densityFunction.minValue();
			double e = transform(type, d);
			double f = transform(type, densityFunction.maxValue());
			return type != DensityFunctions.Mapped.Type.ABS && type != DensityFunctions.Mapped.Type.SQUARE
				? new DensityFunctions.Mapped(type, densityFunction, e, f)
				: new DensityFunctions.Mapped(type, densityFunction, Math.max(0.0, d), Math.max(e, f));
		}

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

		public DensityFunctions.Mapped mapAll(DensityFunction.Visitor visitor) {
			return create(this.type, this.input.mapAll(visitor));
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return this.type.codec;
		}

		static enum Type implements StringRepresentable {
			ABS("abs"),
			SQUARE("square"),
			CUBE("cube"),
			HALF_NEGATIVE("half_negative"),
			QUARTER_NEGATIVE("quarter_negative"),
			SQUEEZE("squeeze");

			private final String name;
			final KeyDispatchDataCodec<DensityFunctions.Mapped> codec = DensityFunctions.singleFunctionArgumentCodec(
				densityFunction -> DensityFunctions.Mapped.create(this, densityFunction), DensityFunctions.Mapped::input
			);

			private Type(String string2) {
				this.name = string2;
			}

			@Override
			public String getSerializedName() {
				return this.name;
			}
		}
	}

	protected static record Marker(DensityFunctions.Marker.Type type, DensityFunction wrapped) implements DensityFunctions.MarkerOrMarked {
		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.wrapped.compute(functionContext);
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			this.wrapped.fillArray(ds, contextProvider);
		}

		@Override
		public double minValue() {
			return this.wrapped.minValue();
		}

		@Override
		public double maxValue() {
			return this.wrapped.maxValue();
		}

		static enum Type implements StringRepresentable {
			Interpolated("interpolated"),
			FlatCache("flat_cache"),
			Cache2D("cache_2d"),
			CacheOnce("cache_once"),
			CacheAllInCell("cache_all_in_cell");

			private final String name;
			final KeyDispatchDataCodec<DensityFunctions.MarkerOrMarked> codec = DensityFunctions.singleFunctionArgumentCodec(
				densityFunction -> new DensityFunctions.Marker(this, densityFunction), DensityFunctions.MarkerOrMarked::wrapped
			);

			private Type(String string2) {
				this.name = string2;
			}

			@Override
			public String getSerializedName() {
				return this.name;
			}
		}
	}

	public interface MarkerOrMarked extends DensityFunction {
		DensityFunctions.Marker.Type type();

		DensityFunction wrapped();

		@Override
		default KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return this.type().codec;
		}

		@Override
		default DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return visitor.apply(new DensityFunctions.Marker(this.type(), this.wrapped().mapAll(visitor)));
		}
	}

	static record MulOrAdd(DensityFunctions.MulOrAdd.Type specificType, DensityFunction input, double minValue, double maxValue, double argument)
		implements DensityFunctions.PureTransformer,
		DensityFunctions.TwoArgumentSimpleFunction {
		@Override
		public DensityFunctions.TwoArgumentSimpleFunction.Type type() {
			return this.specificType == DensityFunctions.MulOrAdd.Type.MUL
				? DensityFunctions.TwoArgumentSimpleFunction.Type.MUL
				: DensityFunctions.TwoArgumentSimpleFunction.Type.ADD;
		}

		@Override
		public DensityFunction argument1() {
			return DensityFunctions.constant(this.argument);
		}

		@Override
		public DensityFunction argument2() {
			return this.input;
		}

		@Override
		public double transform(double d) {
			return switch (this.specificType) {
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
			if (this.specificType == DensityFunctions.MulOrAdd.Type.ADD) {
				f = d + this.argument;
				g = e + this.argument;
			} else if (this.argument >= 0.0) {
				f = d * this.argument;
				g = e * this.argument;
			} else {
				f = e * this.argument;
				g = d * this.argument;
			}

			return new DensityFunctions.MulOrAdd(this.specificType, densityFunction, f, g, this.argument);
		}

		static enum Type {
			MUL,
			ADD;
		}
	}

	protected static record Noise(DensityFunction.NoiseHolder noise, @Deprecated double xzScale, double yScale) implements DensityFunction {
		public static final MapCodec<DensityFunctions.Noise> DATA_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter(DensityFunctions.Noise::noise),
						Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.Noise::xzScale),
						Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.Noise::yScale)
					)
					.apply(instance, DensityFunctions.Noise::new)
		);
		public static final KeyDispatchDataCodec<DensityFunctions.Noise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.noise
				.getValue((double)functionContext.blockX() * this.xzScale, (double)functionContext.blockY() * this.yScale, (double)functionContext.blockZ() * this.xzScale);
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			contextProvider.fillAllDirectly(ds, this);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return visitor.apply(new DensityFunctions.Noise(visitor.visitNoise(this.noise), this.xzScale, this.yScale));
		}

		@Override
		public double minValue() {
			return -this.maxValue();
		}

		@Override
		public double maxValue() {
			return this.noise.maxValue();
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
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
		public static final MapCodec<DensityFunctions.RangeChoice> DATA_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.RangeChoice::input),
						DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min_inclusive").forGetter(DensityFunctions.RangeChoice::minInclusive),
						DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max_exclusive").forGetter(DensityFunctions.RangeChoice::maxExclusive),
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_in_range").forGetter(DensityFunctions.RangeChoice::whenInRange),
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_out_of_range").forGetter(DensityFunctions.RangeChoice::whenOutOfRange)
					)
					.apply(instance, DensityFunctions.RangeChoice::new)
		);
		public static final KeyDispatchDataCodec<DensityFunctions.RangeChoice> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

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
			return visitor.apply(
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

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static record Shift(DensityFunction.NoiseHolder offsetNoise) implements DensityFunctions.ShiftNoise {
		static final KeyDispatchDataCodec<DensityFunctions.Shift> CODEC = DensityFunctions.singleArgumentCodec(
			DensityFunction.NoiseHolder.CODEC, DensityFunctions.Shift::new, DensityFunctions.Shift::offsetNoise
		);

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.compute((double)functionContext.blockX(), (double)functionContext.blockY(), (double)functionContext.blockZ());
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return visitor.apply(new DensityFunctions.Shift(visitor.visitNoise(this.offsetNoise)));
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static record ShiftA(DensityFunction.NoiseHolder offsetNoise) implements DensityFunctions.ShiftNoise {
		static final KeyDispatchDataCodec<DensityFunctions.ShiftA> CODEC = DensityFunctions.singleArgumentCodec(
			DensityFunction.NoiseHolder.CODEC, DensityFunctions.ShiftA::new, DensityFunctions.ShiftA::offsetNoise
		);

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.compute((double)functionContext.blockX(), 0.0, (double)functionContext.blockZ());
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return visitor.apply(new DensityFunctions.ShiftA(visitor.visitNoise(this.offsetNoise)));
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static record ShiftB(DensityFunction.NoiseHolder offsetNoise) implements DensityFunctions.ShiftNoise {
		static final KeyDispatchDataCodec<DensityFunctions.ShiftB> CODEC = DensityFunctions.singleArgumentCodec(
			DensityFunction.NoiseHolder.CODEC, DensityFunctions.ShiftB::new, DensityFunctions.ShiftB::offsetNoise
		);

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.compute((double)functionContext.blockZ(), (double)functionContext.blockX(), 0.0);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return visitor.apply(new DensityFunctions.ShiftB(visitor.visitNoise(this.offsetNoise)));
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	interface ShiftNoise extends DensityFunction {
		DensityFunction.NoiseHolder offsetNoise();

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

		@Override
		default void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			contextProvider.fillAllDirectly(ds, this);
		}
	}

	protected static record ShiftedNoise(
		DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale, DensityFunction.NoiseHolder noise
	) implements DensityFunction {
		private static final MapCodec<DensityFunctions.ShiftedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(DensityFunctions.ShiftedNoise::shiftX),
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_y").forGetter(DensityFunctions.ShiftedNoise::shiftY),
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(DensityFunctions.ShiftedNoise::shiftZ),
						Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.ShiftedNoise::xzScale),
						Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.ShiftedNoise::yScale),
						DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter(DensityFunctions.ShiftedNoise::noise)
					)
					.apply(instance, DensityFunctions.ShiftedNoise::new)
		);
		public static final KeyDispatchDataCodec<DensityFunctions.ShiftedNoise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

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
			return visitor.apply(
				new DensityFunctions.ShiftedNoise(
					this.shiftX.mapAll(visitor), this.shiftY.mapAll(visitor), this.shiftZ.mapAll(visitor), this.xzScale, this.yScale, visitor.visitNoise(this.noise)
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

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	public static record Spline(CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> spline) implements DensityFunction {
		private static final Codec<CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>> SPLINE_CODEC = CubicSpline.codec(
			DensityFunctions.Spline.Coordinate.CODEC
		);
		private static final MapCodec<DensityFunctions.Spline> DATA_CODEC = SPLINE_CODEC.fieldOf("spline")
			.xmap(DensityFunctions.Spline::new, DensityFunctions.Spline::spline);
		public static final KeyDispatchDataCodec<DensityFunctions.Spline> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return (double)this.spline.apply(new DensityFunctions.Spline.Point(functionContext));
		}

		@Override
		public double minValue() {
			return (double)this.spline.minValue();
		}

		@Override
		public double maxValue() {
			return (double)this.spline.maxValue();
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			contextProvider.fillAllDirectly(ds, this);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return visitor.apply(new DensityFunctions.Spline(this.spline.mapAll(coordinate -> coordinate.mapAll(visitor))));
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}

		public static record Coordinate(Holder<DensityFunction> function) implements ToFloatFunction<DensityFunctions.Spline.Point> {
			public static final Codec<DensityFunctions.Spline.Coordinate> CODEC = DensityFunction.CODEC
				.xmap(DensityFunctions.Spline.Coordinate::new, DensityFunctions.Spline.Coordinate::function);

			public String toString() {
				Optional<ResourceKey<DensityFunction>> optional = this.function.unwrapKey();
				if (optional.isPresent()) {
					ResourceKey<DensityFunction> resourceKey = (ResourceKey<DensityFunction>)optional.get();
					if (resourceKey == NoiseRouterData.CONTINENTS) {
						return "continents";
					}

					if (resourceKey == NoiseRouterData.EROSION) {
						return "erosion";
					}

					if (resourceKey == NoiseRouterData.RIDGES) {
						return "weirdness";
					}

					if (resourceKey == NoiseRouterData.RIDGES_FOLDED) {
						return "ridges";
					}
				}

				return "Coordinate[" + this.function + "]";
			}

			public float apply(DensityFunctions.Spline.Point point) {
				return (float)this.function.value().compute(point.context());
			}

			@Override
			public float minValue() {
				return (float)this.function.value().minValue();
			}

			@Override
			public float maxValue() {
				return (float)this.function.value().maxValue();
			}

			public DensityFunctions.Spline.Coordinate mapAll(DensityFunction.Visitor visitor) {
				return new DensityFunctions.Spline.Coordinate(new Holder.Direct<>(this.function.value().mapAll(visitor)));
			}
		}

		public static record Point(DensityFunction.FunctionContext context) {
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

	interface TwoArgumentSimpleFunction extends DensityFunction {
		Logger LOGGER = LogUtils.getLogger();

		static DensityFunctions.TwoArgumentSimpleFunction create(
			DensityFunctions.TwoArgumentSimpleFunction.Type type, DensityFunction densityFunction, DensityFunction densityFunction2
		) {
			double d = densityFunction.minValue();
			double e = densityFunction2.minValue();
			double f = densityFunction.maxValue();
			double g = densityFunction2.maxValue();
			if (type == DensityFunctions.TwoArgumentSimpleFunction.Type.MIN || type == DensityFunctions.TwoArgumentSimpleFunction.Type.MAX) {
				boolean bl = d >= g;
				boolean bl2 = e >= f;
				if (bl || bl2) {
					LOGGER.warn("Creating a " + type + " function between two non-overlapping inputs: " + densityFunction + " and " + densityFunction2);
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
			if (type == DensityFunctions.TwoArgumentSimpleFunction.Type.MUL || type == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD) {
				if (densityFunction instanceof DensityFunctions.Constant constant) {
					return new DensityFunctions.MulOrAdd(
						type == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL,
						densityFunction2,
						h,
						i,
						constant.value
					);
				}

				if (densityFunction2 instanceof DensityFunctions.Constant constant) {
					return new DensityFunctions.MulOrAdd(
						type == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL,
						densityFunction,
						h,
						i,
						constant.value
					);
				}
			}

			return new DensityFunctions.Ap2(type, densityFunction, densityFunction2, h, i);
		}

		DensityFunctions.TwoArgumentSimpleFunction.Type type();

		DensityFunction argument1();

		DensityFunction argument2();

		@Override
		default KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return this.type().codec;
		}

		public static enum Type implements StringRepresentable {
			ADD("add"),
			MUL("mul"),
			MIN("min"),
			MAX("max");

			final KeyDispatchDataCodec<DensityFunctions.TwoArgumentSimpleFunction> codec = DensityFunctions.doubleFunctionArgumentCodec(
				(densityFunction, densityFunction2) -> DensityFunctions.TwoArgumentSimpleFunction.create(this, densityFunction, densityFunction2),
				DensityFunctions.TwoArgumentSimpleFunction::argument1,
				DensityFunctions.TwoArgumentSimpleFunction::argument2
			);
			private final String name;

			private Type(String string2) {
				this.name = string2;
			}

			@Override
			public String getSerializedName() {
				return this.name;
			}
		}
	}

	protected static record WeirdScaledSampler(
		DensityFunction input, DensityFunction.NoiseHolder noise, DensityFunctions.WeirdScaledSampler.RarityValueMapper rarityValueMapper
	) implements DensityFunctions.TransformerWithContext {
		private static final MapCodec<DensityFunctions.WeirdScaledSampler> DATA_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.WeirdScaledSampler::input),
						DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter(DensityFunctions.WeirdScaledSampler::noise),
						DensityFunctions.WeirdScaledSampler.RarityValueMapper.CODEC
							.fieldOf("rarity_value_mapper")
							.forGetter(DensityFunctions.WeirdScaledSampler::rarityValueMapper)
					)
					.apply(instance, DensityFunctions.WeirdScaledSampler::new)
		);
		public static final KeyDispatchDataCodec<DensityFunctions.WeirdScaledSampler> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

		@Override
		public double transform(DensityFunction.FunctionContext functionContext, double d) {
			double e = this.rarityValueMapper.mapper.get(d);
			return e * Math.abs(this.noise.getValue((double)functionContext.blockX() / e, (double)functionContext.blockY() / e, (double)functionContext.blockZ() / e));
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return visitor.apply(new DensityFunctions.WeirdScaledSampler(this.input.mapAll(visitor), visitor.visitNoise(this.noise), this.rarityValueMapper));
		}

		@Override
		public double minValue() {
			return 0.0;
		}

		@Override
		public double maxValue() {
			return this.rarityValueMapper.maxRarity * this.noise.maxValue();
		}

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}

		public static enum RarityValueMapper implements StringRepresentable {
			TYPE1("type_1", NoiseRouterData.QuantizedSpaghettiRarity::getSpaghettiRarity3D, 2.0),
			TYPE2("type_2", NoiseRouterData.QuantizedSpaghettiRarity::getSphaghettiRarity2D, 3.0);

			public static final Codec<DensityFunctions.WeirdScaledSampler.RarityValueMapper> CODEC = StringRepresentable.fromEnum(
				DensityFunctions.WeirdScaledSampler.RarityValueMapper::values
			);
			private final String name;
			final Double2DoubleFunction mapper;
			final double maxRarity;

			private RarityValueMapper(String string2, Double2DoubleFunction double2DoubleFunction, double d) {
				this.name = string2;
				this.mapper = double2DoubleFunction;
				this.maxRarity = d;
			}

			@Override
			public String getSerializedName() {
				return this.name;
			}
		}
	}

	static record YClampedGradient(int fromY, int toY, double fromValue, double toValue) implements DensityFunction.SimpleFunction {
		private static final MapCodec<DensityFunctions.YClampedGradient> DATA_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2).fieldOf("from_y").forGetter(DensityFunctions.YClampedGradient::fromY),
						Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2).fieldOf("to_y").forGetter(DensityFunctions.YClampedGradient::toY),
						DensityFunctions.NOISE_VALUE_CODEC.fieldOf("from_value").forGetter(DensityFunctions.YClampedGradient::fromValue),
						DensityFunctions.NOISE_VALUE_CODEC.fieldOf("to_value").forGetter(DensityFunctions.YClampedGradient::toValue)
					)
					.apply(instance, DensityFunctions.YClampedGradient::new)
		);
		public static final KeyDispatchDataCodec<DensityFunctions.YClampedGradient> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

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

		@Override
		public KeyDispatchDataCodec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}
}
