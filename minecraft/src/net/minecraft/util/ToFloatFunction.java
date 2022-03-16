package net.minecraft.util;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.function.Function;

public interface ToFloatFunction<C> {
	ToFloatFunction<Float> IDENTITY = createUnlimited(f -> f);

	float apply(C object);

	float minValue();

	float maxValue();

	static ToFloatFunction<Float> createUnlimited(Float2FloatFunction float2FloatFunction) {
		return new ToFloatFunction<Float>() {
			public float apply(Float float_) {
				return float2FloatFunction.apply(float_);
			}

			@Override
			public float minValue() {
				return Float.NEGATIVE_INFINITY;
			}

			@Override
			public float maxValue() {
				return Float.POSITIVE_INFINITY;
			}
		};
	}

	default <C2> ToFloatFunction<C2> comap(Function<C2, C> function) {
		final ToFloatFunction<C> toFloatFunction = this;
		return new ToFloatFunction<C2>() {
			@Override
			public float apply(C2 object) {
				return toFloatFunction.apply((C)function.apply(object));
			}

			@Override
			public float minValue() {
				return toFloatFunction.minValue();
			}

			@Override
			public float maxValue() {
				return toFloatFunction.maxValue();
			}
		};
	}
}
