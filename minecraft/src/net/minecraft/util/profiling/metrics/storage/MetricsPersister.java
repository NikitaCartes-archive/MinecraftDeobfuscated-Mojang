package net.minecraft.util.profiling.metrics.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MetricsPersister {
	public static final Path PROFILING_RESULTS_DIR = Paths.get("debug/profiling");
	public static final String METRICS_DIR_NAME = "metrics";
	public static final String DEVIATIONS_DIR_NAME = "deviations";
	public static final String PROFILING_RESULT_FILENAME = "profiling.txt";
	private static final Logger LOGGER = LogManager.getLogger();
	private final String rootFolderName;

	public MetricsPersister(String string) {
		this.rootFolderName = string;
	}

	public Path saveReports(Set<MetricSampler> set, Map<MetricSampler, List<RecordedDeviation>> map, ProfileResults profileResults) {
		try {
			Files.createDirectories(PROFILING_RESULTS_DIR);
		} catch (IOException var8) {
			throw new UncheckedIOException(var8);
		}

		try {
			Path path = Files.createTempDirectory("minecraft-profiling");
			path.toFile().deleteOnExit();
			Files.createDirectories(PROFILING_RESULTS_DIR);
			Path path2 = path.resolve(this.rootFolderName);
			Path path3 = path2.resolve("metrics");
			this.saveMetrics(set, path3);
			if (!map.isEmpty()) {
				this.saveDeviations(map, path2.resolve("deviations"));
			}

			this.saveProfilingTaskExecutionResult(profileResults, path2);
			return path;
		} catch (IOException var7) {
			throw new UncheckedIOException(var7);
		}
	}

	private void saveMetrics(Set<MetricSampler> set, Path path) {
		if (set.isEmpty()) {
			throw new IllegalArgumentException("Expected at least one sampler to persist");
		} else {
			Map<MetricCategory, List<MetricSampler>> map = (Map<MetricCategory, List<MetricSampler>>)set.stream()
				.collect(Collectors.groupingBy(MetricSampler::getCategory));
			map.forEach((metricCategory, list) -> this.saveCategory(metricCategory, list, path));
		}
	}

	private void saveCategory(MetricCategory metricCategory, List<MetricSampler> list, Path path) {
		Path path2 = path.resolve(Util.sanitizeName(metricCategory.getDescription(), ResourceLocation::validPathChar) + ".csv");
		Writer writer = null;

		try {
			Files.createDirectories(path2.getParent());
			writer = Files.newBufferedWriter(path2, StandardCharsets.UTF_8);
			CsvOutput.Builder builder = CsvOutput.builder();
			builder.addColumn("@tick");

			for (MetricSampler metricSampler : list) {
				builder.addColumn(metricSampler.getName());
			}

			CsvOutput csvOutput = builder.build(writer);
			List<MetricSampler.SamplerResult> list2 = (List<MetricSampler.SamplerResult>)list.stream().map(MetricSampler::result).collect(Collectors.toList());
			int i = list2.stream().mapToInt(MetricSampler.SamplerResult::getFirstTick).summaryStatistics().getMin();
			int j = list2.stream().mapToInt(MetricSampler.SamplerResult::getLastTick).summaryStatistics().getMax();

			for (int k = i; k <= j; k++) {
				int l = k;
				Stream<String> stream = list2.stream().map(samplerResult -> String.valueOf(samplerResult.valueAtTick(l)));
				Object[] objects = Stream.concat(Stream.of(String.valueOf(k)), stream).toArray(String[]::new);
				csvOutput.writeRow(objects);
			}

			LOGGER.info("Flushed metrics to {}", path2);
		} catch (Exception var18) {
			LOGGER.error("Could not save profiler results to {}", path2, var18);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	private void saveDeviations(Map<MetricSampler, List<RecordedDeviation>> map, Path path) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.SSS", Locale.UK).withZone(ZoneId.systemDefault());
		map.forEach(
			(metricSampler, list) -> list.forEach(
					recordedDeviation -> {
						String string = dateTimeFormatter.format(recordedDeviation.timestamp);
						Path path2 = path.resolve(Util.sanitizeName(metricSampler.getName(), ResourceLocation::validPathChar))
							.resolve(String.format(Locale.ROOT, "%d@%s.txt", recordedDeviation.tick, string));
						recordedDeviation.profilerResultAtTick.saveResults(path2);
					}
				)
		);
	}

	private void saveProfilingTaskExecutionResult(ProfileResults profileResults, Path path) {
		profileResults.saveResults(path.resolve("profiling.txt"));
	}
}
