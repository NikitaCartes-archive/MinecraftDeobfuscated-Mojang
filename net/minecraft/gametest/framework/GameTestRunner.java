/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestBatchRunner;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.ReportGameListener;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class GameTestRunner {
    public static void runTest(GameTestInfo gameTestInfo, BlockPos blockPos, GameTestTicker gameTestTicker) {
        gameTestInfo.startExecution();
        gameTestTicker.add(gameTestInfo);
        gameTestInfo.addListener(new ReportGameListener(gameTestInfo, gameTestTicker, blockPos));
        gameTestInfo.spawnStructure(blockPos, 2);
    }

    public static Collection<GameTestInfo> runTestBatches(Collection<GameTestBatch> collection, BlockPos blockPos, Rotation rotation, ServerLevel serverLevel, GameTestTicker gameTestTicker, int i) {
        GameTestBatchRunner gameTestBatchRunner = new GameTestBatchRunner(collection, blockPos, rotation, serverLevel, gameTestTicker, i);
        gameTestBatchRunner.start();
        return gameTestBatchRunner.getTestInfos();
    }

    public static Collection<GameTestInfo> runTests(Collection<TestFunction> collection, BlockPos blockPos, Rotation rotation, ServerLevel serverLevel, GameTestTicker gameTestTicker, int i) {
        return GameTestRunner.runTestBatches(GameTestRunner.groupTestsIntoBatches(collection), blockPos, rotation, serverLevel, gameTestTicker, i);
    }

    public static Collection<GameTestBatch> groupTestsIntoBatches(Collection<TestFunction> collection) {
        Map<String, List<TestFunction>> map = collection.stream().collect(Collectors.groupingBy(TestFunction::getBatchName));
        return map.entrySet().stream().flatMap(entry -> {
            String string = (String)entry.getKey();
            Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(string);
            Consumer<ServerLevel> consumer2 = GameTestRegistry.getAfterBatchFunction(string);
            MutableInt mutableInt = new MutableInt();
            Collection collection = (Collection)entry.getValue();
            return Streams.stream(Iterables.partition(collection, 100)).map(list -> new GameTestBatch(string + ":" + mutableInt.incrementAndGet(), ImmutableList.copyOf(list), consumer, consumer2));
        }).collect(ImmutableList.toImmutableList());
    }

    public static void clearAllTests(ServerLevel serverLevel, BlockPos blockPos2, GameTestTicker gameTestTicker, int i) {
        gameTestTicker.clear();
        BlockPos blockPos22 = blockPos2.offset(-i, 0, -i);
        BlockPos blockPos3 = blockPos2.offset(i, 0, i);
        BlockPos.betweenClosedStream(blockPos22, blockPos3).filter(blockPos -> serverLevel.getBlockState((BlockPos)blockPos).is(Blocks.STRUCTURE_BLOCK)).forEach(blockPos -> {
            StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity((BlockPos)blockPos);
            BlockPos blockPos2 = structureBlockEntity.getBlockPos();
            BoundingBox boundingBox = StructureUtils.getStructureBoundingBox(structureBlockEntity);
            StructureUtils.clearSpaceForStructure(boundingBox, blockPos2.getY(), serverLevel);
        });
    }

    public static void clearMarkers(ServerLevel serverLevel) {
        DebugPackets.sendGameTestClearPacket(serverLevel);
    }
}

