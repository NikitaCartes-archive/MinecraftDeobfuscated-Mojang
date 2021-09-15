package net.minecraft.world.level.biome;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public class Climate {
	private static final boolean DEBUG_SLOW_BIOME_SEARCH = false;
	@VisibleForTesting
	protected static final int PARAMETER_COUNT = 7;

	public static Climate.TargetPoint target(float f, float g, float h, float i, float j, float k) {
		return new Climate.TargetPoint(f, g, h, i, j, k);
	}

	public static Climate.ParameterPoint parameters(float f, float g, float h, float i, float j, float k, float l) {
		return new Climate.ParameterPoint(
			Climate.Parameter.point(f),
			Climate.Parameter.point(g),
			Climate.Parameter.point(h),
			Climate.Parameter.point(i),
			Climate.Parameter.point(j),
			Climate.Parameter.point(k),
			l
		);
	}

	public static Climate.ParameterPoint parameters(
		Climate.Parameter parameter,
		Climate.Parameter parameter2,
		Climate.Parameter parameter3,
		Climate.Parameter parameter4,
		Climate.Parameter parameter5,
		Climate.Parameter parameter6,
		float f
	) {
		return new Climate.ParameterPoint(parameter, parameter2, parameter3, parameter4, parameter5, parameter6, f);
	}

	interface DistanceMetric<T> {
		float distance(Climate.RTree.Node<T> node, float[] fs);
	}

	public static final class Parameter {
		public static final Codec<Climate.Parameter> CODEC = ExtraCodecs.intervalCodec(
			Codec.floatRange(-2.0F, 2.0F),
			"min",
			"max",
			(float_, float2) -> float_.compareTo(float2) > 0
					? DataResult.error("Cannon construct interval, min > max (" + float_ + " > " + float2 + ")")
					: DataResult.success(new Climate.Parameter(float_, float2)),
			Climate.Parameter::min,
			Climate.Parameter::max
		);
		private final float min;
		private final float max;

		private Parameter(float f, float g) {
			this.min = f;
			this.max = g;
		}

		public static Climate.Parameter point(float f) {
			return span(f, f);
		}

		public static Climate.Parameter span(float f, float g) {
			if (f > g) {
				throw new IllegalArgumentException("min > max: " + f + " " + g);
			} else {
				return new Climate.Parameter(f, g);
			}
		}

		public static Climate.Parameter span(Climate.Parameter parameter, Climate.Parameter parameter2) {
			if (parameter.min() > parameter2.max()) {
				throw new IllegalArgumentException("min > max: " + parameter + " " + parameter2);
			} else {
				return new Climate.Parameter(parameter.min(), parameter2.max());
			}
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
			} else if (object != null && this.getClass() == object.getClass()) {
				Climate.Parameter parameter = (Climate.Parameter)object;
				return Float.compare(parameter.min, this.min) == 0 && Float.compare(parameter.max, this.max) == 0;
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.min, this.max});
		}

		public String toString() {
			return this.min == this.max ? String.format("%.2f", this.min) : String.format("[%.2f-%.2f]", this.min, this.max);
		}

		public float distance(float f) {
			float g = f - this.max;
			float h = this.min - f;
			return g > 0.0F ? g : Math.max(h, 0.0F);
		}

		public float distance(Climate.Parameter parameter) {
			float f = parameter.min() - this.max;
			float g = this.min - parameter.max();
			return f > 0.0F ? f : Math.max(g, 0.0F);
		}

		public Climate.Parameter span(@Nullable Climate.Parameter parameter) {
			return parameter == null ? this : new Climate.Parameter(Math.min(this.min, parameter.min()), Math.max(this.max, parameter.max()));
		}
	}

	public static class ParameterList<T> {
		private final List<Pair<Climate.ParameterPoint, Supplier<T>>> biomes;
		private final Climate.RTree<T> index;

		public ParameterList(List<Pair<Climate.ParameterPoint, Supplier<T>>> list) {
			this.biomes = list;
			this.index = Climate.RTree.create(list);
		}

		public List<Pair<Climate.ParameterPoint, Supplier<T>>> biomes() {
			return this.biomes;
		}

		public T findBiome(Climate.TargetPoint targetPoint, Supplier<T> supplier) {
			return this.findBiomeIndex(targetPoint);
		}

		@VisibleForTesting
		public T findBiomeBruteForce(Climate.TargetPoint targetPoint, Supplier<T> supplier) {
			float f = Float.MAX_VALUE;
			Supplier<T> supplier2 = supplier;

			for (Pair<Climate.ParameterPoint, Supplier<T>> pair : this.biomes()) {
				float g = pair.getFirst().fitness(targetPoint);
				if (g < f) {
					f = g;
					supplier2 = pair.getSecond();
				}
			}

			return (T)supplier2.get();
		}

		public T findBiomeIndex(Climate.TargetPoint targetPoint) {
			return this.findBiomeIndex(targetPoint, Climate.RTree.Node::distance);
		}

		protected T findBiomeIndex(Climate.TargetPoint targetPoint, Climate.DistanceMetric<T> distanceMetric) {
			return this.index.search(targetPoint, distanceMetric);
		}
	}

	public static final class ParameterPoint {
		public static final Codec<Climate.ParameterPoint> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Climate.Parameter.CODEC.fieldOf("temperature").forGetter(parameterPoint -> parameterPoint.temperature),
						Climate.Parameter.CODEC.fieldOf("humidity").forGetter(parameterPoint -> parameterPoint.humidity),
						Climate.Parameter.CODEC.fieldOf("continentalness").forGetter(parameterPoint -> parameterPoint.continentalness),
						Climate.Parameter.CODEC.fieldOf("erosion").forGetter(parameterPoint -> parameterPoint.erosion),
						Climate.Parameter.CODEC.fieldOf("depth").forGetter(parameterPoint -> parameterPoint.depth),
						Climate.Parameter.CODEC.fieldOf("weirdness").forGetter(parameterPoint -> parameterPoint.weirdness),
						Codec.floatRange(0.0F, 1.0F).fieldOf("offset").forGetter(parameterPoint -> parameterPoint.offset)
					)
					.apply(instance, Climate.ParameterPoint::new)
		);
		private final Climate.Parameter temperature;
		private final Climate.Parameter humidity;
		private final Climate.Parameter continentalness;
		private final Climate.Parameter erosion;
		private final Climate.Parameter depth;
		private final Climate.Parameter weirdness;
		private final float offset;

		ParameterPoint(
			Climate.Parameter parameter,
			Climate.Parameter parameter2,
			Climate.Parameter parameter3,
			Climate.Parameter parameter4,
			Climate.Parameter parameter5,
			Climate.Parameter parameter6,
			float f
		) {
			this.temperature = parameter;
			this.humidity = parameter2;
			this.continentalness = parameter3;
			this.erosion = parameter4;
			this.depth = parameter5;
			this.weirdness = parameter6;
			this.offset = f;
		}

		public String toString() {
			return "[temp: "
				+ this.temperature
				+ ", hum: "
				+ this.humidity
				+ ", cont: "
				+ this.continentalness
				+ ", eros: "
				+ this.erosion
				+ ", depth: "
				+ this.depth
				+ ", weird: "
				+ this.weirdness
				+ ", offset: "
				+ this.offset
				+ "]";
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				Climate.ParameterPoint parameterPoint = (Climate.ParameterPoint)object;
				if (!this.temperature.equals(parameterPoint.temperature)) {
					return false;
				} else if (!this.humidity.equals(parameterPoint.humidity)) {
					return false;
				} else if (!this.continentalness.equals(parameterPoint.continentalness)) {
					return false;
				} else if (!this.erosion.equals(parameterPoint.erosion)) {
					return false;
				} else if (!this.depth.equals(parameterPoint.depth)) {
					return false;
				} else {
					return !this.weirdness.equals(parameterPoint.weirdness) ? false : Float.compare(parameterPoint.offset, this.offset) == 0;
				}
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, this.offset});
		}

		float fitness(Climate.TargetPoint targetPoint) {
			return Mth.square(this.temperature.distance(targetPoint.temperature))
				+ Mth.square(this.humidity.distance(targetPoint.humidity))
				+ Mth.square(this.continentalness.distance(targetPoint.continentalness))
				+ Mth.square(this.erosion.distance(targetPoint.erosion))
				+ Mth.square(this.depth.distance(targetPoint.depth))
				+ Mth.square(this.weirdness.distance(targetPoint.weirdness))
				+ Mth.square(this.offset);
		}

		public Climate.Parameter temperature() {
			return this.temperature;
		}

		public Climate.Parameter humidity() {
			return this.humidity;
		}

		public Climate.Parameter continentalness() {
			return this.continentalness;
		}

		public Climate.Parameter erosion() {
			return this.erosion;
		}

		public Climate.Parameter depth() {
			return this.depth;
		}

		public Climate.Parameter weirdness() {
			return this.weirdness;
		}

		public float offset() {
			return this.offset;
		}

		protected List<Climate.Parameter> parameterSpace() {
			return ImmutableList.of(
				this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, Climate.Parameter.point(this.offset)
			);
		}
	}

	static final class RTree<T> {
		private static final int CHILDREN_PER_NODE = 10;
		private final Climate.RTree.Node<T> root;
		private final ThreadLocal<Climate.RTree.Leaf<T>> lastResult = new ThreadLocal();

		private RTree(Climate.RTree.Node<T> node) {
			this.root = node;
		}

		public static <T> Climate.RTree<T> create(List<Pair<Climate.ParameterPoint, Supplier<T>>> list) {
			if (list.isEmpty()) {
				throw new IllegalArgumentException("Need at least one biome to build the search tree.");
			} else {
				int i = ((Climate.ParameterPoint)((Pair)list.get(0)).getFirst()).parameterSpace().size();
				if (i != 7) {
					throw new IllegalStateException("Expecting parameter space to be 7, got " + i);
				} else {
					List<Climate.RTree.Leaf<T>> list2 = (List<Climate.RTree.Leaf<T>>)list.stream()
						.map(pair -> new Climate.RTree.Leaf((Climate.ParameterPoint)pair.getFirst(), (Supplier<T>)pair.getSecond()))
						.collect(Collectors.toCollection(ArrayList::new));
					return new Climate.RTree<>(build(i, list2));
				}
			}
		}

		private static <T> Climate.RTree.Node<T> build(int i, List<? extends Climate.RTree.Node<T>> list) {
			if (list.isEmpty()) {
				throw new IllegalStateException("Need at least one child to build a node");
			} else if (list.size() == 1) {
				return (Climate.RTree.Node<T>)list.get(0);
			} else if (list.size() <= 10) {
				list.sort(Comparator.comparingDouble(node -> {
					float fx = 0.0F;

					for (int jx = 0; jx < i; jx++) {
						Climate.Parameter parameter = node.parameterSpace[jx];
						fx += Math.abs((parameter.min() + parameter.max()) / 2.0F);
					}

					return (double)fx;
				}));
				return new Climate.RTree.SubTree<>(list);
			} else {
				float f = Float.POSITIVE_INFINITY;
				int j = -1;
				List<Climate.RTree.SubTree<T>> list2 = null;

				for (int k = 0; k < i; k++) {
					sort(list, i, k, false);
					List<Climate.RTree.SubTree<T>> list3 = bucketize(list);
					float g = 0.0F;

					for (Climate.RTree.SubTree<T> subTree : list3) {
						g += cost(subTree.parameterSpace);
					}

					if (f > g) {
						f = g;
						j = k;
						list2 = list3;
					}
				}

				sort(list2, i, j, true);
				return new Climate.RTree.SubTree<>(
					(List<? extends Climate.RTree.Node<T>>)list2.stream().map(subTreex -> build(i, Arrays.asList(subTreex.children))).collect(Collectors.toList())
				);
			}
		}

		private static <T> void sort(List<? extends Climate.RTree.Node<T>> list, int i, int j, boolean bl) {
			Comparator<Climate.RTree.Node<T>> comparator = comparator(j, bl);

			for (int k = 1; k < i; k++) {
				comparator = comparator.thenComparing(comparator((j + k) % i, bl));
			}

			list.sort(comparator);
		}

		private static <T> Comparator<Climate.RTree.Node<T>> comparator(int i, boolean bl) {
			return Comparator.comparingDouble(node -> {
				Climate.Parameter parameter = node.parameterSpace[i];
				float f = (parameter.min() + parameter.max()) / 2.0F;
				return bl ? (double)Math.abs(f) : (double)f;
			});
		}

		private static <T> List<Climate.RTree.SubTree<T>> bucketize(List<? extends Climate.RTree.Node<T>> list) {
			List<Climate.RTree.SubTree<T>> list2 = Lists.<Climate.RTree.SubTree<T>>newArrayList();
			List<Climate.RTree.Node<T>> list3 = Lists.<Climate.RTree.Node<T>>newArrayList();
			int i = (int)Math.pow(10.0, Math.floor(Math.log((double)list.size() - 0.01) / Math.log(10.0)));

			for (Climate.RTree.Node<T> node : list) {
				list3.add(node);
				if (list3.size() >= i) {
					list2.add(new Climate.RTree.SubTree(list3));
					list3 = Lists.<Climate.RTree.Node<T>>newArrayList();
				}
			}

			if (!list3.isEmpty()) {
				list2.add(new Climate.RTree.SubTree(list3));
			}

			return list2;
		}

		private static float cost(Climate.Parameter[] parameters) {
			float f = 0.0F;

			for (Climate.Parameter parameter : parameters) {
				f += Math.abs(parameter.max() - parameter.min());
			}

			return f;
		}

		static <T> List<Climate.Parameter> buildParameterSpace(List<? extends Climate.RTree.Node<T>> list) {
			if (list.isEmpty()) {
				throw new IllegalArgumentException("SubTree needs at least one child");
			} else {
				int i = 7;
				List<Climate.Parameter> list2 = Lists.<Climate.Parameter>newArrayList();

				for (int j = 0; j < 7; j++) {
					list2.add(null);
				}

				for (Climate.RTree.Node<T> node : list) {
					for (int k = 0; k < 7; k++) {
						list2.set(k, node.parameterSpace[k].span((Climate.Parameter)list2.get(k)));
					}
				}

				return list2;
			}
		}

		public T search(Climate.TargetPoint targetPoint, Climate.DistanceMetric<T> distanceMetric) {
			float[] fs = targetPoint.toParameterArray();
			Climate.RTree.Leaf<T> leaf = this.root.search(fs, (Climate.RTree.Leaf<T>)this.lastResult.get(), distanceMetric);
			this.lastResult.set(leaf);
			return (T)leaf.biome.get();
		}

		static final class Leaf<T> extends Climate.RTree.Node<T> {
			final Supplier<T> biome;

			Leaf(Climate.ParameterPoint parameterPoint, Supplier<T> supplier) {
				super(parameterPoint.parameterSpace());
				this.biome = supplier;
			}

			@Override
			protected Climate.RTree.Leaf<T> search(float[] fs, @Nullable Climate.RTree.Leaf<T> leaf, Climate.DistanceMetric<T> distanceMetric) {
				return this;
			}
		}

		abstract static class Node<T> {
			protected final Climate.Parameter[] parameterSpace;

			protected Node(List<Climate.Parameter> list) {
				this.parameterSpace = (Climate.Parameter[])list.toArray(new Climate.Parameter[0]);
			}

			protected abstract Climate.RTree.Leaf<T> search(float[] fs, @Nullable Climate.RTree.Leaf<T> leaf, Climate.DistanceMetric<T> distanceMetric);

			protected float distance(float[] fs) {
				float f = 0.0F;

				for (int i = 0; i < 7; i++) {
					f += Mth.square(this.parameterSpace[i].distance(fs[i]));
				}

				return f;
			}

			public String toString() {
				return Arrays.toString(this.parameterSpace);
			}
		}

		static final class SubTree<T> extends Climate.RTree.Node<T> {
			final Climate.RTree.Node<T>[] children;

			protected SubTree(List<? extends Climate.RTree.Node<T>> list) {
				this(Climate.RTree.buildParameterSpace(list), list);
			}

			protected SubTree(List<Climate.Parameter> list, List<? extends Climate.RTree.Node<T>> list2) {
				super(list);
				this.children = (Climate.RTree.Node<T>[])list2.toArray(new Climate.RTree.Node[0]);
			}

			@Override
			protected Climate.RTree.Leaf<T> search(float[] fs, @Nullable Climate.RTree.Leaf<T> leaf, Climate.DistanceMetric<T> distanceMetric) {
				float f = leaf == null ? Float.POSITIVE_INFINITY : distanceMetric.distance(leaf, fs);
				Climate.RTree.Leaf<T> leaf2 = leaf;

				for (Climate.RTree.Node<T> node : this.children) {
					float g = distanceMetric.distance(node, fs);
					if (f > g) {
						Climate.RTree.Leaf<T> leaf3 = node.search(fs, null, distanceMetric);
						float h = node == leaf3 ? g : distanceMetric.distance(leaf3, fs);
						if (f > h) {
							f = h;
							leaf2 = leaf3;
						}
					}
				}

				return leaf2;
			}
		}
	}

	public interface Sampler {
		Climate.TargetPoint sample(int i, int j, int k);
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
			return new float[]{this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, 0.0F};
		}
	}
}
