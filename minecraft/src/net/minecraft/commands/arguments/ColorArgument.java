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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class ColorArgument implements ArgumentType<ChatFormatting> {
	private static final Collection<String> EXAMPLES = Arrays.asList("red", "green");
	public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.color.invalid", object)
	);

	private ColorArgument() {
	}

	public static ColorArgument color() {
		return new ColorArgument();
	}

	public static ChatFormatting getColor(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, ChatFormatting.class);
	}

	public ChatFormatting parse(StringReader stringReader) throws CommandSyntaxException {
		String string = stringReader.readUnquotedString();
		ChatFormatting chatFormatting = ChatFormatting.getByName(string);
		if (chatFormatting != null && !chatFormatting.isFormat()) {
			return chatFormatting;
		} else {
			throw ERROR_INVALID_VALUE.createWithContext(stringReader, string);
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggest(ChatFormatting.getNames(true, false), suggestionsBuilder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
