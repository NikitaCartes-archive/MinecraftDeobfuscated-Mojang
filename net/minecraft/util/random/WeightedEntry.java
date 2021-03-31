/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.random;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.Weight;

public interface WeightedEntry {
    public Weight getWeight();

    public static <T> Wrapper<T> wrap(T object, int i) {
        return new Wrapper(object, Weight.of(i));
    }

    public static class Wrapper<T>
    implements WeightedEntry {
        private final T data;
        private final Weight weight;

        private Wrapper(T object, Weight weight) {
            this.data = object;
            this.weight = weight;
        }

        public T getData() {
            return this.data;
        }

        @Override
        public Weight getWeight() {
            return this.weight;
        }

        public static <E> Codec<Wrapper<E>> codec(Codec<E> codec) {
            return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)codec.fieldOf("data")).forGetter(Wrapper::getData), ((MapCodec)Weight.CODEC.fieldOf("weight")).forGetter(Wrapper::getWeight)).apply((Applicative<Wrapper, ?>)instance, Wrapper::new));
        }
    }

    public static class IntrusiveBase
    implements WeightedEntry {
        private final Weight weight;

        public IntrusiveBase(int i) {
            this.weight = Weight.of(i);
        }

        public IntrusiveBase(Weight weight) {
            this.weight = weight;
        }

        @Override
        public Weight getWeight() {
            return this.weight;
        }
    }
}

