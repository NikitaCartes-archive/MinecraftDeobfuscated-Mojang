/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.profiling.jfr.parse.JfrStatsParser;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.util.Supplier;
import org.jetbrains.annotations.Nullable;

public class SummaryReporter {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Runnable onDeregistration;

    protected SummaryReporter(Runnable runnable) {
        this.onDeregistration = runnable;
    }

    public void recordingStopped(@Nullable Path path) {
        JfrStatsResult jfrStatsResult;
        if (path == null) {
            return;
        }
        this.onDeregistration.run();
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
        if (loggerContext instanceof LifeCycle) {
            LifeCycle lifeCycle = (LifeCycle)((Object)loggerContext);
            return !lifeCycle.isStopped();
        }
        return true;
    }
}

