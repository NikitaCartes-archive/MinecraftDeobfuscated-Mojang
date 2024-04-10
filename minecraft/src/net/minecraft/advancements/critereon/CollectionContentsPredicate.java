package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface CollectionContentsPredicate<T, P extends Predicate<T>> extends Predicate<Iterable<T>> {
	List<P> unpack();

	static <T, P extends Predicate<T>> Codec<CollectionContentsPredicate<T, P>> codec(Codec<P> codec) {
		return codec.listOf().xmap(CollectionContentsPredicate::of, CollectionContentsPredicate::unpack);
	}

	@SafeVarargs
	static <T, P extends Predicate<T>> CollectionContentsPredicate<T, P> of(P... predicates) {
		return of(List.of(predicates));
	}

	static <T, P extends Predicate<T>> CollectionContentsPredicate<T, P> of(List<P> list) {
		return (CollectionContentsPredicate<T, P>)(switch (list.size()) {
			case 0 -> new CollectionContentsPredicate.Zero();
			case 1 -> new CollectionContentsPredicate.Single((P)list.getFirst());
			default -> new CollectionContentsPredicate.Multiple(list);
		});
	}

	public static record Multiple<T, P extends Predicate<T>>(List<P> tests) implements CollectionContentsPredicate<T, P> {
		public boolean test(Iterable<T> iterable) {
			List<Predicate<T>> list = new ArrayList(this.tests);

			for (T object : iterable) {
				list.removeIf(predicate -> predicate.test(object));
				if (list.isEmpty()) {
					return true;
				}
			}

			return false;
		}

		@Override
		public List<P> unpack() {
			return this.tests;
		}
	}

	public static record Single<T, P extends Predicate<T>>(P test) implements CollectionContentsPredicate<T, P> {
		public boolean test(Iterable<T> iterable) {
			for (T object : iterable) {
				if (this.test.test(object)) {
					return true;
				}
			}

			return false;
		}

		@Override
		public List<P> unpack() {
			return List.of(this.test);
		}
	}

	public static class Zero<T, P extends Predicate<T>> implements CollectionContentsPredicate<T, P> {
		public boolean test(Iterable<T> iterable) {
			return true;
		}

		@Override
		public List<P> unpack() {
			return List.of();
		}
	}
}
