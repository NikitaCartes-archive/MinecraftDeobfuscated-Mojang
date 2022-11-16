package net.minecraft.client.telemetry;

import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.minecraft.UserApiService;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

@Environment(EnvType.CLIENT)
public class ClientTelemetryManager implements AutoCloseable {
	private static final AtomicInteger THREAD_COUNT = new AtomicInteger(1);
	private static final Executor EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable);
		thread.setName("Telemetry-Sender-#" + THREAD_COUNT.getAndIncrement());
		return thread;
	});
	private final UserApiService userApiService;
	private final TelemetryPropertyMap deviceSessionProperties;
	private final Path logDirectory;
	private final CompletableFuture<Optional<TelemetryLogManager>> logManager;

	public ClientTelemetryManager(Minecraft minecraft, UserApiService userApiService, User user) {
		this.userApiService = userApiService;
		TelemetryPropertyMap.Builder builder = TelemetryPropertyMap.builder();
		user.getXuid().ifPresent(string -> builder.put(TelemetryProperty.USER_ID, string));
		user.getClientId().ifPresent(string -> builder.put(TelemetryProperty.CLIENT_ID, string));
		builder.put(TelemetryProperty.MINECRAFT_SESSION_ID, UUID.randomUUID());
		builder.put(TelemetryProperty.GAME_VERSION, SharedConstants.getCurrentVersion().getId());
		builder.put(TelemetryProperty.OPERATING_SYSTEM, Util.getPlatform().telemetryName());
		builder.put(TelemetryProperty.PLATFORM, System.getProperty("os.name"));
		builder.put(TelemetryProperty.CLIENT_MODDED, Minecraft.checkModStatus().shouldReportAsModified());
		this.deviceSessionProperties = builder.build();
		this.logDirectory = minecraft.gameDirectory.toPath().resolve("logs/telemetry");
		this.logManager = TelemetryLogManager.open(this.logDirectory);
	}

	public WorldSessionTelemetryManager createWorldSessionManager(boolean bl, @Nullable Duration duration) {
		return new WorldSessionTelemetryManager(this.createWorldSessionEventSender(), bl, duration);
	}

	private TelemetryEventSender createWorldSessionEventSender() {
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			return TelemetryEventSender.DISABLED;
		} else {
			TelemetrySession telemetrySession = this.userApiService.newTelemetrySession(EXECUTOR);
			if (!telemetrySession.isEnabled()) {
				return TelemetryEventSender.DISABLED;
			} else {
				CompletableFuture<Optional<TelemetryEventLogger>> completableFuture = this.logManager
					.thenCompose(
						optional -> (CompletionStage)optional.map(TelemetryLogManager::openLogger).orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))
					);
				return (telemetryEventType, consumer) -> {
					if (!telemetryEventType.isOptIn() || Minecraft.getInstance().telemetryOptInExtra()) {
						TelemetryPropertyMap.Builder builder = TelemetryPropertyMap.builder();
						builder.putAll(this.deviceSessionProperties);
						builder.put(TelemetryProperty.EVENT_TIMESTAMP_UTC, Instant.now());
						builder.put(TelemetryProperty.OPT_IN, telemetryEventType.isOptIn());
						consumer.accept(builder);
						TelemetryEventInstance telemetryEventInstance = new TelemetryEventInstance(telemetryEventType, builder.build());
						completableFuture.thenAccept(optional -> {
							if (!optional.isEmpty()) {
								((TelemetryEventLogger)optional.get()).log(telemetryEventInstance);
								telemetryEventInstance.export(telemetrySession).send();
							}
						});
					}
				};
			}
		}
	}

	public Path getLogDirectory() {
		return this.logDirectory;
	}

	public void close() {
		this.logManager.thenAccept(optional -> optional.ifPresent(TelemetryLogManager::close));
	}
}
