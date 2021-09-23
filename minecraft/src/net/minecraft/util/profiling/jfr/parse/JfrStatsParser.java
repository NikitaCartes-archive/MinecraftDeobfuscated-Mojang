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
import javax.annotation.Nullable;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.PacketStat;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;

public class JfrStatsParser {
	private Instant recordingStarted = Instant.EPOCH;
	private Instant recordingEnded = Instant.EPOCH;
	private final List<ChunkGenStat> chunkGenStats = Lists.<ChunkGenStat>newArrayList();
	private final List<CpuLoadStat> cpuLoadStat = Lists.<CpuLoadStat>newArrayList();
	private final List<PacketStat> receivedPackets = Lists.<PacketStat>newArrayList();
	private final List<PacketStat> sentPackets = Lists.<PacketStat>newArrayList();
	private final List<FileIOStat> fileWrites = Lists.<FileIOStat>newArrayList();
	private final List<FileIOStat> fileReads = Lists.<FileIOStat>newArrayList();
	private int garbageCollections;
	private Duration gcTotalDuration = Duration.ZERO;
	private final List<GcHeapStat> gcHeapStats = Lists.<GcHeapStat>newArrayList();
	private final List<ThreadAllocationStat> threadAllocationStats = Lists.<ThreadAllocationStat>newArrayList();
	private final List<TickTimeStat> tickTimes = Lists.<TickTimeStat>newArrayList();
	@Nullable
	private Duration worldCreationDuration = null;

	private JfrStatsParser(Stream<RecordedEvent> stream) {
		this.capture(stream);
	}

	public static JfrStatsResult parse(Path path) {
		try {
			final RecordingFile recordingFile = new RecordingFile(path);

			JfrStatsResult var4;
			try {
				Iterator<RecordedEvent> iterator = new Iterator<RecordedEvent>() {
					public boolean hasNext() {
						return recordingFile.hasMoreEvents();
					}

					public RecordedEvent next() {
						if (!this.hasNext()) {
							throw new NoSuchElementException();
						} else {
							try {
								return recordingFile.readEvent();
							} catch (IOException var2) {
								throw new UncheckedIOException(var2);
							}
						}
					}
				};
				Stream<RecordedEvent> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 1297), false);
				var4 = new JfrStatsParser(stream).results();
			} catch (Throwable var6) {
				try {
					recordingFile.close();
				} catch (Throwable var5) {
					var6.addSuppressed(var5);
				}

				throw var6;
			}

			recordingFile.close();
			return var4;
		} catch (IOException var7) {
			throw new UncheckedIOException(var7);
		}
	}

	private JfrStatsResult results() {
		Duration duration = Duration.between(this.recordingStarted, this.recordingEnded);
		return new JfrStatsResult(
			this.recordingStarted,
			this.recordingEnded,
			duration,
			this.worldCreationDuration,
			this.tickTimes,
			this.cpuLoadStat,
			GcHeapStat.summary(duration, this.gcHeapStats, this.gcTotalDuration, this.garbageCollections),
			ThreadAllocationStat.summary(this.threadAllocationStats),
			PacketStat.summary(duration, this.receivedPackets),
			PacketStat.summary(duration, this.sentPackets),
			FileIOStat.summary(duration, this.fileWrites),
			FileIOStat.summary(duration, this.fileReads),
			this.chunkGenStats
		);
	}

	private void capture(Stream<RecordedEvent> stream) {
		stream.forEach(recordedEvent -> {
			if (recordedEvent.getEndTime().isAfter(this.recordingEnded) || this.recordingEnded.equals(Instant.EPOCH)) {
				this.recordingEnded = recordedEvent.getEndTime();
			}

			if (recordedEvent.getStartTime().isBefore(this.recordingStarted) || this.recordingStarted.equals(Instant.EPOCH)) {
				this.recordingStarted = recordedEvent.getStartTime();
			}

			String var2 = recordedEvent.getEventType().getName();
			switch (var2) {
				case "minecraft.ChunkGeneration":
					this.chunkGenStats.add(ChunkGenStat.from(recordedEvent));
					break;
				case "minecraft.LoadWorld":
					this.worldCreationDuration = recordedEvent.getDuration();
					break;
				case "minecraft.ServerTickTime":
					this.tickTimes.add(TickTimeStat.from(recordedEvent));
					break;
				case "minecraft.PacketReceived":
					this.receivedPackets.add(PacketStat.from(recordedEvent));
					break;
				case "minecraft.PacketSent":
					this.sentPackets.add(PacketStat.from(recordedEvent));
					break;
				case "jdk.ThreadAllocationStatistics":
					this.threadAllocationStats.add(ThreadAllocationStat.from(recordedEvent));
					break;
				case "jdk.GCHeapSummary":
					this.gcHeapStats.add(GcHeapStat.from(recordedEvent));
					break;
				case "jdk.CPULoad":
					this.cpuLoadStat.add(CpuLoadStat.from(recordedEvent));
					break;
				case "jdk.FileWrite":
					this.appendFileIO(recordedEvent, this.fileWrites, "bytesWritten");
					break;
				case "jdk.FileRead":
					this.appendFileIO(recordedEvent, this.fileReads, "bytesRead");
					break;
				case "jdk.GarbageCollection":
					this.garbageCollections++;
					this.gcTotalDuration = this.gcTotalDuration.plus(recordedEvent.getDuration());
			}
		});
	}

	private void appendFileIO(RecordedEvent recordedEvent, List<FileIOStat> list, String string) {
		list.add(new FileIOStat(recordedEvent.getDuration(), recordedEvent.getString("path"), recordedEvent.getLong(string)));
	}
}
