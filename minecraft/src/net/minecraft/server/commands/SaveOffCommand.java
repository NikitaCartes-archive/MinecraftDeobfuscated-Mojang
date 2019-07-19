package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;

public class SaveOffCommand {
	private static final SimpleCommandExceptionType ERROR_ALREADY_OFF = new SimpleCommandExceptionType(new TranslatableComponent("commands.save.alreadyOff"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(Commands.literal("save-off").requires(commandSourceStack -> commandSourceStack.hasPermission(4)).executes(commandContext -> {
			CommandSourceStack commandSourceStack = commandContext.getSource();
			boolean bl = false;

			for (ServerLevel serverLevel : commandSourceStack.getServer().getAllLevels()) {
				if (serverLevel != null && !serverLevel.noSave) {
					serverLevel.noSave = true;
					bl = true;
				}
			}

			if (!bl) {
				throw ERROR_ALREADY_OFF.create();
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.save.disabled"), true);
				return 1;
			}
		}));
	}
}
