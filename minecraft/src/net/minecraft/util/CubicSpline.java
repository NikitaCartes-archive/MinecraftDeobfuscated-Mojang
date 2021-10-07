package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CubicSpline<C> implements ToFloatFunction<C> {
	private final ToFloatFunction<C> coordinate;
	private final float[] locations;
	private final List<ToFloatFunction<C>> values;
	private final float[] derivatives;

	CubicSpline(ToFloatFunction<C> toFloatFunction, float[] fs, List<ToFloatFunction<C>> list, float[] gs) {
		if (fs.length == list.size() && fs.length == gs.length) {
			this.coordinate = toFloatFunction;
			this.locations = fs;
			this.values = list;
			this.derivatives = gs;
		} else {
			throw new IllegalArgumentException("All lengths must be equal, got: " + fs.length + " " + list.size() + " " + gs.length);
		}
	}

	@Override
	public float apply(C object) {
		float f = this.coordinate.apply(object);
		int i = Mth.binarySearch(0, this.locations.length, ix -> f < this.locations[ix]) - 1;
		int j = this.locations.length - 1;
		if (i < 0) {
			return ((ToFloatFunction)this.values.get(0)).apply(object) + this.derivatives[0] * (f - this.locations[0]);
		} else if (i == j) {
			return ((ToFloatFunction)this.values.get(j)).apply(object) + this.derivatives[j] * (f - this.locations[j]);
		} else {
			float g = this.locations[i];
			float h = this.locations[i + 1];
			float k = (f - g) / (h - g);
			ToFloatFunction<C> toFloatFunction = (ToFloatFunction<C>)this.values.get(i);
			ToFloatFunction<C> toFloatFunction2 = (ToFloatFunction<C>)this.values.get(i + 1);
			float l = this.derivatives[i];
			float m = this.derivatives[i + 1];
			float n = toFloatFunction.apply(object);
			float o = toFloatFunction2.apply(object);
			float p = l * (h - g) - (o - n);
			float q = -m * (h - g) + (o - n);
			return Mth.lerp(k, n, o) + k * (1.0F - k) * Mth.lerp(k, p, q);
		}
	}

	public static <C> CubicSpline.Builder<C> builder(ToFloatFunction<C> toFloatFunction) {
		return new CubicSpline.Builder<>(toFloatFunction);
	}

	private String toString(float[] fs) {
		return "["
			+ (String)IntStream.range(0, fs.length)
				.mapToDouble(i -> (double)fs[i])
				.mapToObj(d -> String.format(Locale.ROOT, "%.3f", d))
				.collect(Collectors.joining(", "))
			+ "]";
	}

	@VisibleForDebug
	protected ToFloatFunction<C> coordinate() {
		return this.coordinate;
	}

	@VisibleForDebug
	public List<Float> debugLocations() {
		return Collections.unmodifiableList(Floats.asList(this.locations));
	}

	@VisibleForDebug
	public ToFloatFunction<C> debugValue(int i) {
		return (ToFloatFunction<C>)this.values.get(i);
	}

	@VisibleForDebug
	public float debugDerivative(int i) {
		return this.derivatives[i];
	}

	public String toString() {
		return "Spline{coordinate="
			+ this.coordinate
			+ ", locations="
			+ this.toString(this.locations)
			+ ", derivatives="
			+ this.toString(this.derivatives)
			+ ", values="
			+ this.values
			+ "}";
	}

	public static final class Builder<C> {
		private final ToFloatFunction<C> coordinate;
		private final FloatList locations = new FloatArrayList();
		private final List<ToFloatFunction<C>> values = Lists.<ToFloatFunction<C>>newArrayList();
		private final FloatList derivatives = new FloatArrayList();

		protected Builder(ToFloatFunction<C> toFloatFunction) {
			this.coordinate = toFloatFunction;
		}

		public CubicSpline.Builder<C> addPoint(float f, float g, float h) {
			return this.add(f, new CubicSpline.Constant<>(g), h);
		}

		public CubicSpline.Builder<C> addPoint(float f, ToFloatFunction<C> toFloatFunction, float g) {
			return this.add(f, toFloatFunction, g);
		}

		public CubicSpline.Builder<C> addPoint(float f, CubicSpline<C> cubicSpline, float g) {
			return this.add(f, cubicSpline, g);
		}

		private CubicSpline.Builder<C> add(float f, ToFloatFunction<C> toFloatFunction, float g) {
			if (!this.locations.isEmpty() && f <= this.locations.getFloat(this.locations.size() - 1)) {
				throw new IllegalArgumentException("Please register points in ascending order");
			} else {
				this.locations.add(f);
				this.values.add(toFloatFunction);
				this.derivatives.add(g);
				return this;
			}
		}

		public CubicSpline<C> build() {
			if (this.locations.isEmpty()) {
				throw new IllegalStateException("No elements added");
			} else {
				return new CubicSpline<>(this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray());
			}
		}
	}

	static class Constant<C> implements ToFloatFunction<C> {
		private final float value;

		public Constant(float f) {
			this.value = f;
		}

		@Override
		public float apply(C object) {
			return this.value;
		}

		public String toString() {
			return String.format(Locale.ROOT, "k=%.3f", this.value);
		}
	}
}
