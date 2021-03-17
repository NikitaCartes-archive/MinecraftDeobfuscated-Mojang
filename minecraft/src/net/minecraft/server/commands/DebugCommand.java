package net.minecraft.server.commands;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfileResults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugCommand {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(new TranslatableComponent("commands.debug.notRunning"));
	private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.debug.alreadyRunning")
	);
	@Nullable
	private static final FileSystemProvider ZIP_FS_PROVIDER = (FileSystemProvider)FileSystemProvider.installedProviders()
		.stream()
		.filter(fileSystemProvider -> fileSystemProvider.getScheme().equalsIgnoreCase("jar"))
		.findFirst()
		.orElse(null);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("debug")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(Commands.literal("start").executes(commandContext -> start(commandContext.getSource())))
				.then(Commands.literal("stop").executes(commandContext -> stop(commandContext.getSource())))
				.then(Commands.literal("report").executes(commandContext -> report(commandContext.getSource())))
		);
	}

	private static int start(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		if (minecraftServer.isProfiling()) {
			throw ERROR_ALREADY_RUNNING.create();
		} else {
			minecraftServer.startProfiling();
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.debug.started", "Started the debug profiler. Type '/debug stop' to stop it."), true);
			return 0;
		}
	}

	private static int stop(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		if (!minecraftServer.isProfiling()) {
			throw ERROR_NOT_RUNNING.create();
		} else {
			ProfileResults profileResults = minecraftServer.finishProfiling();
			File file = new File(minecraftServer.getFile("debug"), "profile-results-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".txt");
			profileResults.saveResults(file.toPath());
			float f = (float)profileResults.getNanoDuration() / 1.0E9F;
			float g = (float)profileResults.getTickDuration() / f;
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", f), profileResults.getTickDuration(), String.format("%.2f", g)),
				true
			);
			return Mth.floor(g);
		}
	}

	private static int report(CommandSourceStack commandSourceStack) {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		String string = "debug-report-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());

		try {
			Path path = minecraftServer.getFile("debug").toPath();
			Files.createDirectories(path);
			if (!SharedConstants.IS_RUNNING_IN_IDE && ZIP_FS_PROVIDER != null) {
				Path path2 = path.resolve(string + ".zip");
				FileSystem fileSystem = ZIP_FS_PROVIDER.newFileSystem(path2, ImmutableMap.of("create", "true"));
				Throwable var6 = null;

				try {
					minecraftServer.saveDebugReport(fileSystem.getPath("/"));
				} catch (Throwable var16) {
					var6 = var16;
					throw var16;
				} finally {
					if (fileSystem != null) {
						if (var6 != null) {
							try {
								fileSystem.close();
							} catch (Throwable var15) {
								var6.addSuppressed(var15);
							}
						} else {
							fileSystem.close();
						}
					}
				}
			} else {
				Path path2 = path.resolve(string);
				minecraftServer.saveDebugReport(path2);
			}

			commandSourceStack.sendSuccess(new TranslatableComponent("commands.debug.reportSaved", string), false);
			return 1;
		} catch (IOException var18) {
			LOGGER.error("Failed to save debug dump", (Throwable)var18);
			commandSourceStack.sendFailure(new TranslatableComponent("commands.debug.reportFailed"));
			return 0;
		}
	}
}
