/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.UnopenedPack;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReloadCommand {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void reloadPacks(Collection<String> collection, CommandSourceStack commandSourceStack) {
        commandSourceStack.getServer().reloadResources(collection).exceptionally(throwable -> {
            LOGGER.warn("Failed to execute reload", (Throwable)throwable);
            commandSourceStack.sendFailure(new TranslatableComponent("commands.reload.failure"));
            return null;
        });
    }

    private static Collection<String> discoverNewPacks(PackRepository<?> packRepository, WorldData worldData, Collection<String> collection) {
        packRepository.reload();
        ArrayList<String> collection2 = Lists.newArrayList(collection);
        for (String string : packRepository.getAvailableIds()) {
            if (worldData.getDisabledDataPacks().contains(string) || collection2.contains(string)) continue;
            collection2.add(string);
        }
        return collection2;
    }

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("reload").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).executes(commandContext -> {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            MinecraftServer minecraftServer = commandSourceStack.getServer();
            PackRepository<UnopenedPack> packRepository = minecraftServer.getPackRepository();
            WorldData worldData = minecraftServer.getWorldData();
            Collection<String> collection = packRepository.getSelectedIds();
            Collection<String> collection2 = ReloadCommand.discoverNewPacks(packRepository, worldData, collection);
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.reload.success"), true);
            ReloadCommand.reloadPacks(collection2, commandSourceStack);
            return 0;
        }));
    }
}

