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

public interface CubicSpline<C, I extends ToFloatFunction<C>>
extends ToFloatFunction<C> {
    @VisibleForDebug
    public String parityString();

    public CubicSpline<C, I> mapAll(CoordinateVisitor<I> var1);

    public static <C, I extends ToFloatFunction<C>> Codec<CubicSpline<C, I>> codec(Codec<I> codec) {
        record Point<C, I extends ToFloatFunction<C>>(float location, CubicSpline<C, I> value, float derivative) {
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
            return Multipoint.create(toFloatFunction, fs, builder.build(), gs);
        }));
        mutableObject.setValue(Codec.either(Codec.FLOAT, codec3).xmap(either -> (CubicSpline)((Object)either.map(Constant::new, multipoint -> multipoint)), cubicSpline -> {
            Either either;
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

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> constant(float f) {
        return new Constant(f);
    }

    public static <C, I extends ToFloatFunction<C>> Builder<C, I> builder(I toFloatFunction) {
        return new Builder(toFloatFunction);
    }

    public static <C, I extends ToFloatFunction<C>> Builder<C, I> builder(I toFloatFunction, ToFloatFunction<Float> toFloatFunction2) {
        return new Builder(toFloatFunction, toFloatFunction2);
    }

    @VisibleForDebug
    public record Constant<C, I extends ToFloatFunction<C>>(float value) implements CubicSpline<C, I>
    {
        @Override
        public float apply(C object) {
            return this.value;
        }

        @Override
        public String parityString() {
            return String.format(Locale.ROOT, "k=%.3f", Float.valueOf(this.value));
        }

        @Override
        public float minValue() {
            return this.value;
        }

        @Override
        public float maxValue() {
            return this.value;
        }

        @Override
        public CubicSpline<C, I> mapAll(CoordinateVisitor<I> coordinateVisitor) {
            return this;
        }
    }

    public static final class Builder<C, I extends ToFloatFunction<C>> {
        private final I coordinate;
        private final ToFloatFunction<Float> valueTransformer;
        private final FloatList locations = new FloatArrayList();
        private final List<CubicSpline<C, I>> values = Lists.newArrayList();
        private final FloatList derivatives = new FloatArrayList();

        protected Builder(I toFloatFunction) {
            this(toFloatFunction, ToFloatFunction.IDENTITY);
        }

        protected Builder(I toFloatFunction, ToFloatFunction<Float> toFloatFunction2) {
            this.coordinate = toFloatFunction;
            this.valueTransformer = toFloatFunction2;
        }

        public Builder<C, I> addPoint(float f, float g) {
            return this.addPoint(f, new Constant(this.valueTransformer.apply(Float.valueOf(g))), 0.0f);
        }

        public Builder<C, I> addPoint(float f, float g, float h) {
            return this.addPoint(f, new Constant(this.valueTransformer.apply(Float.valueOf(g))), h);
        }

        public Builder<C, I> addPoint(float f, CubicSpline<C, I> cubicSpline) {
            return this.addPoint(f, cubicSpline, 0.0f);
        }

        private Builder<C, I> addPoint(float f, CubicSpline<C, I> cubicSpline, float g) {
            if (!this.locations.isEmpty() && f <= this.locations.getFloat(this.locations.size() - 1)) {
                throw new IllegalArgumentException("Please register points in ascending order");
            }
            this.locations.add(f);
            this.values.add(cubicSpline);
            this.derivatives.add(g);
            return this;
        }

        public CubicSpline<C, I> build() {
            if (this.locations.isEmpty()) {
                throw new IllegalStateException("No elements added");
            }
            return Multipoint.create(this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray());
        }
    }

    @VisibleForDebug
    public record Multipoint<C, I extends ToFloatFunction<C>>(I coordinate, float[] locations, List<CubicSpline<C, I>> values, float[] derivatives, float minValue, float maxValue) implements CubicSpline<C, I>
    {
        public Multipoint {
            Multipoint.validateSizes(fs, list, gs);
        }

        static <C, I extends ToFloatFunction<C>> Multipoint<C, I> create(I toFloatFunction, float[] fs, List<CubicSpline<C, I>> list, float[] gs) {
            float l;
            float k;
            Multipoint.validateSizes(fs, list, gs);
            int i = fs.length - 1;
            float f = Float.POSITIVE_INFINITY;
            float g = Float.NEGATIVE_INFINITY;
            float h = toFloatFunction.minValue();
            float j = toFloatFunction.maxValue();
            if (h < fs[0]) {
                k = Multipoint.linearExtend(h, fs, list.get(0).minValue(), gs, 0);
                l = Multipoint.linearExtend(h, fs, list.get(0).maxValue(), gs, 0);
                f = Math.min(f, Math.min(k, l));
                g = Math.max(g, Math.max(k, l));
            }
            if (j > fs[i]) {
                k = Multipoint.linearExtend(j, fs, list.get(i).minValue(), gs, i);
                l = Multipoint.linearExtend(j, fs, list.get(i).maxValue(), gs, i);
                f = Math.min(f, Math.min(k, l));
                g = Math.max(g, Math.max(k, l));
            }
            for (CubicSpline<C, I> cubicSpline : list) {
                f = Math.min(f, cubicSpline.minValue());
                g = Math.max(g, cubicSpline.maxValue());
            }
            for (int m = 0; m < i; ++m) {
                l = fs[m];
                float n = fs[m + 1];
                float o = n - l;
                CubicSpline<C, I> cubicSpline2 = list.get(m);
                CubicSpline<C, I> cubicSpline3 = list.get(m + 1);
                float p = cubicSpline2.minValue();
                float q = cubicSpline2.maxValue();
                float r = cubicSpline3.minValue();
                float s = cubicSpline3.maxValue();
                float t = gs[m];
                float u = gs[m + 1];
                if (t == 0.0f && u == 0.0f) continue;
                float v = t * o;
                float w = u * o;
                float x = Math.min(p, r);
                float y = Math.max(q, s);
                float z = v - s + p;
                float aa = v - r + q;
                float ab = -w + r - q;
                float ac = -w + s - p;
                float ad = Math.min(z, ab);
                float ae = Math.max(aa, ac);
                f = Math.min(f, x + 0.25f * ad);
                g = Math.max(g, y + 0.25f * ae);
            }
            return new Multipoint<C, I>(toFloatFunction, fs, list, gs, f, g);
        }

        private static float linearExtend(float f, float[] fs, float g, float[] gs, int i) {
            float h = gs[i];
            if (h == 0.0f) {
                return g;
            }
            return g + h * (f - fs[i]);
        }

        private static <C, I extends ToFloatFunction<C>> void validateSizes(float[] fs, List<CubicSpline<C, I>> list, float[] gs) {
            if (fs.length != list.size() || fs.length != gs.length) {
                throw new IllegalArgumentException("All lengths must be equal, got: " + fs.length + " " + list.size() + " " + gs.length);
            }
            if (fs.length == 0) {
                throw new IllegalArgumentException("Cannot create a multipoint spline with no points");
            }
        }

        @Override
        public float apply(C object) {
            float f = this.coordinate.apply(object);
            int i = Multipoint.findIntervalStart(this.locations, f);
            int j = this.locations.length - 1;
            if (i < 0) {
                return Multipoint.linearExtend(f, this.locations, this.values.get(0).apply(object), this.derivatives, 0);
            }
            if (i == j) {
                return Multipoint.linearExtend(f, this.locations, this.values.get(j).apply(object), this.derivatives, j);
            }
            float g = this.locations[i];
            float h = this.locations[i + 1];
            float k = (f - g) / (h - g);
            ToFloatFunction toFloatFunction = this.values.get(i);
            ToFloatFunction toFloatFunction2 = this.values.get(i + 1);
            float l = this.derivatives[i];
            float m = this.derivatives[i + 1];
            float n = toFloatFunction.apply(object);
            float o = toFloatFunction2.apply(object);
            float p = l * (h - g) - (o - n);
            float q = -m * (h - g) + (o - n);
            float r = Mth.lerp(k, n, o) + k * (1.0f - k) * Mth.lerp(k, p, q);
            return r;
        }

        private static int findIntervalStart(float[] fs, float f) {
            return Mth.binarySearch(0, fs.length, i -> f < fs[i]) - 1;
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
        public CubicSpline<C, I> mapAll(CoordinateVisitor<I> coordinateVisitor) {
            return Multipoint.create((ToFloatFunction)coordinateVisitor.visit(this.coordinate), this.locations, this.values().stream().map(cubicSpline -> cubicSpline.mapAll(coordinateVisitor)).toList(), this.derivatives);
        }
    }

    public static interface CoordinateVisitor<I> {
        public I visit(I var1);
    }
}

