/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class Graph {
    private Graph() {
    }

    public static <T> boolean depthFirstSearch(Map<T, Set<T>> map, Set<T> set, Set<T> set2, Consumer<T> consumer, T object) {
        if (set.contains(object)) {
            return false;
        }
        if (set2.contains(object)) {
            return true;
        }
        set2.add(object);
        for (Object object2 : (Set)map.getOrDefault(object, ImmutableSet.of())) {
            if (!Graph.depthFirstSearch(map, set, set2, consumer, object2)) continue;
            return true;
        }
        set2.remove(object);
        set.add(object);
        consumer.accept(object);
        return false;
    }
}

