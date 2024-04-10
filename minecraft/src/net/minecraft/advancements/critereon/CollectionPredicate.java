package net.minecraft.advancements.critereon;

import com.google.common.collect.Iterables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;

public record CollectionPredicate<T, P extends Predicate<T>>(
	Optional<CollectionContentsPredicate<T, P>> contains, Optional<CollectionCountsPredicate<T, P>> counts, Optional<MinMaxBounds.Ints> size
) implements Predicate<Iterable<T>> {
	public static <T, P extends Predicate<T>> Codec<CollectionPredicate<T, P>> codec(Codec<P> codec) {
		return RecordCodecBuilder.create(
			instance -> instance.group(
						CollectionContentsPredicate.<T, P>codec(codec).optionalFieldOf("contains").forGetter(CollectionPredicate::contains),
						CollectionCountsPredicate.<T, P>codec(codec).optionalFieldOf("count").forGetter(CollectionPredicate::counts),
						MinMaxBounds.Ints.CODEC.optionalFieldOf("size").forGetter(CollectionPredicate::size)
					)
					.apply(instance, CollectionPredicate::new)
		);
	}

	public boolean test(Iterable<T> iterable) {
		if (this.contains.isPresent() && !((CollectionContentsPredicate)this.contains.get()).test(iterable)) {
			return false;
		} else {
			return this.counts.isPresent() && !((CollectionCountsPredicate)this.counts.get()).test(iterable)
				? false
				: !this.size.isPresent() || ((MinMaxBounds.Ints)this.size.get()).matches(Iterables.size(iterable));
		}
	}
}
