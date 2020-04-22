/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;

public class DifficultyCommand {
    private static final DynamicCommandExceptionType ERROR_ALREADY_DIFFICULT = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.difficulty.failure", object));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("difficulty");
        for (Difficulty difficulty : Difficulty.values()) {
            literalArgumentBuilder.then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal(difficulty.getKey()).executes(commandContext -> DifficultyCommand.setDifficulty((CommandSourceStack)commandContext.getSource(), difficulty)));
        }
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.requires(commandSourceStack -> commandSourceStack.hasPermission(2))).executes(commandContext -> {
            Difficulty difficulty = ((CommandSourceStack)commandContext.getSource()).getLevel().getDifficulty();
            ((CommandSourceStack)commandContext.getSource()).sendSuccess(new TranslatableComponent("commands.difficulty.query", difficulty.getDisplayName()), false);
            return difficulty.getId();
        }));
    }

    public static int setDifficulty(CommandSourceStack commandSourceStack, Difficulty difficulty) throws CommandSyntaxException {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (minecraftServer.getWorldData().getDifficulty() == difficulty) {
            throw ERROR_ALREADY_DIFFICULT.create(difficulty.getKey());
        }
        minecraftServer.setDifficulty(difficulty, true);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.difficulty.success", difficulty.getDisplayName()), true);
        return 0;
    }
}

