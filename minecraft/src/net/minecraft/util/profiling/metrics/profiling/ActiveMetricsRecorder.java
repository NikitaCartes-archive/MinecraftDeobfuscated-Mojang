package net.minecraft.util.profiling.metrics.profiling;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsSamplerProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.profiling.metrics.storage.RecordedDeviation;

public class ActiveMetricsRecorder implements MetricsRecorder {
	public static final int PROFILING_MAX_DURATION_SECONDS = 10;
	@Nullable
	private static Consumer<Path> globalOnReportFinished = null;
	private final Map<MetricSampler, List<RecordedDeviation>> deviationsBySampler = new Object2ObjectOpenHashMap<>();
	private final ContinuousProfiler taskProfiler;
	private final Executor ioExecutor;
	private final MetricsPersister metricsPersister;
	private final Consumer<ProfileResults> onProfilingEnd;
	private final Consumer<Path> onReportFinished;
	private final MetricsSamplerProvider metricsSamplerProvider;
	private final LongSupplier wallTimeSource;
	private final long deadlineNano;
	private int currentTick;
	private ProfileCollector singleTickProfiler;
	private volatile boolean killSwitch;
	private Set<MetricSampler> thisTickSamplers = ImmutableSet.of();

	private ActiveMetricsRecorder(
		MetricsSamplerProvider metricsSamplerProvider,
		LongSupplier longSupplier,
		Executor executor,
		MetricsPersister metricsPersister,
		Consumer<ProfileResults> consumer,
		Consumer<Path> consumer2
	) {
		this.metricsSamplerProvider = metricsSamplerProvider;
		this.wallTimeSource = longSupplier;
		this.taskProfiler = new ContinuousProfiler(longSupplier, () -> this.currentTick);
		this.ioExecutor = executor;
		this.metricsPersister = metricsPersister;
		this.onProfilingEnd = consumer;
		this.onReportFinished = globalOnReportFinished == null ? consumer2 : consumer2.andThen(globalOnReportFinished);
		this.deadlineNano = longSupplier.getAsLong() + TimeUnit.NANOSECONDS.convert(10L, TimeUnit.SECONDS);
		this.singleTickProfiler = new ActiveProfiler(this.wallTimeSource, () -> this.currentTick, false);
		this.taskProfiler.enable();
	}

	public static ActiveMetricsRecorder createStarted(
		MetricsSamplerProvider metricsSamplerProvider,
		LongSupplier longSupplier,
		Executor executor,
		MetricsPersister metricsPersister,
		Consumer<ProfileResults> consumer,
		Consumer<Path> consumer2
	) {
		return new ActiveMetricsRecorder(metricsSamplerProvider, longSupplier, executor, metricsPersister, consumer, consumer2);
	}

	@Override
	public synchronized void end() {
		if (this.isRecording()) {
			this.killSwitch = true;
		}
	}

	@Override
	public synchronized void cancel() {
		if (this.isRecording()) {
			this.singleTickProfiler = InactiveProfiler.INSTANCE;
			this.onProfilingEnd.accept(EmptyProfileResults.EMPTY);
			this.cleanup(this.thisTickSamplers);
		}
	}

	@Override
	public void startTick() {
		this.verifyStarted();
		this.thisTickSamplers = this.metricsSamplerProvider.samplers(() -> this.singleTickProfiler);

		for (MetricSampler metricSampler : this.thisTickSamplers) {
			metricSampler.onStartTick();
		}

		this.currentTick++;
	}

	@Override
	public void endTick() {
		this.verifyStarted();
		if (this.currentTick != 0) {
			for (MetricSampler metricSampler : this.thisTickSamplers) {
				metricSampler.onEndTick(this.currentTick);
				if (metricSampler.triggersThreshold()) {
					RecordedDeviation recordedDeviation = new RecordedDeviation(Instant.now(), this.currentTick, this.singleTickProfiler.getResults());
					((List)this.deviationsBySampler.computeIfAbsent(metricSampler, metricSamplerx -> Lists.newArrayList())).add(recordedDeviation);
				}
			}

			if (!this.killSwitch && this.wallTimeSource.getAsLong() <= this.deadlineNano) {
				this.singleTickProfiler = new ActiveProfiler(this.wallTimeSource, () -> this.currentTick, false);
			} else {
				this.killSwitch = false;
				ProfileResults profileResults = this.taskProfiler.getResults();
				this.singleTickProfiler = InactiveProfiler.INSTANCE;
				this.onProfilingEnd.accept(profileResults);
				this.scheduleSaveResults(profileResults);
			}
		}
	}

	@Override
	public boolean isRecording() {
		return this.taskProfiler.isEnabled();
	}

	@Override
	public ProfilerFiller getProfiler() {
		return ProfilerFiller.tee(this.taskProfiler.getFiller(), this.singleTickProfiler);
	}

	private void verifyStarted() {
		if (!this.isRecording()) {
			throw new IllegalStateException("Not started!");
		}
	}

	private void scheduleSaveResults(ProfileResults profileResults) {
		HashSet<MetricSampler> hashSet = new HashSet(this.thisTickSamplers);
		this.ioExecutor.execute(() -> {
			Path path = this.metricsPersister.saveReports(hashSet, this.deviationsBySampler, profileResults);
			this.cleanup(hashSet);
			this.onReportFinished.accept(path);
		});
	}

	private void cleanup(Collection<MetricSampler> collection) {
		for (MetricSampler metricSampler : collection) {
			metricSampler.onFinished();
		}

		this.deviationsBySampler.clear();
		this.taskProfiler.disable();
	}

	public static void registerGlobalCompletionCallback(Consumer<Path> consumer) {
		globalOnReportFinished = consumer;
	}
}
