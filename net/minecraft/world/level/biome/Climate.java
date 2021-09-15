/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Climate {
    private static final boolean DEBUG_SLOW_BIOME_SEARCH = false;
    @VisibleForTesting
    protected static final int PARAMETER_COUNT = 7;

    public static TargetPoint target(float f, float g, float h, float i, float j, float k) {
        return new TargetPoint(f, g, h, i, j, k);
    }

    public static ParameterPoint parameters(float f, float g, float h, float i, float j, float k, float l) {
        return new ParameterPoint(Parameter.point(f), Parameter.point(g), Parameter.point(h), Parameter.point(i), Parameter.point(j), Parameter.point(k), l);
    }

    public static ParameterPoint parameters(Parameter parameter, Parameter parameter2, Parameter parameter3, Parameter parameter4, Parameter parameter5, Parameter parameter6, float f) {
        return new ParameterPoint(parameter, parameter2, parameter3, parameter4, parameter5, parameter6, f);
    }

    public static final class TargetPoint {
        final float temperature;
        final float humidity;
        final float continentalness;
        final float erosion;
        final float depth;
        final float weirdness;

        TargetPoint(float f, float g, float h, float i, float j, float k) {
            this.temperature = f;
            this.humidity = g;
            this.continentalness = h;
            this.erosion = i;
            this.depth = j;
            this.weirdness = k;
        }

        public float temperature() {
            return this.temperature;
        }

        public float humidity() {
            return this.humidity;
        }

        public float continentalness() {
            return this.continentalness;
        }

        public float erosion() {
            return this.erosion;
        }

        public float depth() {
            return this.depth;
        }

        public float weirdness() {
            return this.weirdness;
        }

        @VisibleForTesting
        protected float[] toParameterArray() {
            return new float[]{this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, 0.0f};
        }
    }

    public static final class ParameterPoint {
        public static final Codec<ParameterPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Parameter.CODEC.fieldOf("temperature")).forGetter(parameterPoint -> parameterPoint.temperature), ((MapCodec)Parameter.CODEC.fieldOf("humidity")).forGetter(parameterPoint -> parameterPoint.humidity), ((MapCodec)Parameter.CODEC.fieldOf("continentalness")).forGetter(parameterPoint -> parameterPoint.continentalness), ((MapCodec)Parameter.CODEC.fieldOf("erosion")).forGetter(parameterPoint -> parameterPoint.erosion), ((MapCodec)Parameter.CODEC.fieldOf("depth")).forGetter(parameterPoint -> parameterPoint.depth), ((MapCodec)Parameter.CODEC.fieldOf("weirdness")).forGetter(parameterPoint -> parameterPoint.weirdness), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("offset")).forGetter(parameterPoint -> Float.valueOf(parameterPoint.offset))).apply((Applicative<ParameterPoint, ?>)instance, ParameterPoint::new));
        private final Parameter temperature;
        private final Parameter humidity;
        private final Parameter continentalness;
        private final Parameter erosion;
        private final Parameter depth;
        private final Parameter weirdness;
        private final float offset;

        ParameterPoint(Parameter parameter, Parameter parameter2, Parameter parameter3, Parameter parameter4, Parameter parameter5, Parameter parameter6, float f) {
            this.temperature = parameter;
            this.humidity = parameter2;
            this.continentalness = parameter3;
            this.erosion = parameter4;
            this.depth = parameter5;
            this.weirdness = parameter6;
            this.offset = f;
        }

        public String toString() {
            return "[temp: " + this.temperature + ", hum: " + this.humidity + ", cont: " + this.continentalness + ", eros: " + this.erosion + ", depth: " + this.depth + ", weird: " + this.weirdness + ", offset: " + this.offset + "]";
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            ParameterPoint parameterPoint = (ParameterPoint)object;
            if (!this.temperature.equals(parameterPoint.temperature)) {
                return false;
            }
            if (!this.humidity.equals(parameterPoint.humidity)) {
                return false;
            }
            if (!this.continentalness.equals(parameterPoint.continentalness)) {
                return false;
            }
            if (!this.erosion.equals(parameterPoint.erosion)) {
                return false;
            }
            if (!this.depth.equals(parameterPoint.depth)) {
                return false;
            }
            if (!this.weirdness.equals(parameterPoint.weirdness)) {
                return false;
            }
            return Float.compare(parameterPoint.offset, this.offset) == 0;
        }

        public int hashCode() {
            return Objects.hash(this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, Float.valueOf(this.offset));
        }

        float fitness(TargetPoint targetPoint) {
            return Mth.square(this.temperature.distance(targetPoint.temperature)) + Mth.square(this.humidity.distance(targetPoint.humidity)) + Mth.square(this.continentalness.distance(targetPoint.continentalness)) + Mth.square(this.erosion.distance(targetPoint.erosion)) + Mth.square(this.depth.distance(targetPoint.depth)) + Mth.square(this.weirdness.distance(targetPoint.weirdness)) + Mth.square(this.offset);
        }

        public Parameter temperature() {
            return this.temperature;
        }

        public Parameter humidity() {
            return this.humidity;
        }

        public Parameter continentalness() {
            return this.continentalness;
        }

        public Parameter erosion() {
            return this.erosion;
        }

        public Parameter depth() {
            return this.depth;
        }

        public Parameter weirdness() {
            return this.weirdness;
        }

        public float offset() {
            return this.offset;
        }

        protected List<Parameter> parameterSpace() {
            return ImmutableList.of(this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, Parameter.point(this.offset));
        }
    }

    public static final class Parameter {
        public static final Codec<Parameter> CODEC = ExtraCodecs.intervalCodec(Codec.floatRange(-2.0f, 2.0f), "min", "max", (float_, float2) -> {
            if (float_.compareTo((Float)float2) > 0) {
                return DataResult.error("Cannon construct interval, min > max (" + float_ + " > " + float2 + ")");
            }
            return DataResult.success(new Parameter(float_.floatValue(), float2.floatValue()));
        }, Parameter::min, Parameter::max);
        private final float min;
        private final float max;

        private Parameter(float f, float g) {
            this.min = f;
            this.max = g;
        }

        public static Parameter point(float f) {
            return Parameter.span(f, f);
        }

        public static Parameter span(float f, float g) {
            if (f > g) {
                throw new IllegalArgumentException("min > max: " + f + " " + g);
            }
            return new Parameter(f, g);
        }

        public static Parameter span(Parameter parameter, Parameter parameter2) {
            if (parameter.min() > parameter2.max()) {
                throw new IllegalArgumentException("min > max: " + parameter + " " + parameter2);
            }
            return new Parameter(parameter.min(), parameter2.max());
        }

        public float min() {
            return this.min;
        }

        public float max() {
            return this.max;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            Parameter parameter = (Parameter)object;
            return Float.compare(parameter.min, this.min) == 0 && Float.compare(parameter.max, this.max) == 0;
        }

        public int hashCode() {
            return Objects.hash(Float.valueOf(this.min), Float.valueOf(this.max));
        }

        public String toString() {
            return this.min == this.max ? String.format("%.2f", Float.valueOf(this.min)) : String.format("[%.2f-%.2f]", Float.valueOf(this.min), Float.valueOf(this.max));
        }

        public float distance(float f) {
            float g = f - this.max;
            float h = this.min - f;
            if (g > 0.0f) {
                return g;
            }
            return Math.max(h, 0.0f);
        }

        public float distance(Parameter parameter) {
            float f = parameter.min() - this.max;
            float g = this.min - parameter.max();
            if (f > 0.0f) {
                return f;
            }
            return Math.max(g, 0.0f);
        }

        public Parameter span(@Nullable Parameter parameter) {
            return parameter == null ? this : new Parameter(Math.min(this.min, parameter.min()), Math.max(this.max, parameter.max()));
        }
    }

    public static interface Sampler {
        public TargetPoint sample(int var1, int var2, int var3);
    }

    public static class ParameterList<T> {
        private final List<Pair<ParameterPoint, Supplier<T>>> biomes;
        private final RTree<T> index;

        public ParameterList(List<Pair<ParameterPoint, Supplier<T>>> list) {
            this.biomes = list;
            this.index = RTree.create(list);
        }

        public List<Pair<ParameterPoint, Supplier<T>>> biomes() {
            return this.biomes;
        }

        public T findBiome(TargetPoint targetPoint, Supplier<T> supplier) {
            return this.findBiomeIndex(targetPoint);
        }

        @VisibleForTesting
        public T findBiomeBruteForce(TargetPoint targetPoint, Supplier<T> supplier) {
            float f = Float.MAX_VALUE;
            Supplier<T> supplier2 = supplier;
            for (Pair<ParameterPoint, Supplier<T>> pair : this.biomes()) {
                float g = pair.getFirst().fitness(targetPoint);
                if (!(g < f)) continue;
                f = g;
                supplier2 = pair.getSecond();
            }
            return supplier2.get();
        }

        public T findBiomeIndex(TargetPoint targetPoint) {
            return this.findBiomeIndex(targetPoint, RTree.Node::distance);
        }

        protected T findBiomeIndex(TargetPoint targetPoint, DistanceMetric<T> distanceMetric) {
            return this.index.search(targetPoint, distanceMetric);
        }
    }

    static final class RTree<T> {
        private static final int CHILDREN_PER_NODE = 10;
        private final Node<T> root;
        private final ThreadLocal<Leaf<T>> lastResult = new ThreadLocal();

        private RTree(Node<T> node) {
            this.root = node;
        }

        public static <T> RTree<T> create(List<Pair<ParameterPoint, Supplier<T>>> list) {
            if (list.isEmpty()) {
                throw new IllegalArgumentException("Need at least one biome to build the search tree.");
            }
            int i = list.get(0).getFirst().parameterSpace().size();
            if (i != 7) {
                throw new IllegalStateException("Expecting parameter space to be 7, got " + i);
            }
            List list2 = list.stream().map(pair -> new Leaf((ParameterPoint)pair.getFirst(), (Supplier)pair.getSecond())).collect(Collectors.toCollection(ArrayList::new));
            return new RTree<T>(RTree.build(i, list2));
        }

        private static <T> Node<T> build(int i, List<? extends Node<T>> list) {
            if (list.isEmpty()) {
                throw new IllegalStateException("Need at least one child to build a node");
            }
            if (list.size() == 1) {
                return list.get(0);
            }
            if (list.size() <= 10) {
                list.sort(Comparator.comparingDouble(node -> {
                    float f = 0.0f;
                    for (int j = 0; j < i; ++j) {
                        Parameter parameter = node.parameterSpace[j];
                        f += Math.abs((parameter.min() + parameter.max()) / 2.0f);
                    }
                    return f;
                }));
                return new SubTree(list);
            }
            float f = Float.POSITIVE_INFINITY;
            int j = -1;
            List<SubTree<T>> list2 = null;
            for (int k = 0; k < i; ++k) {
                RTree.sort(list, i, k, false);
                List<SubTree<T>> list3 = RTree.bucketize(list);
                float g = 0.0f;
                for (SubTree<T> subTree2 : list3) {
                    g += RTree.cost(subTree2.parameterSpace);
                }
                if (!(f > g)) continue;
                f = g;
                j = k;
                list2 = list3;
            }
            RTree.sort(list2, i, j, true);
            return new SubTree(list2.stream().map(subTree -> RTree.build(i, Arrays.asList(subTree.children))).collect(Collectors.toList()));
        }

        private static <T> void sort(List<? extends Node<T>> list, int i, int j, boolean bl) {
            Comparator<Node<Node<T>>> comparator = RTree.comparator(j, bl);
            for (int k = 1; k < i; ++k) {
                comparator = comparator.thenComparing(RTree.comparator((j + k) % i, bl));
            }
            list.sort(comparator);
        }

        private static <T> Comparator<Node<T>> comparator(int i, boolean bl) {
            return Comparator.comparingDouble(node -> {
                Parameter parameter = node.parameterSpace[i];
                float f = (parameter.min() + parameter.max()) / 2.0f;
                return bl ? (double)Math.abs(f) : (double)f;
            });
        }

        private static <T> List<SubTree<T>> bucketize(List<? extends Node<T>> list) {
            ArrayList<SubTree<T>> list2 = Lists.newArrayList();
            ArrayList<Node<T>> list3 = Lists.newArrayList();
            int i = (int)Math.pow(10.0, Math.floor(Math.log((double)list.size() - 0.01) / Math.log(10.0)));
            for (Node<T> node : list) {
                list3.add(node);
                if (list3.size() < i) continue;
                list2.add(new SubTree(list3));
                list3 = Lists.newArrayList();
            }
            if (!list3.isEmpty()) {
                list2.add(new SubTree(list3));
            }
            return list2;
        }

        private static float cost(Parameter[] parameters) {
            float f = 0.0f;
            for (Parameter parameter : parameters) {
                f += Math.abs(parameter.max() - parameter.min());
            }
            return f;
        }

        static <T> List<Parameter> buildParameterSpace(List<? extends Node<T>> list) {
            if (list.isEmpty()) {
                throw new IllegalArgumentException("SubTree needs at least one child");
            }
            int i = 7;
            ArrayList<Parameter> list2 = Lists.newArrayList();
            for (int j = 0; j < 7; ++j) {
                list2.add(null);
            }
            for (Node<T> node : list) {
                for (int k = 0; k < 7; ++k) {
                    list2.set(k, node.parameterSpace[k].span((Parameter)list2.get(k)));
                }
            }
            return list2;
        }

        public T search(TargetPoint targetPoint, DistanceMetric<T> distanceMetric) {
            float[] fs = targetPoint.toParameterArray();
            Leaf<T> leaf = this.root.search(fs, this.lastResult.get(), distanceMetric);
            this.lastResult.set(leaf);
            return leaf.biome.get();
        }

        static abstract class Node<T> {
            protected final Parameter[] parameterSpace;

            protected Node(List<Parameter> list) {
                this.parameterSpace = list.toArray(new Parameter[0]);
            }

            protected abstract Leaf<T> search(float[] var1, @Nullable Leaf<T> var2, DistanceMetric<T> var3);

            protected float distance(float[] fs) {
                float f = 0.0f;
                for (int i = 0; i < 7; ++i) {
                    f += Mth.square(this.parameterSpace[i].distance(fs[i]));
                }
                return f;
            }

            public String toString() {
                return Arrays.toString(this.parameterSpace);
            }
        }

        static final class SubTree<T>
        extends Node<T> {
            final Node<T>[] children;

            protected SubTree(List<? extends Node<T>> list) {
                this(RTree.buildParameterSpace(list), list);
            }

            protected SubTree(List<Parameter> list, List<? extends Node<T>> list2) {
                super(list);
                this.children = list2.toArray(new Node[0]);
            }

            @Override
            protected Leaf<T> search(float[] fs, @Nullable Leaf<T> leaf, DistanceMetric<T> distanceMetric) {
                float f = leaf == null ? Float.POSITIVE_INFINITY : distanceMetric.distance(leaf, fs);
                Leaf<T> leaf2 = leaf;
                for (Node node : this.children) {
                    float h;
                    float g = distanceMetric.distance(node, fs);
                    if (!(f > g)) continue;
                    Leaf<T> leaf3 = node.search(fs, null, distanceMetric);
                    float f2 = h = node == leaf3 ? g : distanceMetric.distance(leaf3, fs);
                    if (!(f > h)) continue;
                    f = h;
                    leaf2 = leaf3;
                }
                return leaf2;
            }
        }

        static final class Leaf<T>
        extends Node<T> {
            final Supplier<T> biome;

            Leaf(ParameterPoint parameterPoint, Supplier<T> supplier) {
                super(parameterPoint.parameterSpace());
                this.biome = supplier;
            }

            @Override
            protected Leaf<T> search(float[] fs, @Nullable Leaf<T> leaf, DistanceMetric<T> distanceMetric) {
                return this;
            }
        }
    }

    static interface DistanceMetric<T> {
        public float distance(RTree.Node<T> var1, float[] var2);
    }
}

