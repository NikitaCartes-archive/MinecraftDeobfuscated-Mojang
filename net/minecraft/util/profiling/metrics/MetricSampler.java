/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.metrics;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.jetbrains.annotations.Nullable;

public class MetricSampler {
    private final String name;
    private final MetricCategory category;
    private final DoubleSupplier sampler;
    private final ByteBuf ticks;
    private final ByteBuf values;
    private volatile boolean isRunning;
    @Nullable
    private final Runnable beforeTick;
    @Nullable
    final ThresholdTest thresholdTest;
    private double currentValue;

    protected MetricSampler(String string, MetricCategory metricCategory, DoubleSupplier doubleSupplier, @Nullable Runnable runnable, @Nullable ThresholdTest thresholdTest) {
        this.name = string;
        this.category = metricCategory;
        this.beforeTick = runnable;
        this.sampler = doubleSupplier;
        this.thresholdTest = thresholdTest;
        this.values = ByteBufAllocator.DEFAULT.buffer();
        this.ticks = ByteBufAllocator.DEFAULT.buffer();
        this.isRunning = true;
    }

    public static MetricSampler create(String string, MetricCategory metricCategory, DoubleSupplier doubleSupplier) {
        return new MetricSampler(string, metricCategory, doubleSupplier, null, null);
    }

    public static <T> MetricSampler create(String string, MetricCategory metricCategory, T object, ToDoubleFunction<T> toDoubleFunction) {
        return MetricSampler.builder(string, metricCategory, toDoubleFunction, object).build();
    }

    public static <T> MetricSamplerBuilder<T> builder(String string, MetricCategory metricCategory, ToDoubleFunction<T> toDoubleFunction, T object) {
        return new MetricSamplerBuilder<T>(string, metricCategory, toDoubleFunction, object);
    }

    public void onStartTick() {
        if (!this.isRunning) {
            throw new IllegalStateException("Not running");
        }
        if (this.beforeTick != null) {
            this.beforeTick.run();
        }
    }

    public void onEndTick(int i) {
        this.verifyRunning();
        this.currentValue = this.sampler.getAsDouble();
        this.values.writeDouble(this.currentValue);
        this.ticks.writeInt(i);
    }

    public void onFinished() {
        this.verifyRunning();
        this.values.release();
        this.ticks.release();
        this.isRunning = false;
    }

    private void verifyRunning() {
        if (!this.isRunning) {
            throw new IllegalStateException(String.format(Locale.ROOT, "Sampler for metric %s not started!", this.name));
        }
    }

    DoubleSupplier getSampler() {
        return this.sampler;
    }

    public String getName() {
        return this.name;
    }

    public MetricCategory getCategory() {
        return this.category;
    }

    public SamplerResult result() {
        Int2DoubleOpenHashMap int2DoubleMap = new Int2DoubleOpenHashMap();
        int i = Integer.MIN_VALUE;
        int j = Integer.MIN_VALUE;
        while (this.values.isReadable(8)) {
            int k = this.ticks.readInt();
            if (i == Integer.MIN_VALUE) {
                i = k;
            }
            int2DoubleMap.put(k, this.values.readDouble());
            j = k;
        }
        return new SamplerResult(i, j, int2DoubleMap);
    }

    public boolean triggersThreshold() {
        return this.thresholdTest != null && this.thresholdTest.test(this.currentValue);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        MetricSampler metricSampler = (MetricSampler)object;
        return this.name.equals(metricSampler.name) && this.category.equals((Object)metricSampler.category);
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public static interface ThresholdTest {
        public boolean test(double var1);
    }

    public static class MetricSamplerBuilder<T> {
        private final String name;
        private final MetricCategory category;
        private final DoubleSupplier sampler;
        private final T context;
        @Nullable
        private Runnable beforeTick;
        @Nullable
        private ThresholdTest thresholdTest;

        public MetricSamplerBuilder(String string, MetricCategory metricCategory, ToDoubleFunction<T> toDoubleFunction, T object) {
            this.name = string;
            this.category = metricCategory;
            this.sampler = () -> toDoubleFunction.applyAsDouble(object);
            this.context = object;
        }

        public MetricSamplerBuilder<T> withBeforeTick(Consumer<T> consumer) {
            this.beforeTick = () -> consumer.accept(this.context);
            return this;
        }

        public MetricSamplerBuilder<T> withThresholdAlert(ThresholdTest thresholdTest) {
            this.thresholdTest = thresholdTest;
            return this;
        }

        public MetricSampler build() {
            return new MetricSampler(this.name, this.category, this.sampler, this.beforeTick, this.thresholdTest);
        }
    }

    public static class SamplerResult {
        private final Int2DoubleMap recording;
        private final int firstTick;
        private final int lastTick;

        public SamplerResult(int i, int j, Int2DoubleMap int2DoubleMap) {
            this.firstTick = i;
            this.lastTick = j;
            this.recording = int2DoubleMap;
        }

        public double valueAtTick(int i) {
            return this.recording.get(i);
        }

        public int getFirstTick() {
            return this.firstTick;
        }

        public int getLastTick() {
            return this.lastTick;
        }
    }

    public static class ValueIncreasedByPercentage
    implements ThresholdTest {
        private final float percentageIncreaseThreshold;
        private double previousValue = Double.MIN_VALUE;

        public ValueIncreasedByPercentage(float f) {
            this.percentageIncreaseThreshold = f;
        }

        @Override
        public boolean test(double d) {
            boolean bl = this.previousValue == Double.MIN_VALUE || d <= this.previousValue ? false : (d - this.previousValue) / this.previousValue >= (double)this.percentageIncreaseThreshold;
            this.previousValue = d;
            return bl;
        }
    }
}

