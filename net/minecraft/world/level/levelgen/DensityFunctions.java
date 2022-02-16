/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.Arrays;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.slf4j.Logger;

public final class DensityFunctions {
    private DensityFunctions() {
    }

    public static DensityFunction interpolated(DensityFunction densityFunction) {
        return new Marker(Marker.Type.Interpolated, densityFunction);
    }

    public static DensityFunction flatCache(DensityFunction densityFunction) {
        return new Marker(Marker.Type.FlatCache, densityFunction);
    }

    public static DensityFunction cache2d(DensityFunction densityFunction) {
        return new Marker(Marker.Type.Cache2D, densityFunction);
    }

    public static DensityFunction cacheOnce(DensityFunction densityFunction) {
        return new Marker(Marker.Type.CacheOnce, densityFunction);
    }

    public static DensityFunction cacheAllInCell(DensityFunction densityFunction) {
        return new Marker(Marker.Type.CacheAllInCell, densityFunction);
    }

    public static DensityFunction mappedNoise(NormalNoise normalNoise, @Deprecated double d, double e, double f, double g) {
        return DensityFunctions.mapFromUnitTo(new Noise(normalNoise, d, e), f, g);
    }

    public static DensityFunction mappedNoise(NormalNoise normalNoise, double d, double e, double f) {
        return DensityFunctions.mapFromUnitTo(new Noise(normalNoise, 1.0, d), e, f);
    }

    public static DensityFunction mappedNoise(NormalNoise normalNoise, double d, double e) {
        return DensityFunctions.mapFromUnitTo(new Noise(normalNoise, 1.0, 1.0), d, e);
    }

    public static DensityFunction shiftedNoise2d(DensityFunction densityFunction, DensityFunction densityFunction2, double d, NormalNoise normalNoise) {
        return new ShiftedNoise(densityFunction, DensityFunctions.zero(), densityFunction2, d, 0.0, normalNoise);
    }

    public static DensityFunction noise(NormalNoise normalNoise) {
        return new Noise(normalNoise);
    }

    public static DensityFunction noise(NormalNoise normalNoise, double d, double e) {
        return new Noise(normalNoise, d, e);
    }

    public static DensityFunction noise(NormalNoise normalNoise, double d) {
        return new Noise(normalNoise, d);
    }

    public static DensityFunction rangeChoice(DensityFunction densityFunction, double d, double e, DensityFunction densityFunction2, DensityFunction densityFunction3) {
        return new RangeChoice(densityFunction, d, e, densityFunction2, densityFunction3);
    }

    public static DensityFunction shiftA(NormalNoise normalNoise) {
        return new ShiftA(normalNoise);
    }

    public static DensityFunction shiftB(NormalNoise normalNoise) {
        return new ShiftB(normalNoise);
    }

    public static DensityFunction shift0(NormalNoise normalNoise) {
        return new Shift0(normalNoise);
    }

    public static DensityFunction shift1(NormalNoise normalNoise) {
        return new Shift1(normalNoise);
    }

    public static DensityFunction shift2(NormalNoise normalNoise) {
        return new Shift2(normalNoise);
    }

    public static DensityFunction blendDensity(DensityFunction densityFunction) {
        return new BlendDensity(densityFunction);
    }

    public static DensityFunction endIslands(long l) {
        return new EndIslandDensityFunction(l);
    }

    public static DensityFunction weirdScaledSampler(DensityFunction densityFunction, NormalNoise normalNoise, Double2DoubleFunction double2DoubleFunction, double d) {
        return new WeirdScaledSampler(densityFunction, normalNoise, double2DoubleFunction, d);
    }

    public static DensityFunction slide(NoiseSettings noiseSettings, DensityFunction densityFunction) {
        return new Slide(noiseSettings, densityFunction);
    }

    public static DensityFunction add(DensityFunction densityFunction, DensityFunction densityFunction2) {
        return Ap2.create(Ap2.Type.ADD, densityFunction, densityFunction2);
    }

    public static DensityFunction mul(DensityFunction densityFunction, DensityFunction densityFunction2) {
        return Ap2.create(Ap2.Type.MUL, densityFunction, densityFunction2);
    }

    public static DensityFunction min(DensityFunction densityFunction, DensityFunction densityFunction2) {
        return Ap2.create(Ap2.Type.MIN, densityFunction, densityFunction2);
    }

    public static DensityFunction max(DensityFunction densityFunction, DensityFunction densityFunction2) {
        return Ap2.create(Ap2.Type.MAX, densityFunction, densityFunction2);
    }

    public static DensityFunction terrainShaperSpline(DensityFunction densityFunction, DensityFunction densityFunction2, DensityFunction densityFunction3, ToFloatFunction<TerrainShaper.Point> toFloatFunction, double d, double e) {
        return new TerrainShaperSpline(densityFunction, densityFunction2, densityFunction3, toFloatFunction, d, e);
    }

    public static DensityFunction zero() {
        return Constant.ZERO;
    }

    public static DensityFunction constant(double d) {
        return new Constant(d);
    }

    public static DensityFunction yClampedGradient(int i, int j, double d, double e) {
        return new YClampedGradient(i, j, d, e);
    }

    protected static DensityFunction map(DensityFunction densityFunction2, Mapped.Type type) {
        return new Mapped(type, densityFunction2, 0.0, 0.0).mapAll(densityFunction -> densityFunction);
    }

    private static DensityFunction mapFromUnitTo(DensityFunction densityFunction, double d, double e) {
        double f = (d + e) * 0.5;
        double g = (e - d) * 0.5;
        return DensityFunctions.add(DensityFunctions.constant(f), DensityFunctions.mul(DensityFunctions.constant(g), densityFunction));
    }

    public static DensityFunction blendAlpha() {
        return BlendAlpha.INSTANCE;
    }

    public static DensityFunction blendOffset() {
        return BlendOffset.INSTANCE;
    }

    public static DensityFunction lerp(DensityFunction densityFunction, DensityFunction densityFunction2, DensityFunction densityFunction3) {
        DensityFunction densityFunction4 = DensityFunctions.cacheOnce(densityFunction);
        DensityFunction densityFunction5 = DensityFunctions.add(DensityFunctions.mul(densityFunction4, DensityFunctions.constant(-1.0)), DensityFunctions.constant(1.0));
        return DensityFunctions.add(DensityFunctions.mul(densityFunction2, densityFunction5), DensityFunctions.mul(densityFunction3, densityFunction4));
    }

    protected record Marker(Type type, DensityFunction function) implements DensityFunction
    {
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
            return (DensityFunction)visitor.apply(new Marker(this.type, this.function.mapAll(visitor)));
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

    record Noise(NormalNoise noise, @Deprecated double xzScale, double yScale) implements DensityFunction.SimpleFunction
    {
        public Noise(NormalNoise normalNoise, double d) {
            this(normalNoise, 1.0, d);
        }

        public Noise(NormalNoise normalNoise) {
            this(normalNoise, 1.0, 1.0);
        }

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.noise.getValue((double)functionContext.blockX() * this.xzScale, (double)functionContext.blockY() * this.yScale, (double)functionContext.blockZ() * this.xzScale);
        }

        @Override
        public double minValue() {
            return -this.maxValue();
        }

        @Override
        public double maxValue() {
            return this.noise.maxValue();
        }

        @Deprecated
        public double xzScale() {
            return this.xzScale;
        }
    }

    record ShiftedNoise(DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale, NormalNoise noise) implements DensityFunction
    {
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
            return (DensityFunction)visitor.apply(new ShiftedNoise(this.shiftX.mapAll(visitor), this.shiftY.mapAll(visitor), this.shiftZ.mapAll(visitor), this.xzScale, this.yScale, this.noise));
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

    record RangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange) implements DensityFunction
    {
        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            double d = this.input.compute(functionContext);
            if (d >= this.minInclusive && d < this.maxExclusive) {
                return this.whenInRange.compute(functionContext);
            }
            return this.whenOutOfRange.compute(functionContext);
        }

        @Override
        public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
            this.input.fillArray(ds, contextProvider);
            for (int i = 0; i < ds.length; ++i) {
                double d = ds[i];
                ds[i] = d >= this.minInclusive && d < this.maxExclusive ? this.whenInRange.compute(contextProvider.forIndex(i)) : this.whenOutOfRange.compute(contextProvider.forIndex(i));
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return (DensityFunction)visitor.apply(new RangeChoice(this.input.mapAll(visitor), this.minInclusive, this.maxExclusive, this.whenInRange.mapAll(visitor), this.whenOutOfRange.mapAll(visitor)));
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

    record ShiftA(NormalNoise offsetNoise) implements ShiftNoise
    {
        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.compute(functionContext.blockX(), 0.0, functionContext.blockZ());
        }
    }

    record ShiftB(NormalNoise offsetNoise) implements ShiftNoise
    {
        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.compute(functionContext.blockZ(), functionContext.blockX(), 0.0);
        }
    }

    record Shift0(NormalNoise offsetNoise) implements ShiftNoise
    {
        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.compute(functionContext.blockX(), functionContext.blockY(), functionContext.blockZ());
        }
    }

    record Shift1(NormalNoise offsetNoise) implements ShiftNoise
    {
        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.compute(functionContext.blockY(), functionContext.blockZ(), functionContext.blockX());
        }
    }

    record Shift2(NormalNoise offsetNoise) implements ShiftNoise
    {
        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return this.compute(functionContext.blockZ(), functionContext.blockX(), functionContext.blockY());
        }
    }

    record BlendDensity(DensityFunction input) implements TransformerWithContext
    {
        @Override
        public double transform(DensityFunction.FunctionContext functionContext, double d) {
            return functionContext.getBlender().blendDensity(functionContext, d);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return (DensityFunction)visitor.apply(new BlendDensity(this.input.mapAll(visitor)));
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

    static final class EndIslandDensityFunction
    implements DensityFunction.SimpleFunction {
        final SimplexNoise islandNoise;

        public EndIslandDensityFunction(long l) {
            LegacyRandomSource randomSource = new LegacyRandomSource(l);
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

    record WeirdScaledSampler(DensityFunction input, NormalNoise noise, Double2DoubleFunction rarityValueMapper, double maxRarity) implements TransformerWithContext
    {
        @Override
        public double transform(DensityFunction.FunctionContext functionContext, double d) {
            double e = this.rarityValueMapper.get(d);
            return e * Math.abs(this.noise.getValue((double)functionContext.blockX() / e, (double)functionContext.blockY() / e, (double)functionContext.blockZ() / e));
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            this.input.mapAll(visitor);
            return (DensityFunction)visitor.apply(new WeirdScaledSampler(this.input.mapAll(visitor), this.noise, this.rarityValueMapper, this.maxRarity));
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

    record Slide(NoiseSettings settings, DensityFunction input) implements TransformerWithContext
    {
        @Override
        public double transform(DensityFunction.FunctionContext functionContext, double d) {
            return NoiseRouterData.applySlide(this.settings, d, functionContext.blockY());
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return (DensityFunction)visitor.apply(new Slide(this.settings, this.input.mapAll(visitor)));
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

    record Ap2(Type type, DensityFunction f1, DensityFunction f2, double minValue, double maxValue) implements DensityFunction
    {
        private static final Logger LOGGER = LogUtils.getLogger();

        public static DensityFunction create(Type type, DensityFunction densityFunction, DensityFunction densityFunction2) {
            double i;
            double d = densityFunction.minValue();
            double e = densityFunction2.minValue();
            double f = densityFunction.maxValue();
            double g = densityFunction2.maxValue();
            if (type == Type.MIN || type == Type.MAX) {
                boolean bl2;
                boolean bl = d >= g;
                boolean bl3 = bl2 = e >= f;
                if (bl || bl2) {
                    LOGGER.warn("Creating a " + type + " function between two non-overlapping inputs: " + densityFunction + " and " + densityFunction2);
                    if (type == Type.MIN) {
                        return bl2 ? densityFunction : densityFunction2;
                    }
                    return bl2 ? densityFunction2 : densityFunction;
                }
            }
            double h = switch (type) {
                default -> throw new IncompatibleClassChangeError();
                case Type.ADD -> d + e;
                case Type.MAX -> Math.max(d, e);
                case Type.MIN -> Math.min(d, e);
                case Type.MUL -> d > 0.0 && e > 0.0 ? d * e : (f < 0.0 && g < 0.0 ? f * g : Math.min(d * g, f * e));
            };
            switch (type) {
                default: {
                    throw new IncompatibleClassChangeError();
                }
                case ADD: {
                    double d2 = f + g;
                    break;
                }
                case MAX: {
                    double d2 = Math.max(f, g);
                    break;
                }
                case MIN: {
                    double d2 = Math.min(f, g);
                    break;
                }
                case MUL: {
                    double d2 = d > 0.0 && e > 0.0 ? f * g : (i = f < 0.0 && g < 0.0 ? d * e : Math.max(d * e, f * g));
                }
            }
            if (type == Type.MUL || type == Type.ADD) {
                if (densityFunction instanceof Constant) {
                    Constant constant = (Constant)densityFunction;
                    return new MulOrAdd(type == Type.ADD ? MulOrAdd.Type.ADD : MulOrAdd.Type.MUL, densityFunction2, h, i, constant.value);
                }
                if (densityFunction2 instanceof Constant) {
                    Constant constant = (Constant)densityFunction2;
                    return new MulOrAdd(type == Type.ADD ? MulOrAdd.Type.ADD : MulOrAdd.Type.MUL, densityFunction, h, i, constant.value);
                }
            }
            return new Ap2(type, densityFunction, densityFunction2, h, i);
        }

        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            double d = this.f1.compute(functionContext);
            return switch (this.type) {
                default -> throw new IncompatibleClassChangeError();
                case Type.ADD -> d + this.f2.compute(functionContext);
                case Type.MUL -> {
                    if (d == 0.0) {
                        yield 0.0;
                    }
                    yield d * this.f2.compute(functionContext);
                }
                case Type.MIN -> {
                    if (d < this.f2.minValue()) {
                        yield d;
                    }
                    yield Math.min(d, this.f2.compute(functionContext));
                }
                case Type.MAX -> d > this.f2.maxValue() ? d : Math.max(d, this.f2.compute(functionContext));
            };
        }

        @Override
        public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
            this.f1.fillArray(ds, contextProvider);
            switch (this.type) {
                case ADD: {
                    double[] es = new double[ds.length];
                    this.f2.fillArray(es, contextProvider);
                    for (int i = 0; i < ds.length; ++i) {
                        ds[i] = ds[i] + es[i];
                    }
                    break;
                }
                case MUL: {
                    for (int j = 0; j < ds.length; ++j) {
                        double d = ds[j];
                        ds[j] = d == 0.0 ? 0.0 : d * this.f2.compute(contextProvider.forIndex(j));
                    }
                    break;
                }
                case MIN: {
                    double e = this.f2.minValue();
                    for (int k = 0; k < ds.length; ++k) {
                        double f = ds[k];
                        ds[k] = f < e ? f : Math.min(f, this.f2.compute(contextProvider.forIndex(k)));
                    }
                    break;
                }
                case MAX: {
                    double e = this.f2.maxValue();
                    for (int k = 0; k < ds.length; ++k) {
                        double f = ds[k];
                        ds[k] = f > e ? f : Math.max(f, this.f2.compute(contextProvider.forIndex(k)));
                    }
                    break;
                }
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return (DensityFunction)visitor.apply(Ap2.create(this.type, this.f1.mapAll(visitor), this.f2.mapAll(visitor)));
        }

        static enum Type {
            ADD,
            MUL,
            MIN,
            MAX;

        }
    }

    record TerrainShaperSpline(DensityFunction continentalness, DensityFunction erosion, DensityFunction weirdness, ToFloatFunction<TerrainShaper.Point> spline, double minValue, double maxValue) implements DensityFunction
    {
        @Override
        public double compute(DensityFunction.FunctionContext functionContext) {
            return Mth.clamp((double)this.spline.apply(TerrainShaper.makePoint((float)this.continentalness.compute(functionContext), (float)this.erosion.compute(functionContext), (float)this.weirdness.compute(functionContext))), this.minValue, this.maxValue);
        }

        @Override
        public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
            for (int i = 0; i < ds.length; ++i) {
                ds[i] = this.compute(contextProvider.forIndex(i));
            }
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return (DensityFunction)visitor.apply(new TerrainShaperSpline(this.continentalness.mapAll(visitor), this.erosion.mapAll(visitor), this.weirdness.mapAll(visitor), this.spline, this.minValue, this.maxValue));
        }
    }

    record Constant(double value) implements DensityFunction.SimpleFunction
    {
        static final Constant ZERO = new Constant(0.0);

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

    record YClampedGradient(int fromY, int toY, double fromValue, double toValue) implements DensityFunction.SimpleFunction
    {
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

    protected record Mapped(Type type, DensityFunction input, double minValue, double maxValue) implements PureTransformer
    {
        private static double transform(Type type, double d) {
            return switch (type) {
                default -> throw new IncompatibleClassChangeError();
                case Type.ABS -> Math.abs(d);
                case Type.SQUARE -> d * d;
                case Type.CUBE -> d * d * d;
                case Type.HALF_NEGATIVE -> {
                    if (d > 0.0) {
                        yield d;
                    }
                    yield d * 0.5;
                }
                case Type.QUARTER_NEGATIVE -> {
                    if (d > 0.0) {
                        yield d;
                    }
                    yield d * 0.25;
                }
                case Type.SQUEEZE -> {
                    double e = Mth.clamp(d, -1.0, 1.0);
                    yield e / 2.0 - e * e * e / 24.0;
                }
            };
        }

        @Override
        public double transform(double d) {
            return Mapped.transform(this.type, d);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            DensityFunction densityFunction = this.input.mapAll(visitor);
            double d = densityFunction.minValue();
            double e = Mapped.transform(this.type, d);
            double f = Mapped.transform(this.type, densityFunction.maxValue());
            if (this.type == Type.ABS || this.type == Type.SQUARE) {
                return new Mapped(this.type, densityFunction, Math.max(0.0, d), Math.max(e, f));
            }
            return new Mapped(this.type, densityFunction, e, f);
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

    protected static enum BlendAlpha implements DensityFunction.SimpleFunction
    {
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

    protected static enum BlendOffset implements DensityFunction.SimpleFunction
    {
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

    record MulOrAdd(Type type, DensityFunction input, double minValue, double maxValue, double argument) implements PureTransformer
    {
        @Override
        public double transform(double d) {
            return switch (this.type) {
                default -> throw new IncompatibleClassChangeError();
                case Type.MUL -> d * this.argument;
                case Type.ADD -> d + this.argument;
            };
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            double g;
            double f;
            DensityFunction densityFunction = this.input.mapAll(visitor);
            double d = densityFunction.minValue();
            double e = densityFunction.maxValue();
            if (this.type == Type.ADD) {
                f = d + this.argument;
                g = e + this.argument;
            } else if (this.argument >= 0.0) {
                f = d * this.argument;
                g = e * this.argument;
            } else {
                f = e * this.argument;
                g = d * this.argument;
            }
            return new MulOrAdd(this.type, densityFunction, f, g, this.argument);
        }

        static enum Type {
            MUL,
            ADD;

        }
    }

    protected record Clamp(DensityFunction input, double minValue, double maxValue) implements PureTransformer
    {
        @Override
        public double transform(double d) {
            return Mth.clamp(d, this.minValue, this.maxValue);
        }

        @Override
        public DensityFunction mapAll(DensityFunction.Visitor visitor) {
            return new Clamp(this.input.mapAll(visitor), this.minValue, this.maxValue);
        }
    }

    static interface ShiftNoise
    extends DensityFunction.SimpleFunction {
        public NormalNoise offsetNoise();

        @Override
        default public double minValue() {
            return -this.maxValue();
        }

        @Override
        default public double maxValue() {
            return this.offsetNoise().maxValue() * 4.0;
        }

        default public double compute(double d, double e, double f) {
            return this.offsetNoise().getValue(d * 0.25, e * 0.25, f * 0.25) * 4.0;
        }
    }

    static interface PureTransformer
    extends DensityFunction {
        public DensityFunction input();

        @Override
        default public double compute(DensityFunction.FunctionContext functionContext) {
            return this.transform(this.input().compute(functionContext));
        }

        @Override
        default public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
            this.input().fillArray(ds, contextProvider);
            for (int i = 0; i < ds.length; ++i) {
                ds[i] = this.transform(ds[i]);
            }
        }

        public double transform(double var1);
    }

    static interface TransformerWithContext
    extends DensityFunction {
        public DensityFunction input();

        @Override
        default public double compute(DensityFunction.FunctionContext functionContext) {
            return this.transform(functionContext, this.input().compute(functionContext));
        }

        @Override
        default public void fillArray(double[] ds, DensityFunction.ContextProvider contextProvider) {
            this.input().fillArray(ds, contextProvider);
            for (int i = 0; i < ds.length; ++i) {
                ds[i] = this.transform(contextProvider.forIndex(i), ds[i]);
            }
        }

        public double transform(DensityFunction.FunctionContext var1, double var2);
    }
}

