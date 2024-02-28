package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class ItemPredicateParser {
	private final ItemSyntaxParser syntax;

	public ItemPredicateParser(HolderLookup.Provider provider) {
		this.syntax = new ItemSyntaxParser(provider, true);
	}

	public Predicate<ItemStack> parse(StringReader stringReader) throws CommandSyntaxException {
		final List<Predicate<ItemStack>> list = new ArrayList();
		final DataComponentPredicate.Builder builder = DataComponentPredicate.builder();
		this.syntax.parse(stringReader, new ItemSyntaxParser.Visitor() {
			@Override
			public void visitItem(Holder<Item> holder) {
				list.add((Predicate)itemStack -> itemStack.is(holder));
			}

			@Override
			public void visitTag(HolderSet<Item> holderSet) {
				list.add((Predicate)itemStack -> itemStack.is(holderSet));
			}

			@Override
			public <T> void visitComponent(DataComponentType<T> dataComponentType, T object) {
				builder.expect(dataComponentType, object);
			}

			@Override
			public void visitCustomData(CompoundTag compoundTag) {
				list.add(CustomData.itemMatcher(DataComponents.CUSTOM_DATA, compoundTag));
			}
		});
		DataComponentPredicate dataComponentPredicate = builder.build();
		if (!dataComponentPredicate.alwaysMatches()) {
			list.add(dataComponentPredicate::test);
		}

		return Util.allOf(list);
	}

	public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder) {
		return this.syntax.fillSuggestions(suggestionsBuilder);
	}
}
