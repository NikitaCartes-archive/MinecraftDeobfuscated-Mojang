package net.minecraft.util.profiling.jfr.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.time.Duration;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import net.minecraft.Util;
import net.minecraft.util.profiling.jfr.Percentiles;
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

public class JfrResultJsonSerializer implements JfrResultSerializer {
	private static final String BYTES_PER_SECOND = "bytesPerSecond";
	private static final String COUNT = "count";
	private static final String DURATION_NANOS_TOTAL = "durationNanosTotal";
	private static final String TOTAL_BYTES = "totalBytes";
	private static final String COUNT_PER_SECOND = "countPerSecond";
	final Gson gson = new GsonBuilder().setPrettyPrinting().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT).create();

	@Override
	public String format(JfrStatsResult jfrStatsResult) throws IOException {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("startedEpoch", jfrStatsResult.getRecordingStarted().toEpochMilli());
		jsonObject.addProperty("endedEpoch", jfrStatsResult.getRecordingEnded().toEpochMilli());
		jsonObject.addProperty("durationMs", Duration.between(jfrStatsResult.getRecordingStarted(), jfrStatsResult.getRecordingEnded()).toMillis());
		jfrStatsResult.getWorldCreationDuration().ifPresent(duration -> jsonObject.addProperty("worldGenDurationMs", duration.toMillis()));
		jsonObject.add("heap", this.heap(jfrStatsResult.getHeapSummary()));
		jsonObject.add("cpuPercent", this.cpu(jfrStatsResult.getCpuStats()));
		jsonObject.add("network", this.network(jfrStatsResult));
		jsonObject.add("fileIO", this.fileIO(jfrStatsResult));
		jsonObject.add("serverTick", this.serverTicks(jfrStatsResult.getTicktimes()));
		jsonObject.add("threadAllocation", this.threadAllocations(jfrStatsResult.getThreadAllocationSummary()));
		jsonObject.add("chunkGen", this.chunkGen(jfrStatsResult.getChunkGenSummary()));
		return this.gson.toJson((JsonElement)jsonObject);
	}

	private JsonElement heap(GcHeapStat.HeapSummary heapSummary) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("allocationRateBytesPerSecond", heapSummary.allocationRateBytesPerSecond);
		jsonObject.addProperty("gcCount", heapSummary.totalGCs);
		jsonObject.addProperty("gcOverHeadPercent", heapSummary.getGCOverHead());
		jsonObject.addProperty("gcTotalDurationMs", heapSummary.gcTotalDuration.toMillis());
		return jsonObject;
	}

	private JsonElement chunkGen(List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> list) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("durationNanosTotal", list.stream().mapToDouble(pairx -> (double)((TimedStatSummary)pairx.getSecond()).totalDuration.toNanos()).sum());
		JsonArray jsonArray = Util.make(new JsonArray(), jsonArrayx -> jsonObject.add("status", jsonArrayx));

		for (Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>> pair : list) {
			TimedStatSummary<ChunkGenStat> timedStatSummary = pair.getSecond();
			JsonObject jsonObject2 = Util.make(new JsonObject(), jsonArray::add);
			jsonObject2.addProperty("state", pair.getFirst().getName());
			jsonObject2.addProperty("count", timedStatSummary.count);
			jsonObject2.addProperty("durationNanosTotal", timedStatSummary.totalDuration.toNanos());
			jsonObject2.addProperty("durationNanosAvg", timedStatSummary.totalDuration.toNanos() / (long)timedStatSummary.count);
			JsonObject jsonObject3 = Util.make(new JsonObject(), jsonObject2x -> jsonObject2.add("durationNanosPercentiles", jsonObject2x));
			timedStatSummary.percentilesNanos.forEach((integer, double_) -> jsonObject3.addProperty("p" + integer, double_));
			Function<ChunkGenStat, JsonElement> function = chunkGenStat -> {
				JsonObject jsonObjectx = new JsonObject();
				jsonObjectx.addProperty("durationNanos", chunkGenStat.duration.toNanos());
				jsonObjectx.addProperty("level", chunkGenStat.level);
				jsonObjectx.addProperty("blockPosX", chunkGenStat.blockPos.getX());
				jsonObjectx.addProperty("blockPosZ", chunkGenStat.blockPos.getZ());
				return jsonObjectx;
			};
			jsonObject2.add("fastest", (JsonElement)function.apply(timedStatSummary.fastest));
			jsonObject2.add("slowest", (JsonElement)function.apply(timedStatSummary.slowest));
			jsonObject2.add("secondSlowest", (JsonElement)timedStatSummary.secondSlowest.map(function).orElse(JsonNull.INSTANCE));
		}

		return jsonObject;
	}

	private JsonElement threadAllocations(ThreadAllocationStat.ThreadAllocationSummary threadAllocationSummary) {
		JsonArray jsonArray = new JsonArray();
		threadAllocationSummary.allocationsPerSecondByThread.forEach((string, long_) -> jsonArray.add(Util.make(new JsonObject(), jsonObject -> {
				jsonObject.addProperty("thread", string);
				jsonObject.addProperty("bytesPerSecond", long_);
			})));
		return jsonArray;
	}

	private JsonElement serverTicks(List<TickTimeStat> list) {
		if (list.isEmpty()) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = new JsonObject();
			DoubleSummaryStatistics doubleSummaryStatistics = list.stream().mapToDouble(tickTimeStat -> (double)tickTimeStat.currentAverage).summaryStatistics();
			jsonObject.addProperty("minMs", doubleSummaryStatistics.getMin());
			jsonObject.addProperty("averageMs", doubleSummaryStatistics.getAverage());
			jsonObject.addProperty("maxMs", doubleSummaryStatistics.getMax());
			Percentiles.evaluate(list.stream().mapToDouble(tickTimeStat -> (double)tickTimeStat.currentAverage).toArray())
				.forEach((integer, double_) -> jsonObject.addProperty("p" + integer, double_));
			return jsonObject;
		}
	}

	private JsonElement fileIO(JfrStatsResult jfrStatsResult) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("write", this.fileIoSummary(jfrStatsResult.getFileWrites()));
		jsonObject.add("read", this.fileIoSummary(jfrStatsResult.getFileReads()));
		return jsonObject;
	}

	private JsonElement fileIoSummary(FileIOStat.FileIOSummary fileIOSummary) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("totalBytes", fileIOSummary.getTotalBytes());
		jsonObject.addProperty("count", fileIOSummary.getCounts());
		jsonObject.addProperty("bytesPerSecond", fileIOSummary.bytesPerSecond());
		jsonObject.addProperty("countPerSecond", fileIOSummary.countsPerSecond());
		JsonArray jsonArray = new JsonArray();
		jsonObject.add("topContributors", jsonArray);
		fileIOSummary.topContributors().limit(10L).forEach(pair -> {
			JsonObject jsonObjectx = new JsonObject();
			jsonArray.add(jsonObjectx);
			jsonObjectx.addProperty("path", (String)pair.getLeft());
			jsonObjectx.addProperty("totalBytes", (Number)pair.getRight());
		});
		return jsonObject;
	}

	private JsonElement network(JfrStatsResult jfrStatsResult) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("sent", this.packets(jfrStatsResult.getSentPackets()));
		jsonObject.add("received", this.packets(jfrStatsResult.getReceivedPackets()));
		return jsonObject;
	}

	private JsonElement packets(PacketStat.PacketSummary packetSummary) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("totalBytes", packetSummary.getTotalSize());
		jsonObject.addProperty("count", packetSummary.getTotalCount());
		jsonObject.addProperty("bytesPerSecond", packetSummary.getSizePerSecond());
		jsonObject.addProperty("countPerSecond", packetSummary.getCountPerSecond());
		JsonArray jsonArray = new JsonArray();
		jsonObject.add("topContributors", jsonArray);
		packetSummary.getLargestSizeContributors().stream().limit(10L).forEach(pair -> {
			JsonObject jsonObjectx = new JsonObject();
			jsonArray.add(jsonObjectx);
			jsonObjectx.addProperty("packetName", (String)pair.getFirst());
			jsonObjectx.addProperty("totalBytes", (Number)pair.getSecond());
		});
		return jsonObject;
	}

	private JsonElement cpu(List<CpuLoadStat> list) {
		JsonObject jsonObject = new JsonObject();
		BiFunction<List<CpuLoadStat>, ToDoubleFunction<CpuLoadStat>, JsonObject> biFunction = (listx, toDoubleFunction) -> {
			JsonObject jsonObjectx = new JsonObject();
			DoubleSummaryStatistics doubleSummaryStatistics = listx.stream().mapToDouble(toDoubleFunction).summaryStatistics();
			jsonObjectx.addProperty("min", doubleSummaryStatistics.getMin());
			jsonObjectx.addProperty("average", doubleSummaryStatistics.getAverage());
			jsonObjectx.addProperty("max", doubleSummaryStatistics.getMax());
			return jsonObjectx;
		};
		jsonObject.add("jvm", (JsonElement)biFunction.apply(list, (ToDoubleFunction)cpuLoadStat -> cpuLoadStat.jvm));
		jsonObject.add("userJvm", (JsonElement)biFunction.apply(list, (ToDoubleFunction)cpuLoadStat -> cpuLoadStat.userJvm));
		jsonObject.add("system", (JsonElement)biFunction.apply(list, (ToDoubleFunction)cpuLoadStat -> cpuLoadStat.system));
		return jsonObject;
	}
}
