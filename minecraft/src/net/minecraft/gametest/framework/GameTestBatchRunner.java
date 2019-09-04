package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameTestBatchRunner {
	private static final Logger LOGGER = LogManager.getLogger();
	private final List<GameTestBatch> batches;
	private final BlockPos startPos;
	private final ServerLevel level;
	private final GameTestTicker testTicker;
	private final List<GameTestInfo> allTestInfos = Lists.<GameTestInfo>newArrayList();
	private final Map<String, Collection<GameTestInfo>> testInfosPerBatch = Maps.<String, Collection<GameTestInfo>>newHashMap();
	private MultipleTestTracker currentBatchTracker;
	private int currentBatchIndex = 0;
	private BlockPos.MutableBlockPos nextTestPos;
	private int maxDepthOnThisRow = 0;

	public GameTestBatchRunner(Collection<GameTestBatch> collection, BlockPos blockPos, ServerLevel serverLevel, GameTestTicker gameTestTicker) {
		this.batches = Lists.<GameTestBatch>newArrayList(collection);
		this.nextTestPos = new BlockPos.MutableBlockPos(blockPos);
		this.startPos = blockPos;
		this.level = serverLevel;
		this.testTicker = gameTestTicker;
	}

	private void spawnAllStructureBlocksAndShowBounds() {
		this.batches.forEach(gameTestBatch -> {
			Collection<GameTestInfo> collection = Lists.<GameTestInfo>newArrayList();

			for (TestFunction testFunction : gameTestBatch.getTestFunctions()) {
				BlockPos blockPos = new BlockPos(this.nextTestPos);
				StructureUtils.spawnStructure(testFunction.getStructureName(), blockPos, 2, this.level, true);
				GameTestInfo gameTestInfo = new GameTestInfo(testFunction, blockPos, this.level);
				collection.add(gameTestInfo);
				this.allTestInfos.add(gameTestInfo);
				this.testInfosPerBatch.put(gameTestBatch.getName(), collection);
				BlockPos blockPos2 = gameTestInfo.getStructureSize();
				int i = blockPos2 == null ? 1 : blockPos2.getX();
				int j = blockPos2 == null ? 1 : blockPos2.getZ();
				this.maxDepthOnThisRow = Math.max(this.maxDepthOnThisRow, j);
				this.nextTestPos.move(i + 4, 0, 0);
				if (this.allTestInfos.size() % 8 == 0) {
					this.nextTestPos.move(0, 0, this.maxDepthOnThisRow + 5);
					this.nextTestPos.setX(this.startPos.getX());
					this.maxDepthOnThisRow = 0;
				}
			}
		});
	}

	public List<GameTestInfo> getTestInfos() {
		return this.allTestInfos;
	}

	public void start() {
		this.spawnAllStructureBlocksAndShowBounds();
		this.runBatch(0);
	}

	private void runBatch(int i) {
		this.currentBatchIndex = i;
		this.currentBatchTracker = new MultipleTestTracker();
		if (i < this.batches.size()) {
			GameTestBatch gameTestBatch = (GameTestBatch)this.batches.get(this.currentBatchIndex);
			gameTestBatch.runBeforeBatchFunction(this.level);
			this.spawnTestStructures(gameTestBatch);
			String string = gameTestBatch.getName();
			Collection<GameTestInfo> collection = (Collection<GameTestInfo>)this.testInfosPerBatch.get(string);
			LOGGER.info("Running test batch '" + string + "' (" + collection.size() + " tests)...");
			collection.forEach(gameTestInfo -> {
				this.currentBatchTracker.add(gameTestInfo);
				this.currentBatchTracker.setListener(new GameTestListener() {
					@Override
					public void testStructureLoaded(GameTestInfo gameTestInfo) {
					}

					@Override
					public void testPassed(GameTestInfo gameTestInfo) {
						GameTestBatchRunner.this.testCompleted(gameTestInfo);
					}

					@Override
					public void testFailed(GameTestInfo gameTestInfo) {
						GameTestBatchRunner.this.testCompleted(gameTestInfo);
					}
				});
				GameTestRunner.runTest(gameTestInfo, this.testTicker);
			});
		}
	}

	private void testCompleted(GameTestInfo gameTestInfo) {
		if (this.currentBatchTracker.isDone()) {
			this.runBatch(this.currentBatchIndex + 1);
		}
	}

	private void spawnTestStructures(GameTestBatch gameTestBatch) {
		Collection<GameTestInfo> collection = (Collection<GameTestInfo>)this.testInfosPerBatch.get(gameTestBatch.getName());
		collection.forEach(gameTestInfo -> gameTestInfo.spawnStructure(2));
	}
}
