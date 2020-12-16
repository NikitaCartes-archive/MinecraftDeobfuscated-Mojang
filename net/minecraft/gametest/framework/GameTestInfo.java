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
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class GameTestInfo {
    private final TestFunction testFunction;
    @Nullable
    private BlockPos structureBlockPos;
    private final ServerLevel level;
    private final Collection<GameTestListener> listeners = Lists.newArrayList();
    private final int timeoutTicks;
    private final Collection<GameTestSequence> sequences = Lists.newCopyOnWriteArrayList();
    private final Object2LongMap<Runnable> runAtTickTimeMap = new Object2LongOpenHashMap<Runnable>();
    private long startTick;
    private long tickCount;
    private boolean started;
    private final Stopwatch timer = Stopwatch.createUnstarted();
    private boolean done;
    private final Rotation rotation;
    @Nullable
    private Throwable error;
    @Nullable
    private StructureBlockEntity structureBlockEntity;

    public GameTestInfo(TestFunction testFunction, Rotation rotation, ServerLevel serverLevel) {
        this.testFunction = testFunction;
        this.level = serverLevel;
        this.timeoutTicks = testFunction.getMaxTicks();
        this.rotation = testFunction.getRotation().getRotated(rotation);
    }

    void setStructureBlockPos(BlockPos blockPos) {
        this.structureBlockPos = blockPos;
    }

    void startExecution() {
        this.startTick = this.level.getGameTime() + 1L + this.testFunction.getSetupTicks();
        this.timer.start();
    }

    public void tick() {
        if (this.isDone()) {
            return;
        }
        this.tickInternal();
        if (this.isDone()) {
            if (this.error != null) {
                this.listeners.forEach(gameTestListener -> gameTestListener.testFailed(this));
            } else {
                this.listeners.forEach(gameTestListener -> gameTestListener.testPassed(this));
            }
        }
    }

    private void tickInternal() {
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

    public BlockPos getStructureBlockPos() {
        return this.structureBlockPos;
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
        this.error = throwable;
        this.finish();
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

    public void spawnStructure(BlockPos blockPos, int i) {
        this.structureBlockEntity = StructureUtils.spawnStructure(this.getStructureName(), blockPos, this.getRotation(), i, this.level, false);
        this.structureBlockPos = this.structureBlockEntity.getBlockPos();
        this.structureBlockEntity.setStructureName(this.getTestName());
        StructureUtils.addCommandBlockAndButtonToStartTest(this.structureBlockPos, new BlockPos(1, 0, -1), this.getRotation(), this.level);
        this.listeners.forEach(gameTestListener -> gameTestListener.testStructureLoaded(this));
    }

    public void clearStructure() {
        if (this.structureBlockEntity == null) {
            throw new IllegalStateException("Expected structure to be initialized, but it was null");
        }
        BoundingBox boundingBox = StructureUtils.getStructureBoundingBox(this.structureBlockEntity);
        StructureUtils.clearSpaceForStructure(boundingBox, this.structureBlockPos.getY(), this.level);
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

    public Rotation getRotation() {
        return this.rotation;
    }

    public TestFunction getTestFunction() {
        return this.testFunction;
    }

    public boolean isFlaky() {
        return this.testFunction.isFlaky();
    }

    public int maxAttempts() {
        return this.testFunction.getMaxAttempts();
    }

    public int requiredSuccesses() {
        return this.testFunction.getRequiredSuccesses();
    }
}

