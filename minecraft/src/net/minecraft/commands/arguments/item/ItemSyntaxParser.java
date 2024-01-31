package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ItemSyntaxParser {
	static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.item.id.invalid", object)
	);
	static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("arguments.item.tag.unknown", object)
	);
	private static final char SYNTAX_TAG = '#';
	private static final char SYNTAX_START_NBT = '{';
	static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
	final HolderLookup.RegistryLookup<Item> items;
	final boolean acceptTags;

	public ItemSyntaxParser(HolderLookup.Provider provider, boolean bl) {
		this.items = provider.lookupOrThrow(Registries.ITEM);
		this.acceptTags = bl;
	}

	public void parse(StringReader stringReader, ItemSyntaxParser.Visitor visitor) throws CommandSyntaxException {
		int i = stringReader.getCursor();

		try {
			new ItemSyntaxParser.State(stringReader, visitor).parse();
		} catch (CommandSyntaxException var5) {
			stringReader.setCursor(i);
			throw var5;
		}
	}

	public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder) {
		StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
		stringReader.setCursor(suggestionsBuilder.getStart());
		ItemSyntaxParser.SuggestionsVisitor suggestionsVisitor = new ItemSyntaxParser.SuggestionsVisitor();
		ItemSyntaxParser.State state = new ItemSyntaxParser.State(stringReader, suggestionsVisitor);

		try {
			state.parse();
		} catch (CommandSyntaxException var6) {
		}

		return suggestionsVisitor.resolveSuggestions(suggestionsBuilder, stringReader);
	}

	class State {
		private final StringReader reader;
		private final ItemSyntaxParser.Visitor visitor;

		State(StringReader stringReader, ItemSyntaxParser.Visitor visitor) {
			this.reader = stringReader;
			this.visitor = visitor;
		}

		public void parse() throws CommandSyntaxException {
			this.visitor.visitSuggestions(ItemSyntaxParser.this.acceptTags ? this::suggestItemIdOrTag : this::suggestItem);
			if (ItemSyntaxParser.this.acceptTags && this.reader.canRead() && this.reader.peek() == '#') {
				this.readTag();
			} else {
				this.readItem();
			}

			this.visitor.visitSuggestions(this::suggestStartNbt);
			if (this.reader.canRead() && this.reader.peek() == '{') {
				this.visitor.visitSuggestions(ItemSyntaxParser.SUGGEST_NOTHING);
				this.readNbt();
			}
		}

		private void readItem() throws CommandSyntaxException {
			int i = this.reader.getCursor();
			ResourceLocation resourceLocation = ResourceLocation.read(this.reader);
			this.visitor.visitItem((Holder<Item>)ItemSyntaxParser.this.items.get(ResourceKey.create(Registries.ITEM, resourceLocation)).orElseThrow(() -> {
				this.reader.setCursor(i);
				return ItemSyntaxParser.ERROR_UNKNOWN_ITEM.createWithContext(this.reader, resourceLocation);
			}));
		}

		private void readTag() throws CommandSyntaxException {
			int i = this.reader.getCursor();
			this.reader.expect('#');
			this.visitor.visitSuggestions(this::suggestTag);
			ResourceLocation resourceLocation = ResourceLocation.read(this.reader);
			HolderSet<Item> holderSet = (HolderSet<Item>)ItemSyntaxParser.this.items.get(TagKey.create(Registries.ITEM, resourceLocation)).orElseThrow(() -> {
				this.reader.setCursor(i);
				return ItemSyntaxParser.ERROR_UNKNOWN_TAG.createWithContext(this.reader, resourceLocation);
			});
			this.visitor.visitTag(holderSet);
		}

		private void readNbt() throws CommandSyntaxException {
			this.visitor.visitSuggestions(ItemSyntaxParser.SUGGEST_NOTHING);
			this.visitor.visitNbt(new TagParser(this.reader).readStruct());
		}

		private CompletableFuture<Suggestions> suggestStartNbt(SuggestionsBuilder suggestionsBuilder) {
			if (suggestionsBuilder.getRemaining().isEmpty()) {
				suggestionsBuilder.suggest(String.valueOf('{'));
			}

			return suggestionsBuilder.buildFuture();
		}

		private CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder suggestionsBuilder) {
			return SharedSuggestionProvider.suggestResource(ItemSyntaxParser.this.items.listElementIds().map(ResourceKey::location), suggestionsBuilder);
		}

		private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder suggestionsBuilder) {
			return SharedSuggestionProvider.suggestResource(ItemSyntaxParser.this.items.listTagIds().map(TagKey::location), suggestionsBuilder, String.valueOf('#'));
		}

		private CompletableFuture<Suggestions> suggestItemIdOrTag(SuggestionsBuilder suggestionsBuilder) {
			this.suggestTag(suggestionsBuilder);
			return this.suggestItem(suggestionsBuilder);
		}
	}

	static class SuggestionsVisitor implements ItemSyntaxParser.Visitor {
		private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = ItemSyntaxParser.SUGGEST_NOTHING;

		@Override
		public void visitSuggestions(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> function) {
			this.suggestions = function;
		}

		public CompletableFuture<Suggestions> resolveSuggestions(SuggestionsBuilder suggestionsBuilder, StringReader stringReader) {
			return (CompletableFuture<Suggestions>)this.suggestions.apply(suggestionsBuilder.createOffset(stringReader.getCursor()));
		}
	}

	public interface Visitor {
		default void visitItem(Holder<Item> holder) {
		}

		default void visitTag(HolderSet<Item> holderSet) {
		}

		default void visitNbt(CompoundTag compoundTag) {
		}

		default void visitSuggestions(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> function) {
		}
	}
}
