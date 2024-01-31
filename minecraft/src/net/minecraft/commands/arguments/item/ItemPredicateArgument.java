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
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.ItemStack;

public class ItemPredicateArgument implements ArgumentType<ItemPredicateArgument.Result> {
	private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo:'bar'}");
	private final ItemPredicateParser parser;

	public ItemPredicateArgument(CommandBuildContext commandBuildContext) {
		this.parser = new ItemPredicateParser(commandBuildContext);
	}

	public static ItemPredicateArgument itemPredicate(CommandBuildContext commandBuildContext) {
		return new ItemPredicateArgument(commandBuildContext);
	}

	public ItemPredicateArgument.Result parse(StringReader stringReader) throws CommandSyntaxException {
		Predicate<ItemStack> predicate = this.parser.parse(stringReader);
		return predicate::test;
	}

	public static ItemPredicateArgument.Result getItemPredicate(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, ItemPredicateArgument.Result.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return this.parser.fillSuggestions(suggestionsBuilder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public interface Result extends Predicate<ItemStack> {
	}
}
