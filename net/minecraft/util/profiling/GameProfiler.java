/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling;

import java.time.Duration;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameProfiler
implements ProfilerFiller {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final long MAXIMUM_TICK_TIME_NANOS = Duration.ofMillis(300L).toNanos();
    private final IntSupplier getTickTime;
    private final ProfilerImpl continuous = new ProfilerImpl();
    private final ProfilerImpl perTick = new ProfilerImpl();

    public GameProfiler(IntSupplier intSupplier) {
        this.getTickTime = intSupplier;
    }

    public Profiler continuous() {
        return this.continuous;
    }

    @Override
    public void startTick() {
        this.continuous.collector.startTick();
        this.perTick.collector.startTick();
    }

    @Override
    public void endTick() {
        this.continuous.collector.endTick();
        this.perTick.collector.endTick();
    }

    @Override
    public void push(String string) {
        this.continuous.collector.push(string);
        this.perTick.collector.push(string);
    }

    @Override
    public void push(Supplier<String> supplier) {
        this.continuous.collector.push(supplier);
        this.perTick.collector.push(supplier);
    }

    @Override
    public void pop() {
        this.continuous.collector.pop();
        this.perTick.collector.pop();
    }

    @Override
    public void popPush(String string) {
        this.continuous.collector.popPush(string);
        this.perTick.collector.popPush(string);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void popPush(Supplier<String> supplier) {
        this.continuous.collector.popPush(supplier);
        this.perTick.collector.popPush(supplier);
    }

    class ProfilerImpl
    implements Profiler {
        protected ProfileCollector collector = InactiveProfiler.INACTIVE;

        private ProfilerImpl() {
        }

        @Override
        public boolean isEnabled() {
            return this.collector != InactiveProfiler.INACTIVE;
        }

        @Override
        public ProfileResults disable() {
            ProfileResults profileResults = this.collector.getResults();
            this.collector = InactiveProfiler.INACTIVE;
            return profileResults;
        }

        @Override
        @Environment(value=EnvType.CLIENT)
        public ProfileResults getResults() {
            return this.collector.getResults();
        }

        @Override
        public void enable() {
            if (this.collector == InactiveProfiler.INACTIVE) {
                this.collector = new ActiveProfiler(Util.getNanos(), GameProfiler.this.getTickTime, true);
            }
        }
    }

    public static interface Profiler {
        public boolean isEnabled();

        public ProfileResults disable();

        @Environment(value=EnvType.CLIENT)
        public ProfileResults getResults();

        public void enable();
    }
}

