/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.jetbrains.annotations.Nullable;

public class BanPlayerCommands {
    private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(new TranslatableComponent("commands.ban.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("ban").requires(commandSourceStack -> commandSourceStack.getServer().getPlayerList().getBans().isEnabled() && commandSourceStack.hasPermission(3))).then(((RequiredArgumentBuilder)Commands.argument("targets", GameProfileArgument.gameProfile()).executes(commandContext -> BanPlayerCommands.banPlayers((CommandSourceStack)commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets"), null))).then(Commands.argument("reason", MessageArgument.message()).executes(commandContext -> BanPlayerCommands.banPlayers((CommandSourceStack)commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets"), MessageArgument.getMessage(commandContext, "reason"))))));
    }

    private static int banPlayers(CommandSourceStack commandSourceStack, Collection<GameProfile> collection, @Nullable Component component) throws CommandSyntaxException {
        UserBanList userBanList = commandSourceStack.getServer().getPlayerList().getBans();
        int i = 0;
        for (GameProfile gameProfile : collection) {
            if (userBanList.isBanned(gameProfile)) continue;
            UserBanListEntry userBanListEntry = new UserBanListEntry(gameProfile, null, commandSourceStack.getTextName(), null, component == null ? null : component.getString());
            userBanList.add(userBanListEntry);
            ++i;
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.ban.success", ComponentUtils.getDisplayName(gameProfile), userBanListEntry.getReason()), true);
            ServerPlayer serverPlayer = commandSourceStack.getServer().getPlayerList().getPlayer(gameProfile.getId());
            if (serverPlayer == null) continue;
            serverPlayer.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.banned"));
        }
        if (i == 0) {
            throw ERROR_ALREADY_BANNED.create();
        }
        return i;
    }
}

