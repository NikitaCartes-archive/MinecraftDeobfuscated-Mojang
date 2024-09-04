package net.minecraft.client.telemetry;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.eventlog.JsonEventLog;
import net.minecraft.util.thread.ConsecutiveExecutor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class TelemetryEventLog implements AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final JsonEventLog<TelemetryEventInstance> log;
	private final ConsecutiveExecutor consecutiveExecutor;

	public TelemetryEventLog(FileChannel fileChannel, Executor executor) {
		this.log = new JsonEventLog<>(TelemetryEventInstance.CODEC, fileChannel);
		this.consecutiveExecutor = new ConsecutiveExecutor(executor, "telemetry-event-log");
	}

	public TelemetryEventLogger logger() {
		return telemetryEventInstance -> this.consecutiveExecutor.schedule(() -> {
				try {
					this.log.write(telemetryEventInstance);
				} catch (IOException var3) {
					LOGGER.error("Failed to write telemetry event to log", (Throwable)var3);
				}
			});
	}

	public void close() {
		this.consecutiveExecutor.schedule(() -> IOUtils.closeQuietly(this.log));
		this.consecutiveExecutor.close();
	}
}
