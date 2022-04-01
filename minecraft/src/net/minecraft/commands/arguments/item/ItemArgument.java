package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Registry;

public class ItemArgument implements ArgumentType<ItemInput> {
	private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "stick{foo=bar}");

	public static ItemArgument item() {
		return new ItemArgument();
	}

	public ItemInput parse(StringReader stringReader) throws CommandSyntaxException {
		ItemParser itemParser = new ItemParser(stringReader, false).parse();
		return new ItemInput(itemParser.getItem(), itemParser.getNbt());
	}

	public static <S> ItemInput getItem(CommandContext<S> commandContext, String string) {
		return commandContext.getArgument(string, ItemInput.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
		stringReader.setCursor(suggestionsBuilder.getStart());
		ItemParser itemParser = new ItemParser(stringReader, false);

		try {
			itemParser.parse();
		} catch (CommandSyntaxException var6) {
		}

		return itemParser.fillSuggestions(suggestionsBuilder, Registry.ITEM);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
