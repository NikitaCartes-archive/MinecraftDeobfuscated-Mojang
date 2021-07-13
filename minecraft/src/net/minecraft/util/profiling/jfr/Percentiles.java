package net.minecraft.util.profiling.jfr;

import com.google.common.collect.ImmutableMap;
import com.google.common.math.Quantiles;
import com.google.common.math.Quantiles.ScaleAndIndexes;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class Percentiles {
	public static final ScaleAndIndexes DEFAULT_INDEXES = Quantiles.scale(100).indexes(50, 75, 90, 99);

	private Percentiles() {
	}

	public static Map<Integer, Double> evaluate(long[] ls) {
		return (Map<Integer, Double>)(ls.length == 0 ? ImmutableMap.of() : sorted(DEFAULT_INDEXES.compute(ls)));
	}

	public static Map<Integer, Double> evaluate(double[] ds) {
		return (Map<Integer, Double>)(ds.length == 0 ? ImmutableMap.of() : sorted(DEFAULT_INDEXES.compute(ds)));
	}

	private static Map<Integer, Double> sorted(Map<Integer, Double> map) {
		TreeMap<Integer, Double> treeMap = new TreeMap(Comparator.reverseOrder());
		treeMap.putAll(map);
		return treeMap;
	}
}
