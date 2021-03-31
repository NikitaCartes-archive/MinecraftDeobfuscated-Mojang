/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.profiling.storage;

import com.google.common.collect.ImmutableMap;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
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

@Environment(value=EnvType.CLIENT)
public class MetricsPersister {
    public static final Path PROFILING_RESULTS_DIR = Paths.get("debug/profiling", new String[0]);
    public static final String METRICS_DIR_NAME = "metrics";
    public static final String DEVIATIONS_DIR_NAME = "deviations";
    public static final String PROFILING_RESULT_FILENAME = "profiling.txt";
    private static final Logger LOGGER = LogManager.getLogger();
    public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders().stream().filter(fileSystemProvider -> fileSystemProvider.getScheme().equalsIgnoreCase("jar")).findFirst().orElseThrow(() -> new IllegalStateException("No jar file system provider found"));

    public Path saveReports(List<SamplerCategory> list, List<FpsSpikeRecording> list2, ContinuousProfiler continuousProfiler) {
        try {
            Files.createDirectories(PROFILING_RESULTS_DIR, new FileAttribute[0]);
        } catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
        Path path = PROFILING_RESULTS_DIR.resolve(new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".tmp");
        try (FileSystem fileSystem = ZIP_FILE_SYSTEM_PROVIDER.newFileSystem(path, ImmutableMap.of("create", "true"));){
            Files.createDirectories(PROFILING_RESULTS_DIR, new FileAttribute[0]);
            Path path2 = fileSystem.getPath("/", new String[0]);
            Path path3 = path2.resolve(METRICS_DIR_NAME);
            for (SamplerCategory samplerCategory : list) {
                this.saveMetrics(samplerCategory, path3);
            }
            if (!list2.isEmpty()) {
                this.saveSpikeLogs(list2, path2.resolve(DEVIATIONS_DIR_NAME));
            }
            this.saveProfilingTaskExecutionResult(continuousProfiler, path2);
        } catch (IOException iOException2) {
            throw new UncheckedIOException(iOException2);
        }
        return this.renameZipFile(path);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void saveMetrics(SamplerCategory samplerCategory, Path path) {
        String string = samplerCategory.getName();
        List<MetricSampler> list = samplerCategory.getMetricSamplers();
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Expected at least one sampler for category: " + string);
        }
        IntSummaryStatistics intSummaryStatistics = list.stream().collect(Collectors.summarizingInt(MetricSampler::numberOfValues));
        if (intSummaryStatistics.getMax() != intSummaryStatistics.getMin()) {
            throw new IllegalStateException(String.format("Expected all samples within category %s to contain same amount of samples, got %s", samplerCategory, intSummaryStatistics));
        }
        Path path2 = path.resolve(Util.sanitizeName(string, ResourceLocation::validPathChar) + ".csv");
        BufferedWriter writer = null;
        try {
            Files.createDirectories(path2.getParent(), new FileAttribute[0]);
            writer = Files.newBufferedWriter(path2, StandardCharsets.UTF_8, new OpenOption[0]);
            CsvOutput.Builder builder = CsvOutput.builder();
            for (MetricSampler metricSampler : list) {
                builder.addColumn(metricSampler.getMetric().getName());
            }
            CsvOutput csvOutput = builder.build(writer);
            while (list.get(0).hasMoreValues()) {
                Double[] doubles = (Double[])list.stream().map(MetricSampler::readNextValue).toArray(Double[]::new);
                csvOutput.writeRow(doubles);
            }
            LOGGER.info("Flushed metrics to {}", (Object)path2);
            IOUtils.closeQuietly(writer);
        } catch (Exception exception) {
            LOGGER.error("Could not save profiler results to {}", (Object)path2, (Object)exception);
        } finally {
            IOUtils.closeQuietly(writer);
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
        continuousProfiler.getResults().saveResults(path.resolve(PROFILING_RESULT_FILENAME));
    }

    private Path renameZipFile(Path path) {
        try {
            return Files.move(path, path.resolveSibling(StringUtils.substringBefore(path.getFileName().toString(), ".tmp") + ".zip"), new CopyOption[0]);
        } catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }
}

