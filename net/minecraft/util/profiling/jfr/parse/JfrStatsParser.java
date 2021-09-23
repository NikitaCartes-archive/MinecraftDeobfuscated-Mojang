/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.parse;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.PacketStat;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import org.jetbrains.annotations.Nullable;

public class JfrStatsParser {
    private Instant recordingStarted = Instant.EPOCH;
    private Instant recordingEnded = Instant.EPOCH;
    private final List<ChunkGenStat> chunkGenStats = Lists.newArrayList();
    private final List<CpuLoadStat> cpuLoadStat = Lists.newArrayList();
    private final List<PacketStat> receivedPackets = Lists.newArrayList();
    private final List<PacketStat> sentPackets = Lists.newArrayList();
    private final List<FileIOStat> fileWrites = Lists.newArrayList();
    private final List<FileIOStat> fileReads = Lists.newArrayList();
    private int garbageCollections;
    private Duration gcTotalDuration = Duration.ZERO;
    private final List<GcHeapStat> gcHeapStats = Lists.newArrayList();
    private final List<ThreadAllocationStat> threadAllocationStats = Lists.newArrayList();
    private final List<TickTimeStat> tickTimes = Lists.newArrayList();
    @Nullable
    private Duration worldCreationDuration = null;

    private JfrStatsParser(Stream<RecordedEvent> stream) {
        this.capture(stream);
    }

    public static JfrStatsResult parse(Path path) {
        JfrStatsResult jfrStatsResult;
        final RecordingFile recordingFile = new RecordingFile(path);
        try {
            Iterator<RecordedEvent> iterator = new Iterator<RecordedEvent>(){

                @Override
                public boolean hasNext() {
                    return recordingFile.hasMoreEvents();
                }

                @Override
                public RecordedEvent next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    try {
                        return recordingFile.readEvent();
                    } catch (IOException iOException) {
                        throw new UncheckedIOException(iOException);
                    }
                }

                @Override
                public /* synthetic */ Object next() {
                    return this.next();
                }
            };
            Stream<RecordedEvent> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 1297), false);
            jfrStatsResult = new JfrStatsParser(stream).results();
        } catch (Throwable throwable) {
            try {
                try {
                    recordingFile.close();
                } catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            } catch (IOException iOException) {
                throw new UncheckedIOException(iOException);
            }
        }
        recordingFile.close();
        return jfrStatsResult;
    }

    private JfrStatsResult results() {
        Duration duration = Duration.between(this.recordingStarted, this.recordingEnded);
        return new JfrStatsResult(this.recordingStarted, this.recordingEnded, duration, this.worldCreationDuration, this.tickTimes, this.cpuLoadStat, GcHeapStat.summary(duration, this.gcHeapStats, this.gcTotalDuration, this.garbageCollections), ThreadAllocationStat.summary(this.threadAllocationStats), PacketStat.summary(duration, this.receivedPackets), PacketStat.summary(duration, this.sentPackets), FileIOStat.summary(duration, this.fileWrites), FileIOStat.summary(duration, this.fileReads), this.chunkGenStats);
    }

    private void capture(Stream<RecordedEvent> stream) {
        stream.forEach(recordedEvent -> {
            if (recordedEvent.getEndTime().isAfter(this.recordingEnded) || this.recordingEnded.equals(Instant.EPOCH)) {
                this.recordingEnded = recordedEvent.getEndTime();
            }
            if (recordedEvent.getStartTime().isBefore(this.recordingStarted) || this.recordingStarted.equals(Instant.EPOCH)) {
                this.recordingStarted = recordedEvent.getStartTime();
            }
            switch (recordedEvent.getEventType().getName()) {
                case "minecraft.ChunkGeneration": {
                    this.chunkGenStats.add(ChunkGenStat.from(recordedEvent));
                    break;
                }
                case "minecraft.LoadWorld": {
                    this.worldCreationDuration = recordedEvent.getDuration();
                    break;
                }
                case "minecraft.ServerTickTime": {
                    this.tickTimes.add(TickTimeStat.from(recordedEvent));
                    break;
                }
                case "minecraft.PacketReceived": {
                    this.receivedPackets.add(PacketStat.from(recordedEvent));
                    break;
                }
                case "minecraft.PacketSent": {
                    this.sentPackets.add(PacketStat.from(recordedEvent));
                    break;
                }
                case "jdk.ThreadAllocationStatistics": {
                    this.threadAllocationStats.add(ThreadAllocationStat.from(recordedEvent));
                    break;
                }
                case "jdk.GCHeapSummary": {
                    this.gcHeapStats.add(GcHeapStat.from(recordedEvent));
                    break;
                }
                case "jdk.CPULoad": {
                    this.cpuLoadStat.add(CpuLoadStat.from(recordedEvent));
                    break;
                }
                case "jdk.FileWrite": {
                    this.appendFileIO((RecordedEvent)recordedEvent, this.fileWrites, "bytesWritten");
                    break;
                }
                case "jdk.FileRead": {
                    this.appendFileIO((RecordedEvent)recordedEvent, this.fileReads, "bytesRead");
                    break;
                }
                case "jdk.GarbageCollection": {
                    ++this.garbageCollections;
                    this.gcTotalDuration = this.gcTotalDuration.plus(recordedEvent.getDuration());
                    break;
                }
            }
        });
    }

    private void appendFileIO(RecordedEvent recordedEvent, List<FileIOStat> list, String string) {
        list.add(new FileIOStat(recordedEvent.getDuration(), recordedEvent.getString("path"), recordedEvent.getLong(string)));
    }
}

