package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.chase.ChaseClient;
import net.minecraft.server.chase.ChaseServer;

public class ChaseCommand {
	private static ChaseServer chaseServer;
	private static ChaseClient chaseClient;

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("chase")
				.executes(commandContext -> chase(commandContext.getSource(), "localhost", 10000))
				.then(
					Commands.literal("me")
						.executes(commandContext -> me(commandContext.getSource(), 10000))
						.then(
							Commands.argument("port", IntegerArgumentType.integer())
								.executes(commandContext -> me(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "port")))
						)
				)
				.then(
					Commands.argument("host", StringArgumentType.string())
						.executes(commandContext -> chase(commandContext.getSource(), StringArgumentType.getString(commandContext, "host"), 10000))
						.then(
							Commands.argument("port", IntegerArgumentType.integer())
								.executes(
									commandContext -> chase(
											commandContext.getSource(), StringArgumentType.getString(commandContext, "host"), IntegerArgumentType.getInteger(commandContext, "port")
										)
								)
						)
				)
				.then(Commands.literal("stop").executes(commandContext -> stop(commandContext.getSource())))
		);
	}

	private static int stop(CommandSourceStack commandSourceStack) {
		if (chaseClient != null) {
			chaseClient.stop();
			commandSourceStack.sendSuccess(new TextComponent("You will now stop chasing"), false);
			chaseClient = null;
		}

		if (chaseServer != null) {
			chaseServer.stop();
			commandSourceStack.sendSuccess(new TextComponent("You will now stop being chased"), false);
			chaseServer = null;
		}

		return 0;
	}

	private static int me(CommandSourceStack commandSourceStack, int i) {
		if (chaseServer != null) {
			commandSourceStack.sendFailure(new TextComponent("Chase server is already running. Stop it using /chase stop"));
			return 0;
		} else {
			chaseServer = new ChaseServer(i, commandSourceStack.getServer().getPlayerList(), 100);

			try {
				chaseServer.start();
				commandSourceStack.sendSuccess(new TextComponent("Chase server is now running on port " + i + ". Clients can follow you using /chase <ip> <port>"), false);
			} catch (IOException var3) {
				var3.printStackTrace();
				commandSourceStack.sendFailure(new TextComponent("Failed to start chase server on port " + i));
				chaseServer = null;
			}

			return 0;
		}
	}

	private static int chase(CommandSourceStack commandSourceStack, String string, int i) {
		if (chaseClient != null) {
			commandSourceStack.sendFailure(new TextComponent("You are already chasing someone. Stop it using /chase stop"));
			return 0;
		} else {
			chaseClient = new ChaseClient(string, i, commandSourceStack.getServer());
			chaseClient.start();
			commandSourceStack.sendSuccess(
				new TextComponent(
					"You are now chasing "
						+ string
						+ ":"
						+ i
						+ ". If that server does '/chase me' then you will automatically go to the same position. Use '/chase stop' to stop chasing."
				),
				false
			);
			return 0;
		}
	}
}
