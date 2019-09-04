package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.StructureBlockEntity;

public class GameTestInfo {
	private final TestFunction testFunction;
	private final BlockPos testPos;
	private final ServerLevel level;
	private final Collection<GameTestListener> listeners = Lists.<GameTestListener>newArrayList();
	private int remainingTicksUntilTimeout;
	private Runnable succeedWhenThisAssertPasses;
	private boolean started = false;
	private long startTime = -1L;
	private boolean done = false;
	private long doneTime = -1L;
	@Nullable
	private Throwable error;

	public GameTestInfo(TestFunction testFunction, BlockPos blockPos, ServerLevel serverLevel) {
		this.testFunction = testFunction;
		this.testPos = blockPos;
		this.level = serverLevel;
		this.remainingTicksUntilTimeout = testFunction.getMaxTicks();
	}

	public void tick() {
		if (!this.isDone()) {
			this.remainingTicksUntilTimeout--;
			if (this.remainingTicksUntilTimeout <= 0) {
				if (this.succeedWhenThisAssertPasses == null) {
					this.fail(new GameTestTimeoutException("Didn't succeed or fail within " + this.testFunction.getMaxTicks() + " ticks"));
				} else {
					this.tryAssertAndEndTest();
				}
			} else if (this.succeedWhenThisAssertPasses != null) {
				this.tryAssertAndEndTestIfSuccessful();
			}
		}
	}

	public String getTestName() {
		return this.testFunction.getTestName();
	}

	public BlockPos getTestPos() {
		return this.testPos;
	}

	public void spawnStructureAndRunTest(int i) {
		try {
			StructureBlockEntity structureBlockEntity = StructureUtils.spawnStructure(this.testFunction.getStructureName(), this.testPos, i, this.level, false);
			structureBlockEntity.setStructureName(this.getTestName());
			StructureUtils.addCommandBlockAndButtonToStartTest(this.testPos.offset(1, 0, -1), this.level);
			this.listeners.forEach(gameTestListener -> gameTestListener.testStructureLoaded(this));
			this.testFunction.run(new GameTestHelper(this));
		} catch (RuntimeException var3) {
			this.fail(var3);
		}
	}

	@Nullable
	public BlockPos getStructureSize() {
		StructureBlockEntity structureBlockEntity = this.getStructureBlockEntity();
		return structureBlockEntity == null ? null : structureBlockEntity.getStructureSize();
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

	public void succeed() {
		this.done = true;
		this.doneTime = Util.getMillis();
		this.error = null;
		this.succeedWhenThisAssertPasses = null;
		this.listeners.forEach(gameTestListener -> gameTestListener.testPassed(this));
	}

	public void fail(Throwable throwable) {
		this.done = true;
		this.doneTime = Util.getMillis();
		this.error = throwable;
		this.succeedWhenThisAssertPasses = null;
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

	private void tryAssertAndEndTest() {
		try {
			this.succeedWhenThisAssertPasses.run();
			this.succeed();
		} catch (Exception var2) {
			this.fail(var2);
		}
	}

	private void tryAssertAndEndTestIfSuccessful() {
		try {
			this.succeedWhenThisAssertPasses.run();
			this.succeed();
		} catch (Exception var2) {
		}
	}

	public void spawnStructure(int i) {
		StructureUtils.spawnStructure(this.testFunction.getStructureName(), this.testPos, i, this.level, false);
		this.started = true;
		this.startTime = Util.getMillis();
	}

	public boolean isRequired() {
		return this.testFunction.isRequired();
	}

	public boolean isOptional() {
		return !this.testFunction.isRequired();
	}
}
