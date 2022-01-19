package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.profiling.jfr.parse.JfrStatsParser;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class SummaryReporter {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Runnable onDeregistration;

	protected SummaryReporter(Runnable runnable) {
		this.onDeregistration = runnable;
	}

	public void recordingStopped(@Nullable Path path) {
		if (path != null) {
			this.onDeregistration.run();
			infoWithFallback(() -> "Dumped flight recorder profiling to " + path);

			JfrStatsResult jfrStatsResult;
			try {
				jfrStatsResult = JfrStatsParser.parse(path);
			} catch (Throwable var5) {
				warnWithFallback(() -> "Failed to parse JFR recording", var5);
				return;
			}

			try {
				infoWithFallback(jfrStatsResult::asJson);
				Path path2 = path.resolveSibling("jfr-report-" + StringUtils.substringBefore(path.getFileName().toString(), ".jfr") + ".json");
				Files.writeString(path2, jfrStatsResult.asJson(), StandardOpenOption.CREATE);
				infoWithFallback(() -> "Dumped recording summary to " + path2);
			} catch (Throwable var4) {
				warnWithFallback(() -> "Failed to output JFR report", var4);
			}
		}
	}

	private static void infoWithFallback(Supplier<String> supplier) {
		if (LogUtils.isLoggerActive()) {
			LOGGER.info((String)supplier.get());
		} else {
			Bootstrap.realStdoutPrintln((String)supplier.get());
		}
	}

	private static void warnWithFallback(Supplier<String> supplier, Throwable throwable) {
		if (LogUtils.isLoggerActive()) {
			LOGGER.warn((String)supplier.get(), throwable);
		} else {
			Bootstrap.realStdoutPrintln((String)supplier.get());
			throwable.printStackTrace(Bootstrap.STDOUT);
		}
	}
}
