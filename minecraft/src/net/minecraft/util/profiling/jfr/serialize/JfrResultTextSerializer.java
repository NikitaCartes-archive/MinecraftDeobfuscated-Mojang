package net.minecraft.util.profiling.jfr.serialize;

import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.PacketStat;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.apache.commons.lang3.StringUtils;

public class JfrResultTextSerializer implements JfrResultSerializer {
	public static final String NL = System.lineSeparator();
	public static final String MIN_AVG_MAX_PERCENTAGE_FORMAT = "min(%.2f%%) avg(%.2f%%) max(%.2f%%)";

	@Override
	public String format(JfrStatsResult jfrStatsResult) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.format("Started: %s", jfrStatsResult.getRecordingStarted())).append(NL);
		stringBuilder.append(String.format("Ended: %s", jfrStatsResult.getRecordingEnded())).append(NL);
		stringBuilder.append(String.format("Duration: %s", jfrStatsResult.getRecordingDuration())).append(NL);
		stringBuilder.append("== World gen ==").append(NL);
		jfrStatsResult.getWorldCreationDuration().ifPresent(duration -> stringBuilder.append("World gen: ").append(duration).append(NL));
		this.outputChunkStats(stringBuilder, jfrStatsResult.getChunkGenSummary());
		this.outputGCs(stringBuilder, jfrStatsResult.getHeapSummary());
		this.outputThreadAllocations(stringBuilder, jfrStatsResult.getThreadAllocationSummary());
		this.outputCpu(stringBuilder, jfrStatsResult.getCpuStats());
		this.outputServerTicks(stringBuilder, jfrStatsResult.getTicktimes());
		stringBuilder.append(NL).append("== Network IO ==").append(NL);
		this.outputNetworkSummary(stringBuilder, jfrStatsResult.getSentPackets(), "sent");
		this.outputNetworkSummary(stringBuilder, jfrStatsResult.getReceivedPackets(), "received");
		this.outputFileIO(stringBuilder, jfrStatsResult.getFileWrites(), jfrStatsResult.getFileReads());
		return stringBuilder.toString();
	}

	private void outputThreadAllocations(StringBuilder stringBuilder, ThreadAllocationStat.ThreadAllocationSummary threadAllocationSummary) {
		stringBuilder.append(NL).append("== Allocations /s by thread ==").append(NL);
		threadAllocationSummary.allocationsPerSecondByThread
			.forEach((string, long_) -> stringBuilder.append(String.format("%s: %.2fKb", string, bytesToKilobytes(long_))).append(NL));
	}

	private void outputFileIO(StringBuilder stringBuilder, FileIOStat.FileIOSummary fileIOSummary, FileIOStat.FileIOSummary fileIOSummary2) {
		stringBuilder.append(NL).append("== File IO ==").append(NL);
		stringBuilder.append(String.format("Bytes written /s: %.2fKb", bytesToKilobytes(fileIOSummary.bytesPerSecond()))).append(NL);
		stringBuilder.append(String.format("Total writes: %.2fKb", bytesToKilobytes(fileIOSummary.getTotalBytes()))).append(NL);
		stringBuilder.append("Time spent writing: ").append(fileIOSummary.timeSpentInIO()).append(NL);
		stringBuilder.append("Top write contributors:").append(NL);
		fileIOSummary.topContributors()
			.limit(10L)
			.forEach(pair -> stringBuilder.append((String)pair.getLeft()).append(String.format(": %.2fKb", bytesToKilobytes((Long)pair.getRight()))).append(NL));
		stringBuilder.append(String.format("Bytes read /s: %.2fKb", bytesToKilobytes(fileIOSummary2.bytesPerSecond()))).append(NL);
		stringBuilder.append(String.format("Total read bytes: %.2fKb", bytesToKilobytes(fileIOSummary2.getTotalBytes()))).append(NL);
		stringBuilder.append("Time spent reading: ").append(fileIOSummary2.timeSpentInIO()).append(NL);
		stringBuilder.append("Top read contributors:").append(NL);
		fileIOSummary2.topContributors()
			.limit(10L)
			.forEach(pair -> stringBuilder.append((String)pair.getLeft()).append(String.format(": %.2fKb", bytesToKilobytes((Long)pair.getRight()))).append(NL));
	}

	private void outputNetworkSummary(StringBuilder stringBuilder, PacketStat.PacketSummary packetSummary, String string) {
		stringBuilder.append(
				String.format("Total packets %s: count(%s) size(%.2fMb)", string, packetSummary.getTotalCount(), bytesToMegabytes(packetSummary.getTotalSize()))
			)
			.append(NL);
		stringBuilder.append(
				String.format(
					"Packets %s / second: count(%.2f) size(%.2fKb)", string, packetSummary.getCountPerSecond(), bytesToKilobytes(packetSummary.getSizePerSecond())
				)
			)
			.append(NL);
		stringBuilder.append(String.format("Top %s by total size:", string)).append(NL);

		for (Pair<String, Long> pair : packetSummary.getLargestSizeContributors()) {
			String string2 = pair.getFirst();
			long l = pair.getSecond();
			double d = (double)l / (double)packetSummary.getRecordingDuration().getSeconds();
			stringBuilder.append(string2).append(": ").append(String.format("total(%.2fKb) /s(%.2fKb)", bytesToKilobytes(l), bytesToKilobytes(d))).append(NL);
		}
	}

	private void outputServerTicks(StringBuilder stringBuilder, List<TickTimeStat> list) {
		stringBuilder.append(NL).append("== Server ticks ==").append(NL);
		DoubleSummaryStatistics doubleSummaryStatistics = list.stream().mapToDouble(tickTimeStat -> (double)tickTimeStat.currentAverage).summaryStatistics();
		stringBuilder.append("tick time (ms): ")
			.append(
				String.format("min(%.2f) avg(%.2f) max(%.2f)", doubleSummaryStatistics.getMin(), doubleSummaryStatistics.getAverage(), doubleSummaryStatistics.getMax())
			)
			.append(NL);
	}

	private void outputCpu(StringBuilder stringBuilder, List<CpuLoadStat> list) {
		stringBuilder.append(NL).append("== CPU usage ==").append(NL);
		int i = 100;
		DoubleSummaryStatistics doubleSummaryStatistics = list.stream().mapToDouble(cpuLoadStat -> cpuLoadStat.jvm * (double)i).summaryStatistics();
		DoubleSummaryStatistics doubleSummaryStatistics2 = list.stream().mapToDouble(cpuLoadStat -> cpuLoadStat.system * (double)i).summaryStatistics();
		DoubleSummaryStatistics doubleSummaryStatistics3 = list.stream().mapToDouble(cpuLoadStat -> cpuLoadStat.userJvm * (double)i).summaryStatistics();
		stringBuilder.append("jvm: ")
			.append(
				String.format(
					"min(%.2f%%) avg(%.2f%%) max(%.2f%%)", doubleSummaryStatistics.getMin(), doubleSummaryStatistics.getAverage(), doubleSummaryStatistics.getMax()
				)
			)
			.append(NL);
		stringBuilder.append("userJvm: ")
			.append(
				String.format(
					"min(%.2f%%) avg(%.2f%%) max(%.2f%%)", doubleSummaryStatistics3.getMin(), doubleSummaryStatistics3.getAverage(), doubleSummaryStatistics3.getMax()
				)
			)
			.append(NL);
		stringBuilder.append("system: ")
			.append(
				String.format(
					"min(%.2f%%) avg(%.2f%%) max(%.2f%%)", doubleSummaryStatistics2.getMin(), doubleSummaryStatistics2.getAverage(), doubleSummaryStatistics2.getMax()
				)
			)
			.append(NL);
	}

	private void outputGCs(StringBuilder stringBuilder, GcHeapStat.HeapSummary heapSummary) {
		stringBuilder.append(NL).append("== Garbage collections ==").append(NL);
		stringBuilder.append("Total duration: ").append(heapSummary.gcTotalDuration).append(NL);
		stringBuilder.append("Number of GC's: ").append(heapSummary.totalGCs).append(NL);
		stringBuilder.append(String.format("GC overhead: %.2f%%", heapSummary.getGCOverHead() * 100.0F)).append(NL);
		stringBuilder.append(String.format("Allocation rate /s: %.2fMb", bytesToMegabytes(heapSummary.allocationRateBytesPerSecond))).append(NL);
	}

	private void outputChunkStats(StringBuilder stringBuilder, List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> list) {
		int i = list.stream().mapToInt(pairx -> ((ChunkStatus)pairx.getFirst()).getName().length()).max().getAsInt();

		for (Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>> pair : list) {
			TimedStatSummary<ChunkGenStat> timedStatSummary = pair.getSecond();
			stringBuilder.append(StringUtils.rightPad(pair.getFirst().getName(), i)).append(" : ").append(this.formatChunkSummary(timedStatSummary)).append(NL);
		}
	}

	private String formatChunkSummary(TimedStatSummary<ChunkGenStat> timedStatSummary) {
		return String.format(
			"(%s), count(%s), total_duration(%s), slowest(%s), second_slowest(%s), fastest(%s)",
			Arrays.stream(TimedStatSummary.PERCENTILES)
				.mapToObj(i -> String.format("p%d=%s", i, timedStatSummary.percentilesNanos.get(i)))
				.collect(Collectors.joining("/")),
			timedStatSummary.count,
			timedStatSummary.totalDuration,
			this.formatChunkDuration(timedStatSummary.slowest),
			timedStatSummary.secondSlowest.map(this::formatChunkDuration).orElse("n/a"),
			this.formatChunkDuration(timedStatSummary.fastest)
		);
	}

	private String formatChunkDuration(ChunkGenStat chunkGenStat) {
		return String.format(
			"%sms, chunkPos: %s, blockPos: %s",
			chunkGenStat.duration.toMillis(),
			"[" + chunkGenStat.chunkPos.x + ", " + chunkGenStat.chunkPos.z + "]",
			"[" + chunkGenStat.blockPos.getX() + ", " + chunkGenStat.blockPos.getY() + ", " + chunkGenStat.blockPos.getZ() + "]"
		);
	}

	private static double bytesToMegabytes(long l) {
		return bytesToKilobytes(l) / 1024.0;
	}

	private static double bytesToMegabytes(double d) {
		return bytesToKilobytes(d) / 1024.0;
	}

	private static double bytesToKilobytes(long l) {
		return (double)l / 1024.0;
	}

	private static double bytesToKilobytes(double d) {
		return d / 1024.0;
	}
}
