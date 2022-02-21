/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.VisibleForDebug;
import org.apache.commons.lang3.mutable.MutableObject;

public interface CubicSpline<C>
extends ToFloatFunction<C> {
    @VisibleForDebug
    public String parityString();

    public float min();

    public float max();

    public CubicSpline<C> mapAll(CoordinateVisitor<C> var1);

    public static <C> Codec<CubicSpline<C>> codec(Codec<ToFloatFunction<C>> codec) {
        record Point<C>(float location, CubicSpline<C> value, float derivative) {
        }
        MutableObject<Codec<CubicSpline>> mutableObject = new MutableObject<Codec<CubicSpline>>();
        Codec codec2 = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("location")).forGetter(Point::location), ((MapCodec)ExtraCodecs.lazyInitializedCodec(mutableObject::getValue).fieldOf("value")).forGetter(Point::value), ((MapCodec)Codec.FLOAT.fieldOf("derivative")).forGetter(Point::derivative)).apply((Applicative<Point, ?>)instance, (f, cubicSpline, g) -> new Point((float)f, cubicSpline, (float)g)));
        Codec codec3 = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)codec.fieldOf("coordinate")).forGetter(Multipoint::coordinate), ((MapCodec)ExtraCodecs.nonEmptyList(codec2.listOf()).fieldOf("points")).forGetter(multipoint -> IntStream.range(0, multipoint.locations.length).mapToObj(i -> new Point(multipoint.locations()[i], multipoint.values().get(i), multipoint.derivatives()[i])).toList())).apply((Applicative<Multipoint, ?>)instance, (toFloatFunction, list) -> {
            float[] fs = new float[list.size()];
            ImmutableList.Builder builder = ImmutableList.builder();
            float[] gs = new float[list.size()];
            for (int i = 0; i < list.size(); ++i) {
                Point lv = (Point)list.get(i);
                fs[i] = lv.location();
                builder.add(lv.value());
                gs[i] = lv.derivative();
            }
            return new Multipoint(toFloatFunction, fs, builder.build(), gs);
        }));
        mutableObject.setValue(Codec.either(Codec.FLOAT, codec3).xmap(either -> (CubicSpline)((Object)either.map(Constant::new, multipoint -> multipoint)), cubicSpline -> {
            Either<Object, Multipoint<Object>> either;
            if (cubicSpline instanceof Constant) {
                Constant constant = (Constant)cubicSpline;
                either = Either.left(Float.valueOf(constant.value()));
            } else {
                either = Either.right((Multipoint)cubicSpline);
            }
            return either;
        }));
        return (Codec)mutableObject.getValue();
    }

    public static <C> CubicSpline<C> constant(float f) {
        return new Constant(f);
    }

    public static <C> Builder<C> builder(ToFloatFunction<C> toFloatFunction) {
        return new Builder<C>(toFloatFunction);
    }

    public static <C> Builder<C> builder(ToFloatFunction<C> toFloatFunction, ToFloatFunction<Float> toFloatFunction2) {
        return new Builder<C>(toFloatFunction, toFloatFunction2);
    }

    @VisibleForDebug
    public record Constant<C>(float value) implements CubicSpline<C>
    {
        @Override
        public float apply(C object) {
            return this.value;
        }

        @Override
        public String parityString() {
            return String.format("k=%.3f", Float.valueOf(this.value));
        }

        @Override
        public float min() {
            return this.value;
        }

        @Override
        public float max() {
            return this.value;
        }

        @Override
        public CubicSpline<C> mapAll(CoordinateVisitor<C> coordinateVisitor) {
            return this;
        }
    }

    public static final class Builder<C> {
        private final ToFloatFunction<C> coordinate;
        private final ToFloatFunction<Float> valueTransformer;
        private final FloatList locations = new FloatArrayList();
        private final List<CubicSpline<C>> values = Lists.newArrayList();
        private final FloatList derivatives = new FloatArrayList();

        protected Builder(ToFloatFunction<C> toFloatFunction) {
            this(toFloatFunction, float_ -> float_.floatValue());
        }

        protected Builder(ToFloatFunction<C> toFloatFunction, ToFloatFunction<Float> toFloatFunction2) {
            this.coordinate = toFloatFunction;
            this.valueTransformer = toFloatFunction2;
        }

        public Builder<C> addPoint(float f, float g, float h) {
            return this.addPoint(f, new Constant(this.valueTransformer.apply(Float.valueOf(g))), h);
        }

        public Builder<C> addPoint(float f, CubicSpline<C> cubicSpline, float g) {
            if (!this.locations.isEmpty() && f <= this.locations.getFloat(this.locations.size() - 1)) {
                throw new IllegalArgumentException("Please register points in ascending order");
            }
            this.locations.add(f);
            this.values.add(cubicSpline);
            this.derivatives.add(g);
            return this;
        }

        public CubicSpline<C> build() {
            if (this.locations.isEmpty()) {
                throw new IllegalStateException("No elements added");
            }
            return new Multipoint<C>(this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray());
        }
    }

    @VisibleForDebug
    public record Multipoint<C>(ToFloatFunction<C> coordinate, float[] locations, List<CubicSpline<C>> values, float[] derivatives) implements CubicSpline<C>
    {
        public Multipoint {
            if (fs.length != list.size() || fs.length != gs.length) {
                throw new IllegalArgumentException("All lengths must be equal, got: " + fs.length + " " + list.size() + " " + gs.length);
            }
        }

        @Override
        public float apply(C object) {
            float f = this.coordinate.apply(object);
            int i2 = Mth.binarySearch(0, this.locations.length, i -> f < this.locations[i]) - 1;
            int j = this.locations.length - 1;
            if (i2 < 0) {
                return this.values.get(0).apply(object) + this.derivatives[0] * (f - this.locations[0]);
            }
            if (i2 == j) {
                return this.values.get(j).apply(object) + this.derivatives[j] * (f - this.locations[j]);
            }
            float g = this.locations[i2];
            float h = this.locations[i2 + 1];
            float k = (f - g) / (h - g);
            ToFloatFunction toFloatFunction = this.values.get(i2);
            ToFloatFunction toFloatFunction2 = this.values.get(i2 + 1);
            float l = this.derivatives[i2];
            float m = this.derivatives[i2 + 1];
            float n = toFloatFunction.apply(object);
            float o = toFloatFunction2.apply(object);
            float p = l * (h - g) - (o - n);
            float q = -m * (h - g) + (o - n);
            float r = Mth.lerp(k, n, o) + k * (1.0f - k) * Mth.lerp(k, p, q);
            return r;
        }

        @Override
        @VisibleForTesting
        public String parityString() {
            return "Spline{coordinate=" + this.coordinate + ", locations=" + this.toString(this.locations) + ", derivatives=" + this.toString(this.derivatives) + ", values=" + this.values.stream().map(CubicSpline::parityString).collect(Collectors.joining(", ", "[", "]")) + "}";
        }

        private String toString(float[] fs) {
            return "[" + IntStream.range(0, fs.length).mapToDouble(i -> fs[i]).mapToObj(d -> String.format(Locale.ROOT, "%.3f", d)).collect(Collectors.joining(", ")) + "]";
        }

        @Override
        public float min() {
            return (float)this.values().stream().mapToDouble(CubicSpline::min).min().orElseThrow();
        }

        @Override
        public float max() {
            return (float)this.values().stream().mapToDouble(CubicSpline::max).max().orElseThrow();
        }

        @Override
        public CubicSpline<C> mapAll(CoordinateVisitor<C> coordinateVisitor) {
            return new Multipoint<C>(coordinateVisitor.visit(this.coordinate), this.locations, this.values().stream().map(cubicSpline -> cubicSpline.mapAll(coordinateVisitor)).toList(), this.derivatives);
        }
    }

    public static interface CoordinateVisitor<C> {
        public ToFloatFunction<C> visit(ToFloatFunction<C> var1);
    }
}

