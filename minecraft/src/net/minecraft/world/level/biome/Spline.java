package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;

public final class Spline<C> implements ToFloatFunction<C> {
	final ToFloatFunction<C> coordinate;
	final float[] locations;
	final List<ToFloatFunction<C>> values;
	final float[] derivatives;
	String name;

	Spline(String string, ToFloatFunction<C> toFloatFunction, float[] fs, List<ToFloatFunction<C>> list, float[] gs) {
		this.name = string;
		if (fs.length == list.size() && fs.length == gs.length) {
			this.coordinate = toFloatFunction;
			this.locations = fs;
			this.values = list;
			this.derivatives = gs;
		} else {
			throw new IllegalArgumentException("All lengths must be equal, got: " + fs.length + " " + list.size() + " " + gs.length);
		}
	}

	public Spline<C> sampler() {
		return this;
	}

	@Override
	public float apply(C object) {
		return Mth.splineInterpolate(this.coordinate.apply(object), this.locations, this.values, this.derivatives).apply(object);
	}

	public static <C> Spline.Builder<C> builder(ToFloatFunction<C> toFloatFunction) {
		return new Spline.Builder<>(toFloatFunction);
	}

	private String toString(float[] fs) {
		return "["
			+ (String)IntStream.range(0, fs.length).mapToDouble(i -> (double)fs[i]).mapToObj(d -> String.format("%.2f", d)).collect(Collectors.joining(", "))
			+ "]";
	}

	public String toString() {
		return "Spline{name="
			+ this.name
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
		@Nullable
		private Float previousLocation = null;
		private String name = null;

		public Builder(ToFloatFunction<C> toFloatFunction) {
			this.coordinate = toFloatFunction;
		}

		public Spline.Builder<C> named(String string) {
			this.name = string;
			return this;
		}

		public Spline.Builder<C> addPoint(float f, float g, float h) {
			return this.add(f, new Spline.Constant<>(g), h);
		}

		public Spline.Builder<C> addPoint(float f, ToFloatFunction<C> toFloatFunction, float g) {
			return this.add(f, toFloatFunction, g);
		}

		public Spline.Builder<C> addPoint(float f, Spline<C> spline, float g) {
			return this.add(f, spline.sampler(), g);
		}

		private Spline.Builder<C> add(float f, ToFloatFunction<C> toFloatFunction, float g) {
			if (this.previousLocation != null && f <= this.previousLocation) {
				throw new IllegalArgumentException("The way things are right now, we depend on registration in descending order");
			} else {
				this.locations.add(f);
				this.values.add(toFloatFunction);
				this.derivatives.add(g);
				this.previousLocation = f;
				return this;
			}
		}

		public Spline<C> build() {
			if (this.name == null) {
				throw new IllegalStateException("Splines require a name");
			} else if (this.locations.isEmpty()) {
				throw new IllegalStateException("No elements added");
			} else {
				return new Spline<>(this.name, this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray());
			}
		}
	}

	static class Constant<C> implements ToFloatFunction<C> {
		private float k;

		public Constant(float f) {
			this.k = f;
		}

		@Override
		public float apply(C object) {
			return this.k;
		}

		public String toString() {
			return String.format("k=%.2f", this.k);
		}
	}

	public interface FloatAdder {
		float combine(float f, float g);
	}
}
