package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public interface HolderSet<T> extends Iterable<Holder<T>> {
	Stream<Holder<T>> stream();

	int size();

	Either<TagKey<T>, List<Holder<T>>> unwrap();

	Optional<Holder<T>> getRandomElement(RandomSource randomSource);

	Holder<T> get(int i);

	boolean contains(Holder<T> holder);

	boolean isValidInRegistry(Registry<T> registry);

	Optional<TagKey<T>> unwrapKey();

	@Deprecated
	@VisibleForTesting
	static <T> HolderSet.Named<T> emptyNamed(Registry<T> registry, TagKey<T> tagKey) {
		return new HolderSet.Named<>(registry, tagKey);
	}

	@SafeVarargs
	static <T> HolderSet.Direct<T> direct(Holder<T>... holders) {
		return new HolderSet.Direct<>(List.of(holders));
	}

	static <T> HolderSet.Direct<T> direct(List<? extends Holder<T>> list) {
		return new HolderSet.Direct<>(List.copyOf(list));
	}

	@SafeVarargs
	static <E, T> HolderSet.Direct<T> direct(Function<E, Holder<T>> function, E... objects) {
		return direct(Stream.of(objects).map(function).toList());
	}

	static <E, T> HolderSet.Direct<T> direct(Function<E, Holder<T>> function, List<E> list) {
		return direct(list.stream().map(function).toList());
	}

	public static class Direct<T> extends HolderSet.ListBacked<T> {
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
		public Optional<TagKey<T>> unwrapKey() {
			return Optional.empty();
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

	public abstract static class ListBacked<T> implements HolderSet<T> {
		protected abstract List<Holder<T>> contents();

		@Override
		public int size() {
			return this.contents().size();
		}

		public Spliterator<Holder<T>> spliterator() {
			return this.contents().spliterator();
		}

		@NotNull
		public Iterator<Holder<T>> iterator() {
			return this.contents().iterator();
		}

		@Override
		public Stream<Holder<T>> stream() {
			return this.contents().stream();
		}

		@Override
		public Optional<Holder<T>> getRandomElement(RandomSource randomSource) {
			return Util.getRandomSafe(this.contents(), randomSource);
		}

		@Override
		public Holder<T> get(int i) {
			return (Holder<T>)this.contents().get(i);
		}

		@Override
		public boolean isValidInRegistry(Registry<T> registry) {
			return true;
		}
	}

	public static class Named<T> extends HolderSet.ListBacked<T> {
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
		public Optional<TagKey<T>> unwrapKey() {
			return Optional.of(this.key);
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
}
