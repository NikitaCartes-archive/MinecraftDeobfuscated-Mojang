package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class SeedCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, boolean bl) {
		commandDispatcher.register(
			Commands.literal("seed")
				.requires(commandSourceStack -> !bl || commandSourceStack.hasPermission(2))
				.executes(
					commandContext -> {
						long l = commandContext.getSource().getLevel().getSeed();
						Component component = ComponentUtils.wrapInSquareBrackets(
							new TextComponent(String.valueOf(l))
								.withStyle(
									style -> style.withColor(ChatFormatting.GREEN)
											.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(l)))
											.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.copy.click")))
											.withInsertion(String.valueOf(l))
								)
						);
						commandContext.getSource().sendSuccess(new TranslatableComponent("commands.seed.success", component), false);
						return (int)l;
					}
				)
		);
	}
}
