package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameTestBatchRunner {
	private static final Logger LOGGER = LogManager.getLogger();
	private final BlockPos firstTestNorthWestCorner;
	final ServerLevel level;
	private final GameTestTicker testTicker;
	private final int testsPerRow;
	private final List<GameTestInfo> allTestInfos;
	private final List<Pair<GameTestBatch, Collection<GameTestInfo>>> batches;
	private final BlockPos.MutableBlockPos nextTestNorthWestCorner;

	public GameTestBatchRunner(
		Collection<GameTestBatch> collection, BlockPos blockPos, Rotation rotation, ServerLevel serverLevel, GameTestTicker gameTestTicker, int i
	) {
		this.nextTestNorthWestCorner = blockPos.mutable();
		this.firstTestNorthWestCorner = blockPos;
		this.level = serverLevel;
		this.testTicker = gameTestTicker;
		this.testsPerRow = i;
		this.batches = (List<Pair<GameTestBatch, Collection<GameTestInfo>>>)collection.stream()
			.map(
				gameTestBatch -> {
					Collection<GameTestInfo> collectionx = (Collection<GameTestInfo>)gameTestBatch.getTestFunctions()
						.stream()
						.map(testFunction -> new GameTestInfo(testFunction, rotation, serverLevel))
						.collect(ImmutableList.toImmutableList());
					return Pair.of(gameTestBatch, collectionx);
				}
			)
			.collect(ImmutableList.toImmutableList());
		this.allTestInfos = (List<GameTestInfo>)this.batches
			.stream()
			.flatMap(pair -> ((Collection)pair.getSecond()).stream())
			.collect(ImmutableList.toImmutableList());
	}

	public List<GameTestInfo> getTestInfos() {
		return this.allTestInfos;
	}

	public void start() {
		this.runBatch(0);
	}

	void runBatch(int i) {
		if (i < this.batches.size()) {
			Pair<GameTestBatch, Collection<GameTestInfo>> pair = (Pair<GameTestBatch, Collection<GameTestInfo>>)this.batches.get(i);
			final GameTestBatch gameTestBatch = pair.getFirst();
			Collection<GameTestInfo> collection = pair.getSecond();
			Map<GameTestInfo, BlockPos> map = this.createStructuresForBatch(collection);
			String string = gameTestBatch.getName();
			LOGGER.info("Running test batch '{}' ({} tests)...", string, collection.size());
			gameTestBatch.runBeforeBatchFunction(this.level);
			final MultipleTestTracker multipleTestTracker = new MultipleTestTracker();
			collection.forEach(multipleTestTracker::addTestToTrack);
			multipleTestTracker.addListener(new GameTestListener() {
				private void testCompleted() {
					if (multipleTestTracker.isDone()) {
						gameTestBatch.runAfterBatchFunction(GameTestBatchRunner.this.level);
						GameTestBatchRunner.this.runBatch(i + 1);
					}
				}

				@Override
				public void testStructureLoaded(GameTestInfo gameTestInfo) {
				}

				@Override
				public void testPassed(GameTestInfo gameTestInfo) {
					this.testCompleted();
				}

				@Override
				public void testFailed(GameTestInfo gameTestInfo) {
					this.testCompleted();
				}
			});
			collection.forEach(gameTestInfo -> {
				BlockPos blockPos = (BlockPos)map.get(gameTestInfo);
				GameTestRunner.runTest(gameTestInfo, blockPos, this.testTicker);
			});
		}
	}

	private Map<GameTestInfo, BlockPos> createStructuresForBatch(Collection<GameTestInfo> collection) {
		Map<GameTestInfo, BlockPos> map = Maps.<GameTestInfo, BlockPos>newHashMap();
		int i = 0;
		AABB aABB = new AABB(this.nextTestNorthWestCorner);

		for (GameTestInfo gameTestInfo : collection) {
			BlockPos blockPos = new BlockPos(this.nextTestNorthWestCorner);
			StructureBlockEntity structureBlockEntity = StructureUtils.spawnStructure(
				gameTestInfo.getStructureName(), blockPos, gameTestInfo.getRotation(), 2, this.level, true
			);
			AABB aABB2 = StructureUtils.getStructureBounds(structureBlockEntity);
			gameTestInfo.setStructureBlockPos(structureBlockEntity.getBlockPos());
			map.put(gameTestInfo, new BlockPos(this.nextTestNorthWestCorner));
			aABB = aABB.minmax(aABB2);
			this.nextTestNorthWestCorner.move((int)aABB2.getXsize() + 5, 0, 0);
			if (i++ % this.testsPerRow == this.testsPerRow - 1) {
				this.nextTestNorthWestCorner.move(0, 0, (int)aABB.getZsize() + 6);
				this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
				aABB = new AABB(this.nextTestNorthWestCorner);
			}
		}

		return map;
	}
}
