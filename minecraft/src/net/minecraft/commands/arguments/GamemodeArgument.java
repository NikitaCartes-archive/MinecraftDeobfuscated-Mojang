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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;

public class GamemodeArgument implements ArgumentType<GameType> {
	private static final Collection<String> EXAMPLES = Arrays.asList("survival", "creative");
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_GAMEMODE = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("gamemode.gamemodeNotFound", object)
	);

	public static GamemodeArgument gamemode() {
		return new GamemodeArgument();
	}

	public static GameType getGamemode(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, GameType.class);
	}

	public GameType parse(StringReader stringReader) throws CommandSyntaxException {
		String string = ResourceLocation.read(stringReader).getPath();
		GameType gameType = GameType.byName(string, null);
		if (gameType == null) {
			throw ERROR_UNKNOWN_GAMEMODE.create(string);
		} else {
			return gameType;
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggest(Arrays.stream(GameType.values()).map(GameType::getName), suggestionsBuilder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
