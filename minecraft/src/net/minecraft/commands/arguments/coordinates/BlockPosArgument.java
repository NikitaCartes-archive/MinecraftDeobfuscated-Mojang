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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class BlockPosArgument implements ArgumentType<Coordinates> {
	private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "~0.5 ~1 ~-5");
	public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType(Component.translatable("argument.pos.unloaded"));
	public static final SimpleCommandExceptionType ERROR_OUT_OF_WORLD = new SimpleCommandExceptionType(Component.translatable("argument.pos.outofworld"));
	public static final SimpleCommandExceptionType ERROR_OUT_OF_BOUNDS = new SimpleCommandExceptionType(Component.translatable("argument.pos.outofbounds"));

	public static BlockPosArgument blockPos() {
		return new BlockPosArgument();
	}

	public static BlockPos getLoadedBlockPos(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		ServerLevel serverLevel = commandContext.getSource().getLevel();
		return getLoadedBlockPos(commandContext, serverLevel, string);
	}

	public static BlockPos getLoadedBlockPos(CommandContext<CommandSourceStack> commandContext, ServerLevel serverLevel, String string) throws CommandSyntaxException {
		BlockPos blockPos = getBlockPos(commandContext, string);
		if (!serverLevel.hasChunkAt(blockPos)) {
			throw ERROR_NOT_LOADED.create();
		} else if (!serverLevel.isInWorldBounds(blockPos)) {
			throw ERROR_OUT_OF_WORLD.create();
		} else {
			return blockPos;
		}
	}

	public static BlockPos getBlockPos(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.<Coordinates>getArgument(string, Coordinates.class).getBlockPos(commandContext.getSource());
	}

	public static BlockPos getSpawnablePos(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		BlockPos blockPos = getBlockPos(commandContext, string);
		if (!Level.isInSpawnableBounds(blockPos)) {
			throw ERROR_OUT_OF_BOUNDS.create();
		} else {
			return blockPos;
		}
	}

	public Coordinates parse(StringReader stringReader) throws CommandSyntaxException {
		return (Coordinates)(stringReader.canRead() && stringReader.peek() == '^' ? LocalCoordinates.parse(stringReader) : WorldCoordinates.parseInt(stringReader));
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

			return SharedSuggestionProvider.suggestCoordinates(string, collection, suggestionsBuilder, Commands.createValidator(this::parse));
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
