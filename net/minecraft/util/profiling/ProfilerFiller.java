/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.metrics.MetricCategory;

public interface ProfilerFiller {
    public static final String ROOT = "root";

    public void startTick();

    public void endTick();

    public void push(String var1);

    public void push(Supplier<String> var1);

    public void pop();

    public void popPush(String var1);

    public void popPush(Supplier<String> var1);

    public void markForCharting(MetricCategory var1);

    default public void incrementCounter(String string) {
        this.incrementCounter(string, 1);
    }

    public void incrementCounter(String var1, int var2);

    default public void incrementCounter(Supplier<String> supplier) {
        this.incrementCounter(supplier, 1);
    }

    public void incrementCounter(Supplier<String> var1, int var2);

    public static ProfilerFiller tee(final ProfilerFiller profilerFiller, final ProfilerFiller profilerFiller2) {
        if (profilerFiller == InactiveProfiler.INSTANCE) {
            return profilerFiller2;
        }
        if (profilerFiller2 == InactiveProfiler.INSTANCE) {
            return profilerFiller;
        }
        return new ProfilerFiller(){

            @Override
            public void startTick() {
                profilerFiller.startTick();
                profilerFiller2.startTick();
            }

            @Override
            public void endTick() {
                profilerFiller.endTick();
                profilerFiller2.endTick();
            }

            @Override
            public void push(String string) {
                profilerFiller.push(string);
                profilerFiller2.push(string);
            }

            @Override
            public void push(Supplier<String> supplier) {
                profilerFiller.push(supplier);
                profilerFiller2.push(supplier);
            }

            @Override
            public void markForCharting(MetricCategory metricCategory) {
                profilerFiller.markForCharting(metricCategory);
                profilerFiller2.markForCharting(metricCategory);
            }

            @Override
            public void pop() {
                profilerFiller.pop();
                profilerFiller2.pop();
            }

            @Override
            public void popPush(String string) {
                profilerFiller.popPush(string);
                profilerFiller2.popPush(string);
            }

            @Override
            public void popPush(Supplier<String> supplier) {
                profilerFiller.popPush(supplier);
                profilerFiller2.popPush(supplier);
            }

            @Override
            public void incrementCounter(String string, int i) {
                profilerFiller.incrementCounter(string, i);
                profilerFiller2.incrementCounter(string, i);
            }

            @Override
            public void incrementCounter(Supplier<String> supplier, int i) {
                profilerFiller.incrementCounter(supplier, i);
                profilerFiller2.incrementCounter(supplier, i);
            }
        };
    }
}

