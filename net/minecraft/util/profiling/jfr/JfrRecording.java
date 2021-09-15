/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import jdk.jfr.Configuration;
import jdk.jfr.Event;
import jdk.jfr.FlightRecorder;
import jdk.jfr.FlightRecorderListener;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.profiling.jfr.event.network.PacketReceivedEvent;
import net.minecraft.util.profiling.jfr.event.network.PacketSentEvent;
import net.minecraft.util.profiling.jfr.event.ticking.ServerTickTimeEvent;
import net.minecraft.util.profiling.jfr.event.worldgen.ChunkGenerationEvent;
import net.minecraft.util.profiling.jfr.event.worldgen.WorldLoadFinishedEvent;
import net.minecraft.util.profiling.jfr.parse.JfrStatsParser;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.util.Supplier;
import org.jetbrains.annotations.Nullable;

public class JfrRecording {
    static final Logger LOGGER = LogManager.getLogger();
    public static final String ROOT_CATEGORY = "Minecraft";
    public static final String WORLD_GEN_CATEGORY = "World Generation";
    public static final String TICK_CATEGORY = "Ticking";
    public static final String NETWORK_CATEGORY = "Network";
    public static final List<Class<? extends Event>> CUSTOM_EVENTS = List.of(ChunkGenerationEvent.class, WorldLoadFinishedEvent.class, ServerTickTimeEvent.class, PacketReceivedEvent.class, PacketSentEvent.class);
    private static final String FLIGHT_RECORDER_CONFIG = "/flightrecorder-config.jfc";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd-HHmm").toFormatter().withZone(ZoneId.systemDefault());
    @Nullable
    private static Recording recording;

    private JfrRecording() {
    }

    private static boolean start(Reader reader, Environment environment) {
        if (!FlightRecorder.isAvailable()) {
            LOGGER.warn("Flight Recorder not available!");
            return false;
        }
        if (JfrRecording.recording != null) {
            LOGGER.warn("Profiling already in progress");
            return false;
        }
        try {
            Configuration configuration = Configuration.create(reader);
            String string = DATE_TIME_FORMATTER.format(Instant.now());
            JfrRecording.recording = Util.make(new Recording(configuration), recording -> {
                CUSTOM_EVENTS.forEach(recording::enable);
                recording.setDumpOnExit(true);
                recording.setToDisk(true);
                recording.setName("%s-%s-%s".formatted(environment.getDescription(), SharedConstants.getCurrentVersion().getName(), string));
            });
            Path path = Paths.get("debug/%s-%s.jfr".formatted(environment.getDescription(), string), new String[0]);
            if (!Files.exists(path.getParent(), new LinkOption[0])) {
                Files.createDirectories(path.getParent(), new FileAttribute[0]);
            }
            JfrRecording.recording.setDestination(path);
            JfrRecording.recording.start();
            FlightRecorder.addListener(new SummaryReporter(JfrRecording.recording, () -> {
                recording = null;
            }));
        } catch (IOException | ParseException exception) {
            LOGGER.warn("Failed to start jfr profiling", (Throwable)exception);
            return false;
        }
        LOGGER.info("Started flight recorder profiling id({}):name({}) - will dump to {} on exit or stop command", (Object)JfrRecording.recording.getId(), (Object)JfrRecording.recording.getName(), (Object)JfrRecording.recording.getDestination());
        return true;
    }

    public static boolean start(Environment environment) {
        boolean bl;
        URL uRL = JfrRecording.class.getResource(FLIGHT_RECORDER_CONFIG);
        if (uRL == null) {
            LOGGER.warn("Could not find default flight recorder config at {}", (Object)FLIGHT_RECORDER_CONFIG);
            return false;
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(uRL.openStream()));
        try {
            bl = JfrRecording.start(bufferedReader, environment);
        } catch (Throwable throwable) {
            try {
                try {
                    bufferedReader.close();
                } catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            } catch (IOException iOException) {
                LOGGER.warn("Failed to start flight recorder using configuration at {}", (Object)uRL, (Object)iOException);
                return false;
            }
        }
        bufferedReader.close();
        return bl;
    }

    public static Path stop() {
        if (recording == null) {
            throw new IllegalStateException("Not currently profiling");
        }
        Path path = recording.getDestination();
        recording.stop();
        return path;
    }

    public static boolean isRunning() {
        return recording != null;
    }

    public static enum Environment {
        CLIENT("client"),
        SERVER("server");

        private final String description;

        private Environment(String string2) {
            this.description = string2;
        }

        public static Environment from(MinecraftServer minecraftServer) {
            return minecraftServer.isDedicatedServer() ? SERVER : CLIENT;
        }

        String getDescription() {
            return this.description;
        }
    }

    static class SummaryReporter
    implements FlightRecorderListener {
        private final Recording recording;
        private final Runnable onDeregistration;

        SummaryReporter(Recording recording, Runnable runnable) {
            this.recording = recording;
            this.onDeregistration = runnable;
        }

        @Override
        public void recordingStateChanged(Recording recording) {
            JfrStatsResult jfrStatsResult;
            if (recording != this.recording || this.recording.getState() != RecordingState.STOPPED || recording.getDestination() == null) {
                return;
            }
            FlightRecorder.removeListener(this);
            this.onDeregistration.run();
            Path path = recording.getDestination();
            SummaryReporter.infoWithFallback(() -> "Dumped flight recorder profiling to " + path);
            try {
                jfrStatsResult = JfrStatsParser.parse(path);
            } catch (Throwable throwable) {
                SummaryReporter.warnWithFallback(() -> "Failed to parse JFR recording", throwable);
                return;
            }
            try {
                SummaryReporter.infoWithFallback(jfrStatsResult::asJson);
                Path path2 = path.resolveSibling("jfr-report-" + StringUtils.substringBefore(path.getFileName().toString(), ".jfr") + ".json");
                Files.writeString(path2, (CharSequence)jfrStatsResult.asJson(), StandardOpenOption.CREATE);
                SummaryReporter.infoWithFallback(() -> "Dumped recording summary to " + path2);
            } catch (Throwable throwable) {
                SummaryReporter.warnWithFallback(() -> "Failed to output JFR report", throwable);
            }
        }

        private static void infoWithFallback(Supplier<String> supplier) {
            if (SummaryReporter.log4jIsActive()) {
                LOGGER.info(supplier);
            } else {
                Bootstrap.realStdoutPrintln(supplier.get());
            }
        }

        private static void warnWithFallback(Supplier<String> supplier, Throwable throwable) {
            if (SummaryReporter.log4jIsActive()) {
                LOGGER.warn(supplier, throwable);
            } else {
                Bootstrap.realStdoutPrintln(supplier.get());
                throwable.printStackTrace(Bootstrap.STDOUT);
            }
        }

        private static boolean log4jIsActive() {
            LoggerContext loggerContext = LogManager.getContext();
            LoggerContext loggerContext2 = loggerContext;
            if (loggerContext2 instanceof LifeCycle) {
                LifeCycle lifeCycle = (LifeCycle)((Object)loggerContext2);
                return !lifeCycle.isStopped();
            }
            return true;
        }
    }
}

