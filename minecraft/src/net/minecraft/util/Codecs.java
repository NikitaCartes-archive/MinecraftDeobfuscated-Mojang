package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.Function;

public class Codecs {
	private static Function<Integer, DataResult<Integer>> checkRange(int i, int j) {
		return integer -> integer >= i && integer <= j
				? DataResult.success(integer)
				: DataResult.error("Value " + integer + " outside of range [" + i + ":" + j + "]", integer);
	}

	public static Codec<Integer> intRange(int i, int j) {
		Function<Integer, DataResult<Integer>> function = checkRange(i, j);
		return Codec.INT.flatXmap(function, function);
	}

	private static Function<Double, DataResult<Double>> checkRange(double d, double e) {
		return double_ -> double_ >= d && double_ <= e
				? DataResult.success(double_)
				: DataResult.error("Value " + double_ + " outside of range [" + d + ":" + e + "]", double_);
	}

	public static Codec<Double> doubleRange(double d, double e) {
		Function<Double, DataResult<Double>> function = checkRange(d, e);
		return Codec.DOUBLE.flatXmap(function, function);
	}
}
