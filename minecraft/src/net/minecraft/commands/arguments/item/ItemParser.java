package net.minecraft.commands.arguments.item;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.Property;

public class ItemParser {
	public static final SimpleCommandExceptionType ERROR_NO_TAGS_ALLOWED = new SimpleCommandExceptionType(
		new TranslatableComponent("argument.item.tag.disallowed")
	);
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("argument.item.id.invalid", object)
	);
	private static final BiFunction<SuggestionsBuilder, TagCollection<Item>, CompletableFuture<Suggestions>> SUGGEST_NOTHING = (suggestionsBuilder, tagCollection) -> suggestionsBuilder.buildFuture();
	private final StringReader reader;
	private final boolean forTesting;
	private final Map<Property<?>, Comparable<?>> properties = Maps.<Property<?>, Comparable<?>>newHashMap();
	private Item item;
	@Nullable
	private CompoundTag nbt;
	private ResourceLocation tag = new ResourceLocation("");
	private int tagCursor;
	private BiFunction<SuggestionsBuilder, TagCollection<Item>, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

	public ItemParser(StringReader stringReader, boolean bl) {
		this.reader = stringReader;
		this.forTesting = bl;
	}

	public Item getItem() {
		return this.item;
	}

	@Nullable
	public CompoundTag getNbt() {
		return this.nbt;
	}

	public ResourceLocation getTag() {
		return this.tag;
	}

	public void readItem() throws CommandSyntaxException {
		int i = this.reader.getCursor();
		ResourceLocation resourceLocation = ResourceLocation.read(this.reader);
		this.item = (Item)Registry.ITEM.getOptional(resourceLocation).orElseThrow(() -> {
			this.reader.setCursor(i);
			return ERROR_UNKNOWN_ITEM.createWithContext(this.reader, resourceLocation.toString());
		});
	}

	public void readTag() throws CommandSyntaxException {
		if (!this.forTesting) {
			throw ERROR_NO_TAGS_ALLOWED.create();
		} else {
			this.suggestions = this::suggestTag;
			this.reader.expect('#');
			this.tagCursor = this.reader.getCursor();
			this.tag = ResourceLocation.read(this.reader);
		}
	}

	public void readNbt() throws CommandSyntaxException {
		this.nbt = new TagParser(this.reader).readStruct();
	}

	public ItemParser parse() throws CommandSyntaxException {
		this.suggestions = this::suggestItemIdOrTag;
		if (this.reader.canRead() && this.reader.peek() == '#') {
			this.readTag();
		} else {
			this.readItem();
			this.suggestions = this::suggestOpenNbt;
		}

		if (this.reader.canRead() && this.reader.peek() == '{') {
			this.suggestions = SUGGEST_NOTHING;
			this.readNbt();
		}

		return this;
	}

	private CompletableFuture<Suggestions> suggestOpenNbt(SuggestionsBuilder suggestionsBuilder, TagCollection<Item> tagCollection) {
		if (suggestionsBuilder.getRemaining().isEmpty()) {
			suggestionsBuilder.suggest(String.valueOf('{'));
		}

		return suggestionsBuilder.buildFuture();
	}

	private CompletableFuture<Suggestions> suggestTag(SuggestionsBuilder suggestionsBuilder, TagCollection<Item> tagCollection) {
		return SharedSuggestionProvider.suggestResource(tagCollection.getAvailableTags(), suggestionsBuilder.createOffset(this.tagCursor));
	}

	private CompletableFuture<Suggestions> suggestItemIdOrTag(SuggestionsBuilder suggestionsBuilder, TagCollection<Item> tagCollection) {
		if (this.forTesting) {
			SharedSuggestionProvider.suggestResource(tagCollection.getAvailableTags(), suggestionsBuilder, String.valueOf('#'));
		}

		return SharedSuggestionProvider.suggestResource(Registry.ITEM.keySet(), suggestionsBuilder);
	}

	public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder, TagCollection<Item> tagCollection) {
		return (CompletableFuture<Suggestions>)this.suggestions.apply(suggestionsBuilder.createOffset(this.reader.getCursor()), tagCollection);
	}
}
