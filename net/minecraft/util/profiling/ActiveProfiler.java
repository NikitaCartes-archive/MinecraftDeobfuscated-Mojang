/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.util.profiling.FilledProfileResults;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerPathEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ActiveProfiler
implements ProfileCollector {
    private static final long WARNING_TIME_NANOS = Duration.ofMillis(100L).toNanos();
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<String> paths = Lists.newArrayList();
    private final LongList startTimes = new LongArrayList();
    private final Map<String, PathEntry> entries = Maps.newHashMap();
    private final IntSupplier getTickTime;
    private final LongSupplier getRealTime;
    private final long startTimeNano;
    private final int startTimeTicks;
    private String path = "";
    private boolean started;
    @Nullable
    private PathEntry currentEntry;
    private final boolean warn;

    public ActiveProfiler(LongSupplier longSupplier, IntSupplier intSupplier, boolean bl) {
        this.startTimeNano = longSupplier.getAsLong();
        this.getRealTime = longSupplier;
        this.startTimeTicks = intSupplier.getAsInt();
        this.getTickTime = intSupplier;
        this.warn = bl;
    }

    @Override
    public void startTick() {
        if (this.started) {
            LOGGER.error("Profiler tick already started - missing endTick()?");
            return;
        }
        this.started = true;
        this.path = "";
        this.paths.clear();
        this.push("root");
    }

    @Override
    public void endTick() {
        if (!this.started) {
            LOGGER.error("Profiler tick already ended - missing startTick()?");
            return;
        }
        this.pop();
        this.started = false;
        if (!this.path.isEmpty()) {
            LOGGER.error("Profiler tick ended before path was fully popped (remainder: '{}'). Mismatched push/pop?", () -> ProfileResults.demanglePath(this.path));
        }
    }

    @Override
    public void push(String string) {
        if (!this.started) {
            LOGGER.error("Cannot push '{}' to profiler if profiler tick hasn't started - missing startTick()?", (Object)string);
            return;
        }
        if (!this.path.isEmpty()) {
            this.path = this.path + '\u001e';
        }
        this.path = this.path + string;
        this.paths.add(this.path);
        this.startTimes.add(Util.getNanos());
        this.currentEntry = null;
    }

    @Override
    public void push(Supplier<String> supplier) {
        this.push(supplier.get());
    }

    @Override
    public void pop() {
        if (!this.started) {
            LOGGER.error("Cannot pop from profiler if profiler tick hasn't started - missing startTick()?");
            return;
        }
        if (this.startTimes.isEmpty()) {
            LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
            return;
        }
        long l = Util.getNanos();
        long m = this.startTimes.removeLong(this.startTimes.size() - 1);
        this.paths.remove(this.paths.size() - 1);
        long n = l - m;
        PathEntry pathEntry = this.getCurrentEntry();
        pathEntry.duration = pathEntry.duration + n;
        pathEntry.count = pathEntry.count + 1L;
        if (this.warn && n > WARNING_TIME_NANOS) {
            LOGGER.warn("Something's taking too long! '{}' took aprox {} ms", () -> ProfileResults.demanglePath(this.path), () -> (double)n / 1000000.0);
        }
        this.path = this.paths.isEmpty() ? "" : this.paths.get(this.paths.size() - 1);
        this.currentEntry = null;
    }

    @Override
    public void popPush(String string) {
        this.pop();
        this.push(string);
    }

    @Override
    public void popPush(Supplier<String> supplier) {
        this.pop();
        this.push(supplier);
    }

    private PathEntry getCurrentEntry() {
        if (this.currentEntry == null) {
            this.currentEntry = this.entries.computeIfAbsent(this.path, string -> new PathEntry());
        }
        return this.currentEntry;
    }

    @Override
    public void incrementCounter(String string) {
        this.getCurrentEntry().counters.addTo(string, 1L);
    }

    @Override
    public void incrementCounter(Supplier<String> supplier) {
        this.getCurrentEntry().counters.addTo(supplier.get(), 1L);
    }

    @Override
    public ProfileResults getResults() {
        return new FilledProfileResults(this.entries, this.startTimeNano, this.startTimeTicks, this.getRealTime.getAsLong(), this.getTickTime.getAsInt());
    }

    @Override
    @Nullable
    public PathEntry getEntry(String string) {
        return this.entries.get(string);
    }

    public static class PathEntry
    implements ProfilerPathEntry {
        private long duration;
        private long count;
        private final Object2LongOpenHashMap<String> counters = new Object2LongOpenHashMap();

        @Override
        public long getDuration() {
            return this.duration;
        }

        @Override
        public long getCount() {
            return this.count;
        }

        @Override
        public Object2LongMap<String> getCounters() {
            return Object2LongMaps.unmodifiable(this.counters);
        }
    }
}

