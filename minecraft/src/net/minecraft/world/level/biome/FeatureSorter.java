package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.Graph;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;

public class FeatureSorter {
	public static <T> List<FeatureSorter.StepFeatureData> buildFeaturesPerStep(List<T> list, Function<T, List<HolderSet<PlacedFeature>>> function, boolean bl) {
		Object2IntMap<PlacedFeature> object2IntMap = new Object2IntOpenHashMap<>();
		MutableInt mutableInt = new MutableInt(0);

		record FeatureData(int featureIndex, int step, PlacedFeature feature) {
		}

		Comparator<FeatureData> comparator = Comparator.comparingInt(FeatureData::step).thenComparingInt(FeatureData::featureIndex);
		Map<FeatureData, Set<FeatureData>> map = new TreeMap(comparator);
		int i = 0;

		for (T object : list) {
			List<FeatureData> list2 = Lists.<FeatureData>newArrayList();
			List<HolderSet<PlacedFeature>> list3 = (List<HolderSet<PlacedFeature>>)function.apply(object);
			i = Math.max(i, list3.size());

			for (int j = 0; j < list3.size(); j++) {
				for (Holder<PlacedFeature> holder : (HolderSet)list3.get(j)) {
					PlacedFeature placedFeature = holder.value();
					list2.add(
						new FeatureData(
							object2IntMap.computeIfAbsent(placedFeature, (Object2IntFunction<? super PlacedFeature>)(objectx -> mutableInt.getAndIncrement())), j, placedFeature
						)
					);
				}
			}

			for (int j = 0; j < list2.size(); j++) {
				Set<FeatureData> set = (Set<FeatureData>)map.computeIfAbsent((FeatureData)list2.get(j), arg -> new TreeSet(comparator));
				if (j < list2.size() - 1) {
					set.add((FeatureData)list2.get(j + 1));
				}
			}
		}

		Set<FeatureData> set2 = new TreeSet(comparator);
		Set<FeatureData> set3 = new TreeSet(comparator);
		List<FeatureData> list2 = Lists.<FeatureData>newArrayList();

		for (FeatureData lv : map.keySet()) {
			if (!set3.isEmpty()) {
				throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
			}

			if (!set2.contains(lv) && Graph.depthFirstSearch(map, set2, set3, list2::add, lv)) {
				if (!bl) {
					throw new IllegalStateException("Feature order cycle found");
				}

				List<T> list4 = new ArrayList(list);

				int k;
				do {
					k = list4.size();
					ListIterator<T> listIterator = list4.listIterator();

					while (listIterator.hasNext()) {
						T object2 = (T)listIterator.next();
						listIterator.remove();

						try {
							buildFeaturesPerStep(list4, function, false);
						} catch (IllegalStateException var18) {
							continue;
						}

						listIterator.add(object2);
					}
				} while (k != list4.size());

				throw new IllegalStateException("Feature order cycle found, involved sources: " + list4);
			}
		}

		Collections.reverse(list2);
		Builder<FeatureSorter.StepFeatureData> builder = ImmutableList.builder();

		for (int jx = 0; jx < i; jx++) {
			int l = jx;
			List<PlacedFeature> list5 = (List<PlacedFeature>)list2.stream().filter(arg -> arg.step() == l).map(FeatureData::feature).collect(Collectors.toList());
			builder.add(new FeatureSorter.StepFeatureData(list5));
		}

		return builder.build();
	}

	public static record StepFeatureData(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {
		StepFeatureData(List<PlacedFeature> list) {
			this(list, Util.createIndexIdentityLookup(list));
		}
	}
}
