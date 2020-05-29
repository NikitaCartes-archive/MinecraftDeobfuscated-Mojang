package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;

public class GameTestInfo {
	private final TestFunction testFunction;
	@Nullable
	private BlockPos structureBlockPos;
	private final ServerLevel level;
	private final Collection<GameTestListener> listeners = Lists.<GameTestListener>newArrayList();
	private final int timeoutTicks;
	private final Collection<GameTestSequence> sequences = Lists.<GameTestSequence>newCopyOnWriteArrayList();
	private Object2LongMap<Runnable> runAtTickTimeMap = new Object2LongOpenHashMap<>();
	private long startTick;
	private long tickCount;
	private boolean started = false;
	private final Stopwatch timer = Stopwatch.createUnstarted();
	private boolean done = false;
	private final Rotation rotation;
	@Nullable
	private Throwable error;

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
		if (!this.isDone()) {
			this.tickCount = this.level.getGameTime() - this.startTick;
			if (this.tickCount >= 0L) {
				if (this.tickCount == 0L) {
					this.startTest();
				}

				ObjectIterator<Entry<Runnable>> objectIterator = this.runAtTickTimeMap.object2LongEntrySet().iterator();

				while (objectIterator.hasNext()) {
					Entry<Runnable> entry = (Entry<Runnable>)objectIterator.next();
					if (entry.getLongValue() <= this.tickCount) {
						try {
							((Runnable)entry.getKey()).run();
						} catch (Exception var4) {
							this.fail(var4);
						}

						objectIterator.remove();
					}
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
		}
	}

	private void startTest() {
		if (this.started) {
			throw new IllegalStateException("Test already started");
		} else {
			this.started = true;

			try {
				this.testFunction.run(new GameTestHelper(this));
			} catch (Exception var2) {
				this.fail(var2);
			}
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

	public void spawnStructure(BlockPos blockPos, int i) {
		StructureBlockEntity structureBlockEntity = StructureUtils.spawnStructure(this.getStructureName(), blockPos, this.getRotation(), i, this.level, false);
		this.setStructureBlockPos(structureBlockEntity.getBlockPos());
		structureBlockEntity.setStructureName(this.getTestName());
		StructureUtils.addCommandBlockAndButtonToStartTest(this.structureBlockPos, new BlockPos(1, 0, -1), this.getRotation(), this.level);
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

	public Rotation getRotation() {
		return this.rotation;
	}

	public TestFunction getTestFunction() {
		return this.testFunction;
	}
}
