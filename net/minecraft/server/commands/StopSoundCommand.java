/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

public class StopSoundCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        RequiredArgumentBuilder requiredArgumentBuilder = (RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes(commandContext -> StopSoundCommand.stopSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), null, null))).then(Commands.literal("*").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("sound", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_SOUNDS).executes(commandContext -> StopSoundCommand.stopSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), null, ResourceLocationArgument.getId(commandContext, "sound")))));
        for (SoundSource soundSource : SoundSource.values()) {
            requiredArgumentBuilder.then(((LiteralArgumentBuilder)Commands.literal(soundSource.getName()).executes(commandContext -> StopSoundCommand.stopSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), soundSource, null))).then(Commands.argument("sound", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_SOUNDS).executes(commandContext -> StopSoundCommand.stopSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), soundSource, ResourceLocationArgument.getId(commandContext, "sound")))));
        }
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("stopsound").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(requiredArgumentBuilder));
    }

    private static int stopSound(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, @Nullable SoundSource soundSource, @Nullable ResourceLocation resourceLocation) {
        ClientboundStopSoundPacket clientboundStopSoundPacket = new ClientboundStopSoundPacket(resourceLocation, soundSource);
        for (ServerPlayer serverPlayer : collection) {
            serverPlayer.connection.send(clientboundStopSoundPacket);
        }
        if (soundSource != null) {
            if (resourceLocation != null) {
                commandSourceStack.sendSuccess(new TranslatableComponent("commands.stopsound.success.source.sound", resourceLocation, soundSource.getName()), true);
            } else {
                commandSourceStack.sendSuccess(new TranslatableComponent("commands.stopsound.success.source.any", soundSource.getName()), true);
            }
        } else if (resourceLocation != null) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.stopsound.success.sourceless.sound", resourceLocation), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.stopsound.success.sourceless.any", new Object[0]), true);
        }
        return collection.size();
    }
}

