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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPredicateParser {
	private final ItemSyntaxParser syntax;

	public ItemPredicateParser(HolderLookup.Provider provider) {
		this.syntax = new ItemSyntaxParser(provider, true);
	}

	public Predicate<ItemStack> parse(StringReader stringReader) throws CommandSyntaxException {
		final List<Predicate<ItemStack>> list = new ArrayList();
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
			public void visitNbt(CompoundTag compoundTag) {
				if (!compoundTag.isEmpty()) {
					list.add((Predicate)itemStack -> {
						CompoundTag compoundTag2 = itemStack.getTag();
						return NbtUtils.compareNbt(compoundTag, compoundTag2, true);
					});
				}
			}
		});
		return Util.allOf(list);
	}

	public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder) {
		return this.syntax.fillSuggestions(suggestionsBuilder);
	}
}
