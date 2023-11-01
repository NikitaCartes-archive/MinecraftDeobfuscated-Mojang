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
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
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
	private boolean rerunUntilFailed;
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
		this.startTick = this.level.getGameTime() + this.testFunction.getSetupTicks();
		this.timer.start();
	}

	public void tick() {
		if (!this.isDone()) {
			if (this.structureBlockEntity == null) {
				this.fail(new IllegalStateException("Running test without structure block entity"));
			}

			if (this.chunksLoaded
				|| StructureUtils.getStructureBoundingBox(this.structureBlockEntity)
					.intersectingChunks()
					.allMatch(chunkPos -> this.level.isPositionEntityTicking(chunkPos.getWorldPosition()))) {
				this.chunksLoaded = true;
				if (this.ticksToWaitForChunkLoading > 0) {
					this.ticksToWaitForChunkLoading--;
				} else {
					if (!this.placedStructure) {
						this.placedStructure = true;
						this.structureBlockEntity.placeStructure(this.level);
						BoundingBox boundingBox = StructureUtils.getStructureBoundingBox(this.structureBlockEntity);
						this.level.getBlockTicks().clearArea(boundingBox);
						this.level.clearBlockEvents(boundingBox);
						this.startExecution();
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
			}
		}
	}

	private void tickInternal() {
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

	public void setRunAtTickTime(long l, Runnable runnable) {
		this.runAtTickTimeMap.put(runnable, l);
	}

	public String getTestName() {
		return this.testFunction.getTestName();
	}

	public BlockPos getStructureBlockPos() {
		return this.structureBlockPos;
	}

	@Nullable
	public BlockPos getStructureOrigin() {
		StructureBlockEntity structureBlockEntity = this.getStructureBlockEntity();
		return structureBlockEntity == null ? null : StructureUtils.getStructureOrigin(structureBlockEntity);
	}

	@Nullable
	public Vec3i getStructureSize() {
		StructureBlockEntity structureBlockEntity = this.getStructureBlockEntity();
		return structureBlockEntity == null ? null : structureBlockEntity.getStructureSize();
	}

	@Nullable
	public AABB getStructureBounds() {
		StructureBlockEntity structureBlockEntity = this.getStructureBlockEntity();
		return structureBlockEntity == null ? null : StructureUtils.getStructureBounds(structureBlockEntity);
	}

	@Nullable
	private StructureBlockEntity getStructureBlockEntity() {
		return (StructureBlockEntity)this.level.getBlockEntity(this.structureBlockPos);
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

	public void prepareTestStructure(BlockPos blockPos) {
		this.structureBlockEntity = StructureUtils.prepareTestStructure(this.getStructureName(), blockPos, this.getRotation(), this.level);
		this.structureBlockPos = this.structureBlockEntity.getBlockPos();
		this.structureBlockEntity.setStructureName(this.getStructureName());
		StructureUtils.addCommandBlockAndButtonToStartTest(this.structureBlockPos, new BlockPos(1, 0, -1), this.getRotation(), this.level);
		this.listeners.forEach(gameTestListener -> gameTestListener.testStructureLoaded(this));
	}

	public void clearStructure() {
		if (this.structureBlockEntity == null) {
			throw new IllegalStateException("Expected structure to be initialized, but it was null");
		} else {
			BoundingBox boundingBox = StructureUtils.getStructureBoundingBox(this.structureBlockEntity);
			StructureUtils.clearSpaceForStructure(boundingBox, this.level);
		}
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

	public int getTimeoutTicks() {
		return this.timeoutTicks;
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

	public void setRerunUntilFailed(boolean bl) {
		this.rerunUntilFailed = bl;
	}

	public boolean rerunUntilFailed() {
		return this.rerunUntilFailed;
	}
}
