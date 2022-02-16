/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HolderSet<T>
extends Iterable<Holder<T>> {
    public Stream<Holder<T>> stream();

    public int size();

    public Either<TagKey<T>, List<Holder<T>>> unwrap();

    public Optional<Holder<T>> getRandomElement(Random var1);

    public Holder<T> get(int var1);

    public boolean contains(Holder<T> var1);

    public boolean isValidInRegistry(Registry<T> var1);

    @SafeVarargs
    public static <T> Direct<T> direct(Holder<T> ... holders) {
        return new Direct<T>(List.of(holders));
    }

    public static <T> Direct<T> direct(List<? extends Holder<T>> list) {
        return new Direct(List.copyOf(list));
    }

    @SafeVarargs
    public static <E, T> Direct<T> direct(Function<E, Holder<T>> function, E ... objects) {
        return HolderSet.direct(Stream.of(objects).map(function).toList());
    }

    public static <E, T> Direct<T> direct(Function<E, Holder<T>> function, List<E> list) {
        return HolderSet.direct(list.stream().map(function).toList());
    }

    public static class Direct<T>
    extends ListBacked<T> {
        private final List<Holder<T>> contents;
        @Nullable
        private Set<Holder<T>> contentsSet;

        Direct(List<Holder<T>> list) {
            this.contents = list;
        }

        @Override
        protected List<Holder<T>> contents() {
            return this.contents;
        }

        @Override
        public Either<TagKey<T>, List<Holder<T>>> unwrap() {
            return Either.right(this.contents);
        }

        @Override
        public boolean contains(Holder<T> holder) {
            if (this.contentsSet == null) {
                this.contentsSet = Set.copyOf(this.contents);
            }
            return this.contentsSet.contains(holder);
        }

        public String toString() {
            return "DirectSet[" + this.contents + "]";
        }
    }

    public static class Named<T>
    extends ListBacked<T> {
        private final Registry<T> registry;
        private final TagKey<T> key;
        private List<Holder<T>> contents = List.of();

        Named(Registry<T> registry, TagKey<T> tagKey) {
            this.registry = registry;
            this.key = tagKey;
        }

        void bind(List<Holder<T>> list) {
            this.contents = List.copyOf(list);
        }

        public TagKey<T> key() {
            return this.key;
        }

        @Override
        protected List<Holder<T>> contents() {
            return this.contents;
        }

        @Override
        public Either<TagKey<T>, List<Holder<T>>> unwrap() {
            return Either.left(this.key);
        }

        @Override
        public boolean contains(Holder<T> holder) {
            return holder.is(this.key);
        }

        public String toString() {
            return "NamedSet(" + this.key + ")[" + this.contents + "]";
        }

        @Override
        public boolean isValidInRegistry(Registry<T> registry) {
            return this.registry == registry;
        }
    }

    public static abstract class ListBacked<T>
    implements HolderSet<T> {
        protected abstract List<Holder<T>> contents();

        @Override
        public int size() {
            return this.contents().size();
        }

        @Override
        public Spliterator<Holder<T>> spliterator() {
            return this.contents().spliterator();
        }

        @Override
        @NotNull
        public Iterator<Holder<T>> iterator() {
            return this.contents().iterator();
        }

        @Override
        public Stream<Holder<T>> stream() {
            return this.contents().stream();
        }

        @Override
        public Optional<Holder<T>> getRandomElement(Random random) {
            return Util.getRandomSafe(this.contents(), random);
        }

        @Override
        public Holder<T> get(int i) {
            return this.contents().get(i);
        }

        @Override
        public boolean isValidInRegistry(Registry<T> registry) {
            return true;
        }
    }
}

