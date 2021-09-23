package net.minecraft.util.profiling.jfr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import jdk.jfr.Configuration;
import jdk.jfr.Event;
import jdk.jfr.EventType;
import jdk.jfr.FlightRecorder;
import jdk.jfr.FlightRecorderListener;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.jfr.event.ChunkGenerationEvent;
import net.minecraft.util.profiling.jfr.event.PacketReceivedEvent;
import net.minecraft.util.profiling.jfr.event.PacketSentEvent;
import net.minecraft.util.profiling.jfr.event.ServerTickTimeEvent;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JfrProfiler implements JvmProfiler {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final String ROOT_CATEGORY = "Minecraft";
	public static final String WORLD_GEN_CATEGORY = "World Generation";
	public static final String TICK_CATEGORY = "Ticking";
	public static final String NETWORK_CATEGORY = "Network";
	private static final List<Class<? extends Event>> CUSTOM_EVENTS = List.of(
		ChunkGenerationEvent.class, WorldLoadFinishedEvent.class, ServerTickTimeEvent.class, PacketReceivedEvent.class, PacketSentEvent.class
	);
	private static final String FLIGHT_RECORDER_CONFIG = "/flightrecorder-config.jfc";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
		.appendPattern("yyyy-MM-dd-HHmmss")
		.toFormatter()
		.withZone(ZoneId.systemDefault());
	@Nullable
	Recording recording;
	private long nextTickTimeReport;

	protected JfrProfiler() {
	}

	@Override
	public void initialize() {
		CUSTOM_EVENTS.forEach(FlightRecorder::register);
	}

	@Override
	public boolean start(Environment environment) {
		URL uRL = JfrProfiler.class.getResource("/flightrecorder-config.jfc");
		if (uRL == null) {
			LOGGER.warn("Could not find default flight recorder config at {}", "/flightrecorder-config.jfc");
			return false;
		} else {
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(uRL.openStream()));

				boolean var4;
				try {
					var4 = this.start(bufferedReader, environment);
				} catch (Throwable var7) {
					try {
						bufferedReader.close();
					} catch (Throwable var6) {
						var7.addSuppressed(var6);
					}

					throw var7;
				}

				bufferedReader.close();
				return var4;
			} catch (IOException var8) {
				LOGGER.warn("Failed to start flight recorder using configuration at {}", uRL, var8);
				return false;
			}
		}
	}

	@Override
	public Path stop() {
		if (this.recording == null) {
			throw new IllegalStateException("Not currently profiling");
		} else {
			Path path = this.recording.getDestination();
			this.recording.stop();
			return path;
		}
	}

	@Override
	public boolean isRunning() {
		return this.recording != null;
	}

	@Override
	public boolean isAvailable() {
		return FlightRecorder.isAvailable();
	}

	@Override
	public void onServerTick(float f) {
		if (EventType.getEventType(ServerTickTimeEvent.class).isEnabled()) {
			long l = Util.timeSource.getAsLong();
			if (this.nextTickTimeReport <= l) {
				new ServerTickTimeEvent(f).commit();
				this.nextTickTimeReport = l + TimeUnit.SECONDS.toNanos(1L);
			}
		}
	}

	private boolean start(Reader reader, Environment environment) {
		if (this.recording != null) {
			LOGGER.warn("Profiling already in progress");
			return false;
		} else {
			try {
				Configuration configuration = Configuration.create(reader);
				String string = DATE_TIME_FORMATTER.format(Instant.now());
				this.recording = Util.make(new Recording(configuration), recording -> {
					CUSTOM_EVENTS.forEach(recording::enable);
					recording.setDumpOnExit(true);
					recording.setToDisk(true);
					recording.setName("%s-%s-%s".formatted(environment.getDescription(), SharedConstants.getCurrentVersion().getName(), string));
				});
				Path path = Paths.get("debug/%s-%s.jfr".formatted(environment.getDescription(), string));
				if (!Files.exists(path.getParent(), new LinkOption[0])) {
					Files.createDirectories(path.getParent());
				}

				this.recording.setDestination(path);
				this.recording.start();
				this.setupSummaryListener();
			} catch (ParseException | IOException var6) {
				LOGGER.warn("Failed to start jfr profiling", (Throwable)var6);
				return false;
			}

			LOGGER.info(
				"Started flight recorder profiling id({}):name({}) - will dump to {} on exit or stop command",
				this.recording.getId(),
				this.recording.getName(),
				this.recording.getDestination()
			);
			return true;
		}
	}

	private void setupSummaryListener() {
		FlightRecorder.addListener(new FlightRecorderListener() {
			final SummaryReporter summaryReporter = new SummaryReporter(() -> JfrProfiler.this.recording = null);

			public void recordingStateChanged(Recording recording) {
				if (recording == JfrProfiler.this.recording && recording.getState() == RecordingState.STOPPED) {
					this.summaryReporter.recordingStopped(recording.getDestination());
					FlightRecorder.removeListener(this);
				}
			}
		});
	}

	@Override
	public void onPacketReceived(Supplier<String> supplier, SocketAddress socketAddress, int i) {
		if (EventType.getEventType(PacketReceivedEvent.class).isEnabled()) {
			new PacketReceivedEvent((String)supplier.get(), socketAddress, i).commit();
		}
	}

	@Override
	public void onPacketSent(Supplier<String> supplier, SocketAddress socketAddress, int i) {
		if (EventType.getEventType(PacketSentEvent.class).isEnabled()) {
			new PacketReceivedEvent((String)supplier.get(), socketAddress, i).commit();
		}
	}

	@Nullable
	@Override
	public ProfiledDuration onWorldLoadedStarted() {
		if (!EventType.getEventType(WorldLoadFinishedEvent.class).isEnabled()) {
			return null;
		} else {
			WorldLoadFinishedEvent worldLoadFinishedEvent = new WorldLoadFinishedEvent();
			worldLoadFinishedEvent.begin();
			return worldLoadFinishedEvent::commit;
		}
	}

	@Nullable
	@Override
	public ProfiledDuration onChunkGenerate(ChunkPos chunkPos, ResourceKey<Level> resourceKey, String string) {
		if (!EventType.getEventType(ChunkGenerationEvent.class).isEnabled()) {
			return null;
		} else {
			ChunkGenerationEvent chunkGenerationEvent = new ChunkGenerationEvent(chunkPos, resourceKey, string);
			chunkGenerationEvent.begin();
			return chunkGenerationEvent::commit;
		}
	}
}
