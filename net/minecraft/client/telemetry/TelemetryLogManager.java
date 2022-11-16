/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.telemetry;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.telemetry.TelemetryEventLog;
import net.minecraft.client.telemetry.TelemetryEventLogger;
import net.minecraft.util.eventlog.EventLogDirectory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TelemetryLogManager
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String RAW_EXTENSION = ".json";
    private static final int EXPIRY_DAYS = 7;
    private final EventLogDirectory directory;
    @Nullable
    private CompletableFuture<Optional<TelemetryEventLog>> sessionLog;

    private TelemetryLogManager(EventLogDirectory eventLogDirectory) {
        this.directory = eventLogDirectory;
    }

    public static CompletableFuture<Optional<TelemetryLogManager>> open(Path path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                EventLogDirectory eventLogDirectory = EventLogDirectory.open(path, RAW_EXTENSION);
                eventLogDirectory.listFiles().prune(LocalDate.now(), 7).compressAll();
                return Optional.of(new TelemetryLogManager(eventLogDirectory));
            } catch (Exception exception) {
                LOGGER.error("Failed to create telemetry log manager", exception);
                return Optional.empty();
            }
        }, Util.backgroundExecutor());
    }

    public CompletableFuture<Optional<TelemetryEventLogger>> openLogger() {
        if (this.sessionLog == null) {
            this.sessionLog = CompletableFuture.supplyAsync(() -> {
                try {
                    EventLogDirectory.RawFile rawFile = this.directory.createNewFile(LocalDate.now());
                    FileChannel fileChannel = rawFile.openChannel();
                    return Optional.of(new TelemetryEventLog(fileChannel, Util.backgroundExecutor()));
                } catch (IOException iOException) {
                    LOGGER.error("Failed to open channel for telemetry event log", iOException);
                    return Optional.empty();
                }
            }, Util.backgroundExecutor());
        }
        return this.sessionLog.thenApply(optional -> optional.map(TelemetryEventLog::logger));
    }

    @Override
    public void close() {
        if (this.sessionLog != null) {
            this.sessionLog.thenAccept(optional -> optional.ifPresent(TelemetryEventLog::close));
        }
    }
}

