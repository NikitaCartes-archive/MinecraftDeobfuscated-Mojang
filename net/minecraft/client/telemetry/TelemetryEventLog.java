/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.telemetry;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryEventInstance;
import net.minecraft.client.telemetry.TelemetryEventLogger;
import net.minecraft.util.eventlog.JsonEventLog;
import net.minecraft.util.thread.ProcessorMailbox;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TelemetryEventLog
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final JsonEventLog<TelemetryEventInstance> log;
    private final ProcessorMailbox<Runnable> mailbox;

    public TelemetryEventLog(FileChannel fileChannel, Executor executor) {
        this.log = new JsonEventLog<TelemetryEventInstance>(TelemetryEventInstance.CODEC, fileChannel);
        this.mailbox = ProcessorMailbox.create(executor, "telemetry-event-log");
    }

    public TelemetryEventLogger logger() {
        return telemetryEventInstance -> this.mailbox.tell(() -> {
            try {
                this.log.write(telemetryEventInstance);
            } catch (IOException iOException) {
                LOGGER.error("Failed to write telemetry event to log", iOException);
            }
        });
    }

    @Override
    public void close() {
        this.mailbox.tell(() -> IOUtils.closeQuietly(this.log));
        this.mailbox.close();
    }
}

