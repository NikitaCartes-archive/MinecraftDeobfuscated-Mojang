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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

public class ItemArgument implements ArgumentType<ItemInput> {
	private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "stick{foo=bar}");
	private final HolderLookup<Item> items;

	public ItemArgument(CommandBuildContext commandBuildContext) {
		this.items = commandBuildContext.holderLookup(Registry.ITEM_REGISTRY);
	}

	public static ItemArgument item(CommandBuildContext commandBuildContext) {
		return new ItemArgument(commandBuildContext);
	}

	public ItemInput parse(StringReader stringReader) throws CommandSyntaxException {
		ItemParser.ItemResult itemResult = ItemParser.parseForItem(this.items, stringReader);
		return new ItemInput(itemResult.item(), itemResult.nbt());
	}

	public static <S> ItemInput getItem(CommandContext<S> commandContext, String string) {
		return commandContext.getArgument(string, ItemInput.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return ItemParser.fillSuggestions(this.items, suggestionsBuilder, false);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
