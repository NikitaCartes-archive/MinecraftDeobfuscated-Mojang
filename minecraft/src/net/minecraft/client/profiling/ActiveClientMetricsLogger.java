package net.minecraft.client.profiling;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.profiling.metric.FpsSpikeRecording;
import net.minecraft.client.profiling.metric.MetricSampler;
import net.minecraft.client.profiling.metric.SamplerCategory;
import net.minecraft.client.profiling.metric.TaskSamplerBuilder;
import net.minecraft.client.profiling.storage.MetricsPersister;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.registry.MeasurementRegistry;

@Environment(EnvType.CLIENT)
public class ActiveClientMetricsLogger implements ClientMetricsLogger {
	public static final int PROFILING_MAX_DURATION_SECONDS = 10;
	@Nullable
	private static Consumer<Path> globalOnReportFinished = null;
	private final List<SamplerCategory> samplerCategories = new ObjectArrayList<>();
	private final ContinuousProfiler taskProfiler;
	private final Executor ioExecutor;
	private final MetricsPersister metricsPersister;
	private final Runnable onFinished;
	private final Consumer<Path> onReportFinished;
	private final LongSupplier wallTimeSource;
	private final List<FpsSpikeRecording> fpsSpikeRecordings = new ObjectArrayList<>();
	private final long deadlineNano;
	private int currentTick;
	private ProfileCollector singleTickProfiler;
	private volatile boolean killSwitch;

	private ActiveClientMetricsLogger(LongSupplier longSupplier, Executor executor, MetricsPersister metricsPersister, Runnable runnable, Consumer<Path> consumer) {
		this.wallTimeSource = longSupplier;
		this.taskProfiler = new ContinuousProfiler(longSupplier, () -> this.currentTick);
		this.ioExecutor = executor;
		this.metricsPersister = metricsPersister;
		this.onFinished = runnable;
		this.onReportFinished = globalOnReportFinished == null ? consumer : consumer.andThen(globalOnReportFinished);
		this.deadlineNano = longSupplier.getAsLong() + TimeUnit.NANOSECONDS.convert(10L, TimeUnit.SECONDS);
		this.addSamplers();
		this.singleTickProfiler = new ActiveProfiler(this.wallTimeSource, () -> this.currentTick, false);
		this.taskProfiler.enable();
	}

	public static ActiveClientMetricsLogger createStarted(
		LongSupplier longSupplier, Executor executor, MetricsPersister metricsPersister, Runnable runnable, Consumer<Path> consumer
	) {
		return new ActiveClientMetricsLogger(longSupplier, executor, metricsPersister, runnable, consumer);
	}

	private void addSamplers() {
		this.samplerCategories
			.add(
				new SamplerCategory(
					"JVM", MetricSampler.create("heap (Mb)", () -> (double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576.0)
				)
			);
		this.samplerCategories.add(new SamplerCategory("Frame times (ms)", this.frameTimeSampler(this.wallTimeSource)));
		this.samplerCategories
			.add(
				new SamplerCategory(
					"Task total durations (ms)",
					this.profilerTaskSampler("gameRendering").forPath("root", "gameRenderer"),
					this.profilerTaskSampler("updateDisplay").forPath("root", "updateDisplay"),
					this.profilerTaskSampler("skyRendering").forPath("root", "gameRenderer", "level", "sky")
				)
			);
		LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
		this.samplerCategories
			.add(
				new SamplerCategory(
					"Rendering chunk dispatching",
					MetricSampler.create("totalChunks", levelRenderer, LevelRenderer::getTotalChunks),
					MetricSampler.create("renderedChunks", levelRenderer, LevelRenderer::countRenderedChunks),
					MetricSampler.create("lastViewDistance", levelRenderer, LevelRenderer::getLastViewDistance)
				)
			);
		ChunkRenderDispatcher chunkRenderDispatcher = levelRenderer.getChunkRenderDispatcher();
		this.samplerCategories
			.add(
				new SamplerCategory(
					"Rendering chunk stats",
					MetricSampler.create("toUpload", chunkRenderDispatcher, ChunkRenderDispatcher::getToUpload),
					MetricSampler.create("freeBufferCount", chunkRenderDispatcher, ChunkRenderDispatcher::getFreeBufferCount),
					MetricSampler.create("toBatchCount", chunkRenderDispatcher, ChunkRenderDispatcher::getToBatchCount)
				)
			);
		MeasurementRegistry.INSTANCE
			.getMetricsByCategories()
			.forEach(
				(measurementCategory, list) -> {
					List<MetricSampler> list2 = (List<MetricSampler>)list.stream()
						.map(measuredMetric -> MetricSampler.create(measuredMetric.getMetric(), measuredMetric.getCurrentValue()))
						.collect(Collectors.toList());
					this.samplerCategories.add(new SamplerCategory(measurementCategory.getName(), list2));
				}
			);
	}

	private TaskSamplerBuilder profilerTaskSampler(String string) {
		return new TaskSamplerBuilder(string, () -> this.singleTickProfiler);
	}

	private MetricSampler frameTimeSampler(LongSupplier longSupplier) {
		Stopwatch stopwatch = Stopwatch.createUnstarted(new Ticker() {
			@Override
			public long read() {
				return longSupplier.getAsLong();
			}
		});
		ToDoubleFunction<Stopwatch> toDoubleFunction = stopwatchx -> {
			if (stopwatchx.isRunning()) {
				stopwatchx.stop();
			}

			long l = stopwatchx.elapsed(TimeUnit.MILLISECONDS);
			stopwatchx.reset();
			return (double)l;
		};
		MetricSampler.ValueIncreased valueIncreased = new MetricSampler.ValueIncreased(
			0.5F, d -> this.fpsSpikeRecordings.add(new FpsSpikeRecording(new Date(), this.currentTick, this.singleTickProfiler.getResults()))
		);
		return MetricSampler.builder("frametime", toDoubleFunction, stopwatch).withBeforeTick(Stopwatch::start).withThresholdAlert(valueIncreased).build();
	}

	@Override
	public synchronized void end() {
		if (this.isRecording()) {
			this.killSwitch = true;
		}
	}

	@Override
	public void startTick() {
		this.verifyStarted();

		for (SamplerCategory samplerCategory : this.samplerCategories) {
			samplerCategory.onStartTick();
		}

		this.currentTick++;
	}

	@Override
	public void endTick() {
		this.verifyStarted();
		if (this.currentTick != 0) {
			for (SamplerCategory samplerCategory : this.samplerCategories) {
				samplerCategory.onEndTick();
			}

			if (!this.killSwitch && this.wallTimeSource.getAsLong() <= this.deadlineNano) {
				this.singleTickProfiler = new ActiveProfiler(this.wallTimeSource, () -> this.currentTick, false);
			} else {
				this.onFinished.run();
				this.killSwitch = false;
				this.singleTickProfiler = InactiveProfiler.INSTANCE;
				this.scheduleSaveResults();
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

	private void scheduleSaveResults() {
		this.ioExecutor.execute(() -> {
			Path path = this.metricsPersister.saveReports(this.samplerCategories, this.fpsSpikeRecordings, this.taskProfiler);

			for (SamplerCategory samplerCategory : this.samplerCategories) {
				samplerCategory.onFinished();
			}

			this.samplerCategories.clear();
			this.fpsSpikeRecordings.clear();
			this.taskProfiler.disable();
			this.onReportFinished.accept(path);
		});
	}

	public static void registerGlobalCompletionCallback(Consumer<Path> consumer) {
		globalOnReportFinished = consumer;
	}
}
