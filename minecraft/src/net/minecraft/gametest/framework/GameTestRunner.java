package net.minecraft.gametest.framework;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
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

	public static Collection<GameTestInfo> runTestBatches(
		Collection<GameTestBatch> collection, BlockPos blockPos, Rotation rotation, ServerLevel serverLevel, GameTestTicker gameTestTicker, int i
	) {
		GameTestBatchRunner gameTestBatchRunner = new GameTestBatchRunner(collection, blockPos, rotation, serverLevel, gameTestTicker, i);
		gameTestBatchRunner.start();
		return gameTestBatchRunner.getTestInfos();
	}

	public static Collection<GameTestInfo> runTests(
		Collection<TestFunction> collection, BlockPos blockPos, Rotation rotation, ServerLevel serverLevel, GameTestTicker gameTestTicker, int i
	) {
		return runTestBatches(groupTestsIntoBatches(collection), blockPos, rotation, serverLevel, gameTestTicker, i);
	}

	public static Collection<GameTestBatch> groupTestsIntoBatches(Collection<TestFunction> collection) {
		Map<String, Collection<TestFunction>> map = Maps.<String, Collection<TestFunction>>newHashMap();
		collection.forEach(testFunction -> {
			String string = testFunction.getBatchName();
			Collection<TestFunction> collectionx = (Collection<TestFunction>)map.computeIfAbsent(string, stringx -> Lists.newArrayList());
			collectionx.add(testFunction);
		});
		return (Collection<GameTestBatch>)map.keySet()
			.stream()
			.flatMap(
				string -> {
					Collection<TestFunction> collectionx = (Collection<TestFunction>)map.get(string);
					Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(string);
					Consumer<ServerLevel> consumer2 = GameTestRegistry.getAfterBatchFunction(string);
					MutableInt mutableInt = new MutableInt();
					return Streams.stream(Iterables.partition(collectionx, 100))
						.map(list -> new GameTestBatch(string + ":" + mutableInt.incrementAndGet(), list, consumer, consumer2));
				}
			)
			.collect(Collectors.toList());
	}

	public static void clearAllTests(ServerLevel serverLevel, BlockPos blockPos, GameTestTicker gameTestTicker, int i) {
		gameTestTicker.clear();
		BlockPos blockPos2 = blockPos.offset(-i, 0, -i);
		BlockPos blockPos3 = blockPos.offset(i, 0, i);
		BlockPos.betweenClosedStream(blockPos2, blockPos3)
			.filter(blockPosx -> serverLevel.getBlockState(blockPosx).is(Blocks.STRUCTURE_BLOCK))
			.forEach(blockPosx -> {
				StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPosx);
				BlockPos blockPos2x = structureBlockEntity.getBlockPos();
				BoundingBox boundingBox = StructureUtils.getStructureBoundingBox(structureBlockEntity);
				StructureUtils.clearSpaceForStructure(boundingBox, blockPos2x.getY(), serverLevel);
			});
	}

	public static void clearMarkers(ServerLevel serverLevel) {
		DebugPackets.sendGameTestClearPacket(serverLevel);
	}
}
