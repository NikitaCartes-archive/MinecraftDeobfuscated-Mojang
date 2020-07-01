package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReloadCommand {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void reloadPacks(Collection<String> collection, CommandSourceStack commandSourceStack) {
		commandSourceStack.getServer().reloadResources(collection).exceptionally(throwable -> {
			LOGGER.warn("Failed to execute reload", throwable);
			commandSourceStack.sendFailure(new TranslatableComponent("commands.reload.failure"));
			return null;
		});
	}

	private static Collection<String> discoverNewPacks(PackRepository packRepository, WorldData worldData, Collection<String> collection) {
		packRepository.reload();
		Collection<String> collection2 = Lists.<String>newArrayList(collection);
		Collection<String> collection3 = worldData.getDataPackConfig().getDisabled();

		for (String string : packRepository.getAvailableIds()) {
			if (!collection3.contains(string) && !collection2.contains(string)) {
				collection2.add(string);
			}
		}

		return collection2;
	}

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(Commands.literal("reload").requires(commandSourceStack -> commandSourceStack.hasPermission(2)).executes(commandContext -> {
			CommandSourceStack commandSourceStack = commandContext.getSource();
			MinecraftServer minecraftServer = commandSourceStack.getServer();
			PackRepository packRepository = minecraftServer.getPackRepository();
			WorldData worldData = minecraftServer.getWorldData();
			Collection<String> collection = packRepository.getSelectedIds();
			Collection<String> collection2 = discoverNewPacks(packRepository, worldData, collection);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.reload.success"), true);
			reloadPacks(collection2, commandSourceStack);
			return 0;
		}));
	}
}
