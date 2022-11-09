/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

public class PublishCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.publish.failed"));
    private static final DynamicCommandExceptionType ERROR_ALREADY_PUBLISHED = new DynamicCommandExceptionType(object -> Component.translatable("commands.publish.alreadyPublished", object));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("publish").requires(commandSourceStack -> commandSourceStack.hasPermission(4))).executes(commandContext -> PublishCommand.publish((CommandSourceStack)commandContext.getSource(), HttpUtil.getAvailablePort(), false, null))).then(((RequiredArgumentBuilder)Commands.argument("allowCommands", BoolArgumentType.bool()).executes(commandContext -> PublishCommand.publish((CommandSourceStack)commandContext.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool(commandContext, "allowCommands"), null))).then(((RequiredArgumentBuilder)Commands.argument("gamemode", GameModeArgument.gameMode()).executes(commandContext -> PublishCommand.publish((CommandSourceStack)commandContext.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool(commandContext, "allowCommands"), GameModeArgument.getGameMode(commandContext, "gamemode")))).then(Commands.argument("port", IntegerArgumentType.integer(0, 65535)).executes(commandContext -> PublishCommand.publish((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "port"), BoolArgumentType.getBool(commandContext, "allowCommands"), GameModeArgument.getGameMode(commandContext, "gamemode")))))));
    }

    private static int publish(CommandSourceStack commandSourceStack, int i, boolean bl, @Nullable GameType gameType) throws CommandSyntaxException {
        if (commandSourceStack.getServer().isPublished()) {
            throw ERROR_ALREADY_PUBLISHED.create(commandSourceStack.getServer().getPort());
        }
        if (!commandSourceStack.getServer().publishServer(gameType, bl, i)) {
            throw ERROR_FAILED.create();
        }
        commandSourceStack.sendSuccess(PublishCommand.getSuccessMessage(i), true);
        return i;
    }

    public static MutableComponent getSuccessMessage(int i) {
        MutableComponent component = ComponentUtils.copyOnClickText(String.valueOf(i));
        return Component.translatable("commands.publish.started", component);
    }
}

