package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

public class GameTestInfo {
	private final TestFunction testFunction;
	@Nullable
	private BlockPos structureBlockPos;
	@Nullable
	private BlockPos northWestCorner;
	private final ServerLevel level;
	private final Collection<GameTestListener> listeners = Lists.<GameTestListener>newArrayList();
	private final int timeoutTicks;
	private final Collection<GameTestSequence> sequences = Lists.<GameTestSequence>newCopyOnWriteArrayList();
	private final Object2LongMap<Runnable> runAtTickTimeMap = new Object2LongOpenHashMap<>();
	private long startTick;
	private int ticksToWaitForChunkLoading = 20;
	private boolean placedStructure;
	private boolean chunksLoaded;
	private long tickCount;
	private boolean started;
	private final RetryOptions retryOptions;
	private final Stopwatch timer = Stopwatch.createUnstarted();
	private boolean done;
	private final Rotation rotation;
	@Nullable
	private Throwable error;
	@Nullable
	private StructureBlockEntity structureBlockEntity;

	public GameTestInfo(TestFunction testFunction, Rotation rotation, ServerLevel serverLevel, RetryOptions retryOptions) {
		this.testFunction = testFunction;
		this.level = serverLevel;
		this.retryOptions = retryOptions;
		this.timeoutTicks = testFunction.maxTicks();
		this.rotation = testFunction.rotation().getRotated(rotation);
	}

	void setStructureBlockPos(BlockPos blockPos) {
		this.structureBlockPos = blockPos;
	}

	public GameTestInfo startExecution(int i) {
		this.startTick = this.level.getGameTime() + this.testFunction.setupTicks() + (long)i;
		this.timer.start();
		return this;
	}

	public GameTestInfo placeStructure() {
		if (this.placedStructure) {
			return this;
		} else {
			this.ticksToWaitForChunkLoading = 0;
			this.placedStructure = true;
			StructureBlockEntity structureBlockEntity = this.getStructureBlockEntity();
			structureBlockEntity.placeStructure(this.level);
			BoundingBox boundingBox = StructureUtils.getStructureBoundingBox(structureBlockEntity);
			this.level.getBlockTicks().clearArea(boundingBox);
			this.level.clearBlockEvents(boundingBox);
			return this;
		}
	}

	private boolean ensureStructureIsPlaced() {
		if (this.placedStructure) {
			return true;
		} else if (this.ticksToWaitForChunkLoading > 0) {
			this.ticksToWaitForChunkLoading--;
			return false;
		} else {
			this.placeStructure().startExecution(0);
			return true;
		}
	}

	public void tick(GameTestRunner gameTestRunner) {
		if (!this.isDone()) {
			if (this.structureBlockEntity == null) {
				this.fail(new IllegalStateException("Running test without structure block entity"));
			}

			if (this.chunksLoaded
				|| StructureUtils.getStructureBoundingBox(this.structureBlockEntity)
					.intersectingChunks()
					.allMatch(chunkPos -> this.level.isPositionEntityTicking(chunkPos.getWorldPosition()))) {
				this.chunksLoaded = true;
				if (this.ensureStructureIsPlaced()) {
					this.tickInternal();
					if (this.isDone()) {
						if (this.error != null) {
							this.listeners.forEach(gameTestListener -> gameTestListener.testFailed(this, gameTestRunner));
						} else {
							this.listeners.forEach(gameTestListener -> gameTestListener.testPassed(this, gameTestRunner));
						}
					}
				}
			}
		}
	}

	private void tickInternal() {
		this.tickCount = this.level.getGameTime() - this.startTick;
		if (this.tickCount >= 0L) {
			if (!this.started) {
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
					this.fail(new GameTestTimeoutException("Didn't succeed or fail within " + this.testFunction.maxTicks() + " ticks"));
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

	private void startTest() {
		if (!this.started) {
			this.started = true;

			try {
				this.testFunction.run(new GameTestHelper(this));
			} catch (Exception var2) {
				this.fail(var2);
			}
		}
	}

	public void setRunAtTickTime(long l, Runnable runnable) {
		this.runAtTickTimeMap.put(runnable, l);
	}

	public String getTestName() {
		return this.testFunction.testName();
	}

	@Nullable
	public BlockPos getStructureBlockPos() {
		return this.structureBlockPos;
	}

	public AABB getStructureBounds() {
		StructureBlockEntity structureBlockEntity = this.getStructureBlockEntity();
		return StructureUtils.getStructureBounds(structureBlockEntity);
	}

	public StructureBlockEntity getStructureBlockEntity() {
		if (this.structureBlockEntity == null) {
			if (this.structureBlockPos == null) {
				throw new IllegalStateException("Could not find a structureBlockEntity for this GameTestInfo");
			}

			this.structureBlockEntity = (StructureBlockEntity)this.level.getBlockEntity(this.structureBlockPos);
			if (this.structureBlockEntity == null) {
				throw new IllegalStateException("Could not find a structureBlockEntity at the given coordinate " + this.structureBlockPos);
			}
		}

		return this.structureBlockEntity;
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

	public long getRunTime() {
		return this.timer.elapsed(TimeUnit.MILLISECONDS);
	}

	private void finish() {
		if (!this.done) {
			this.done = true;
			if (this.timer.isRunning()) {
				this.timer.stop();
			}
		}
	}

	public void succeed() {
		if (this.error == null) {
			this.finish();
			AABB aABB = this.getStructureBounds();
			List<Entity> list = this.getLevel().getEntitiesOfClass(Entity.class, aABB.inflate(1.0), entity -> !(entity instanceof Player));
			list.forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));
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

	public GameTestInfo prepareTestStructure() {
		BlockPos blockPos = this.getOrCalculateNorthwestCorner();
		this.structureBlockEntity = StructureUtils.prepareTestStructure(this, blockPos, this.getRotation(), this.level);
		this.structureBlockPos = this.structureBlockEntity.getBlockPos();
		StructureUtils.addCommandBlockAndButtonToStartTest(this.structureBlockPos, new BlockPos(1, 0, -1), this.getRotation(), this.level);
		StructureUtils.encaseStructure(this.getStructureBounds(), this.level, !this.testFunction.skyAccess());
		this.listeners.forEach(gameTestListener -> gameTestListener.testStructureLoaded(this));
		return this;
	}

	long getTick() {
		return this.tickCount;
	}

	GameTestSequence createSequence() {
		GameTestSequence gameTestSequence = new GameTestSequence(this);
		this.sequences.add(gameTestSequence);
		return gameTestSequence;
	}

	public boolean isRequired() {
		return this.testFunction.required();
	}

	public boolean isOptional() {
		return !this.testFunction.required();
	}

	public String getStructureName() {
		return this.testFunction.structureName();
	}

	public Rotation getRotation() {
		return this.rotation;
	}

	public TestFunction getTestFunction() {
		return this.testFunction;
	}

	public int getTimeoutTicks() {
		return this.timeoutTicks;
	}

	public boolean isFlaky() {
		return this.testFunction.isFlaky();
	}

	public int maxAttempts() {
		return this.testFunction.maxAttempts();
	}

	public int requiredSuccesses() {
		return this.testFunction.requiredSuccesses();
	}

	public RetryOptions retryOptions() {
		return this.retryOptions;
	}

	public Stream<GameTestListener> getListeners() {
		return this.listeners.stream();
	}

	public GameTestInfo copyReset() {
		GameTestInfo gameTestInfo = new GameTestInfo(this.testFunction, this.rotation, this.level, this.retryOptions());
		if (this.northWestCorner != null) {
			gameTestInfo.setNorthWestCorner(this.northWestCorner);
		}

		if (this.structureBlockPos != null) {
			gameTestInfo.setStructureBlockPos(this.structureBlockPos);
		}

		return gameTestInfo;
	}

	public BlockPos getOrCalculateNorthwestCorner() {
		if (this.northWestCorner == null) {
			BoundingBox boundingBox = StructureUtils.getStructureBoundingBox(this.getStructureBlockEntity());
			this.northWestCorner = new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
		}

		return this.northWestCorner;
	}

	public void setNorthWestCorner(BlockPos blockPos) {
		this.northWestCorner = blockPos;
	}
}
