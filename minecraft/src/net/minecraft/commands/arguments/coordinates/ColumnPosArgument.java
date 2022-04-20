package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;

public class ColumnPosArgument implements ArgumentType<Coordinates> {
	private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "~1 ~-2", "^ ^", "^-1 ^0");
	public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.translatable("argument.pos2d.incomplete"));

	public static ColumnPosArgument columnPos() {
		return new ColumnPosArgument();
	}

	public static ColumnPos getColumnPos(CommandContext<CommandSourceStack> commandContext, String string) {
		BlockPos blockPos = commandContext.<Coordinates>getArgument(string, Coordinates.class).getBlockPos(commandContext.getSource());
		return new ColumnPos(blockPos.getX(), blockPos.getZ());
	}

	public Coordinates parse(StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();
		if (!stringReader.canRead()) {
			throw ERROR_NOT_COMPLETE.createWithContext(stringReader);
		} else {
			WorldCoordinate worldCoordinate = WorldCoordinate.parseInt(stringReader);
			if (stringReader.canRead() && stringReader.peek() == ' ') {
				stringReader.skip();
				WorldCoordinate worldCoordinate2 = WorldCoordinate.parseInt(stringReader);
				return new WorldCoordinates(worldCoordinate, new WorldCoordinate(true, 0.0), worldCoordinate2);
			} else {
				stringReader.setCursor(i);
				throw ERROR_NOT_COMPLETE.createWithContext(stringReader);
			}
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		if (!(commandContext.getSource() instanceof SharedSuggestionProvider)) {
			return Suggestions.empty();
		} else {
			String string = suggestionsBuilder.getRemaining();
			Collection<SharedSuggestionProvider.TextCoordinates> collection;
			if (!string.isEmpty() && string.charAt(0) == '^') {
				collection = Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL);
			} else {
				collection = ((SharedSuggestionProvider)commandContext.getSource()).getRelevantCoordinates();
			}

			return SharedSuggestionProvider.suggest2DCoordinates(string, collection, suggestionsBuilder, Commands.createValidator(this::parse));
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
