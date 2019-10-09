/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

public class WeightedList<U> {
    protected final List<WeightedEntry<? extends U>> entries = Lists.newArrayList();
    private final Random random;

    public WeightedList(Random random) {
        this.random = random;
    }

    public WeightedList() {
        this(new Random());
    }

    public <T> WeightedList(Dynamic<T> dynamic2, Function<Dynamic<T>, U> function) {
        this();
        dynamic2.asStream().forEach(dynamic -> dynamic.get("data").map(dynamic2 -> {
            Object object = function.apply((Dynamic)dynamic2);
            int i = dynamic.get("weight").asInt(1);
            return this.add(object, i);
        }));
    }

    public <T> T serialize(DynamicOps<T> dynamicOps, Function<U, Dynamic<T>> function) {
        return (T)dynamicOps.createList(this.streamEntries().map(weightedEntry -> dynamicOps.createMap(ImmutableMap.builder().put(dynamicOps.createString("data"), ((Dynamic)function.apply(weightedEntry.getData())).getValue()).put(dynamicOps.createString("weight"), dynamicOps.createInt(weightedEntry.getWeight())).build())));
    }

    public WeightedList<U> add(U object, int i) {
        this.entries.add(new WeightedEntry(object, i));
        return this;
    }

    public WeightedList<U> shuffle() {
        return this.shuffle(this.random);
    }

    public WeightedList<U> shuffle(Random random) {
        this.entries.forEach(weightedEntry -> ((WeightedEntry)weightedEntry).setRandom(random.nextFloat()));
        this.entries.sort(Comparator.comparingDouble(object -> ((WeightedEntry)object).getRandWeight()));
        return this;
    }

    public Stream<? extends U> stream() {
        return this.entries.stream().map(WeightedEntry::getData);
    }

    public Stream<WeightedEntry<? extends U>> streamEntries() {
        return this.entries.stream();
    }

    public U getOne(Random random) {
        return this.shuffle(random).stream().findFirst().orElseThrow(RuntimeException::new);
    }

    public String toString() {
        return "WeightedList[" + this.entries + "]";
    }

    public class WeightedEntry<T> {
        private final T data;
        private final int weight;
        private double randWeight;

        private WeightedEntry(T object, int i) {
            this.weight = i;
            this.data = object;
        }

        private double getRandWeight() {
            return this.randWeight;
        }

        private void setRandom(float f) {
            this.randWeight = -Math.pow(f, 1.0f / (float)this.weight);
        }

        public T getData() {
            return this.data;
        }

        public int getWeight() {
            return this.weight;
        }

        public String toString() {
            return "" + this.weight + ":" + this.data;
        }
    }
}

