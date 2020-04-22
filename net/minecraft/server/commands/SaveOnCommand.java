/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;

public class SaveOnCommand {
    private static final SimpleCommandExceptionType ERROR_ALREADY_ON = new SimpleCommandExceptionType(new TranslatableComponent("commands.save.alreadyOn"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("save-on").requires(commandSourceStack -> commandSourceStack.hasPermission(4))).executes(commandContext -> {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            boolean bl = false;
            for (ServerLevel serverLevel : commandSourceStack.getServer().getAllLevels()) {
                if (serverLevel == null || !serverLevel.noSave) continue;
                serverLevel.noSave = false;
                bl = true;
            }
            if (!bl) {
                throw ERROR_ALREADY_ON.create();
            }
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.save.enabled"), true);
            return 1;
        }));
    }
}

