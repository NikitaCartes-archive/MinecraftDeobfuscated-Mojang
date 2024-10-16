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
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableObject;

public class ItemParser {
	static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.item.id.invalid", object)
	);
	static final DynamicCommandExceptionType ERROR_UNKNOWN_COMPONENT = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("arguments.item.component.unknown", object)
	);
	static final Dynamic2CommandExceptionType ERROR_MALFORMED_COMPONENT = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("arguments.item.component.malformed", object, object2)
	);
	static final SimpleCommandExceptionType ERROR_EXPECTED_COMPONENT = new SimpleCommandExceptionType(Component.translatable("arguments.item.component.expected"));
	static final DynamicCommandExceptionType ERROR_REPEATED_COMPONENT = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("arguments.item.component.repeated", object)
	);
	private static final DynamicCommandExceptionType ERROR_MALFORMED_ITEM = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("arguments.item.malformed", object)
	);
	public static final char SYNTAX_START_COMPONENTS = '[';
	public static final char SYNTAX_END_COMPONENTS = ']';
	public static final char SYNTAX_COMPONENT_SEPARATOR = ',';
	public static final char SYNTAX_COMPONENT_ASSIGNMENT = '=';
	public static final char SYNTAX_REMOVED_COMPONENT = '!';
	static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
	final HolderLookup.RegistryLookup<Item> items;
	final DynamicOps<Tag> registryOps;

	public ItemParser(HolderLookup.Provider provider) {
		this.items = provider.lookupOrThrow(Registries.ITEM);
		this.registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
	}

	public ItemParser.ItemResult parse(StringReader stringReader) throws CommandSyntaxException {
		final MutableObject<Holder<Item>> mutableObject = new MutableObject<>();
		final DataComponentPatch.Builder builder = DataComponentPatch.builder();
		this.parse(stringReader, new ItemParser.Visitor() {
			@Override
			public void visitItem(Holder<Item> holder) {
				mutableObject.setValue(holder);
			}

			@Override
			public <T> void visitComponent(DataComponentType<T> dataComponentType, T object) {
				builder.set(dataComponentType, object);
			}

			@Override
			public <T> void visitRemovedComponent(DataComponentType<T> dataComponentType) {
				builder.remove(dataComponentType);
			}
		});
		Holder<Item> holder = (Holder<Item>)Objects.requireNonNull(mutableObject.getValue(), "Parser gave no item");
		DataComponentPatch dataComponentPatch = builder.build();
		validateComponents(stringReader, holder, dataComponentPatch);
		return new ItemParser.ItemResult(holder, dataComponentPatch);
	}

	private static void validateComponents(StringReader stringReader, Holder<Item> holder, DataComponentPatch dataComponentPatch) throws CommandSyntaxException {
		DataComponentMap dataComponentMap = PatchedDataComponentMap.fromPatch(holder.value().components(), dataComponentPatch);
		DataResult<Unit> dataResult = ItemStack.validateComponents(dataComponentMap);
		dataResult.getOrThrow(string -> ERROR_MALFORMED_ITEM.createWithContext(stringReader, string));
	}

	public void parse(StringReader stringReader, ItemParser.Visitor visitor) throws CommandSyntaxException {
		int i = stringReader.getCursor();

		try {
			new ItemParser.State(stringReader, visitor).parse();
		} catch (CommandSyntaxException var5) {
			stringReader.setCursor(i);
			throw var5;
		}
	}

	public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder) {
		StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
		stringReader.setCursor(suggestionsBuilder.getStart());
		ItemParser.SuggestionsVisitor suggestionsVisitor = new ItemParser.SuggestionsVisitor();
		ItemParser.State state = new ItemParser.State(stringReader, suggestionsVisitor);

		try {
			state.parse();
		} catch (CommandSyntaxException var6) {
		}

		return suggestionsVisitor.resolveSuggestions(suggestionsBuilder, stringReader);
	}

	public static record ItemResult(Holder<Item> item, DataComponentPatch components) {
	}

	class State {
		private final StringReader reader;
		private final ItemParser.Visitor visitor;

		State(final StringReader stringReader, final ItemParser.Visitor visitor) {
			this.reader = stringReader;
			this.visitor = visitor;
		}

		public void parse() throws CommandSyntaxException {
			this.visitor.visitSuggestions(this::suggestItem);
			this.readItem();
			this.visitor.visitSuggestions(this::suggestStartComponents);
			if (this.reader.canRead() && this.reader.peek() == '[') {
				this.visitor.visitSuggestions(ItemParser.SUGGEST_NOTHING);
				this.readComponents();
			}
		}

		private void readItem() throws CommandSyntaxException {
			int i = this.reader.getCursor();
			ResourceLocation resourceLocation = ResourceLocation.read(this.reader);
			this.visitor.visitItem((Holder<Item>)ItemParser.this.items.get(ResourceKey.create(Registries.ITEM, resourceLocation)).orElseThrow(() -> {
				this.reader.setCursor(i);
				return ItemParser.ERROR_UNKNOWN_ITEM.createWithContext(this.reader, resourceLocation);
			}));
		}

		private void readComponents() throws CommandSyntaxException {
			this.reader.expect('[');
			this.visitor.visitSuggestions(this::suggestComponentAssignmentOrRemoval);
			Set<DataComponentType<?>> set = new ReferenceArraySet<>();

			while (this.reader.canRead() && this.reader.peek() != ']') {
				this.reader.skipWhitespace();
				if (this.reader.canRead() && this.reader.peek() == '!') {
					this.reader.skip();
					this.visitor.visitSuggestions(this::suggestComponent);
					DataComponentType<?> dataComponentType = readComponentType(this.reader);
					if (!set.add(dataComponentType)) {
						throw ItemParser.ERROR_REPEATED_COMPONENT.create(dataComponentType);
					}

					this.visitor.visitRemovedComponent(dataComponentType);
					this.visitor.visitSuggestions(ItemParser.SUGGEST_NOTHING);
					this.reader.skipWhitespace();
				} else {
					DataComponentType<?> dataComponentType = readComponentType(this.reader);
					if (!set.add(dataComponentType)) {
						throw ItemParser.ERROR_REPEATED_COMPONENT.create(dataComponentType);
					}

					this.visitor.visitSuggestions(this::suggestAssignment);
					this.reader.skipWhitespace();
					this.reader.expect('=');
					this.visitor.visitSuggestions(ItemParser.SUGGEST_NOTHING);
					this.reader.skipWhitespace();
					this.readComponent(dataComponentType);
					this.reader.skipWhitespace();
				}

				this.visitor.visitSuggestions(this::suggestNextOrEndComponents);
				if (!this.reader.canRead() || this.reader.peek() != ',') {
					break;
				}

				this.reader.skip();
				this.reader.skipWhitespace();
				this.visitor.visitSuggestions(this::suggestComponentAssignmentOrRemoval);
				if (!this.reader.canRead()) {
					throw ItemParser.ERROR_EXPECTED_COMPONENT.createWithContext(this.reader);
				}
			}

			this.reader.expect(']');
			this.visitor.visitSuggestions(ItemParser.SUGGEST_NOTHING);
		}

		public static DataComponentType<?> readComponentType(StringReader stringReader) throws CommandSyntaxException {
			if (!stringReader.canRead()) {
				throw ItemParser.ERROR_EXPECTED_COMPONENT.createWithContext(stringReader);
			} else {
				int i = stringReader.getCursor();
				ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
				DataComponentType<?> dataComponentType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(resourceLocation);
				if (dataComponentType != null && !dataComponentType.isTransient()) {
					return dataComponentType;
				} else {
					stringReader.setCursor(i);
					throw ItemParser.ERROR_UNKNOWN_COMPONENT.createWithContext(stringReader, resourceLocation);
				}
			}
		}

		private <T> void readComponent(DataComponentType<T> dataComponentType) throws CommandSyntaxException {
			int i = this.reader.getCursor();
			Tag tag = new TagParser(this.reader).readValue();
			DataResult<T> dataResult = dataComponentType.codecOrThrow().parse(ItemParser.this.registryOps, tag);
			this.visitor.visitComponent(dataComponentType, dataResult.getOrThrow(string -> {
				this.reader.setCursor(i);
				return ItemParser.ERROR_MALFORMED_COMPONENT.createWithContext(this.reader, dataComponentType.toString(), string);
			}));
		}

		private CompletableFuture<Suggestions> suggestStartComponents(SuggestionsBuilder suggestionsBuilder) {
			if (suggestionsBuilder.getRemaining().isEmpty()) {
				suggestionsBuilder.suggest(String.valueOf('['));
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
			return SharedSuggestionProvider.suggestResource(ItemParser.this.items.listElementIds().map(ResourceKey::location), suggestionsBuilder);
		}

		private CompletableFuture<Suggestions> suggestComponentAssignmentOrRemoval(SuggestionsBuilder suggestionsBuilder) {
			suggestionsBuilder.suggest(String.valueOf('!'));
			return this.suggestComponent(suggestionsBuilder, String.valueOf('='));
		}

		private CompletableFuture<Suggestions> suggestComponent(SuggestionsBuilder suggestionsBuilder) {
			return this.suggestComponent(suggestionsBuilder, "");
		}

		private CompletableFuture<Suggestions> suggestComponent(SuggestionsBuilder suggestionsBuilder, String string) {
			String string2 = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
			SharedSuggestionProvider.filterResources(
				BuiltInRegistries.DATA_COMPONENT_TYPE.entrySet(), string2, entry -> ((ResourceKey)entry.getKey()).location(), entry -> {
					DataComponentType<?> dataComponentType = (DataComponentType<?>)entry.getValue();
					if (dataComponentType.codec() != null) {
						ResourceLocation resourceLocation = ((ResourceKey)entry.getKey()).location();
						suggestionsBuilder.suggest(resourceLocation + string);
					}
				}
			);
			return suggestionsBuilder.buildFuture();
		}
	}

	static class SuggestionsVisitor implements ItemParser.Visitor {
		private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = ItemParser.SUGGEST_NOTHING;

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

		default <T> void visitComponent(DataComponentType<T> dataComponentType, T object) {
		}

		default <T> void visitRemovedComponent(DataComponentType<T> dataComponentType) {
		}

		default void visitSuggestions(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> function) {
		}
	}
}
