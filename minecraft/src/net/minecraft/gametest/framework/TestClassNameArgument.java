package net.minecraft.gametest.framework;

import com.mojang.brigadier.Message;
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
import net.minecraft.network.chat.TextComponent;

public class TestClassNameArgument implements ArgumentType<String> {
	private static final Collection<String> EXAMPLES = Arrays.asList("techtests", "mobtests");

	public String parse(StringReader stringReader) throws CommandSyntaxException {
		String string = stringReader.readUnquotedString();
		if (GameTestRegistry.isTestClass(string)) {
			return string;
		} else {
			Message message = new TextComponent("No such test class: " + string);
			throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
		}
	}

	public static TestClassNameArgument testClassName() {
		return new TestClassNameArgument();
	}

	public static String getTestClassName(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, String.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggest(GameTestRegistry.getAllTestClassNames().stream(), suggestionsBuilder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
