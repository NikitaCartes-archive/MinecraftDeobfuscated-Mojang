package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;

public class DifficultyCommand {
	private static final DynamicCommandExceptionType ERROR_ALREADY_DIFFICULT = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.difficulty.failure", object)
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("difficulty");

		for (Difficulty difficulty : Difficulty.values()) {
			literalArgumentBuilder.then(Commands.literal(difficulty.getKey()).executes(commandContext -> setDifficulty(commandContext.getSource(), difficulty)));
		}

		commandDispatcher.register(literalArgumentBuilder.requires(commandSourceStack -> commandSourceStack.hasPermission(2)).executes(commandContext -> {
			Difficulty difficultyx = commandContext.getSource().getLevel().getDifficulty();
			commandContext.getSource().sendSuccess(new TranslatableComponent("commands.difficulty.query", difficultyx.getDisplayName()), false);
			return difficultyx.getId();
		}));
	}

	public static int setDifficulty(CommandSourceStack commandSourceStack, Difficulty difficulty) throws CommandSyntaxException {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		if (minecraftServer.getWorldData().getDifficulty() == difficulty) {
			throw ERROR_ALREADY_DIFFICULT.create(difficulty.getKey());
		} else {
			minecraftServer.setDifficulty(difficulty, true);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.difficulty.success", difficulty.getDisplayName()), true);
			return 0;
		}
	}
}
