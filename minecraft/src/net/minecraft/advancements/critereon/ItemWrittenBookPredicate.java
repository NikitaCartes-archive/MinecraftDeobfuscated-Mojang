package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;

public record ItemWrittenBookPredicate(
	Optional<CollectionPredicate<Filterable<Component>, ItemWrittenBookPredicate.PagePredicate>> pages,
	Optional<String> author,
	Optional<String> title,
	MinMaxBounds.Ints generation,
	Optional<Boolean> resolved
) implements SingleComponentItemPredicate<WrittenBookContent> {
	public static final Codec<ItemWrittenBookPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					CollectionPredicate.codec(ItemWrittenBookPredicate.PagePredicate.CODEC).optionalFieldOf("pages").forGetter(ItemWrittenBookPredicate::pages),
					Codec.STRING.optionalFieldOf("author").forGetter(ItemWrittenBookPredicate::author),
					Codec.STRING.optionalFieldOf("title").forGetter(ItemWrittenBookPredicate::title),
					MinMaxBounds.Ints.CODEC.optionalFieldOf("generation", MinMaxBounds.Ints.ANY).forGetter(ItemWrittenBookPredicate::generation),
					Codec.BOOL.optionalFieldOf("resolved").forGetter(ItemWrittenBookPredicate::resolved)
				)
				.apply(instance, ItemWrittenBookPredicate::new)
	);

	@Override
	public DataComponentType<WrittenBookContent> componentType() {
		return DataComponents.WRITTEN_BOOK_CONTENT;
	}

	public boolean matches(ItemStack itemStack, WrittenBookContent writtenBookContent) {
		if (this.author.isPresent() && !((String)this.author.get()).equals(writtenBookContent.author())) {
			return false;
		} else if (this.title.isPresent() && !((String)this.title.get()).equals(writtenBookContent.title().raw())) {
			return false;
		} else if (!this.generation.matches(writtenBookContent.generation())) {
			return false;
		} else {
			return this.resolved.isPresent() && this.resolved.get() != writtenBookContent.resolved()
				? false
				: !this.pages.isPresent() || ((CollectionPredicate)this.pages.get()).test((Iterable)writtenBookContent.pages());
		}
	}

	public static record PagePredicate(Component contents) implements Predicate<Filterable<Component>> {
		public static final Codec<ItemWrittenBookPredicate.PagePredicate> CODEC = ComponentSerialization.CODEC
			.xmap(ItemWrittenBookPredicate.PagePredicate::new, ItemWrittenBookPredicate.PagePredicate::contents);

		public boolean test(Filterable<Component> filterable) {
			return filterable.raw().equals(this.contents);
		}
	}
}
