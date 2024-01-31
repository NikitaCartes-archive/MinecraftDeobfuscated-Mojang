package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.mutable.MutableObject;

public class ItemParser {
	private final ItemSyntaxParser syntax;

	public ItemParser(HolderLookup.Provider provider) {
		this.syntax = new ItemSyntaxParser(provider, false);
	}

	public ItemParser.ItemResult parse(StringReader stringReader) throws CommandSyntaxException {
		final MutableObject<Holder<Item>> mutableObject = new MutableObject<>();
		final MutableObject<CompoundTag> mutableObject2 = new MutableObject<>();
		this.syntax.parse(stringReader, new ItemSyntaxParser.Visitor() {
			@Override
			public void visitItem(Holder<Item> holder) {
				mutableObject.setValue(holder);
			}

			@Override
			public void visitNbt(CompoundTag compoundTag) {
				mutableObject2.setValue(compoundTag);
			}
		});
		return new ItemParser.ItemResult((Holder<Item>)Objects.requireNonNull(mutableObject.getValue(), "Parser gave no item"), mutableObject2.getValue());
	}

	public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder) {
		return this.syntax.fillSuggestions(suggestionsBuilder);
	}

	public static record ItemResult(Holder<Item> item, @Nullable CompoundTag nbt) {
	}
}
