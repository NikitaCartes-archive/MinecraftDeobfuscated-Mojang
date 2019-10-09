package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameTestBatchRunner {
	private static final Logger LOGGER = LogManager.getLogger();
	private final BlockPos startPos;
	private final ServerLevel level;
	private final GameTestTicker testTicker;
	private final List<GameTestInfo> allTestInfos = Lists.<GameTestInfo>newArrayList();
	private final List<Pair<GameTestBatch, Collection<GameTestInfo>>> batches = Lists.<Pair<GameTestBatch, Collection<GameTestInfo>>>newArrayList();
	private MultipleTestTracker currentBatchTracker;
	private int currentBatchIndex = 0;
	private BlockPos.MutableBlockPos nextTestPos;
	private int maxDepthOnThisRow = 0;

	public GameTestBatchRunner(Collection<GameTestBatch> collection, BlockPos blockPos, ServerLevel serverLevel, GameTestTicker gameTestTicker) {
		this.nextTestPos = new BlockPos.MutableBlockPos(blockPos);
		this.startPos = blockPos;
		this.level = serverLevel;
		this.testTicker = gameTestTicker;
		collection.forEach(gameTestBatch -> {
			Collection<GameTestInfo> collectionx = Lists.<GameTestInfo>newArrayList();

			for (TestFunction testFunction : gameTestBatch.getTestFunctions()) {
				GameTestInfo gameTestInfo = new GameTestInfo(testFunction, serverLevel);
				collectionx.add(gameTestInfo);
				this.allTestInfos.add(gameTestInfo);
			}

			this.batches.add(Pair.of(gameTestBatch, collectionx));
		});
	}

	public List<GameTestInfo> getTestInfos() {
		return this.allTestInfos;
	}

	public void start() {
		this.runBatch(0);
	}

	private void runBatch(int i) {
		this.currentBatchIndex = i;
		this.currentBatchTracker = new MultipleTestTracker();
		if (i < this.batches.size()) {
			Pair<GameTestBatch, Collection<GameTestInfo>> pair = (Pair<GameTestBatch, Collection<GameTestInfo>>)this.batches.get(this.currentBatchIndex);
			GameTestBatch gameTestBatch = pair.getFirst();
			Collection<GameTestInfo> collection = pair.getSecond();
			this.createStructuresForBatch(collection);
			gameTestBatch.runBeforeBatchFunction(this.level);
			String string = gameTestBatch.getName();
			LOGGER.info("Running test batch '" + string + "' (" + collection.size() + " tests)...");
			collection.forEach(gameTestInfo -> {
				this.currentBatchTracker.add(gameTestInfo);
				this.currentBatchTracker.setListener(new GameTestListener() {
					@Override
					public void testStructureLoaded(GameTestInfo gameTestInfo) {
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

	private void createStructuresForBatch(Collection<GameTestInfo> collection) {
		int i = 0;

		for (GameTestInfo gameTestInfo : collection) {
			BlockPos blockPos = new BlockPos(this.nextTestPos);
			gameTestInfo.assignPosition(blockPos);
			StructureUtils.spawnStructure(gameTestInfo.getStructureName(), blockPos, 2, this.level, true);
			BlockPos blockPos2 = gameTestInfo.getStructureSize();
			int j = blockPos2 == null ? 1 : blockPos2.getX();
			int k = blockPos2 == null ? 1 : blockPos2.getZ();
			this.maxDepthOnThisRow = Math.max(this.maxDepthOnThisRow, k);
			this.nextTestPos.move(j + 4, 0, 0);
			if (i++ % 8 == 0) {
				this.nextTestPos.move(0, 0, this.maxDepthOnThisRow + 5);
				this.nextTestPos.setX(this.startPos.getX());
				this.maxDepthOnThisRow = 0;
			}
		}
	}
}
