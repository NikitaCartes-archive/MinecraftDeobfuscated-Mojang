/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
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
    public static <T> List<StepFeatureData> buildFeaturesPerStep(List<T> list, Function<T, List<HolderSet<PlacedFeature>>> function, boolean bl) {
        record FeatureData(int featureIndex, int step, PlacedFeature feature) {
        }
        ArrayList<FeatureData> list2;
        Object2IntOpenHashMap<PlacedFeature> object2IntMap = new Object2IntOpenHashMap<PlacedFeature>();
        MutableInt mutableInt = new MutableInt(0);
        Comparator<FeatureData> comparator = Comparator.comparingInt(FeatureData::step).thenComparingInt(FeatureData::featureIndex);
        TreeMap<FeatureData, Set> map = new TreeMap<FeatureData, Set>(comparator);
        int i = 0;
        for (T object2 : list) {
            int j;
            list2 = Lists.newArrayList();
            List<HolderSet<PlacedFeature>> list3 = function.apply(object2);
            i = Math.max(i, list3.size());
            for (j = 0; j < list3.size(); ++j) {
                for (Holder holder : (HolderSet)list3.get(j)) {
                    PlacedFeature placedFeature = (PlacedFeature)holder.value();
                    list2.add(new FeatureData(object2IntMap.computeIfAbsent(placedFeature, object -> mutableInt.getAndIncrement()), j, placedFeature));
                }
            }
            for (j = 0; j < list2.size(); ++j) {
                Set set = map.computeIfAbsent((FeatureData)list2.get(j), arg -> new TreeSet(comparator));
                if (j >= list2.size() - 1) continue;
                set.add((FeatureData)list2.get(j + 1));
            }
        }
        TreeSet<FeatureData> set2 = new TreeSet<FeatureData>(comparator);
        TreeSet<FeatureData> set3 = new TreeSet<FeatureData>(comparator);
        list2 = Lists.newArrayList();
        for (FeatureData lv : map.keySet()) {
            if (!set3.isEmpty()) {
                throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
            }
            if (set2.contains(lv) || !Graph.depthFirstSearch(map, set2, set3, list2::add, lv)) continue;
            if (bl) {
                int k;
                ArrayList<T> list4 = new ArrayList<T>(list);
                do {
                    k = list4.size();
                    ListIterator listIterator = list4.listIterator();
                    while (listIterator.hasNext()) {
                        Object object2 = listIterator.next();
                        listIterator.remove();
                        try {
                            FeatureSorter.buildFeaturesPerStep(list4, function, false);
                        } catch (IllegalStateException illegalStateException) {
                            continue;
                        }
                        listIterator.add(object2);
                    }
                } while (k != list4.size());
                throw new IllegalStateException("Feature order cycle found, involved sources: " + list4);
            }
            throw new IllegalStateException("Feature order cycle found");
        }
        Collections.reverse(list2);
        ImmutableList.Builder builder = ImmutableList.builder();
        int j = 0;
        while (j < i) {
            int l = j++;
            List<PlacedFeature> list5 = list2.stream().filter(arg -> arg.step() == l).map(FeatureData::feature).collect(Collectors.toList());
            builder.add(new StepFeatureData(list5));
        }
        return builder.build();
    }

    public record StepFeatureData(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {
        StepFeatureData(List<PlacedFeature> list) {
            this(list, Util.createIndexLookup(list, i -> new Object2IntOpenCustomHashMap(i, Util.identityStrategy())));
        }
    }
}

