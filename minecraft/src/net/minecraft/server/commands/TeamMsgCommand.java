package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
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
						.executes(commandContext -> sendMessage(commandContext.getSource(), MessageArgument.getChatMessage(commandContext, "message")))
				)
		);
		commandDispatcher.register(Commands.literal("tm").redirect(literalCommandNode));
	}

	private static int sendMessage(CommandSourceStack commandSourceStack, MessageArgument.ChatMessage chatMessage) throws CommandSyntaxException {
		Entity entity = commandSourceStack.getEntityOrException();
		PlayerTeam playerTeam = (PlayerTeam)entity.getTeam();
		if (playerTeam == null) {
			throw ERROR_NOT_ON_TEAM.create();
		} else {
			Component component = playerTeam.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
			ChatSender chatSender = commandSourceStack.asChatSender().withTeamName(component);
			List<ServerPlayer> list = commandSourceStack.getServer()
				.getPlayerList()
				.getPlayers()
				.stream()
				.filter(serverPlayer -> serverPlayer == entity || serverPlayer.getTeam() == playerTeam)
				.toList();
			if (list.isEmpty()) {
				return 0;
			} else {
				chatMessage.resolve(commandSourceStack)
					.thenAcceptAsync(
						filteredText -> {
							for (ServerPlayer serverPlayer : list) {
								if (serverPlayer == entity) {
									serverPlayer.sendSystemMessage(
										Component.translatable("chat.type.team.sent", component, commandSourceStack.getDisplayName(), ((PlayerChatMessage)filteredText.raw()).serverContent())
									);
								} else {
									PlayerChatMessage playerChatMessage = (PlayerChatMessage)filteredText.filter(commandSourceStack, serverPlayer);
									if (playerChatMessage != null) {
										serverPlayer.sendChatMessage(playerChatMessage, chatSender, ChatType.TEAM_MSG_COMMAND);
									}
								}
							}
						},
						commandSourceStack.getServer()
					);
				return list.size();
			}
		}
	}
}
