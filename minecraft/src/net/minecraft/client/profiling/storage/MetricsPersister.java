package net.minecraft.client.profiling.storage;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.profiling.metric.FpsSpikeRecording;
import net.minecraft.client.profiling.metric.MetricSampler;
import net.minecraft.client.profiling.metric.SamplerCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.profiling.ContinuousProfiler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class MetricsPersister {
	public static final Path PROFILING_RESULTS_DIR = Paths.get("debug/profiling");
	public static final String METRICS_DIR_NAME = "metrics";
	public static final String DEVIATIONS_DIR_NAME = "deviations";
	public static final String PROFILING_RESULT_FILENAME = "profiling.txt";
	private static final Logger LOGGER = LogManager.getLogger();
	public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = (FileSystemProvider)FileSystemProvider.installedProviders()
		.stream()
		.filter(fileSystemProvider -> fileSystemProvider.getScheme().equalsIgnoreCase("jar"))
		.findFirst()
		.orElseThrow(() -> new IllegalStateException("No jar file system provider found"));

	public Path saveReports(List<SamplerCategory> list, List<FpsSpikeRecording> list2, ContinuousProfiler continuousProfiler) {
		try {
			Files.createDirectories(PROFILING_RESULTS_DIR);
		} catch (IOException var20) {
			throw new UncheckedIOException(var20);
		}

		Path path = PROFILING_RESULTS_DIR.resolve(new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".tmp");

		try {
			FileSystem fileSystem = ZIP_FILE_SYSTEM_PROVIDER.newFileSystem(path, ImmutableMap.of("create", "true"));
			Throwable var6 = null;

			try {
				Files.createDirectories(PROFILING_RESULTS_DIR);
				Path path2 = fileSystem.getPath("/");
				Path path3 = path2.resolve("metrics");

				for (SamplerCategory samplerCategory : list) {
					this.saveMetrics(samplerCategory, path3);
				}

				if (!list2.isEmpty()) {
					this.saveSpikeLogs(list2, path2.resolve("deviations"));
				}

				this.saveProfilingTaskExecutionResult(continuousProfiler, path2);
			} catch (Throwable var21) {
				var6 = var21;
				throw var21;
			} finally {
				if (fileSystem != null) {
					if (var6 != null) {
						try {
							fileSystem.close();
						} catch (Throwable var19) {
							var6.addSuppressed(var19);
						}
					} else {
						fileSystem.close();
					}
				}
			}
		} catch (IOException var23) {
			throw new UncheckedIOException(var23);
		}

		return this.renameZipFile(path);
	}

	private void saveMetrics(SamplerCategory samplerCategory, Path path) {
		String string = samplerCategory.getName();
		List<MetricSampler> list = samplerCategory.getMetricSamplers();
		if (list.isEmpty()) {
			throw new IllegalArgumentException("Expected at least one sampler for category: " + string);
		} else {
			IntSummaryStatistics intSummaryStatistics = (IntSummaryStatistics)list.stream().collect(Collectors.summarizingInt(MetricSampler::numberOfValues));
			if (intSummaryStatistics.getMax() != intSummaryStatistics.getMin()) {
				throw new IllegalStateException(
					String.format("Expected all samples within category %s to contain same amount of samples, got %s", samplerCategory, intSummaryStatistics)
				);
			} else {
				Path path2 = path.resolve(Util.sanitizeName(string, ResourceLocation::validPathChar) + ".csv");
				Writer writer = null;

				try {
					Files.createDirectories(path2.getParent());
					writer = Files.newBufferedWriter(path2, StandardCharsets.UTF_8);
					CsvOutput.Builder builder = CsvOutput.builder();

					for (MetricSampler metricSampler : list) {
						builder.addColumn(metricSampler.getMetric().getName());
					}

					CsvOutput csvOutput = builder.build(writer);

					while (((MetricSampler)list.get(0)).hasMoreValues()) {
						Double[] doubles = (Double[])list.stream().map(MetricSampler::readNextValue).toArray(Double[]::new);
						csvOutput.writeRow(doubles);
					}

					LOGGER.info("Flushed metrics to {}", path2);
				} catch (Exception var14) {
					LOGGER.error("Could not save profiler results to {}", path2, var14);
				} finally {
					IOUtils.closeQuietly(writer);
				}
			}
		}
	}

	private void saveSpikeLogs(List<FpsSpikeRecording> list, Path path) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");

		for (FpsSpikeRecording fpsSpikeRecording : list) {
			String string = simpleDateFormat.format(fpsSpikeRecording.timestamp);
			Path path2 = path.resolve(String.format("%d@%s.txt", fpsSpikeRecording.tick, string));
			fpsSpikeRecording.profilerResultForSpikeFrame.saveResults(path2);
		}
	}

	private void saveProfilingTaskExecutionResult(ContinuousProfiler continuousProfiler, Path path) {
		continuousProfiler.getResults().saveResults(path.resolve("profiling.txt"));
	}

	private Path renameZipFile(Path path) {
		try {
			return Files.move(path, path.resolveSibling(StringUtils.substringBefore(path.getFileName().toString(), ".tmp") + ".zip"));
		} catch (IOException var3) {
			throw new UncheckedIOException(var3);
		}
	}
}
