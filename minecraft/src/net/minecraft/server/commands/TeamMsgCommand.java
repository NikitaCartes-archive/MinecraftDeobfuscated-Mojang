package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

public class TeamMsgCommand {
	private static final Style SUGGEST_STYLE = Style.EMPTY
		.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.type.team.hover")))
		.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
	private static final SimpleCommandExceptionType ERROR_NOT_ON_TEAM = new SimpleCommandExceptionType(Component.translatable("commands.teammsg.failed.noteam"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(
			Commands.literal("teammsg")
				.then(
					Commands.argument("message", MessageArgument.message())
						.executes(
							commandContext -> {
								CommandSourceStack commandSourceStack = commandContext.getSource();
								Entity entity = commandSourceStack.getEntityOrException();
								PlayerTeam playerTeam = (PlayerTeam)entity.getTeam();
								if (playerTeam == null) {
									throw ERROR_NOT_ON_TEAM.create();
								} else {
									List<ServerPlayer> list = commandSourceStack.getServer()
										.getPlayerList()
										.getPlayers()
										.stream()
										.filter(serverPlayer -> serverPlayer == entity || serverPlayer.getTeam() == playerTeam)
										.toList();
									if (!list.isEmpty()) {
										MessageArgument.resolveChatMessage(
											commandContext, "message", playerChatMessage -> sendMessage(commandSourceStack, entity, playerTeam, list, playerChatMessage)
										);
									}

									return list.size();
								}
							}
						)
				)
		);
		commandDispatcher.register(Commands.literal("tm").redirect(literalCommandNode));
	}

	private static void sendMessage(
		CommandSourceStack commandSourceStack, Entity entity, PlayerTeam playerTeam, List<ServerPlayer> list, PlayerChatMessage playerChatMessage
	) {
		Component component = playerTeam.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
		ChatType.Bound bound = ChatType.bind(ChatType.TEAM_MSG_COMMAND_INCOMING, commandSourceStack).withTargetName(component);
		ChatType.Bound bound2 = ChatType.bind(ChatType.TEAM_MSG_COMMAND_OUTGOING, commandSourceStack).withTargetName(component);
		OutgoingChatMessage outgoingChatMessage = OutgoingChatMessage.create(playerChatMessage);
		boolean bl = false;

		for (ServerPlayer serverPlayer : list) {
			ChatType.Bound bound3 = serverPlayer == entity ? bound2 : bound;
			boolean bl2 = commandSourceStack.shouldFilterMessageTo(serverPlayer);
			serverPlayer.sendChatMessage(outgoingChatMessage, bl2, bound3);
			bl |= bl2 && playerChatMessage.isFullyFiltered();
		}

		if (bl) {
			commandSourceStack.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
		}
	}
}
