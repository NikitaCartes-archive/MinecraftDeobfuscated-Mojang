/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.io.IOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.chase.ChaseClient;
import net.minecraft.server.chase.ChaseServer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ChaseCommand {
    private static final String DEFAULT_CONNECT_HOST = "localhost";
    private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";
    private static final int DEFAULT_PORT = 10000;
    private static final int BROADCAST_INTERVAL_MS = 100;
    public static BiMap<String, ResourceKey<Level>> DIMENSION_NAMES = ImmutableBiMap.of("o", Level.OVERWORLD, "n", Level.NETHER, "e", Level.END);
    @Nullable
    private static ChaseServer chaseServer;
    @Nullable
    private static ChaseClient chaseClient;

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("chase").then((ArgumentBuilder<CommandSourceStack, ?>)((LiteralArgumentBuilder)Commands.literal("follow").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("host", StringArgumentType.string()).executes(commandContext -> ChaseCommand.follow((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString(commandContext, "host"), 10000))).then(Commands.argument("port", IntegerArgumentType.integer(1, 65535)).executes(commandContext -> ChaseCommand.follow((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString(commandContext, "host"), IntegerArgumentType.getInteger(commandContext, "port")))))).executes(commandContext -> ChaseCommand.follow((CommandSourceStack)commandContext.getSource(), DEFAULT_CONNECT_HOST, 10000)))).then(((LiteralArgumentBuilder)Commands.literal("lead").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("bind_address", StringArgumentType.string()).executes(commandContext -> ChaseCommand.lead((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString(commandContext, "bind_address"), 10000))).then(Commands.argument("port", IntegerArgumentType.integer(1024, 65535)).executes(commandContext -> ChaseCommand.lead((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString(commandContext, "bind_address"), IntegerArgumentType.getInteger(commandContext, "port")))))).executes(commandContext -> ChaseCommand.lead((CommandSourceStack)commandContext.getSource(), DEFAULT_BIND_ADDRESS, 10000)))).then(Commands.literal("stop").executes(commandContext -> ChaseCommand.stop((CommandSourceStack)commandContext.getSource()))));
    }

    private static int stop(CommandSourceStack commandSourceStack) {
        if (chaseClient != null) {
            chaseClient.stop();
            commandSourceStack.sendSuccess(Component.literal("You have now stopped chasing"), false);
            chaseClient = null;
        }
        if (chaseServer != null) {
            chaseServer.stop();
            commandSourceStack.sendSuccess(Component.literal("You are no longer being chased"), false);
            chaseServer = null;
        }
        return 0;
    }

    private static boolean alreadyRunning(CommandSourceStack commandSourceStack) {
        if (chaseServer != null) {
            commandSourceStack.sendFailure(Component.literal("Chase server is already running. Stop it using /chase stop"));
            return true;
        }
        if (chaseClient != null) {
            commandSourceStack.sendFailure(Component.literal("You are already chasing someone. Stop it using /chase stop"));
            return true;
        }
        return false;
    }

    private static int lead(CommandSourceStack commandSourceStack, String string, int i) {
        if (ChaseCommand.alreadyRunning(commandSourceStack)) {
            return 0;
        }
        chaseServer = new ChaseServer(string, i, commandSourceStack.getServer().getPlayerList(), 100);
        try {
            chaseServer.start();
            commandSourceStack.sendSuccess(Component.literal("Chase server is now running on port " + i + ". Clients can follow you using /chase follow <ip> <port>"), false);
        } catch (IOException iOException) {
            iOException.printStackTrace();
            commandSourceStack.sendFailure(Component.literal("Failed to start chase server on port " + i));
            chaseServer = null;
        }
        return 0;
    }

    private static int follow(CommandSourceStack commandSourceStack, String string, int i) {
        if (ChaseCommand.alreadyRunning(commandSourceStack)) {
            return 0;
        }
        chaseClient = new ChaseClient(string, i, commandSourceStack.getServer());
        chaseClient.start();
        commandSourceStack.sendSuccess(Component.literal("You are now chasing " + string + ":" + i + ". If that server does '/chase lead' then you will automatically go to the same position. Use '/chase stop' to stop chasing."), false);
        return 0;
    }
}

