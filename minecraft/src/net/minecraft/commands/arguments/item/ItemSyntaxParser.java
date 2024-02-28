package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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
	static final DynamicCommandExceptionType ERROR_UNKNOWN_COMPONENT = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("arguments.item.component.unknown", object)
	);
	static final Dynamic2CommandExceptionType ERROR_MALFORMED_COMPONENT = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("arguments.item.component.malformed", object, object2)
	);
	static final SimpleCommandExceptionType ERROR_EXPECTED_COMPONENT = new SimpleCommandExceptionType(Component.translatable("arguments.item.component.expected"));
	private static final char SYNTAX_TAG = '#';
	public static final char SYNTAX_START_COMPONENTS = '[';
	public static final char SYNTAX_END_COMPONENTS = ']';
	public static final char SYNTAX_COMPONENT_SEPARATOR = ',';
	public static final char SYNTAX_COMPONENT_ASSIGNMENT = '=';
	public static final char SYNTAX_START_NBT = '{';
	static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
	final HolderLookup.RegistryLookup<Item> items;
	final DynamicOps<Tag> registryOps;
	final boolean acceptTags;

	public ItemSyntaxParser(HolderLookup.Provider provider, boolean bl) {
		this.items = provider.lookupOrThrow(Registries.ITEM);
		this.registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
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
		private boolean hasComponents;

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

			this.visitor.visitSuggestions(this::suggestStartComponentsOrNbt);
			if (this.reader.canRead() && this.reader.peek() == '[') {
				this.readComponents();
				this.hasComponents = true;
			}

			this.visitor.visitSuggestions(this::suggestStartComponentsOrNbt);
			if (this.reader.canRead() && this.reader.peek() == '{') {
				this.readCustomData();
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

		private void readComponents() throws CommandSyntaxException {
			this.reader.expect('[');
			this.visitor.visitSuggestions(this::suggestComponentAssignment);

			while (this.reader.canRead() && this.reader.peek() != ']') {
				this.reader.skipWhitespace();
				DataComponentType<?> dataComponentType = readComponentType(this.reader);
				this.visitor.visitSuggestions(this::suggestAssignment);
				this.reader.skipWhitespace();
				this.reader.expect('=');
				this.visitor.visitSuggestions(ItemSyntaxParser.SUGGEST_NOTHING);
				this.reader.skipWhitespace();
				this.readComponent(dataComponentType);
				this.reader.skipWhitespace();
				this.visitor.visitSuggestions(this::suggestNextOrEndComponents);
				if (!this.reader.canRead() || this.reader.peek() != ',') {
					break;
				}

				this.reader.skip();
				this.reader.skipWhitespace();
				this.visitor.visitSuggestions(this::suggestComponentAssignment);
				if (!this.reader.canRead()) {
					throw ItemSyntaxParser.ERROR_EXPECTED_COMPONENT.createWithContext(this.reader);
				}
			}

			this.reader.expect(']');
			this.visitor.visitSuggestions(ItemSyntaxParser.SUGGEST_NOTHING);
		}

		public static DataComponentType<?> readComponentType(StringReader stringReader) throws CommandSyntaxException {
			if (!stringReader.canRead()) {
				throw ItemSyntaxParser.ERROR_EXPECTED_COMPONENT.createWithContext(stringReader);
			} else {
				int i = stringReader.getCursor();
				ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
				DataComponentType<?> dataComponentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(resourceLocation);
				if (dataComponentType != null && !dataComponentType.isTransient()) {
					return dataComponentType;
				} else {
					stringReader.setCursor(i);
					throw ItemSyntaxParser.ERROR_UNKNOWN_COMPONENT.createWithContext(stringReader, resourceLocation);
				}
			}
		}

		private <T> void readComponent(DataComponentType<T> dataComponentType) throws CommandSyntaxException {
			int i = this.reader.getCursor();
			Tag tag = new TagParser(this.reader).readValue();
			DataResult<T> dataResult = dataComponentType.codecOrThrow().parse(ItemSyntaxParser.this.registryOps, tag);
			this.visitor.visitComponent(dataComponentType, Util.getOrThrow(dataResult, string -> {
				this.reader.setCursor(i);
				return ItemSyntaxParser.ERROR_MALFORMED_COMPONENT.createWithContext(this.reader, dataComponentType.toString(), string);
			}));
		}

		private void readCustomData() throws CommandSyntaxException {
			this.visitor.visitSuggestions(ItemSyntaxParser.SUGGEST_NOTHING);
			this.visitor.visitCustomData(new TagParser(this.reader).readStruct());
		}

		private CompletableFuture<Suggestions> suggestStartComponentsOrNbt(SuggestionsBuilder suggestionsBuilder) {
			if (suggestionsBuilder.getRemaining().isEmpty()) {
				if (!this.hasComponents) {
					suggestionsBuilder.suggest(String.valueOf('['));
				}

				suggestionsBuilder.suggest(String.valueOf('{'));
			}

			return suggestionsBuilder.buildFuture();
		}

		private CompletableFuture<Suggestions> suggestNextOrEndComponents(SuggestionsBuilder suggestionsBuilder) {
			if (suggestionsBuilder.getRemaining().isEmpty()) {
				suggestionsBuilder.suggest(String.valueOf(','));
				suggestionsBuilder.suggest(String.valueOf(']'));
			}

			return suggestionsBuilder.buildFuture();
		}

		private CompletableFuture<Suggestions> suggestAssignment(SuggestionsBuilder suggestionsBuilder) {
			if (suggestionsBuilder.getRemaining().isEmpty()) {
				suggestionsBuilder.suggest(String.valueOf('='));
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

		private CompletableFuture<Suggestions> suggestComponentAssignment(SuggestionsBuilder suggestionsBuilder) {
			String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
			SharedSuggestionProvider.filterResources(
				BuiltInRegistries.DATA_COMPONENT_TYPE.entrySet(), string, entry -> ((ResourceKey)entry.getKey()).location(), entry -> {
					DataComponentType<?> dataComponentType = (DataComponentType<?>)entry.getValue();
					if (dataComponentType.codec() != null) {
						ResourceLocation resourceLocation = ((ResourceKey)entry.getKey()).location();
						suggestionsBuilder.suggest(resourceLocation.toString() + "=");
					}
				}
			);
			return suggestionsBuilder.buildFuture();
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

		default <T> void visitComponent(DataComponentType<T> dataComponentType, T object) {
		}

		default void visitCustomData(CompoundTag compoundTag) {
		}

		default void visitSuggestions(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> function) {
		}
	}
}
