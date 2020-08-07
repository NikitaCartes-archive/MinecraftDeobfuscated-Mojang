package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

public class TeamMsgCommand {
	private static final Style SUGGEST_STYLE = Style.EMPTY
		.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.type.team.hover")))
		.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
	private static final SimpleCommandExceptionType ERROR_NOT_ON_TEAM = new SimpleCommandExceptionType(new TranslatableComponent("commands.teammsg.failed.noteam"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(
			Commands.literal("teammsg")
				.then(
					Commands.argument("message", MessageArgument.message())
						.executes(commandContext -> sendMessage(commandContext.getSource(), MessageArgument.getMessage(commandContext, "message")))
				)
		);
		commandDispatcher.register(Commands.literal("tm").redirect(literalCommandNode));
	}

	private static int sendMessage(CommandSourceStack commandSourceStack, Component component) throws CommandSyntaxException {
		Entity entity = commandSourceStack.getEntityOrException();
		PlayerTeam playerTeam = (PlayerTeam)entity.getTeam();
		if (playerTeam == null) {
			throw ERROR_NOT_ON_TEAM.create();
		} else {
			Component component2 = playerTeam.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
			List<ServerPlayer> list = commandSourceStack.getServer().getPlayerList().getPlayers();

			for (ServerPlayer serverPlayer : list) {
				if (serverPlayer == entity) {
					serverPlayer.sendMessage(new TranslatableComponent("chat.type.team.sent", component2, commandSourceStack.getDisplayName(), component), entity.getUUID());
				} else if (serverPlayer.getTeam() == playerTeam) {
					serverPlayer.sendMessage(new TranslatableComponent("chat.type.team.text", component2, commandSourceStack.getDisplayName(), component), entity.getUUID());
				}
			}

			return list.size();
		}
	}
}
