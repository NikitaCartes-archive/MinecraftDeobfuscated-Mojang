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
import net.minecraft.world.scores.DisplaySlot;

public class ScoreboardSlotArgument implements ArgumentType<DisplaySlot> {
	private static final Collection<String> EXAMPLES = Arrays.asList("sidebar", "foo.bar");
	public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.scoreboardDisplaySlot.invalid", object)
	);

	private ScoreboardSlotArgument() {
	}

	public static ScoreboardSlotArgument displaySlot() {
		return new ScoreboardSlotArgument();
	}

	public static DisplaySlot getDisplaySlot(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, DisplaySlot.class);
	}

	public DisplaySlot parse(StringReader stringReader) throws CommandSyntaxException {
		String string = stringReader.readUnquotedString();
		DisplaySlot displaySlot = (DisplaySlot)DisplaySlot.CODEC.byName(string);
		if (displaySlot == null) {
			throw ERROR_INVALID_VALUE.createWithContext(stringReader, string);
		} else {
			return displaySlot;
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggest(Arrays.stream(DisplaySlot.values()).map(DisplaySlot::getSerializedName), suggestionsBuilder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
