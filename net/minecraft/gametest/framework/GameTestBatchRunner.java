/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameTestBatchRunner {
    private static final Logger LOGGER = LogManager.getLogger();
    private final BlockPos firstTestNorthWestCorner;
    private final ServerLevel level;
    private final GameTestTicker testTicker;
    private final int testsPerRow;
    private final List<GameTestInfo> allTestInfos = Lists.newArrayList();
    private final Map<GameTestInfo, BlockPos> northWestCorners = Maps.newHashMap();
    private final List<Pair<GameTestBatch, Collection<GameTestInfo>>> batches = Lists.newArrayList();
    private MultipleTestTracker currentBatchTracker;
    private int currentBatchIndex = 0;
    private BlockPos.MutableBlockPos nextTestNorthWestCorner;

    public GameTestBatchRunner(Collection<GameTestBatch> collection, BlockPos blockPos, Rotation rotation, ServerLevel serverLevel, GameTestTicker gameTestTicker, int i) {
        this.nextTestNorthWestCorner = blockPos.mutable();
        this.firstTestNorthWestCorner = blockPos;
        this.level = serverLevel;
        this.testTicker = gameTestTicker;
        this.testsPerRow = i;
        collection.forEach(gameTestBatch -> {
            ArrayList<GameTestInfo> collection = Lists.newArrayList();
            Collection<TestFunction> collection2 = gameTestBatch.getTestFunctions();
            for (TestFunction testFunction : collection2) {
                GameTestInfo gameTestInfo = new GameTestInfo(testFunction, rotation, serverLevel);
                collection.add(gameTestInfo);
                this.allTestInfos.add(gameTestInfo);
            }
            this.batches.add(Pair.of(gameTestBatch, collection));
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
        if (i >= this.batches.size()) {
            return;
        }
        Pair<GameTestBatch, Collection<GameTestInfo>> pair = this.batches.get(this.currentBatchIndex);
        GameTestBatch gameTestBatch = pair.getFirst();
        Collection<GameTestInfo> collection = pair.getSecond();
        this.createStructuresForBatch(collection);
        gameTestBatch.runBeforeBatchFunction(this.level);
        String string = gameTestBatch.getName();
        LOGGER.info("Running test batch '" + string + "' (" + collection.size() + " tests)...");
        collection.forEach(gameTestInfo -> {
            this.currentBatchTracker.addTestToTrack((GameTestInfo)gameTestInfo);
            this.currentBatchTracker.addListener(new GameTestListener(){

                @Override
                public void testStructureLoaded(GameTestInfo gameTestInfo) {
                }

                @Override
                public void testFailed(GameTestInfo gameTestInfo) {
                    GameTestBatchRunner.this.testCompleted(gameTestInfo);
                }
            });
            BlockPos blockPos = this.northWestCorners.get(gameTestInfo);
            GameTestRunner.runTest(gameTestInfo, blockPos, this.testTicker);
        });
    }

    private void testCompleted(GameTestInfo gameTestInfo) {
        if (this.currentBatchTracker.isDone()) {
            this.runBatch(this.currentBatchIndex + 1);
        }
    }

    private void createStructuresForBatch(Collection<GameTestInfo> collection) {
        int i = 0;
        AABB aABB = new AABB(this.nextTestNorthWestCorner);
        for (GameTestInfo gameTestInfo : collection) {
            BlockPos blockPos = new BlockPos(this.nextTestNorthWestCorner);
            StructureBlockEntity structureBlockEntity = StructureUtils.spawnStructure(gameTestInfo.getStructureName(), blockPos, gameTestInfo.getRotation(), 2, this.level, true);
            AABB aABB2 = StructureUtils.getStructureBounds(structureBlockEntity);
            gameTestInfo.setStructureBlockPos(structureBlockEntity.getBlockPos());
            this.northWestCorners.put(gameTestInfo, new BlockPos(this.nextTestNorthWestCorner));
            aABB = aABB.minmax(aABB2);
            this.nextTestNorthWestCorner.move((int)aABB2.getXsize() + 5, 0, 0);
            if (i++ % this.testsPerRow != this.testsPerRow - 1) continue;
            this.nextTestNorthWestCorner.move(0, 0, (int)aABB.getZsize() + 6);
            this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
            aABB = new AABB(this.nextTestNorthWestCorner);
        }
    }
}

