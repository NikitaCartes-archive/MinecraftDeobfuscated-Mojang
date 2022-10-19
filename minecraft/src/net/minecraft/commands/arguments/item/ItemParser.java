package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ItemParser {
	private static final SimpleCommandExceptionType ERROR_NO_TAGS_ALLOWED = new SimpleCommandExceptionType(Component.translatable("argument.item.tag.disallowed"));
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(
		object -> Component.translatable("argument.item.id.invalid", object)
	);
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
		object -> Component.translatable("arguments.item.tag.unknown", object)
	);
	private static final char SYNTAX_START_NBT = '{';
	private static final char SYNTAX_TAG = '#';
	private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
	private final HolderLookup<Item> items;
	private final StringReader reader;
	private final boolean allowTags;
	private Either<Holder<Item>, HolderSet<Item>> result;
	@Nullable
	private CompoundTag nbt;
	private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

	private ItemParser(HolderLookup<Item> holderLookup, StringReader stringReader, boolean bl) {
		this.items = holderLookup;
		this.reader = stringReader;
		this.allowTags = bl;
	}

	public static ItemParser.ItemResult parseForItem(HolderLookup<Item> holderLookup, StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();

		try {
			ItemParser itemParser = new ItemParser(holderLookup, stringReader, false);
			itemParser.parse();
			Holder<Item> holder = (Holder<Item>)itemParser.result.left().orElseThrow(() -> new IllegalStateException("Parser returned unexpected tag name"));
			return new ItemParser.ItemResult(holder, itemParser.nbt);
		} catch (CommandSyntaxException var5) {
			stringReader.setCursor(i);
			throw var5;
		}
	}

	public static Either<ItemParser.ItemResult, ItemParser.TagResult> parseForTesting(HolderLookup<Item> holderLookup, StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();

		try {
			ItemParser itemParser = new ItemParser(holderLookup, stringReader, true);
			itemParser.parse();
			return itemParser.result
				.mapBoth(holder -> new ItemParser.ItemResult(holder, itemParser.nbt), holderSet -> new ItemParser.TagResult(holderSet, itemParser.nbt));
		} catch (CommandSyntaxException var4) {
			stringReader.setCursor(i);
			throw var4;
		}
	}

	public static CompletableFuture<Suggestions> fillSuggestions(HolderLookup<Item> holderLookup, SuggestionsBuilder suggestionsBuilder, boolean bl) {
		StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
		stringReader.setCursor(suggestionsBuilder.getStart());
		ItemParser itemParser = new ItemParser(holderLookup, stringReader, bl);

		try {
			itemParser.parse();
		} catch (CommandSyntaxException var6) {
		}

		return (CompletableFuture<Suggestions>)itemParser.suggestions.apply(suggestionsBuilder.createOffset(stringReader.getCursor()));
	}

	private void readItem() throws CommandSyntaxException {
		int i = this.reader.getCursor();
		ResourceLocation resourceLocation = ResourceLocation.read(this.reader);
		Optional<? extends Holder<Item>> optional = this.items.get(ResourceKey.create(Registry.ITEM_REGISTRY, resourceLocation));
		this.result = Either.left((Holder<Item>)optional.orElseThrow(() -> {
			this.reader.setCursor(i);
			return ERROR_UNKNOWN_ITEM.createWithContext(this.reader, resourceLocation);
		}));
	}

	private void readTag() throws CommandSyntaxException {
		if (!this.allowTags) {
			throw ERROR_NO_TAGS_ALLOWED.createWithContext(this.reader);
		} else {
			int i = this.reader.getCursor();
			this.reader.expect('#');
			this.suggestions = this::suggestTag;
			ResourceLocation resourceLocation = ResourceLocation.read(this.reader);
			Optional<? extends HolderSet<Item>> optional = this.items.get(TagKey.create(Registry.ITEM_REGISTRY, resourceLocation));
			this.result = Either.right((HolderSet<Item>)optional.orElseThrow(() -> {
				this.reader.setCursor(i);
				return ERROR_UNKNOWN_TAG.createWithContext(this.reader, resourceLocation);
			}));
		}
	}

	private void readNbt() throws CommandSyntaxException {
		this.nbt = new TagParser(this.reader).readStruct();
	}

	private void parse() throws CommandSyntaxException {
		if (this.allowTags) {
			this.suggestions = this::suggestItemIdOrTag;
		} else {
			this.suggestions = this::suggestItem;
		}

		if (this.reader.canRead() && this.reader.peek() == '#') {
			this.readTag();
		} else {
			this.readItem();
		}

		this.suggestions = this::suggestOpenNbt;
		if (this.reader.canRead() && this.reader.peek() == '{') {
			this.suggestions = SUGGEST_NOTHING;
			this.readNbt();
		}
	}

	private CompletableFuture<Suggestions> suggestOpenNbt(SuggestionsBuilder suggestionsBuilder) {
		if (suggestionsBuilder.getRemaining().isEmpty()) {
			suggestionsBuilder.suggest(String.valueOf('{'));
		}

		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggestResource(this.items.listTags().map(TagKey::location), suggestionsBuilder, String.valueOf('#'));
	}

	private CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggestResource(this.items.listElements().map(ResourceKey::location), suggestionsBuilder);
	}

	private CompletableFuture<Suggestions> suggestItemIdOrTag(SuggestionsBuilder suggestionsBuilder) {
		this.suggestTag(suggestionsBuilder);
		return this.suggestItem(suggestionsBuilder);
	}

	public static record ItemResult(Holder<Item> item, @Nullable CompoundTag nbt) {
	}

	public static record TagResult(HolderSet<Item> tag, @Nullable CompoundTag nbt) {
	}
}
