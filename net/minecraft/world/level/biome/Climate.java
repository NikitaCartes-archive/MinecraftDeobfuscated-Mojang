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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.jetbrains.annotations.Nullable;

public class Climate {
    private static final boolean DEBUG_SLOW_BIOME_SEARCH = false;
    private static final float QUANTIZATION_FACTOR = 10000.0f;
    @VisibleForTesting
    protected static final int PARAMETER_COUNT = 7;

    public static TargetPoint target(float f, float g, float h, float i, float j, float k) {
        return new TargetPoint(Climate.quantizeCoord(f), Climate.quantizeCoord(g), Climate.quantizeCoord(h), Climate.quantizeCoord(i), Climate.quantizeCoord(j), Climate.quantizeCoord(k));
    }

    public static ParameterPoint parameters(float f, float g, float h, float i, float j, float k, float l) {
        return new ParameterPoint(Parameter.point(f), Parameter.point(g), Parameter.point(h), Parameter.point(i), Parameter.point(j), Parameter.point(k), Climate.quantizeCoord(l));
    }

    public static ParameterPoint parameters(Parameter parameter, Parameter parameter2, Parameter parameter3, Parameter parameter4, Parameter parameter5, Parameter parameter6, float f) {
        return new ParameterPoint(parameter, parameter2, parameter3, parameter4, parameter5, parameter6, Climate.quantizeCoord(f));
    }

    public static long quantizeCoord(float f) {
        return (long)(f * 10000.0f);
    }

    public static float unquantizeCoord(long l) {
        return (float)l / 10000.0f;
    }

    public static Sampler empty() {
        DensityFunction densityFunction = DensityFunctions.zero();
        return new Sampler(densityFunction, densityFunction, densityFunction, densityFunction, densityFunction, densityFunction, List.of());
    }

    public static BlockPos findSpawnPosition(List<ParameterPoint> list, Sampler sampler) {
        return new SpawnFinder(list, (Sampler)sampler).result.location();
    }

    public record TargetPoint(long temperature, long humidity, long continentalness, long erosion, long depth, long weirdness) {
        @VisibleForTesting
        protected long[] toParameterArray() {
            return new long[]{this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, 0L};
        }
    }

    public record ParameterPoint(Parameter temperature, Parameter humidity, Parameter continentalness, Parameter erosion, Parameter depth, Parameter weirdness, long offset) {
        public static final Codec<ParameterPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Parameter.CODEC.fieldOf("temperature")).forGetter(parameterPoint -> parameterPoint.temperature), ((MapCodec)Parameter.CODEC.fieldOf("humidity")).forGetter(parameterPoint -> parameterPoint.humidity), ((MapCodec)Parameter.CODEC.fieldOf("continentalness")).forGetter(parameterPoint -> parameterPoint.continentalness), ((MapCodec)Parameter.CODEC.fieldOf("erosion")).forGetter(parameterPoint -> parameterPoint.erosion), ((MapCodec)Parameter.CODEC.fieldOf("depth")).forGetter(parameterPoint -> parameterPoint.depth), ((MapCodec)Parameter.CODEC.fieldOf("weirdness")).forGetter(parameterPoint -> parameterPoint.weirdness), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("offset")).xmap(Climate::quantizeCoord, Climate::unquantizeCoord).forGetter(parameterPoint -> parameterPoint.offset)).apply((Applicative<ParameterPoint, ?>)instance, ParameterPoint::new));

        long fitness(TargetPoint targetPoint) {
            return Mth.square(this.temperature.distance(targetPoint.temperature)) + Mth.square(this.humidity.distance(targetPoint.humidity)) + Mth.square(this.continentalness.distance(targetPoint.continentalness)) + Mth.square(this.erosion.distance(targetPoint.erosion)) + Mth.square(this.depth.distance(targetPoint.depth)) + Mth.square(this.weirdness.distance(targetPoint.weirdness)) + Mth.square(this.offset);
        }

        protected List<Parameter> parameterSpace() {
            return ImmutableList.of(this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, new Parameter(this.offset, this.offset));
        }
    }

    public record Parameter(long min, long max) {
        public static final Codec<Parameter> CODEC = ExtraCodecs.intervalCodec(Codec.floatRange(-2.0f, 2.0f), "min", "max", (float_, float2) -> {
            if (float_.compareTo((Float)float2) > 0) {
                return DataResult.error("Cannon construct interval, min > max (" + float_ + " > " + float2 + ")");
            }
            return DataResult.success(new Parameter(Climate.quantizeCoord(float_.floatValue()), Climate.quantizeCoord(float2.floatValue())));
        }, parameter -> Float.valueOf(Climate.unquantizeCoord(parameter.min())), parameter -> Float.valueOf(Climate.unquantizeCoord(parameter.max())));

        public static Parameter point(float f) {
            return Parameter.span(f, f);
        }

        public static Parameter span(float f, float g) {
            if (f > g) {
                throw new IllegalArgumentException("min > max: " + f + " " + g);
            }
            return new Parameter(Climate.quantizeCoord(f), Climate.quantizeCoord(g));
        }

        public static Parameter span(Parameter parameter, Parameter parameter2) {
            if (parameter.min() > parameter2.max()) {
                throw new IllegalArgumentException("min > max: " + parameter + " " + parameter2);
            }
            return new Parameter(parameter.min(), parameter2.max());
        }

        @Override
        public String toString() {
            return this.min == this.max ? String.format("%d", this.min) : String.format("[%d-%d]", this.min, this.max);
        }

        public long distance(long l) {
            long m = l - this.max;
            long n = this.min - l;
            if (m > 0L) {
                return m;
            }
            return Math.max(n, 0L);
        }

        public long distance(Parameter parameter) {
            long l = parameter.min() - this.max;
            long m = this.min - parameter.max();
            if (l > 0L) {
                return l;
            }
            return Math.max(m, 0L);
        }

        public Parameter span(@Nullable Parameter parameter) {
            return parameter == null ? this : new Parameter(Math.min(this.min, parameter.min()), Math.max(this.max, parameter.max()));
        }
    }

    public record Sampler(DensityFunction temperature, DensityFunction humidity, DensityFunction continentalness, DensityFunction erosion, DensityFunction depth, DensityFunction weirdness, List<ParameterPoint> spawnTarget) {
        public TargetPoint sample(int i, int j, int k) {
            int l = QuartPos.toBlock(i);
            int m = QuartPos.toBlock(j);
            int n = QuartPos.toBlock(k);
            DensityFunction.SinglePointContext singlePointContext = new DensityFunction.SinglePointContext(l, m, n);
            return Climate.target((float)this.temperature.compute(singlePointContext), (float)this.humidity.compute(singlePointContext), (float)this.continentalness.compute(singlePointContext), (float)this.erosion.compute(singlePointContext), (float)this.depth.compute(singlePointContext), (float)this.weirdness.compute(singlePointContext));
        }

        public BlockPos findSpawnPosition() {
            if (this.spawnTarget.isEmpty()) {
                return BlockPos.ZERO;
            }
            return Climate.findSpawnPosition(this.spawnTarget, this);
        }
    }

    static class SpawnFinder {
        Result result;

        SpawnFinder(List<ParameterPoint> list, Sampler sampler) {
            this.result = SpawnFinder.getSpawnPositionAndFitness(list, sampler, 0, 0);
            this.radialSearch(list, sampler, 2048.0f, 512.0f);
            this.radialSearch(list, sampler, 512.0f, 32.0f);
        }

        private void radialSearch(List<ParameterPoint> list, Sampler sampler, float f, float g) {
            float h = 0.0f;
            float i = g;
            BlockPos blockPos = this.result.location();
            while (i <= f) {
                int k;
                int j = blockPos.getX() + (int)(Math.sin(h) * (double)i);
                Result result = SpawnFinder.getSpawnPositionAndFitness(list, sampler, j, k = blockPos.getZ() + (int)(Math.cos(h) * (double)i));
                if (result.fitness() < this.result.fitness()) {
                    this.result = result;
                }
                if (!((double)(h += g / i) > Math.PI * 2)) continue;
                h = 0.0f;
                i += g;
            }
        }

        private static Result getSpawnPositionAndFitness(List<ParameterPoint> list, Sampler sampler, int i, int j) {
            double d = Mth.square(2500.0);
            int k = 2;
            long l = (long)((double)Mth.square(10000.0f) * Math.pow((double)(Mth.square((long)i) + Mth.square((long)j)) / d, 2.0));
            TargetPoint targetPoint = sampler.sample(QuartPos.fromBlock(i), 0, QuartPos.fromBlock(j));
            TargetPoint targetPoint2 = new TargetPoint(targetPoint.temperature(), targetPoint.humidity(), targetPoint.continentalness(), targetPoint.erosion(), 0L, targetPoint.weirdness());
            long m = Long.MAX_VALUE;
            for (ParameterPoint parameterPoint : list) {
                m = Math.min(m, parameterPoint.fitness(targetPoint2));
            }
            return new Result(new BlockPos(i, 0, j), l + m);
        }

        record Result(BlockPos location, long fitness) {
        }
    }

    public static class ParameterList<T> {
        private final List<Pair<ParameterPoint, T>> values;
        private final RTree<T> index;

        public ParameterList(List<Pair<ParameterPoint, T>> list) {
            this.values = list;
            this.index = RTree.create(list);
        }

        public List<Pair<ParameterPoint, T>> values() {
            return this.values;
        }

        public T findValue(TargetPoint targetPoint) {
            return this.findValueIndex(targetPoint);
        }

        @VisibleForTesting
        public T findValueBruteForce(TargetPoint targetPoint) {
            Iterator<Pair<ParameterPoint, T>> iterator = this.values().iterator();
            Pair<ParameterPoint, T> pair = iterator.next();
            long l = pair.getFirst().fitness(targetPoint);
            T object = pair.getSecond();
            while (iterator.hasNext()) {
                Pair<ParameterPoint, T> pair2 = iterator.next();
                long m = pair2.getFirst().fitness(targetPoint);
                if (m >= l) continue;
                l = m;
                object = pair2.getSecond();
            }
            return object;
        }

        public T findValueIndex(TargetPoint targetPoint) {
            return this.findValueIndex(targetPoint, RTree.Node::distance);
        }

        protected T findValueIndex(TargetPoint targetPoint, DistanceMetric<T> distanceMetric) {
            return this.index.search(targetPoint, distanceMetric);
        }
    }

    protected static final class RTree<T> {
        private static final int CHILDREN_PER_NODE = 10;
        private final Node<T> root;
        private final ThreadLocal<Leaf<T>> lastResult = new ThreadLocal();

        private RTree(Node<T> node) {
            this.root = node;
        }

        public static <T> RTree<T> create(List<Pair<ParameterPoint, T>> list) {
            if (list.isEmpty()) {
                throw new IllegalArgumentException("Need at least one value to build the search tree.");
            }
            int i = list.get(0).getFirst().parameterSpace().size();
            if (i != 7) {
                throw new IllegalStateException("Expecting parameter space to be 7, got " + i);
            }
            List list2 = list.stream().map(pair -> new Leaf((ParameterPoint)pair.getFirst(), pair.getSecond())).collect(Collectors.toCollection(ArrayList::new));
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
                list.sort(Comparator.comparingLong(node -> {
                    long l = 0L;
                    for (int j = 0; j < i; ++j) {
                        Parameter parameter = node.parameterSpace[j];
                        l += Math.abs((parameter.min() + parameter.max()) / 2L);
                    }
                    return l;
                }));
                return new SubTree(list);
            }
            long l = Long.MAX_VALUE;
            int j = -1;
            List<SubTree<T>> list2 = null;
            for (int k = 0; k < i; ++k) {
                RTree.sort(list, i, k, false);
                List<SubTree<T>> list3 = RTree.bucketize(list);
                long m = 0L;
                for (SubTree<T> subTree2 : list3) {
                    m += RTree.cost(subTree2.parameterSpace);
                }
                if (l <= m) continue;
                l = m;
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
            return Comparator.comparingLong(node -> {
                Parameter parameter = node.parameterSpace[i];
                long l = (parameter.min() + parameter.max()) / 2L;
                return bl ? Math.abs(l) : l;
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

        private static long cost(Parameter[] parameters) {
            long l = 0L;
            for (Parameter parameter : parameters) {
                l += Math.abs(parameter.max() - parameter.min());
            }
            return l;
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
            long[] ls = targetPoint.toParameterArray();
            Leaf<T> leaf = this.root.search(ls, this.lastResult.get(), distanceMetric);
            this.lastResult.set(leaf);
            return leaf.value;
        }

        static abstract class Node<T> {
            protected final Parameter[] parameterSpace;

            protected Node(List<Parameter> list) {
                this.parameterSpace = list.toArray(new Parameter[0]);
            }

            protected abstract Leaf<T> search(long[] var1, @Nullable Leaf<T> var2, DistanceMetric<T> var3);

            protected long distance(long[] ls) {
                long l = 0L;
                for (int i = 0; i < 7; ++i) {
                    l += Mth.square(this.parameterSpace[i].distance(ls[i]));
                }
                return l;
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
            protected Leaf<T> search(long[] ls, @Nullable Leaf<T> leaf, DistanceMetric<T> distanceMetric) {
                long l = leaf == null ? Long.MAX_VALUE : distanceMetric.distance(leaf, ls);
                Leaf<T> leaf2 = leaf;
                for (Node<T> node : this.children) {
                    long n;
                    long m = distanceMetric.distance(node, ls);
                    if (l <= m) continue;
                    Leaf<T> leaf3 = node.search(ls, leaf2, distanceMetric);
                    long l2 = n = node == leaf3 ? m : distanceMetric.distance(leaf3, ls);
                    if (l <= n) continue;
                    l = n;
                    leaf2 = leaf3;
                }
                return leaf2;
            }
        }

        static final class Leaf<T>
        extends Node<T> {
            final T value;

            Leaf(ParameterPoint parameterPoint, T object) {
                super(parameterPoint.parameterSpace());
                this.value = object;
            }

            @Override
            protected Leaf<T> search(long[] ls, @Nullable Leaf<T> leaf, DistanceMetric<T> distanceMetric) {
                return this;
            }
        }
    }

    static interface DistanceMetric<T> {
        public long distance(RTree.Node<T> var1, long[] var2);
    }
}

