package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.Score;

public class OperationArgument implements ArgumentType<OperationArgument.Operation> {
	private static final Collection<String> EXAMPLES = Arrays.asList("=", ">", "<");
	private static final SimpleCommandExceptionType ERROR_INVALID_OPERATION = new SimpleCommandExceptionType(
		new TranslatableComponent("arguments.operation.invalid")
	);
	private static final SimpleCommandExceptionType ERROR_DIVIDE_BY_ZERO = new SimpleCommandExceptionType(new TranslatableComponent("arguments.operation.div0"));

	public static OperationArgument operation() {
		return new OperationArgument();
	}

	public static OperationArgument.Operation getOperation(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return commandContext.getArgument(string, OperationArgument.Operation.class);
	}

	public OperationArgument.Operation parse(StringReader stringReader) throws CommandSyntaxException {
		if (!stringReader.canRead()) {
			throw ERROR_INVALID_OPERATION.create();
		} else {
			int i = stringReader.getCursor();

			while (stringReader.canRead() && stringReader.peek() != ' ') {
				stringReader.skip();
			}

			return getOperation(stringReader.getString().substring(i, stringReader.getCursor()));
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggest(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, suggestionsBuilder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	private static OperationArgument.Operation getOperation(String string) throws CommandSyntaxException {
		return (OperationArgument.Operation)(string.equals("><") ? (score, score2) -> {
			int i = score.getScore();
			score.setScore(score2.getScore());
			score2.setScore(i);
		} : getSimpleOperation(string));
	}

	private static OperationArgument.SimpleOperation getSimpleOperation(String string) throws CommandSyntaxException {
		switch (string) {
			case "=":
				return (i, j) -> j;
			case "+=":
				return (i, j) -> i + j;
			case "-=":
				return (i, j) -> i - j;
			case "*=":
				return (i, j) -> i * j;
			case "/=":
				return (i, j) -> {
					if (j == 0) {
						throw ERROR_DIVIDE_BY_ZERO.create();
					} else {
						return Mth.intFloorDiv(i, j);
					}
				};
			case "%=":
				return (i, j) -> {
					if (j == 0) {
						throw ERROR_DIVIDE_BY_ZERO.create();
					} else {
						return Mth.positiveModulo(i, j);
					}
				};
			case "<":
				return Math::min;
			case ">":
				return Math::max;
			default:
				throw ERROR_INVALID_OPERATION.create();
		}
	}

	@FunctionalInterface
	public interface Operation {
		void apply(Score score, Score score2) throws CommandSyntaxException;
	}

	@FunctionalInterface
	interface SimpleOperation extends OperationArgument.Operation {
		int apply(int i, int j) throws CommandSyntaxException;

		@Override
		default void apply(Score score, Score score2) throws CommandSyntaxException {
			score.setScore(this.apply(score.getScore(), score2.getScore()));
		}
	}
}
