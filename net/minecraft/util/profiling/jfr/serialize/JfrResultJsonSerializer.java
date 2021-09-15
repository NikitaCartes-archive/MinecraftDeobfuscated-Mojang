/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import com.mojang.datafixers.util.Pair;
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

public class JfrResultJsonSerializer {
    private static final String BYTES_PER_SECOND = "bytesPerSecond";
    private static final String COUNT = "count";
    private static final String DURATION_NANOS_TOTAL = "durationNanosTotal";
    private static final String TOTAL_BYTES = "totalBytes";
    private static final String COUNT_PER_SECOND = "countPerSecond";
    final Gson gson = new GsonBuilder().setPrettyPrinting().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT).create();

    public String format(JfrStatsResult jfrStatsResult) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("startedEpoch", jfrStatsResult.recordingStarted().toEpochMilli());
        jsonObject.addProperty("endedEpoch", jfrStatsResult.recordingEnded().toEpochMilli());
        jsonObject.addProperty("durationMs", jfrStatsResult.recordingDuration().toMillis());
        Duration duration = jfrStatsResult.worldCreationDuration();
        if (duration != null) {
            jsonObject.addProperty("worldGenDurationMs", duration.toMillis());
        }
        jsonObject.add("heap", this.heap(jfrStatsResult.heapSummary()));
        jsonObject.add("cpuPercent", this.cpu(jfrStatsResult.cpuLoadStats()));
        jsonObject.add("network", this.network(jfrStatsResult));
        jsonObject.add("fileIO", this.fileIO(jfrStatsResult));
        jsonObject.add("serverTick", this.serverTicks(jfrStatsResult.tickTimes()));
        jsonObject.add("threadAllocation", this.threadAllocations(jfrStatsResult.threadAllocationSummary()));
        jsonObject.add("chunkGen", this.chunkGen(jfrStatsResult.chunkGenSummary()));
        return this.gson.toJson(jsonObject);
    }

    private JsonElement heap(GcHeapStat.Summary summary) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("allocationRateBytesPerSecond", summary.allocationRateBytesPerSecond());
        jsonObject.addProperty("gcCount", summary.totalGCs());
        jsonObject.addProperty("gcOverHeadPercent", Float.valueOf(summary.gcOverHead()));
        jsonObject.addProperty("gcTotalDurationMs", summary.gcTotalDuration().toMillis());
        return jsonObject;
    }

    private JsonElement chunkGen(List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> list) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(DURATION_NANOS_TOTAL, list.stream().mapToDouble(pair -> ((TimedStatSummary)pair.getSecond()).totalDuration().toNanos()).sum());
        JsonArray jsonArray2 = Util.make(new JsonArray(), jsonArray -> jsonObject.add("status", (JsonElement)jsonArray));
        for (Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>> pair2 : list) {
            TimedStatSummary<ChunkGenStat> timedStatSummary = pair2.getSecond();
            JsonObject jsonObject22 = Util.make(new JsonObject(), jsonArray2::add);
            jsonObject22.addProperty("state", pair2.getFirst().getName());
            jsonObject22.addProperty(COUNT, timedStatSummary.count());
            jsonObject22.addProperty(DURATION_NANOS_TOTAL, timedStatSummary.totalDuration().toNanos());
            jsonObject22.addProperty("durationNanosAvg", timedStatSummary.totalDuration().toNanos() / (long)timedStatSummary.count());
            JsonObject jsonObject3 = Util.make(new JsonObject(), jsonObject2 -> jsonObject22.add("durationNanosPercentiles", (JsonElement)jsonObject2));
            timedStatSummary.percentilesNanos().forEach((integer, double_) -> jsonObject3.addProperty("p" + integer, (Number)double_));
            Function<ChunkGenStat, JsonElement> function = chunkGenStat -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("durationNanos", chunkGenStat.duration().toNanos());
                jsonObject.addProperty("level", chunkGenStat.level());
                jsonObject.addProperty("chunkPosX", chunkGenStat.chunkPos().x);
                jsonObject.addProperty("chunkPosZ", chunkGenStat.chunkPos().z);
                jsonObject.addProperty("worldPosX", chunkGenStat.worldPos().x);
                jsonObject.addProperty("worldPosZ", chunkGenStat.worldPos().z);
                return jsonObject;
            };
            jsonObject22.add("fastest", function.apply((ChunkGenStat)timedStatSummary.fastest()));
            jsonObject22.add("slowest", function.apply((ChunkGenStat)timedStatSummary.slowest()));
            jsonObject22.add("secondSlowest", timedStatSummary.secondSlowest() != null ? function.apply((ChunkGenStat)timedStatSummary.secondSlowest()) : JsonNull.INSTANCE);
        }
        return jsonObject;
    }

    private JsonElement threadAllocations(ThreadAllocationStat.Summary summary) {
        JsonArray jsonArray = new JsonArray();
        summary.allocationsPerSecondByThread().forEach((string, double_) -> jsonArray.add(Util.make(new JsonObject(), jsonObject -> {
            jsonObject.addProperty("thread", (String)string);
            jsonObject.addProperty(BYTES_PER_SECOND, (Number)double_);
        })));
        return jsonArray;
    }

    private JsonElement serverTicks(List<TickTimeStat> list) {
        if (list.isEmpty()) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        DoubleSummaryStatistics doubleSummaryStatistics = list.stream().mapToDouble(TickTimeStat::currentAverage).summaryStatistics();
        jsonObject.addProperty("minMs", doubleSummaryStatistics.getMin());
        jsonObject.addProperty("averageMs", doubleSummaryStatistics.getAverage());
        jsonObject.addProperty("maxMs", doubleSummaryStatistics.getMax());
        Percentiles.evaluate(list.stream().mapToDouble(TickTimeStat::currentAverage).toArray()).forEach((integer, double_) -> jsonObject.addProperty("p" + integer, (Number)double_));
        return jsonObject;
    }

    private JsonElement fileIO(JfrStatsResult jfrStatsResult) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("write", this.fileIoSummary(jfrStatsResult.fileWrites()));
        jsonObject.add("read", this.fileIoSummary(jfrStatsResult.fileReads()));
        return jsonObject;
    }

    private JsonElement fileIoSummary(FileIOStat.Summary summary) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TOTAL_BYTES, summary.totalBytes());
        jsonObject.addProperty(COUNT, summary.counts());
        jsonObject.addProperty(BYTES_PER_SECOND, summary.bytesPerSecond());
        jsonObject.addProperty(COUNT_PER_SECOND, summary.countsPerSecond());
        JsonArray jsonArray = new JsonArray();
        jsonObject.add("topContributors", jsonArray);
        summary.topTenContributorsByTotalBytes().forEach(pair -> {
            JsonObject jsonObject = new JsonObject();
            jsonArray.add(jsonObject);
            jsonObject.addProperty("path", (String)pair.getFirst());
            jsonObject.addProperty(TOTAL_BYTES, (Number)pair.getSecond());
        });
        return jsonObject;
    }

    private JsonElement network(JfrStatsResult jfrStatsResult) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("sent", this.packets(jfrStatsResult.sentPackets()));
        jsonObject.add("received", this.packets(jfrStatsResult.receivedPackets()));
        return jsonObject;
    }

    private JsonElement packets(PacketStat.Summary summary) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TOTAL_BYTES, summary.totalSize());
        jsonObject.addProperty(COUNT, summary.totalCount());
        jsonObject.addProperty(BYTES_PER_SECOND, summary.sizePerSecond());
        jsonObject.addProperty(COUNT_PER_SECOND, summary.countsPerSecond());
        JsonArray jsonArray = new JsonArray();
        jsonObject.add("topContributors", jsonArray);
        summary.largestSizeContributors().stream().limit(10L).forEach(pair -> {
            JsonObject jsonObject = new JsonObject();
            jsonArray.add(jsonObject);
            jsonObject.addProperty("packetName", (String)pair.getFirst());
            jsonObject.addProperty(TOTAL_BYTES, (Number)pair.getSecond());
        });
        return jsonObject;
    }

    private JsonElement cpu(List<CpuLoadStat> list2) {
        JsonObject jsonObject = new JsonObject();
        BiFunction<List, ToDoubleFunction, JsonObject> biFunction = (list, toDoubleFunction) -> {
            JsonObject jsonObject = new JsonObject();
            DoubleSummaryStatistics doubleSummaryStatistics = list.stream().mapToDouble(toDoubleFunction).summaryStatistics();
            jsonObject.addProperty("min", doubleSummaryStatistics.getMin());
            jsonObject.addProperty("average", doubleSummaryStatistics.getAverage());
            jsonObject.addProperty("max", doubleSummaryStatistics.getMax());
            return jsonObject;
        };
        jsonObject.add("jvm", biFunction.apply(list2, CpuLoadStat::jvm));
        jsonObject.add("userJvm", biFunction.apply(list2, CpuLoadStat::userJvm));
        jsonObject.add("system", biFunction.apply(list2, CpuLoadStat::system));
        return jsonObject;
    }
}

