package net.minecraft.commands.arguments.blocks;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStateParser {
	public static final SimpleCommandExceptionType ERROR_NO_TAGS_ALLOWED = new SimpleCommandExceptionType(Component.translatable("argument.block.tag.disallowed"));
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_BLOCK = new DynamicCommandExceptionType(
		object -> Component.translatable("argument.block.id.invalid", object)
	);
	public static final Dynamic2CommandExceptionType ERROR_UNKNOWN_PROPERTY = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatable("argument.block.property.unknown", object, object2)
	);
	public static final Dynamic2CommandExceptionType ERROR_DUPLICATE_PROPERTY = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatable("argument.block.property.duplicate", object2, object)
	);
	public static final Dynamic3CommandExceptionType ERROR_INVALID_VALUE = new Dynamic3CommandExceptionType(
		(object, object2, object3) -> Component.translatable("argument.block.property.invalid", object, object3, object2)
	);
	public static final Dynamic2CommandExceptionType ERROR_EXPECTED_VALUE = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatable("argument.block.property.novalue", object, object2)
	);
	public static final SimpleCommandExceptionType ERROR_EXPECTED_END_OF_PROPERTIES = new SimpleCommandExceptionType(
		Component.translatable("argument.block.property.unclosed")
	);
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
		object -> Component.translatable("arguments.block.tag.unknown", object)
	);
	private static final char SYNTAX_START_PROPERTIES = '[';
	private static final char SYNTAX_START_NBT = '{';
	private static final char SYNTAX_END_PROPERTIES = ']';
	private static final char SYNTAX_EQUALS = '=';
	private static final char SYNTAX_PROPERTY_SEPARATOR = ',';
	private static final char SYNTAX_TAG = '#';
	private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
	private final HolderLookup<Block> blocks;
	private final StringReader reader;
	private final boolean forTesting;
	private final boolean allowNbt;
	private final Map<Property<?>, Comparable<?>> properties = Maps.<Property<?>, Comparable<?>>newHashMap();
	private final Map<String, String> vagueProperties = Maps.<String, String>newHashMap();
	private ResourceLocation id = new ResourceLocation("");
	@Nullable
	private StateDefinition<Block, BlockState> definition;
	@Nullable
	private BlockState state;
	@Nullable
	private CompoundTag nbt;
	@Nullable
	private HolderSet<Block> tag;
	private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

	private BlockStateParser(HolderLookup<Block> holderLookup, StringReader stringReader, boolean bl, boolean bl2) {
		this.blocks = holderLookup;
		this.reader = stringReader;
		this.forTesting = bl;
		this.allowNbt = bl2;
	}

	public static BlockStateParser.BlockResult parseForBlock(HolderLookup<Block> holderLookup, String string, boolean bl) throws CommandSyntaxException {
		return parseForBlock(holderLookup, new StringReader(string), bl);
	}

	public static BlockStateParser.BlockResult parseForBlock(HolderLookup<Block> holderLookup, StringReader stringReader, boolean bl) throws CommandSyntaxException {
		int i = stringReader.getCursor();

		try {
			BlockStateParser blockStateParser = new BlockStateParser(holderLookup, stringReader, false, bl);
			blockStateParser.parse();
			return new BlockStateParser.BlockResult(blockStateParser.state, blockStateParser.properties, blockStateParser.nbt);
		} catch (CommandSyntaxException var5) {
			stringReader.setCursor(i);
			throw var5;
		}
	}

	public static Either<BlockStateParser.BlockResult, BlockStateParser.TagResult> parseForTesting(HolderLookup<Block> holderLookup, String string, boolean bl) throws CommandSyntaxException {
		return parseForTesting(holderLookup, new StringReader(string), bl);
	}

	public static Either<BlockStateParser.BlockResult, BlockStateParser.TagResult> parseForTesting(
		HolderLookup<Block> holderLookup, StringReader stringReader, boolean bl
	) throws CommandSyntaxException {
		int i = stringReader.getCursor();

		try {
			BlockStateParser blockStateParser = new BlockStateParser(holderLookup, stringReader, true, bl);
			blockStateParser.parse();
			return blockStateParser.tag != null
				? Either.right(new BlockStateParser.TagResult(blockStateParser.tag, blockStateParser.vagueProperties, blockStateParser.nbt))
				: Either.left(new BlockStateParser.BlockResult(blockStateParser.state, blockStateParser.properties, blockStateParser.nbt));
		} catch (CommandSyntaxException var5) {
			stringReader.setCursor(i);
			throw var5;
		}
	}

	public static CompletableFuture<Suggestions> fillSuggestions(HolderLookup<Block> holderLookup, SuggestionsBuilder suggestionsBuilder, boolean bl, boolean bl2) {
		StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
		stringReader.setCursor(suggestionsBuilder.getStart());
		BlockStateParser blockStateParser = new BlockStateParser(holderLookup, stringReader, bl, bl2);

		try {
			blockStateParser.parse();
		} catch (CommandSyntaxException var7) {
		}

		return (CompletableFuture<Suggestions>)blockStateParser.suggestions.apply(suggestionsBuilder.createOffset(stringReader.getCursor()));
	}

	private void parse() throws CommandSyntaxException {
		if (this.forTesting) {
			this.suggestions = this::suggestBlockIdOrTag;
		} else {
			this.suggestions = this::suggestItem;
		}

		if (this.reader.canRead() && this.reader.peek() == '#') {
			this.readTag();
			this.suggestions = this::suggestOpenVaguePropertiesOrNbt;
			if (this.reader.canRead() && this.reader.peek() == '[') {
				this.readVagueProperties();
				this.suggestions = this::suggestOpenNbt;
			}
		} else {
			this.readBlock();
			this.suggestions = this::suggestOpenPropertiesOrNbt;
			if (this.reader.canRead() && this.reader.peek() == '[') {
				this.readProperties();
				this.suggestions = this::suggestOpenNbt;
			}
		}

		if (this.allowNbt && this.reader.canRead() && this.reader.peek() == '{') {
			this.suggestions = SUGGEST_NOTHING;
			this.readNbt();
		}
	}

	private CompletableFuture<Suggestions> suggestPropertyNameOrEnd(SuggestionsBuilder suggestionsBuilder) {
		if (suggestionsBuilder.getRemaining().isEmpty()) {
			suggestionsBuilder.suggest(String.valueOf(']'));
		}

		return this.suggestPropertyName(suggestionsBuilder);
	}

	private CompletableFuture<Suggestions> suggestVaguePropertyNameOrEnd(SuggestionsBuilder suggestionsBuilder) {
		if (suggestionsBuilder.getRemaining().isEmpty()) {
			suggestionsBuilder.suggest(String.valueOf(']'));
		}

		return this.suggestVaguePropertyName(suggestionsBuilder);
	}

	private CompletableFuture<Suggestions> suggestPropertyName(SuggestionsBuilder suggestionsBuilder) {
		String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);

		for (Property<?> property : this.state.getProperties()) {
			if (!this.properties.containsKey(property) && property.getName().startsWith(string)) {
				suggestionsBuilder.suggest(property.getName() + "=");
			}
		}

		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestVaguePropertyName(SuggestionsBuilder suggestionsBuilder) {
		String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
		if (this.tag != null) {
			for (Holder<Block> holder : this.tag) {
				for (Property<?> property : holder.value().getStateDefinition().getProperties()) {
					if (!this.vagueProperties.containsKey(property.getName()) && property.getName().startsWith(string)) {
						suggestionsBuilder.suggest(property.getName() + "=");
					}
				}
			}
		}

		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestOpenNbt(SuggestionsBuilder suggestionsBuilder) {
		if (suggestionsBuilder.getRemaining().isEmpty() && this.hasBlockEntity()) {
			suggestionsBuilder.suggest(String.valueOf('{'));
		}

		return suggestionsBuilder.buildFuture();
	}

	private boolean hasBlockEntity() {
		if (this.state != null) {
			return this.state.hasBlockEntity();
		} else {
			if (this.tag != null) {
				for (Holder<Block> holder : this.tag) {
					if (holder.value().defaultBlockState().hasBlockEntity()) {
						return true;
					}
				}
			}

			return false;
		}
	}

	private CompletableFuture<Suggestions> suggestEquals(SuggestionsBuilder suggestionsBuilder) {
		if (suggestionsBuilder.getRemaining().isEmpty()) {
			suggestionsBuilder.suggest(String.valueOf('='));
		}

		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestNextPropertyOrEnd(SuggestionsBuilder suggestionsBuilder) {
		if (suggestionsBuilder.getRemaining().isEmpty()) {
			suggestionsBuilder.suggest(String.valueOf(']'));
		}

		if (suggestionsBuilder.getRemaining().isEmpty() && this.properties.size() < this.state.getProperties().size()) {
			suggestionsBuilder.suggest(String.valueOf(','));
		}

		return suggestionsBuilder.buildFuture();
	}

	private static <T extends Comparable<T>> SuggestionsBuilder addSuggestions(SuggestionsBuilder suggestionsBuilder, Property<T> property) {
		for (T comparable : property.getPossibleValues()) {
			if (comparable instanceof Integer integer) {
				suggestionsBuilder.suggest(integer);
			} else {
				suggestionsBuilder.suggest(property.getName(comparable));
			}
		}

		return suggestionsBuilder;
	}

	private CompletableFuture<Suggestions> suggestVaguePropertyValue(SuggestionsBuilder suggestionsBuilder, String string) {
		boolean bl = false;
		if (this.tag != null) {
			for (Holder<Block> holder : this.tag) {
				Block block = holder.value();
				Property<?> property = block.getStateDefinition().getProperty(string);
				if (property != null) {
					addSuggestions(suggestionsBuilder, property);
				}

				if (!bl) {
					for (Property<?> property2 : block.getStateDefinition().getProperties()) {
						if (!this.vagueProperties.containsKey(property2.getName())) {
							bl = true;
							break;
						}
					}
				}
			}
		}

		if (bl) {
			suggestionsBuilder.suggest(String.valueOf(','));
		}

		suggestionsBuilder.suggest(String.valueOf(']'));
		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestOpenVaguePropertiesOrNbt(SuggestionsBuilder suggestionsBuilder) {
		if (suggestionsBuilder.getRemaining().isEmpty() && this.tag != null) {
			boolean bl = false;
			boolean bl2 = false;

			for (Holder<Block> holder : this.tag) {
				Block block = holder.value();
				bl |= !block.getStateDefinition().getProperties().isEmpty();
				bl2 |= block.defaultBlockState().hasBlockEntity();
				if (bl && bl2) {
					break;
				}
			}

			if (bl) {
				suggestionsBuilder.suggest(String.valueOf('['));
			}

			if (bl2) {
				suggestionsBuilder.suggest(String.valueOf('{'));
			}
		}

		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestOpenPropertiesOrNbt(SuggestionsBuilder suggestionsBuilder) {
		if (suggestionsBuilder.getRemaining().isEmpty()) {
			if (!this.definition.getProperties().isEmpty()) {
				suggestionsBuilder.suggest(String.valueOf('['));
			}

			if (this.state.hasBlockEntity()) {
				suggestionsBuilder.suggest(String.valueOf('{'));
			}
		}

		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggestResource(this.blocks.listTagIds().map(TagKey::location), suggestionsBuilder, String.valueOf('#'));
	}

	private CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggestResource(this.blocks.listElementIds().map(ResourceKey::location), suggestionsBuilder);
	}

	private CompletableFuture<Suggestions> suggestBlockIdOrTag(SuggestionsBuilder suggestionsBuilder) {
		this.suggestTag(suggestionsBuilder);
		this.suggestItem(suggestionsBuilder);
		return suggestionsBuilder.buildFuture();
	}

	private void readBlock() throws CommandSyntaxException {
		int i = this.reader.getCursor();
		this.id = ResourceLocation.read(this.reader);
		Block block = (Block)((Holder.Reference)this.blocks.get(ResourceKey.create(Registries.BLOCK, this.id)).orElseThrow(() -> {
			this.reader.setCursor(i);
			return ERROR_UNKNOWN_BLOCK.createWithContext(this.reader, this.id.toString());
		})).value();
		this.definition = block.getStateDefinition();
		this.state = block.defaultBlockState();
	}

	private void readTag() throws CommandSyntaxException {
		if (!this.forTesting) {
			throw ERROR_NO_TAGS_ALLOWED.createWithContext(this.reader);
		} else {
			int i = this.reader.getCursor();
			this.reader.expect('#');
			this.suggestions = this::suggestTag;
			ResourceLocation resourceLocation = ResourceLocation.read(this.reader);
			this.tag = (HolderSet<Block>)this.blocks.get(TagKey.create(Registries.BLOCK, resourceLocation)).orElseThrow(() -> {
				this.reader.setCursor(i);
				return ERROR_UNKNOWN_TAG.createWithContext(this.reader, resourceLocation.toString());
			});
		}
	}

	private void readProperties() throws CommandSyntaxException {
		this.reader.skip();
		this.suggestions = this::suggestPropertyNameOrEnd;
		this.reader.skipWhitespace();

		while (this.reader.canRead() && this.reader.peek() != ']') {
			this.reader.skipWhitespace();
			int i = this.reader.getCursor();
			String string = this.reader.readString();
			Property<?> property = this.definition.getProperty(string);
			if (property == null) {
				this.reader.setCursor(i);
				throw ERROR_UNKNOWN_PROPERTY.createWithContext(this.reader, this.id.toString(), string);
			}

			if (this.properties.containsKey(property)) {
				this.reader.setCursor(i);
				throw ERROR_DUPLICATE_PROPERTY.createWithContext(this.reader, this.id.toString(), string);
			}

			this.reader.skipWhitespace();
			this.suggestions = this::suggestEquals;
			if (!this.reader.canRead() || this.reader.peek() != '=') {
				throw ERROR_EXPECTED_VALUE.createWithContext(this.reader, this.id.toString(), string);
			}

			this.reader.skip();
			this.reader.skipWhitespace();
			this.suggestions = suggestionsBuilder -> addSuggestions(suggestionsBuilder, property).buildFuture();
			int j = this.reader.getCursor();
			this.setValue(property, this.reader.readString(), j);
			this.suggestions = this::suggestNextPropertyOrEnd;
			this.reader.skipWhitespace();
			if (this.reader.canRead()) {
				if (this.reader.peek() != ',') {
					if (this.reader.peek() != ']') {
						throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
					}
					break;
				}

				this.reader.skip();
				this.suggestions = this::suggestPropertyName;
			}
		}

		if (this.reader.canRead()) {
			this.reader.skip();
		} else {
			throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
		}
	}

	private void readVagueProperties() throws CommandSyntaxException {
		this.reader.skip();
		this.suggestions = this::suggestVaguePropertyNameOrEnd;
		int i = -1;
		this.reader.skipWhitespace();

		while (this.reader.canRead() && this.reader.peek() != ']') {
			this.reader.skipWhitespace();
			int j = this.reader.getCursor();
			String string = this.reader.readString();
			if (this.vagueProperties.containsKey(string)) {
				this.reader.setCursor(j);
				throw ERROR_DUPLICATE_PROPERTY.createWithContext(this.reader, this.id.toString(), string);
			}

			this.reader.skipWhitespace();
			if (!this.reader.canRead() || this.reader.peek() != '=') {
				this.reader.setCursor(j);
				throw ERROR_EXPECTED_VALUE.createWithContext(this.reader, this.id.toString(), string);
			}

			this.reader.skip();
			this.reader.skipWhitespace();
			this.suggestions = suggestionsBuilder -> this.suggestVaguePropertyValue(suggestionsBuilder, string);
			i = this.reader.getCursor();
			String string2 = this.reader.readString();
			this.vagueProperties.put(string, string2);
			this.reader.skipWhitespace();
			if (this.reader.canRead()) {
				i = -1;
				if (this.reader.peek() != ',') {
					if (this.reader.peek() != ']') {
						throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
					}
					break;
				}

				this.reader.skip();
				this.suggestions = this::suggestVaguePropertyName;
			}
		}

		if (this.reader.canRead()) {
			this.reader.skip();
		} else {
			if (i >= 0) {
				this.reader.setCursor(i);
			}

			throw ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(this.reader);
		}
	}

	private void readNbt() throws CommandSyntaxException {
		this.nbt = new TagParser(this.reader).readStruct();
	}

	private <T extends Comparable<T>> void setValue(Property<T> property, String string, int i) throws CommandSyntaxException {
		Optional<T> optional = property.getValue(string);
		if (optional.isPresent()) {
			this.state = this.state.setValue(property, (Comparable)optional.get());
			this.properties.put(property, (Comparable)optional.get());
		} else {
			this.reader.setCursor(i);
			throw ERROR_INVALID_VALUE.createWithContext(this.reader, this.id.toString(), property.getName(), string);
		}
	}

	public static String serialize(BlockState blockState) {
		StringBuilder stringBuilder = new StringBuilder(
			(String)blockState.getBlockHolder().unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("air")
		);
		if (!blockState.getProperties().isEmpty()) {
			stringBuilder.append('[');
			boolean bl = false;

			for (Entry<Property<?>, Comparable<?>> entry : blockState.getValues().entrySet()) {
				if (bl) {
					stringBuilder.append(',');
				}

				appendProperty(stringBuilder, (Property)entry.getKey(), (Comparable<?>)entry.getValue());
				bl = true;
			}

			stringBuilder.append(']');
		}

		return stringBuilder.toString();
	}

	private static <T extends Comparable<T>> void appendProperty(StringBuilder stringBuilder, Property<T> property, Comparable<?> comparable) {
		stringBuilder.append(property.getName());
		stringBuilder.append('=');
		stringBuilder.append(property.getName((T)comparable));
	}

	public static record BlockResult(BlockState blockState, Map<Property<?>, Comparable<?>> properties, @Nullable CompoundTag nbt) {
	}

	public static record TagResult(HolderSet<Block> tag, Map<String, String> vagueProperties, @Nullable CompoundTag nbt) {
	}
}
