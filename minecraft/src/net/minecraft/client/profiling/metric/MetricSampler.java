package net.minecraft.client.profiling.metric;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.profiling.registry.Metric;

@Environment(EnvType.CLIENT)
public class MetricSampler {
	private final Metric metric;
	private final DoubleSupplier sampler;
	private final ByteBuf values;
	private volatile boolean isRunning;
	@Nullable
	private final Runnable beforeTick;
	@Nullable
	private final MetricSampler.ThresholdAlerter thresholdAlerter;

	<T> MetricSampler(Metric metric, DoubleSupplier doubleSupplier, @Nullable Runnable runnable, @Nullable MetricSampler.ThresholdAlerter thresholdAlerter) {
		this.metric = metric;
		this.beforeTick = runnable;
		this.sampler = doubleSupplier;
		this.thresholdAlerter = thresholdAlerter;
		this.values = new FriendlyByteBuf(Unpooled.directBuffer());
		this.isRunning = true;
	}

	public static MetricSampler create(Metric metric, DoubleSupplier doubleSupplier) {
		return new MetricSampler(metric, doubleSupplier, null, null);
	}

	public static MetricSampler create(String string, DoubleSupplier doubleSupplier) {
		return create(new Metric(string), doubleSupplier);
	}

	public static <T> MetricSampler create(String string, T object, ToDoubleFunction<T> toDoubleFunction) {
		return builder(string, toDoubleFunction, object).build();
	}

	public static <T> MetricSampler.MetricSamplerBuilder<T> builder(String string, ToDoubleFunction<T> toDoubleFunction, T object) {
		return new MetricSampler.MetricSamplerBuilder<>(new Metric(string), toDoubleFunction, object);
	}

	public int numberOfValues() {
		return this.values.readableBytes() / 8;
	}

	public void onStartTick() {
		if (!this.isRunning) {
			throw new IllegalStateException("Not running");
		} else {
			if (this.beforeTick != null) {
				this.beforeTick.run();
			}
		}
	}

	public void onEndTick() {
		this.verifyRunning();
		double d = this.sampler.getAsDouble();
		this.values.writeDouble(d);
		if (this.thresholdAlerter != null) {
			this.thresholdAlerter.test(d);
		}
	}

	public void onFinished() {
		this.verifyRunning();
		this.values.release();
		this.isRunning = false;
	}

	private void verifyRunning() {
		if (!this.isRunning) {
			throw new IllegalStateException(String.format("Sampler for metric %s not started!", this.metric.getName()));
		}
	}

	public Metric getMetric() {
		return this.metric;
	}

	public boolean hasMoreValues() {
		return this.values.isReadable(8);
	}

	public double readNextValue() {
		return this.values.readDouble();
	}

	@Environment(EnvType.CLIENT)
	public static class MetricSamplerBuilder<T> {
		private final Metric metric;
		private final DoubleSupplier sampler;
		private final T context;
		@Nullable
		private Runnable beforeTick = null;
		@Nullable
		private MetricSampler.ThresholdAlerter thresholdAlerter;

		public MetricSamplerBuilder(Metric metric, ToDoubleFunction<T> toDoubleFunction, T object) {
			this.metric = metric;
			this.sampler = () -> toDoubleFunction.applyAsDouble(object);
			this.context = object;
		}

		public MetricSampler.MetricSamplerBuilder<T> withBeforeTick(Consumer<T> consumer) {
			this.beforeTick = () -> consumer.accept(this.context);
			return this;
		}

		public MetricSampler.MetricSamplerBuilder<T> withThresholdAlert(MetricSampler.ThresholdAlerter thresholdAlerter) {
			this.thresholdAlerter = thresholdAlerter;
			return this;
		}

		public MetricSampler build() {
			return new MetricSampler(this.metric, this.sampler, this.beforeTick, this.thresholdAlerter);
		}
	}

	@Environment(EnvType.CLIENT)
	public interface ThresholdAlerter {
		void test(double d);
	}

	@Environment(EnvType.CLIENT)
	public static class ValueIncreased implements MetricSampler.ThresholdAlerter {
		private final float percentageIncreaseThreshold;
		private final DoubleConsumer action;
		private double previousValue = Double.MIN_VALUE;

		public ValueIncreased(float f, DoubleConsumer doubleConsumer) {
			this.percentageIncreaseThreshold = f;
			this.action = doubleConsumer;
		}

		@Override
		public void test(double d) {
			boolean bl = this.previousValue != Double.MIN_VALUE
				&& d > this.previousValue
				&& (d - this.previousValue) / this.previousValue >= (double)this.percentageIncreaseThreshold;
			if (bl) {
				this.action.accept(d);
			}

			this.previousValue = d;
		}
	}
}
