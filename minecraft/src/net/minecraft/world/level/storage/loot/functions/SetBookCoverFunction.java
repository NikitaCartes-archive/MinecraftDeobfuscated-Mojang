package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBookCoverFunction extends LootItemConditionalFunction {
	public static final Codec<SetBookCoverFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.<Optional<Filterable<String>>, Optional<String>, Optional<Integer>>and(
					instance.group(
						ExtraCodecs.strictOptionalField(Filterable.codec(ExtraCodecs.sizeLimitedString(0, 32)), "title")
							.forGetter(setBookCoverFunction -> setBookCoverFunction.title),
						ExtraCodecs.strictOptionalField(Codec.STRING, "author").forGetter(setBookCoverFunction -> setBookCoverFunction.author),
						ExtraCodecs.strictOptionalField(ExtraCodecs.intRange(0, 3), "generation").forGetter(setBookCoverFunction -> setBookCoverFunction.generation)
					)
				)
				.apply(instance, SetBookCoverFunction::new)
	);
	private final Optional<String> author;
	private final Optional<Filterable<String>> title;
	private final Optional<Integer> generation;

	public SetBookCoverFunction(List<LootItemCondition> list, Optional<Filterable<String>> optional, Optional<String> optional2, Optional<Integer> optional3) {
		super(list);
		this.author = optional2;
		this.title = optional;
		this.generation = optional3;
	}

	@Override
	protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.update(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY, this::apply);
		return itemStack;
	}

	private WrittenBookContent apply(WrittenBookContent writtenBookContent) {
		return new WrittenBookContent(
			(Filterable<String>)this.title.orElseGet(writtenBookContent::title),
			(String)this.author.orElseGet(writtenBookContent::author),
			(Integer)this.generation.orElseGet(writtenBookContent::generation),
			writtenBookContent.pages(),
			writtenBookContent.resolved()
		);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_BOOK_COVER;
	}
}
