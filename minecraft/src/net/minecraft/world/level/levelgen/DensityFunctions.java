package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.slf4j.Logger;

public final class DensityFunctions {
	private static final Codec<DensityFunction> CODEC = Registry.DENSITY_FUNCTION_TYPES.byNameCodec().dispatch(DensityFunction::codec, Function.identity());
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

		register(registry, "slide", DensityFunctions.Slide.CODEC);

		for (DensityFunctions.TwoArgumentSimpleFunction.Type type3 : DensityFunctions.TwoArgumentSimpleFunction.Type.values()) {
			register(registry, type3.getSerializedName(), type3.codec);
		}

		register(registry, "spline", DensityFunctions.Spline.CODEC);
		register(registry, "terrain_shaper_spline", DensityFunctions.TerrainShaperSpline.CODEC);
		register(registry, "constant", DensityFunctions.Constant.CODEC);
		return register(registry, "y_clamped_gradient", DensityFunctions.YClampedGradient.CODEC);
	}

	private static Codec<? extends DensityFunction> register(
		Registry<Codec<? extends DensityFunction>> registry, String string, Codec<? extends DensityFunction> codec
	) {
		return Registry.register(registry, string, codec);
	}

	static <A, O> Codec<O> singleArgumentCodec(Codec<A> codec, Function<A, O> function, Function<O, A> function2) {
		return codec.fieldOf("argument").xmap(function, function2).codec();
	}

	static <O> Codec<O> singleFunctionArgumentCodec(Function<DensityFunction, O> function, Function<O, DensityFunction> function2) {
		return singleArgumentCodec(DensityFunction.HOLDER_HELPER_CODEC, function, function2);
	}

	static <O> Codec<O> doubleFunctionArgumentCodec(
		BiFunction<DensityFunction, DensityFunction, O> biFunction, Function<O, DensityFunction> function, Function<O, DensityFunction> function2
	) {
		return RecordCodecBuilder.create(
			instance -> instance.group(
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument1").forGetter(function),
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument2").forGetter(function2)
					)
					.apply(instance, biFunction)
		);
	}

	static <O> Codec<O> makeCodec(MapCodec<O> mapCodec) {
		return mapCodec.codec();
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
		return mapFromUnitTo(new DensityFunctions.Noise(holder, null, d, e), f, g);
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
		return new DensityFunctions.ShiftedNoise(densityFunction, zero(), densityFunction2, d, 0.0, holder, null);
	}

	public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> holder) {
		return noise(holder, 1.0, 1.0);
	}

	public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> holder, double d, double e) {
		return new DensityFunctions.Noise(holder, null, d, e);
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
		return new DensityFunctions.ShiftA(holder, null);
	}

	public static DensityFunction shiftB(Holder<NormalNoise.NoiseParameters> holder) {
		return new DensityFunctions.ShiftB(holder, null);
	}

	public static DensityFunction shift(Holder<NormalNoise.NoiseParameters> holder) {
		return new DensityFunctions.Shift(holder, null);
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
		return new DensityFunctions.WeirdScaledSampler(densityFunction, holder, null, rarityValueMapper);
	}

	public static DensityFunction slide(NoiseSettings noiseSettings, DensityFunction densityFunction) {
		return new DensityFunctions.Slide(noiseSettings, densityFunction);
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

	public static DensityFunction terrainShaperSpline(
		DensityFunction densityFunction,
		DensityFunction densityFunction2,
		DensityFunction densityFunction3,
		DensityFunctions.TerrainShaperSpline.SplineType splineType,
		double d,
		double e
	) {
		return new DensityFunctions.TerrainShaperSpline(densityFunction, densityFunction2, densityFunction3, null, splineType, d, e);
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
		DensityFunction densityFunction4 = cacheOnce(densityFunction);
		DensityFunction densityFunction5 = add(mul(densityFunction4, constant(-1.0)), constant(1.0));
		return add(mul(densityFunction2, densityFunction5), mul(densityFunction3, densityFunction4));
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
			return (DensityFunction)visitor.apply(
				DensityFunctions.TwoArgumentSimpleFunction.create(this.type, this.argument1.mapAll(visitor), this.argument2.mapAll(visitor))
			);
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
		Codec<DensityFunction> CODEC = Codec.unit(DensityFunctions.BeardifierMarker.INSTANCE);

		@Override
		default Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static enum BlendAlpha implements DensityFunction.SimpleFunction {
		INSTANCE;

		public static final Codec<DensityFunction> CODEC = Codec.unit(INSTANCE);

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
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	static record BlendDensity(DensityFunction input) implements DensityFunctions.TransformerWithContext {
		static final Codec<DensityFunctions.BlendDensity> CODEC = DensityFunctions.singleFunctionArgumentCodec(
			DensityFunctions.BlendDensity::new, DensityFunctions.BlendDensity::input
		);

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

		@Override
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static enum BlendOffset implements DensityFunction.SimpleFunction {
		INSTANCE;

		public static final Codec<DensityFunction> CODEC = Codec.unit(INSTANCE);

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
		public Codec<? extends DensityFunction> codec() {
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
		public static final Codec<DensityFunctions.Clamp> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

		@Override
		public double transform(double d) {
			return Mth.clamp(d, this.minValue, this.maxValue);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return new DensityFunctions.Clamp(this.input.mapAll(visitor), this.minValue, this.maxValue);
		}

		@Override
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	static record Constant(double value) implements DensityFunction.SimpleFunction {
		static final Codec<DensityFunctions.Constant> CODEC = DensityFunctions.singleArgumentCodec(
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
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static final class EndIslandDensityFunction implements DensityFunction.SimpleFunction {
		public static final Codec<DensityFunctions.EndIslandDensityFunction> CODEC = Codec.unit(new DensityFunctions.EndIslandDensityFunction(0L));
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

		@Override
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static record HolderHolder(Holder<DensityFunction> function) implements DensityFunction {
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
			return (DensityFunction)visitor.apply(new DensityFunctions.HolderHolder(new Holder.Direct<>(this.function.value().mapAll(visitor))));
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
		public Codec<? extends DensityFunction> codec() {
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
		public Codec<? extends DensityFunction> codec() {
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
			final Codec<DensityFunctions.Mapped> codec = DensityFunctions.singleFunctionArgumentCodec(
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
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return (DensityFunction)visitor.apply(new DensityFunctions.Marker(this.type, this.wrapped.mapAll(visitor)));
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
			final Codec<DensityFunctions.MarkerOrMarked> codec = DensityFunctions.singleFunctionArgumentCodec(
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
		default Codec<? extends DensityFunction> codec() {
			return this.type().codec;
		}
	}

	static record MulOrAdd(DensityFunctions.MulOrAdd.Type specificType, DensityFunction input, double minValue, double maxValue, double argument)
		implements DensityFunctions.TwoArgumentSimpleFunction,
		DensityFunctions.PureTransformer {
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

	protected static record Noise(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise noise, @Deprecated double xzScale, double yScale)
		implements DensityFunction.SimpleFunction {
		public static final MapCodec<DensityFunctions.Noise> DATA_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						NormalNoise.NoiseParameters.CODEC.fieldOf("noise").forGetter(DensityFunctions.Noise::noiseData),
						Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.Noise::xzScale),
						Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.Noise::yScale)
					)
					.apply(instance, DensityFunctions.Noise::createUnseeded)
		);
		public static final Codec<DensityFunctions.Noise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

		public static DensityFunctions.Noise createUnseeded(Holder<NormalNoise.NoiseParameters> holder, @Deprecated double d, double e) {
			return new DensityFunctions.Noise(holder, null, d, e);
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.noise == null
				? 0.0
				: this.noise
					.getValue((double)functionContext.blockX() * this.xzScale, (double)functionContext.blockY() * this.yScale, (double)functionContext.blockZ() * this.xzScale);
		}

		@Override
		public double minValue() {
			return -this.maxValue();
		}

		@Override
		public double maxValue() {
			return this.noise == null ? 2.0 : this.noise.maxValue();
		}

		@Override
		public Codec<? extends DensityFunction> codec() {
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
		public static final Codec<DensityFunctions.RangeChoice> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

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

		@Override
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	static record Shift(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
		static final Codec<DensityFunctions.Shift> CODEC = DensityFunctions.singleArgumentCodec(
			NormalNoise.NoiseParameters.CODEC, holder -> new DensityFunctions.Shift(holder, null), DensityFunctions.Shift::noiseData
		);

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.compute((double)functionContext.blockX(), (double)functionContext.blockY(), (double)functionContext.blockZ());
		}

		@Override
		public DensityFunctions.ShiftNoise withNewNoise(NormalNoise normalNoise) {
			return new DensityFunctions.Shift(this.noiseData, normalNoise);
		}

		@Override
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static record ShiftA(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
		static final Codec<DensityFunctions.ShiftA> CODEC = DensityFunctions.singleArgumentCodec(
			NormalNoise.NoiseParameters.CODEC, holder -> new DensityFunctions.ShiftA(holder, null), DensityFunctions.ShiftA::noiseData
		);

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.compute((double)functionContext.blockX(), 0.0, (double)functionContext.blockZ());
		}

		@Override
		public DensityFunctions.ShiftNoise withNewNoise(NormalNoise normalNoise) {
			return new DensityFunctions.ShiftA(this.noiseData, normalNoise);
		}

		@Override
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static record ShiftB(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) implements DensityFunctions.ShiftNoise {
		static final Codec<DensityFunctions.ShiftB> CODEC = DensityFunctions.singleArgumentCodec(
			NormalNoise.NoiseParameters.CODEC, holder -> new DensityFunctions.ShiftB(holder, null), DensityFunctions.ShiftB::noiseData
		);

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.compute((double)functionContext.blockZ(), (double)functionContext.blockX(), 0.0);
		}

		@Override
		public DensityFunctions.ShiftNoise withNewNoise(NormalNoise normalNoise) {
			return new DensityFunctions.ShiftB(this.noiseData, normalNoise);
		}

		@Override
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	interface ShiftNoise extends DensityFunction.SimpleFunction {
		Holder<NormalNoise.NoiseParameters> noiseData();

		@Nullable
		NormalNoise offsetNoise();

		@Override
		default double minValue() {
			return -this.maxValue();
		}

		@Override
		default double maxValue() {
			NormalNoise normalNoise = this.offsetNoise();
			return (normalNoise == null ? 2.0 : normalNoise.maxValue()) * 4.0;
		}

		default double compute(double d, double e, double f) {
			NormalNoise normalNoise = this.offsetNoise();
			return normalNoise == null ? 0.0 : normalNoise.getValue(d * 0.25, e * 0.25, f * 0.25) * 4.0;
		}

		DensityFunctions.ShiftNoise withNewNoise(NormalNoise normalNoise);
	}

	protected static record ShiftedNoise(
		DensityFunction shiftX,
		DensityFunction shiftY,
		DensityFunction shiftZ,
		double xzScale,
		double yScale,
		Holder<NormalNoise.NoiseParameters> noiseData,
		@Nullable NormalNoise noise
	) implements DensityFunction {
		private static final MapCodec<DensityFunctions.ShiftedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(DensityFunctions.ShiftedNoise::shiftX),
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_y").forGetter(DensityFunctions.ShiftedNoise::shiftY),
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(DensityFunctions.ShiftedNoise::shiftZ),
						Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.ShiftedNoise::xzScale),
						Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.ShiftedNoise::yScale),
						NormalNoise.NoiseParameters.CODEC.fieldOf("noise").forGetter(DensityFunctions.ShiftedNoise::noiseData)
					)
					.apply(instance, DensityFunctions.ShiftedNoise::createUnseeded)
		);
		public static final Codec<DensityFunctions.ShiftedNoise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

		public static DensityFunctions.ShiftedNoise createUnseeded(
			DensityFunction densityFunction,
			DensityFunction densityFunction2,
			DensityFunction densityFunction3,
			double d,
			double e,
			Holder<NormalNoise.NoiseParameters> holder
		) {
			return new DensityFunctions.ShiftedNoise(densityFunction, densityFunction2, densityFunction3, d, e, holder, null);
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			if (this.noise == null) {
				return 0.0;
			} else {
				double d = (double)functionContext.blockX() * this.xzScale + this.shiftX.compute(functionContext);
				double e = (double)functionContext.blockY() * this.yScale + this.shiftY.compute(functionContext);
				double f = (double)functionContext.blockZ() * this.xzScale + this.shiftZ.compute(functionContext);
				return this.noise.getValue(d, e, f);
			}
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			contextProvider.fillAllDirectly(ds, this);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return (DensityFunction)visitor.apply(
				new DensityFunctions.ShiftedNoise(
					this.shiftX.mapAll(visitor), this.shiftY.mapAll(visitor), this.shiftZ.mapAll(visitor), this.xzScale, this.yScale, this.noiseData, this.noise
				)
			);
		}

		@Override
		public double minValue() {
			return -this.maxValue();
		}

		@Override
		public double maxValue() {
			return this.noise == null ? 2.0 : this.noise.maxValue();
		}

		@Override
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	protected static record Slide(@Nullable NoiseSettings settings, DensityFunction input) implements DensityFunctions.TransformerWithContext {
		public static final Codec<DensityFunctions.Slide> CODEC = DensityFunctions.singleFunctionArgumentCodec(
			densityFunction -> new DensityFunctions.Slide(null, densityFunction), DensityFunctions.Slide::input
		);

		@Override
		public double transform(DensityFunction.FunctionContext functionContext, double d) {
			return this.settings == null ? d : NoiseRouterData.applySlide(this.settings, d, (double)functionContext.blockY());
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return (DensityFunction)visitor.apply(new DensityFunctions.Slide(this.settings, this.input.mapAll(visitor)));
		}

		@Override
		public double minValue() {
			return this.settings == null
				? this.input.minValue()
				: Math.min(this.input.minValue(), Math.min(this.settings.bottomSlideSettings().target(), this.settings.topSlideSettings().target()));
		}

		@Override
		public double maxValue() {
			return this.settings == null
				? this.input.maxValue()
				: Math.max(this.input.maxValue(), Math.max(this.settings.bottomSlideSettings().target(), this.settings.topSlideSettings().target()));
		}

		@Override
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	public static record Spline(CubicSpline<TerrainShaper.PointCustom> spline, double minValue, double maxValue) implements DensityFunction {
		private static final MapCodec<DensityFunctions.Spline> DATA_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						TerrainShaper.SPLINE_CUSTOM_CODEC.fieldOf("spline").forGetter(DensityFunctions.Spline::spline),
						DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min_value").forGetter(DensityFunctions.Spline::minValue),
						DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max_value").forGetter(DensityFunctions.Spline::maxValue)
					)
					.apply(instance, DensityFunctions.Spline::new)
		);
		public static final Codec<DensityFunctions.Spline> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return Mth.clamp((double)this.spline.apply(TerrainShaper.makePoint(functionContext)), this.minValue, this.maxValue);
		}

		@Override
		public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
			contextProvider.fillAllDirectly(ds, this);
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			return (DensityFunction)visitor.apply(
				new DensityFunctions.Spline(
					this.spline
						.mapAll(
							toFloatFunction -> (ToFloatFunction<TerrainShaper.PointCustom>)(toFloatFunction instanceof TerrainShaper.CoordinateCustom coordinateCustom
									? coordinateCustom.mapAll(visitor)
									: toFloatFunction)
						),
					this.minValue,
					this.maxValue
				)
			);
		}

		@Override
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}

	@Deprecated
	public static record TerrainShaperSpline(
		DensityFunction continentalness,
		DensityFunction erosion,
		DensityFunction weirdness,
		@Nullable TerrainShaper shaper,
		DensityFunctions.TerrainShaperSpline.SplineType spline,
		double minValue,
		double maxValue
	) implements DensityFunction {
		private static final MapCodec<DensityFunctions.TerrainShaperSpline> DATA_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("continentalness").forGetter(DensityFunctions.TerrainShaperSpline::continentalness),
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("erosion").forGetter(DensityFunctions.TerrainShaperSpline::erosion),
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("weirdness").forGetter(DensityFunctions.TerrainShaperSpline::weirdness),
						DensityFunctions.TerrainShaperSpline.SplineType.CODEC.fieldOf("spline").forGetter(DensityFunctions.TerrainShaperSpline::spline),
						DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min_value").forGetter(DensityFunctions.TerrainShaperSpline::minValue),
						DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max_value").forGetter(DensityFunctions.TerrainShaperSpline::maxValue)
					)
					.apply(instance, DensityFunctions.TerrainShaperSpline::createUnseeded)
		);
		public static final Codec<DensityFunctions.TerrainShaperSpline> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

		public static DensityFunctions.TerrainShaperSpline createUnseeded(
			DensityFunction densityFunction,
			DensityFunction densityFunction2,
			DensityFunction densityFunction3,
			DensityFunctions.TerrainShaperSpline.SplineType splineType,
			double d,
			double e
		) {
			return new DensityFunctions.TerrainShaperSpline(densityFunction, densityFunction2, densityFunction3, null, splineType, d, e);
		}

		@Override
		public double compute(DensityFunction.FunctionContext functionContext) {
			return this.shaper == null
				? 0.0
				: Mth.clamp(
					(double)this.spline
						.spline
						.apply(
							this.shaper,
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
					this.continentalness.mapAll(visitor), this.erosion.mapAll(visitor), this.weirdness.mapAll(visitor), this.shaper, this.spline, this.minValue, this.maxValue
				)
			);
		}

		@Override
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}

		interface Spline {
			float apply(TerrainShaper terrainShaper, TerrainShaper.Point point);
		}

		public static enum SplineType implements StringRepresentable {
			OFFSET("offset", TerrainShaper::offset),
			FACTOR("factor", TerrainShaper::factor),
			JAGGEDNESS("jaggedness", TerrainShaper::jaggedness);

			private static final Map<String, DensityFunctions.TerrainShaperSpline.SplineType> BY_NAME = (Map<String, DensityFunctions.TerrainShaperSpline.SplineType>)Arrays.stream(
					values()
				)
				.collect(Collectors.toMap(DensityFunctions.TerrainShaperSpline.SplineType::getSerializedName, splineType -> splineType));
			public static final Codec<DensityFunctions.TerrainShaperSpline.SplineType> CODEC = StringRepresentable.fromEnum(
				DensityFunctions.TerrainShaperSpline.SplineType::values, BY_NAME::get
			);
			private final String name;
			final DensityFunctions.TerrainShaperSpline.Spline spline;

			private SplineType(String string2, DensityFunctions.TerrainShaperSpline.Spline spline) {
				this.name = string2;
				this.spline = spline;
			}

			@Override
			public String getSerializedName() {
				return this.name;
			}
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
		default Codec<? extends DensityFunction> codec() {
			return this.type().codec;
		}

		public static enum Type implements StringRepresentable {
			ADD("add"),
			MUL("mul"),
			MIN("min"),
			MAX("max");

			final Codec<DensityFunctions.TwoArgumentSimpleFunction> codec = DensityFunctions.doubleFunctionArgumentCodec(
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
		DensityFunction input,
		Holder<NormalNoise.NoiseParameters> noiseData,
		@Nullable NormalNoise noise,
		DensityFunctions.WeirdScaledSampler.RarityValueMapper rarityValueMapper
	) implements DensityFunctions.TransformerWithContext {
		private static final MapCodec<DensityFunctions.WeirdScaledSampler> DATA_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.WeirdScaledSampler::input),
						NormalNoise.NoiseParameters.CODEC.fieldOf("noise").forGetter(DensityFunctions.WeirdScaledSampler::noiseData),
						DensityFunctions.WeirdScaledSampler.RarityValueMapper.CODEC
							.fieldOf("rarity_value_mapper")
							.forGetter(DensityFunctions.WeirdScaledSampler::rarityValueMapper)
					)
					.apply(instance, DensityFunctions.WeirdScaledSampler::createUnseeded)
		);
		public static final Codec<DensityFunctions.WeirdScaledSampler> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

		public static DensityFunctions.WeirdScaledSampler createUnseeded(
			DensityFunction densityFunction, Holder<NormalNoise.NoiseParameters> holder, DensityFunctions.WeirdScaledSampler.RarityValueMapper rarityValueMapper
		) {
			return new DensityFunctions.WeirdScaledSampler(densityFunction, holder, null, rarityValueMapper);
		}

		@Override
		public double transform(DensityFunction.FunctionContext functionContext, double d) {
			if (this.noise == null) {
				return 0.0;
			} else {
				double e = this.rarityValueMapper.mapper.get(d);
				return e * Math.abs(this.noise.getValue((double)functionContext.blockX() / e, (double)functionContext.blockY() / e, (double)functionContext.blockZ() / e));
			}
		}

		@Override
		public DensityFunction mapAll(DensityFunction.Visitor visitor) {
			this.input.mapAll(visitor);
			return (DensityFunction)visitor.apply(
				new DensityFunctions.WeirdScaledSampler(this.input.mapAll(visitor), this.noiseData, this.noise, this.rarityValueMapper)
			);
		}

		@Override
		public double minValue() {
			return 0.0;
		}

		@Override
		public double maxValue() {
			return this.rarityValueMapper.maxRarity * (this.noise == null ? 2.0 : this.noise.maxValue());
		}

		@Override
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}

		public static enum RarityValueMapper implements StringRepresentable {
			TYPE1("type_1", NoiseRouterData.QuantizedSpaghettiRarity::getSpaghettiRarity3D, 2.0),
			TYPE2("type_2", NoiseRouterData.QuantizedSpaghettiRarity::getSphaghettiRarity2D, 3.0);

			private static final Map<String, DensityFunctions.WeirdScaledSampler.RarityValueMapper> BY_NAME = (Map<String, DensityFunctions.WeirdScaledSampler.RarityValueMapper>)Arrays.stream(
					values()
				)
				.collect(Collectors.toMap(DensityFunctions.WeirdScaledSampler.RarityValueMapper::getSerializedName, rarityValueMapper -> rarityValueMapper));
			public static final Codec<DensityFunctions.WeirdScaledSampler.RarityValueMapper> CODEC = StringRepresentable.fromEnum(
				DensityFunctions.WeirdScaledSampler.RarityValueMapper::values, BY_NAME::get
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
		public static final Codec<DensityFunctions.YClampedGradient> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

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
		public Codec<? extends DensityFunction> codec() {
			return CODEC;
		}
	}
}
