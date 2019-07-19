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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class ObjectiveArgument implements ArgumentType<String> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "*", "012");
	private static final DynamicCommandExceptionType ERROR_OBJECTIVE_NOT_FOUND = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("arguments.objective.notFound", object)
	);
	private static final DynamicCommandExceptionType ERROR_OBJECTIVE_READ_ONLY = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("arguments.objective.readonly", object)
	);
	public static final DynamicCommandExceptionType ERROR_OBJECTIVE_NAME_TOO_LONG = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.scoreboard.objectives.add.longName", object)
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
		String string = stringReader.readUnquotedString();
		if (string.length() > 16) {
			throw ERROR_OBJECTIVE_NAME_TOO_LONG.create(16);
		} else {
			return string;
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		if (commandContext.getSource() instanceof CommandSourceStack) {
			return SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getServer().getScoreboard().getObjectiveNames(), suggestionsBuilder);
		} else if (commandContext.getSource() instanceof SharedSuggestionProvider) {
			SharedSuggestionProvider sharedSuggestionProvider = (SharedSuggestionProvider)commandContext.getSource();
			return sharedSuggestionProvider.customSuggestion(commandContext, suggestionsBuilder);
		} else {
			return Suggestions.empty();
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
