package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.VisibleForTesting;

public interface HolderSet<T> extends Iterable<Holder<T>> {
	Stream<Holder<T>> stream();

	int size();

	boolean isBound();

	Either<TagKey<T>, List<Holder<T>>> unwrap();

	Optional<Holder<T>> getRandomElement(RandomSource randomSource);

	Holder<T> get(int i);

	boolean contains(Holder<T> holder);

	boolean canSerializeIn(HolderOwner<T> holderOwner);

	Optional<TagKey<T>> unwrapKey();

	@Deprecated
	@VisibleForTesting
	static <T> HolderSet.Named<T> emptyNamed(HolderOwner<T> holderOwner, TagKey<T> tagKey) {
		return new HolderSet.Named<T>(holderOwner, tagKey) {
			@Override
			protected List<Holder<T>> contents() {
				throw new UnsupportedOperationException("Tag " + this.key() + " can't be dereferenced during construction");
			}
		};
	}

	static <T> HolderSet<T> empty() {
		return (HolderSet<T>)HolderSet.Direct.EMPTY;
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

	static <E, T> HolderSet.Direct<T> direct(Function<E, Holder<T>> function, Collection<E> collection) {
		return direct(collection.stream().map(function).toList());
	}

	public static final class Direct<T> extends HolderSet.ListBacked<T> {
		static final HolderSet.Direct<?> EMPTY = new HolderSet.Direct(List.of());
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
		public boolean isBound() {
			return true;
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

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else {
				if (object instanceof HolderSet.Direct<?> direct && this.contents.equals(direct.contents)) {
					return true;
				}

				return false;
			}
		}

		public int hashCode() {
			return this.contents.hashCode();
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
		public boolean canSerializeIn(HolderOwner<T> holderOwner) {
			return true;
		}
	}

	public static class Named<T> extends HolderSet.ListBacked<T> {
		private final HolderOwner<T> owner;
		private final TagKey<T> key;
		@Nullable
		private List<Holder<T>> contents;

		Named(HolderOwner<T> holderOwner, TagKey<T> tagKey) {
			this.owner = holderOwner;
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
			if (this.contents == null) {
				throw new IllegalStateException("Trying to access unbound tag '" + this.key + "' from registry " + this.owner);
			} else {
				return this.contents;
			}
		}

		@Override
		public boolean isBound() {
			return this.contents != null;
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
		public boolean canSerializeIn(HolderOwner<T> holderOwner) {
			return this.owner.canSerializeIn(holderOwner);
		}
	}
}
