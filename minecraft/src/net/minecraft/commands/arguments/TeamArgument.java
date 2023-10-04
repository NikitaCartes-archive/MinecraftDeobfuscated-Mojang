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
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class TeamArgument implements ArgumentType<String> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "123");
	private static final DynamicCommandExceptionType ERROR_TEAM_NOT_FOUND = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("team.notFound", object)
	);

	public static TeamArgument team() {
		return new TeamArgument();
	}

	public static PlayerTeam getTeam(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		String string2 = commandContext.getArgument(string, String.class);
		Scoreboard scoreboard = commandContext.getSource().getServer().getScoreboard();
		PlayerTeam playerTeam = scoreboard.getPlayerTeam(string2);
		if (playerTeam == null) {
			throw ERROR_TEAM_NOT_FOUND.create(string2);
		} else {
			return playerTeam;
		}
	}

	public String parse(StringReader stringReader) throws CommandSyntaxException {
		return stringReader.readUnquotedString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return commandContext.getSource() instanceof SharedSuggestionProvider
			? SharedSuggestionProvider.suggest(((SharedSuggestionProvider)commandContext.getSource()).getAllTeams(), suggestionsBuilder)
			: Suggestions.empty();
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
