/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerPathEntry;
import net.minecraft.util.profiling.ResultField;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilledProfileResults
implements ProfileResults {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ProfilerPathEntry EMPTY = new ProfilerPathEntry(){

        @Override
        public long getDuration() {
            return 0L;
        }

        @Override
        public long getCount() {
            return 0L;
        }

        @Override
        public Object2LongMap<String> getCounters() {
            return Object2LongMaps.emptyMap();
        }
    };
    private static final Splitter SPLITTER = Splitter.on('\u001e');
    private static final Comparator<Map.Entry<String, CounterCollector>> COUNTER_ENTRY_COMPARATOR = Map.Entry.comparingByValue(Comparator.comparingLong(counterCollector -> CounterCollector.method_24265(counterCollector))).reversed();
    private final Map<String, ? extends ProfilerPathEntry> entries;
    private final long startTimeNano;
    private final int startTimeTicks;
    private final long endTimeNano;
    private final int endTimeTicks;
    private final int tickDuration;

    public FilledProfileResults(Map<String, ? extends ProfilerPathEntry> map, long l, int i, long m, int j) {
        this.entries = map;
        this.startTimeNano = l;
        this.startTimeTicks = i;
        this.endTimeNano = m;
        this.endTimeTicks = j;
        this.tickDuration = j - i;
    }

    private ProfilerPathEntry getEntry(String string) {
        ProfilerPathEntry profilerPathEntry = this.entries.get(string);
        return profilerPathEntry != null ? profilerPathEntry : EMPTY;
    }

    @Override
    public List<ResultField> getTimes(String string) {
        String string2 = string;
        ProfilerPathEntry profilerPathEntry = this.getEntry("root");
        long l = profilerPathEntry.getDuration();
        ProfilerPathEntry profilerPathEntry2 = this.getEntry(string);
        long m = profilerPathEntry2.getDuration();
        long n = profilerPathEntry2.getCount();
        ArrayList<ResultField> list = Lists.newArrayList();
        if (!string.isEmpty()) {
            string = string + '\u001e';
        }
        long o = 0L;
        for (String string3 : this.entries.keySet()) {
            if (!FilledProfileResults.isDirectChild(string, string3)) continue;
            o += this.getEntry(string3).getDuration();
        }
        float f = o;
        if (o < m) {
            o = m;
        }
        if (l < o) {
            l = o;
        }
        for (String string4 : this.entries.keySet()) {
            if (!FilledProfileResults.isDirectChild(string, string4)) continue;
            ProfilerPathEntry profilerPathEntry3 = this.getEntry(string4);
            long p = profilerPathEntry3.getDuration();
            double d = (double)p * 100.0 / (double)o;
            double e = (double)p * 100.0 / (double)l;
            String string5 = string4.substring(string.length());
            list.add(new ResultField(string5, d, e, profilerPathEntry3.getCount()));
        }
        if ((float)o > f) {
            list.add(new ResultField("unspecified", (double)((float)o - f) * 100.0 / (double)o, (double)((float)o - f) * 100.0 / (double)l, n));
        }
        Collections.sort(list);
        list.add(0, new ResultField(string2, 100.0, (double)o * 100.0 / (double)l, n));
        return list;
    }

    private static boolean isDirectChild(String string, String string2) {
        return string2.length() > string.length() && string2.startsWith(string) && string2.indexOf(30, string.length() + 1) < 0;
    }

    private Map<String, CounterCollector> getCounterValues() {
        TreeMap<String, CounterCollector> map = Maps.newTreeMap();
        this.entries.forEach((string, profilerPathEntry) -> {
            Object2LongMap<String> object2LongMap = profilerPathEntry.getCounters();
            if (!object2LongMap.isEmpty()) {
                List<String> list = SPLITTER.splitToList((CharSequence)string);
                object2LongMap.forEach((string2, long_) -> map.computeIfAbsent((String)string2, string -> new CounterCollector()).addValue(list.iterator(), (long)long_));
            }
        });
        return map;
    }

    @Override
    public long getStartTimeNano() {
        return this.startTimeNano;
    }

    @Override
    public int getStartTimeTicks() {
        return this.startTimeTicks;
    }

    @Override
    public long getEndTimeNano() {
        return this.endTimeNano;
    }

    @Override
    public int getEndTimeTicks() {
        return this.endTimeTicks;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean saveResults(File file) {
        boolean bl;
        file.getParentFile().mkdirs();
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter((OutputStream)new FileOutputStream(file), StandardCharsets.UTF_8);
            writer.write(this.getProfilerResults(this.getNanoDuration(), this.getTickDuration()));
            bl = true;
        } catch (Throwable throwable) {
            boolean bl2;
            try {
                LOGGER.error("Could not save profiler results to {}", (Object)file, (Object)throwable);
                bl2 = false;
            } catch (Throwable throwable2) {
                IOUtils.closeQuietly(writer);
                throw throwable2;
            }
            IOUtils.closeQuietly(writer);
            return bl2;
        }
        IOUtils.closeQuietly(writer);
        return bl;
    }

    protected String getProfilerResults(long l, int i) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("---- Minecraft Profiler Results ----\n");
        stringBuilder.append("// ");
        stringBuilder.append(FilledProfileResults.getComment());
        stringBuilder.append("\n\n");
        stringBuilder.append("Version: ").append(SharedConstants.getCurrentVersion().getId()).append('\n');
        stringBuilder.append("Time span: ").append(l / 1000000L).append(" ms\n");
        stringBuilder.append("Tick span: ").append(i).append(" ticks\n");
        stringBuilder.append("// This is approximately ").append(String.format(Locale.ROOT, "%.2f", Float.valueOf((float)i / ((float)l / 1.0E9f)))).append(" ticks per second. It should be ").append(20).append(" ticks per second\n\n");
        stringBuilder.append("--- BEGIN PROFILE DUMP ---\n\n");
        this.appendProfilerResults(0, "root", stringBuilder);
        stringBuilder.append("--- END PROFILE DUMP ---\n\n");
        Map<String, CounterCollector> map = this.getCounterValues();
        if (!map.isEmpty()) {
            stringBuilder.append("--- BEGIN COUNTER DUMP ---\n\n");
            this.appendCounters(map, stringBuilder, i);
            stringBuilder.append("--- END COUNTER DUMP ---\n\n");
        }
        return stringBuilder.toString();
    }

    private static StringBuilder indentLine(StringBuilder stringBuilder, int i) {
        stringBuilder.append(String.format("[%02d] ", i));
        for (int j = 0; j < i; ++j) {
            stringBuilder.append("|   ");
        }
        return stringBuilder;
    }

    private void appendProfilerResults(int i, String string2, StringBuilder stringBuilder) {
        List<ResultField> list = this.getTimes(string2);
        Object2LongMap<String> object2LongMap = this.entries.get(string2).getCounters();
        object2LongMap.forEach((string, long_) -> FilledProfileResults.indentLine(stringBuilder, i).append('#').append((String)string).append(' ').append(long_).append('/').append(long_ / (long)this.tickDuration).append('\n'));
        if (list.size() < 3) {
            return;
        }
        for (int j = 1; j < list.size(); ++j) {
            ResultField resultField = list.get(j);
            FilledProfileResults.indentLine(stringBuilder, i).append(resultField.name).append('(').append(resultField.count).append('/').append(String.format(Locale.ROOT, "%.0f", Float.valueOf((float)resultField.count / (float)this.tickDuration))).append(')').append(" - ").append(String.format(Locale.ROOT, "%.2f", resultField.percentage)).append("%/").append(String.format(Locale.ROOT, "%.2f", resultField.globalPercentage)).append("%\n");
            if ("unspecified".equals(resultField.name)) continue;
            try {
                this.appendProfilerResults(i + 1, string2 + '\u001e' + resultField.name, stringBuilder);
                continue;
            } catch (Exception exception) {
                stringBuilder.append("[[ EXCEPTION ").append(exception).append(" ]]");
            }
        }
    }

    private void appendCounterResults(int i, String string, CounterCollector counterCollector, int j, StringBuilder stringBuilder) {
        FilledProfileResults.indentLine(stringBuilder, i).append(string).append(" total:").append(counterCollector.selfValue).append('/').append(counterCollector.totalValue).append(" average: ").append(counterCollector.selfValue / (long)j).append('/').append(counterCollector.totalValue / (long)j).append('\n');
        counterCollector.children.entrySet().stream().sorted(COUNTER_ENTRY_COMPARATOR).forEach(entry -> this.appendCounterResults(i + 1, (String)entry.getKey(), (CounterCollector)entry.getValue(), j, stringBuilder));
    }

    private void appendCounters(Map<String, CounterCollector> map, StringBuilder stringBuilder, int i) {
        map.forEach((string, counterCollector) -> {
            stringBuilder.append("-- Counter: ").append((String)string).append(" --\n");
            this.appendCounterResults(0, "root", (CounterCollector)((CounterCollector)counterCollector).children.get("root"), i, stringBuilder);
            stringBuilder.append("\n\n");
        });
    }

    private static String getComment() {
        String[] strings = new String[]{"Shiny numbers!", "Am I not running fast enough? :(", "I'm working as hard as I can!", "Will I ever be good enough for you? :(", "Speedy. Zoooooom!", "Hello world", "40% better than a crash report.", "Now with extra numbers", "Now with less numbers", "Now with the same numbers", "You should add flames to things, it makes them go faster!", "Do you feel the need for... optimization?", "*cracks redstone whip*", "Maybe if you treated it better then it'll have more motivation to work faster! Poor server."};
        try {
            return strings[(int)(Util.getNanos() % (long)strings.length)];
        } catch (Throwable throwable) {
            return "Witty comment unavailable :(";
        }
    }

    @Override
    public int getTickDuration() {
        return this.tickDuration;
    }

    static class CounterCollector {
        private long selfValue;
        private long totalValue;
        private final Map<String, CounterCollector> children = Maps.newHashMap();

        private CounterCollector() {
        }

        public void addValue(Iterator<String> iterator, long l) {
            this.totalValue += l;
            if (!iterator.hasNext()) {
                this.selfValue += l;
            } else {
                this.children.computeIfAbsent(iterator.next(), string -> new CounterCollector()).addValue(iterator, l);
            }
        }
    }
}

