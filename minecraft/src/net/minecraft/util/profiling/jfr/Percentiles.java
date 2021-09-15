package net.minecraft.util.profiling.jfr;

import com.google.common.math.Quantiles;
import com.google.common.math.Quantiles.ScaleAndIndexes;
import it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleSortedMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleSortedMaps;
import java.util.Comparator;
import java.util.Map;
import net.minecraft.Util;

public class Percentiles {
	public static final ScaleAndIndexes DEFAULT_INDEXES = Quantiles.scale(100).indexes(50, 75, 90, 99);

	private Percentiles() {
	}

	public static Map<Integer, Double> evaluate(long[] ls) {
		return ls.length == 0 ? Map.of() : sorted(DEFAULT_INDEXES.compute(ls));
	}

	public static Map<Integer, Double> evaluate(double[] ds) {
		return ds.length == 0 ? Map.of() : sorted(DEFAULT_INDEXES.compute(ds));
	}

	private static Map<Integer, Double> sorted(Map<Integer, Double> map) {
		Int2DoubleSortedMap int2DoubleSortedMap = Util.make(
			new Int2DoubleRBTreeMap(Comparator.reverseOrder()), int2DoubleRBTreeMap -> int2DoubleRBTreeMap.putAll(map)
		);
		return Int2DoubleSortedMaps.unmodifiable(int2DoubleSortedMap);
	}
}
