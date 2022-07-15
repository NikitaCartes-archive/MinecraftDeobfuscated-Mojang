/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.function.LongSupplier;
import net.minecraft.Util;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SingleTickProfiler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final LongSupplier realTime;
    private final long saveThreshold;
    private int tick;
    private final File location;
    private ProfileCollector profiler = InactiveProfiler.INSTANCE;

    public SingleTickProfiler(LongSupplier longSupplier, String string, long l) {
        this.realTime = longSupplier;
        this.location = new File("debug", string);
        this.saveThreshold = l;
    }

    public ProfilerFiller startTick() {
        this.profiler = new ActiveProfiler(this.realTime, () -> this.tick, false);
        ++this.tick;
        return this.profiler;
    }

    public void endTick() {
        if (this.profiler == InactiveProfiler.INSTANCE) {
            return;
        }
        ProfileResults profileResults = this.profiler.getResults();
        this.profiler = InactiveProfiler.INSTANCE;
        if (profileResults.getNanoDuration() >= this.saveThreshold) {
            File file = new File(this.location, "tick-results-" + Util.getFilenameFormattedDateTime() + ".txt");
            profileResults.saveResults(file.toPath());
            LOGGER.info("Recorded long tick -- wrote info to: {}", (Object)file.getAbsolutePath());
        }
    }

    @Nullable
    public static SingleTickProfiler createTickProfiler(String string) {
        return null;
    }

    public static ProfilerFiller decorateFiller(ProfilerFiller profilerFiller, @Nullable SingleTickProfiler singleTickProfiler) {
        if (singleTickProfiler != null) {
            return ProfilerFiller.tee(singleTickProfiler.startTick(), profilerFiller);
        }
        return profilerFiller;
    }
}

