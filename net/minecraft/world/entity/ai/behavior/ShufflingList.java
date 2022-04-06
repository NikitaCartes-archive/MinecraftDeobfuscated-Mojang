/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.RandomSource;

public class ShufflingList<U> {
    protected final List<WeightedEntry<U>> entries;
    private final RandomSource random = RandomSource.create();

    public ShufflingList() {
        this.entries = Lists.newArrayList();
    }

    private ShufflingList(List<WeightedEntry<U>> list) {
        this.entries = Lists.newArrayList(list);
    }

    public static <U> Codec<ShufflingList<U>> codec(Codec<U> codec) {
        return WeightedEntry.codec(codec).listOf().xmap(ShufflingList::new, shufflingList -> shufflingList.entries);
    }

    public ShufflingList<U> add(U object, int i) {
        this.entries.add(new WeightedEntry<U>(object, i));
        return this;
    }

    public ShufflingList<U> shuffle() {
        this.entries.forEach(weightedEntry -> weightedEntry.setRandom(this.random.nextFloat()));
        this.entries.sort(Comparator.comparingDouble(WeightedEntry::getRandWeight));
        return this;
    }

    public Stream<U> stream() {
        return this.entries.stream().map(WeightedEntry::getData);
    }

    public String toString() {
        return "ShufflingList[" + this.entries + "]";
    }

    public static class WeightedEntry<T> {
        final T data;
        final int weight;
        private double randWeight;

        WeightedEntry(T object, int i) {
            this.weight = i;
            this.data = object;
        }

        private double getRandWeight() {
            return this.randWeight;
        }

        void setRandom(float f) {
            this.randWeight = -Math.pow(f, 1.0f / (float)this.weight);
        }

        public T getData() {
            return this.data;
        }

        public int getWeight() {
            return this.weight;
        }

        public String toString() {
            return this.weight + ":" + this.data;
        }

        public static <E> Codec<WeightedEntry<E>> codec(final Codec<E> codec) {
            return new Codec<WeightedEntry<E>>(){

                @Override
                public <T> DataResult<Pair<WeightedEntry<E>, T>> decode(DynamicOps<T> dynamicOps, T object2) {
                    Dynamic dynamic = new Dynamic(dynamicOps, object2);
                    return dynamic.get("data").flatMap(codec::parse).map((? super R object) -> new WeightedEntry<Object>(object, dynamic.get("weight").asInt(1))).map((? super R weightedEntry) -> Pair.of(weightedEntry, dynamicOps.empty()));
                }

                @Override
                public <T> DataResult<T> encode(WeightedEntry<E> weightedEntry, DynamicOps<T> dynamicOps, T object) {
                    return dynamicOps.mapBuilder().add("weight", dynamicOps.createInt(weightedEntry.weight)).add("data", codec.encodeStart(dynamicOps, weightedEntry.data)).build(object);
                }

                @Override
                public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
                    return this.encode((WeightedEntry)object, dynamicOps, object2);
                }
            };
        }
    }
}

