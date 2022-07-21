package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FileZipper;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class PerfCommand {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(Component.translatable("commands.perf.notRunning"));
	private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(Component.translatable("commands.perf.alreadyRunning"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("perf")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(4))
				.then(Commands.literal("start").executes(commandContext -> startProfilingDedicatedServer(commandContext.getSource())))
				.then(Commands.literal("stop").executes(commandContext -> stopProfilingDedicatedServer(commandContext.getSource())))
		);
	}

	private static int startProfilingDedicatedServer(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		if (minecraftServer.isRecordingMetrics()) {
			throw ERROR_ALREADY_RUNNING.create();
		} else {
			Consumer<ProfileResults> consumer = profileResults -> whenStopped(commandSourceStack, profileResults);
			Consumer<Path> consumer2 = path -> saveResults(commandSourceStack, path, minecraftServer);
			minecraftServer.startRecordingMetrics(consumer, consumer2);
			commandSourceStack.sendSuccess(Component.translatable("commands.perf.started"), false);
			return 0;
		}
	}

	private static int stopProfilingDedicatedServer(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		if (!minecraftServer.isRecordingMetrics()) {
			throw ERROR_NOT_RUNNING.create();
		} else {
			minecraftServer.finishRecordingMetrics();
			return 0;
		}
	}

	private static void saveResults(CommandSourceStack commandSourceStack, Path path, MinecraftServer minecraftServer) {
		String string = String.format(
			Locale.ROOT, "%s-%s-%s", Util.getFilenameFormattedDateTime(), minecraftServer.getWorldData().getLevelName(), SharedConstants.getCurrentVersion().getId()
		);

		String string2;
		try {
			string2 = FileUtil.findAvailableName(MetricsPersister.PROFILING_RESULTS_DIR, string, ".zip");
		} catch (IOException var11) {
			commandSourceStack.sendFailure(Component.translatable("commands.perf.reportFailed"));
			LOGGER.error("Failed to create report name", (Throwable)var11);
			return;
		}

		FileZipper fileZipper = new FileZipper(MetricsPersister.PROFILING_RESULTS_DIR.resolve(string2));

		try {
			fileZipper.add(Paths.get("system.txt"), minecraftServer.fillSystemReport(new SystemReport()).toLineSeparatedString());
			fileZipper.add(path);
		} catch (Throwable var10) {
			try {
				fileZipper.close();
			} catch (Throwable var8) {
				var10.addSuppressed(var8);
			}

			throw var10;
		}

		fileZipper.close();

		try {
			FileUtils.forceDelete(path.toFile());
		} catch (IOException var9) {
			LOGGER.warn("Failed to delete temporary profiling file {}", path, var9);
		}

		commandSourceStack.sendSuccess(Component.translatable("commands.perf.reportSaved", string2), false);
	}

	private static void whenStopped(CommandSourceStack commandSourceStack, ProfileResults profileResults) {
		if (profileResults != EmptyProfileResults.EMPTY) {
			int i = profileResults.getTickDuration();
			double d = (double)profileResults.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
			commandSourceStack.sendSuccess(
				Component.translatable("commands.perf.stopped", String.format(Locale.ROOT, "%.2f", d), i, String.format(Locale.ROOT, "%.2f", (double)i / d)), false
			);
		}
	}
}
