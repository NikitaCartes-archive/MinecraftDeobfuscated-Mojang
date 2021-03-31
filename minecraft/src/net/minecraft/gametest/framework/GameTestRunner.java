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
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class GameTestRunner {
	private static final int MAX_TESTS_PER_BATCH = 100;
	public static final int PADDING_AROUND_EACH_STRUCTURE = 2;
	public static final int SPACE_BETWEEN_COLUMNS = 5;
	public static final int SPACE_BETWEEN_ROWS = 6;
	public static final int DEFAULT_TESTS_PER_ROW = 8;

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
		Map<String, List<TestFunction>> map = (Map<String, List<TestFunction>>)collection.stream().collect(Collectors.groupingBy(TestFunction::getBatchName));
		return (Collection<GameTestBatch>)map.entrySet()
			.stream()
			.flatMap(
				entry -> {
					String string = (String)entry.getKey();
					Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(string);
					Consumer<ServerLevel> consumer2 = GameTestRegistry.getAfterBatchFunction(string);
					MutableInt mutableInt = new MutableInt();
					Collection<TestFunction> collectionx = (Collection<TestFunction>)entry.getValue();
					return Streams.stream(Iterables.partition(collectionx, 100))
						.map(list -> new GameTestBatch(string + ":" + mutableInt.incrementAndGet(), ImmutableList.<TestFunction>copyOf(list), consumer, consumer2));
				}
			)
			.collect(ImmutableList.toImmutableList());
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
