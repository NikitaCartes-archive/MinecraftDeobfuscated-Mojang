/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.gametest.framework.GameTestTimeoutException;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.jetbrains.annotations.Nullable;

public class GameTestInfo {
    private final TestFunction testFunction;
    private BlockPos testPos;
    private final ServerLevel level;
    private final Collection<GameTestListener> listeners = Lists.newArrayList();
    private final int timeoutTicks;
    private final Collection<GameTestSequence> sequences = Lists.newCopyOnWriteArrayList();
    private Object2LongMap<Runnable> runAtTickTimeMap = new Object2LongOpenHashMap<Runnable>();
    private long startTick;
    private long tickCount;
    private boolean started = false;
    private final Stopwatch timer = Stopwatch.createUnstarted();
    private boolean done = false;
    @Nullable
    private Throwable error;

    public GameTestInfo(TestFunction testFunction, ServerLevel serverLevel) {
        this.testFunction = testFunction;
        this.level = serverLevel;
        this.timeoutTicks = testFunction.getMaxTicks();
    }

    public GameTestInfo(TestFunction testFunction, BlockPos blockPos, ServerLevel serverLevel) {
        this(testFunction, serverLevel);
        this.assignPosition(blockPos);
    }

    void assignPosition(BlockPos blockPos) {
        this.testPos = blockPos;
    }

    void startExecution() {
        this.startTick = this.level.getGameTime() + 1L + this.testFunction.getSetupTicks();
        this.timer.start();
    }

    public void tick() {
        if (this.isDone()) {
            return;
        }
        this.tickCount = this.level.getGameTime() - this.startTick;
        if (this.tickCount < 0L) {
            return;
        }
        if (this.tickCount == 0L) {
            this.startTest();
        }
        Iterator objectIterator = this.runAtTickTimeMap.object2LongEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Object2LongMap.Entry entry = (Object2LongMap.Entry)objectIterator.next();
            if (entry.getLongValue() > this.tickCount) continue;
            try {
                ((Runnable)entry.getKey()).run();
            } catch (Exception exception) {
                this.fail(exception);
            }
            objectIterator.remove();
        }
        if (this.tickCount > (long)this.timeoutTicks) {
            if (this.sequences.isEmpty()) {
                this.fail(new GameTestTimeoutException("Didn't succeed or fail within " + this.testFunction.getMaxTicks() + " ticks"));
            } else {
                this.sequences.forEach(gameTestSequence -> gameTestSequence.tickAndFailIfNotComplete(this.tickCount));
                if (this.error == null) {
                    this.fail(new GameTestTimeoutException("No sequences finished"));
                }
            }
        } else {
            this.sequences.forEach(gameTestSequence -> gameTestSequence.tickAndContinue(this.tickCount));
        }
    }

    private void startTest() {
        if (this.started) {
            throw new IllegalStateException("Test already started");
        }
        this.started = true;
        try {
            this.testFunction.run(new GameTestHelper(this));
        } catch (Exception exception) {
            this.fail(exception);
        }
    }

    public String getTestName() {
        return this.testFunction.getTestName();
    }

    public BlockPos getTestPos() {
        return this.testPos;
    }

    @Nullable
    public BlockPos getStructureSize() {
        StructureBlockEntity structureBlockEntity = this.getStructureBlockEntity();
        if (structureBlockEntity == null) {
            return null;
        }
        return structureBlockEntity.getStructureSize();
    }

    @Nullable
    private StructureBlockEntity getStructureBlockEntity() {
        return (StructureBlockEntity)this.level.getBlockEntity(this.testPos);
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public boolean hasSucceeded() {
        return this.done && this.error == null;
    }

    public boolean hasFailed() {
        return this.error != null;
    }

    public boolean hasStarted() {
        return this.started;
    }

    public boolean isDone() {
        return this.done;
    }

    private void finish() {
        if (!this.done) {
            this.done = true;
            this.timer.stop();
        }
    }

    public void fail(Throwable throwable) {
        this.finish();
        this.error = throwable;
        this.listeners.forEach(gameTestListener -> gameTestListener.testFailed(this));
    }

    @Nullable
    public Throwable getError() {
        return this.error;
    }

    public String toString() {
        return this.getTestName();
    }

    public void addListener(GameTestListener gameTestListener) {
        this.listeners.add(gameTestListener);
    }

    public void spawnStructure(int i) {
        StructureBlockEntity structureBlockEntity = StructureUtils.spawnStructure(this.testFunction.getStructureName(), this.testPos, i, this.level, false);
        structureBlockEntity.setStructureName(this.getTestName());
        StructureUtils.addCommandBlockAndButtonToStartTest(this.testPos.offset(1, 0, -1), this.level);
        this.listeners.forEach(gameTestListener -> gameTestListener.testStructureLoaded(this));
    }

    public boolean isRequired() {
        return this.testFunction.isRequired();
    }

    public boolean isOptional() {
        return !this.testFunction.isRequired();
    }

    public String getStructureName() {
        return this.testFunction.getStructureName();
    }
}

