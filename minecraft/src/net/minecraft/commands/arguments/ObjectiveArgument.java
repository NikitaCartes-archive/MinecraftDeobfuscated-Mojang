package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class ObjectiveArgument implements ArgumentType<String> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "*", "012");
	private static final DynamicCommandExceptionType ERROR_OBJECTIVE_NOT_FOUND = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("arguments.objective.notFound", object)
	);
	private static final DynamicCommandExceptionType ERROR_OBJECTIVE_READ_ONLY = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("arguments.objective.readonly", object)
	);

	public static ObjectiveArgument objective() {
		return new ObjectiveArgument();
	}

	public static Objective getObjective(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		String string2 = commandContext.getArgument(string, String.class);
		Scoreboard scoreboard = commandContext.getSource().getServer().getScoreboard();
		Objective objective = scoreboard.getObjective(string2);
		if (objective == null) {
			throw ERROR_OBJECTIVE_NOT_FOUND.create(string2);
		} else {
			return objective;
		}
	}

	public static Objective getWritableObjective(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		Objective objective = getObjective(commandContext, string);
		if (objective.getCriteria().isReadOnly()) {
			throw ERROR_OBJECTIVE_READ_ONLY.create(objective.getName());
		} else {
			return objective;
		}
	}

	public String parse(StringReader stringReader) throws CommandSyntaxException {
		return stringReader.readUnquotedString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		S object = commandContext.getSource();
		if (object instanceof CommandSourceStack commandSourceStack) {
			return SharedSuggestionProvider.suggest(commandSourceStack.getServer().getScoreboard().getObjectiveNames(), suggestionsBuilder);
		} else {
			return object instanceof SharedSuggestionProvider sharedSuggestionProvider ? sharedSuggestionProvider.customSuggestion(commandContext) : Suggestions.empty();
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
