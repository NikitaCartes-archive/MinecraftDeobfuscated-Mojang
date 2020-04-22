/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.FileSystemProvider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfileResults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DebugCommand {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(new TranslatableComponent("commands.debug.notRunning"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(new TranslatableComponent("commands.debug.alreadyRunning"));
    @Nullable
    private static final FileSystemProvider ZIP_FS_PROVIDER = FileSystemProvider.installedProviders().stream().filter(fileSystemProvider -> fileSystemProvider.getScheme().equalsIgnoreCase("jar")).findFirst().orElse(null);

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("debug").requires(commandSourceStack -> commandSourceStack.hasPermission(3))).then(Commands.literal("start").executes(commandContext -> DebugCommand.start((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("stop").executes(commandContext -> DebugCommand.stop((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("report").executes(commandContext -> DebugCommand.report((CommandSourceStack)commandContext.getSource()))));
    }

    private static int start(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (minecraftServer.isProfiling()) {
            throw ERROR_ALREADY_RUNNING.create();
        }
        minecraftServer.startProfiling();
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.debug.started", "Started the debug profiler. Type '/debug stop' to stop it."), true);
        return 0;
    }

    private static int stop(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (!minecraftServer.isProfiling()) {
            throw ERROR_NOT_RUNNING.create();
        }
        ProfileResults profileResults = minecraftServer.finishProfiling();
        File file = new File(minecraftServer.getFile("debug"), "profile-results-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".txt");
        profileResults.saveResults(file);
        float f = (float)profileResults.getNanoDuration() / 1.0E9f;
        float g = (float)profileResults.getTickDuration() / f;
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", Float.valueOf(f)), profileResults.getTickDuration(), String.format("%.2f", Float.valueOf(g))), true);
        return Mth.floor(g);
    }

    private static int report(CommandSourceStack commandSourceStack) {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        String string = "debug-report-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
        try {
            Path path = minecraftServer.getFile("debug").toPath();
            Files.createDirectories(path, new FileAttribute[0]);
            if (SharedConstants.IS_RUNNING_IN_IDE || ZIP_FS_PROVIDER == null) {
                Path path2 = path.resolve(string);
                minecraftServer.saveDebugReport(path2);
            } else {
                Path path2 = path.resolve(string + ".zip");
                try (FileSystem fileSystem = ZIP_FS_PROVIDER.newFileSystem(path2, ImmutableMap.of("create", "true"));){
                    minecraftServer.saveDebugReport(fileSystem.getPath("/", new String[0]));
                }
            }
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.debug.reportSaved", string), false);
            return 1;
        } catch (IOException iOException) {
            LOGGER.error("Failed to save debug dump", (Throwable)iOException);
            commandSourceStack.sendFailure(new TranslatableComponent("commands.debug.reportFailed"));
            return 0;
        }
    }
}

