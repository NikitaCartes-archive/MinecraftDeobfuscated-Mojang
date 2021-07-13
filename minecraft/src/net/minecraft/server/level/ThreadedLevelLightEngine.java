package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadedLevelLightEngine extends LevelLightEngine implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ProcessorMailbox<Runnable> taskMailbox;
	private final ObjectList<Pair<ThreadedLevelLightEngine.TaskType, Runnable>> lightTasks = new ObjectArrayList<>();
	private final ChunkMap chunkMap;
	private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> sorterMailbox;
	private volatile int taskPerBatch = 5;
	private final AtomicBoolean scheduled = new AtomicBoolean();

	public ThreadedLevelLightEngine(
		LightChunkGetter lightChunkGetter,
		ChunkMap chunkMap,
		boolean bl,
		ProcessorMailbox<Runnable> processorMailbox,
		ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> processorHandle
	) {
		super(lightChunkGetter, true, bl);
		this.chunkMap = chunkMap;
		this.sorterMailbox = processorHandle;
		this.taskMailbox = processorMailbox;
	}

	public void close() {
	}

	@Override
	public int runUpdates(int i, boolean bl, boolean bl2) {
		throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Ran authomatically on a different thread!"));
	}

	@Override
	public void onBlockEmissionIncrease(BlockPos blockPos, int i) {
		throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Ran authomatically on a different thread!"));
	}

	@Override
	public void checkBlock(BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.immutable();
		this.addTask(
			SectionPos.blockToSectionCoord(blockPos.getX()),
			SectionPos.blockToSectionCoord(blockPos.getZ()),
			ThreadedLevelLightEngine.TaskType.POST_UPDATE,
			Util.name(() -> super.checkBlock(blockPos2), () -> "checkBlock " + blockPos2)
		);
	}

	protected void updateChunkStatus(ChunkPos chunkPos) {
		this.addTask(chunkPos.x, chunkPos.z, () -> 0, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
			super.retainData(chunkPos, false);
			super.enableLightSources(chunkPos, false);

			for (int i = this.getMinLightSection(); i < this.getMaxLightSection(); i++) {
				super.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkPos, i), null, true);
				super.queueSectionData(LightLayer.SKY, SectionPos.of(chunkPos, i), null, true);
			}

			for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); i++) {
				super.updateSectionStatus(SectionPos.of(chunkPos, i), true);
			}
		}, () -> "updateChunkStatus " + chunkPos + " true"));
	}

	@Override
	public void updateSectionStatus(SectionPos sectionPos, boolean bl) {
		this.addTask(
			sectionPos.x(),
			sectionPos.z(),
			() -> 0,
			ThreadedLevelLightEngine.TaskType.PRE_UPDATE,
			Util.name(() -> super.updateSectionStatus(sectionPos, bl), () -> "updateSectionStatus " + sectionPos + " " + bl)
		);
	}

	@Override
	public void enableLightSources(ChunkPos chunkPos, boolean bl) {
		this.addTask(
			chunkPos.x,
			chunkPos.z,
			ThreadedLevelLightEngine.TaskType.PRE_UPDATE,
			Util.name(() -> super.enableLightSources(chunkPos, bl), () -> "enableLight " + chunkPos + " " + bl)
		);
	}

	@Override
	public void queueSectionData(LightLayer lightLayer, SectionPos sectionPos, @Nullable DataLayer dataLayer, boolean bl) {
		this.addTask(
			sectionPos.x(),
			sectionPos.z(),
			() -> 0,
			ThreadedLevelLightEngine.TaskType.PRE_UPDATE,
			Util.name(() -> super.queueSectionData(lightLayer, sectionPos, dataLayer, bl), () -> "queueData " + sectionPos)
		);
	}

	private void addTask(int i, int j, ThreadedLevelLightEngine.TaskType taskType, Runnable runnable) {
		this.addTask(i, j, this.chunkMap.getChunkQueueLevel(ChunkPos.asLong(i, j)), taskType, runnable);
	}

	private void addTask(int i, int j, IntSupplier intSupplier, ThreadedLevelLightEngine.TaskType taskType, Runnable runnable) {
		this.sorterMailbox.tell(ChunkTaskPriorityQueueSorter.message((Runnable)(() -> {
			this.lightTasks.add(Pair.of(taskType, runnable));
			if (this.lightTasks.size() >= this.taskPerBatch) {
				this.runUpdate();
			}
		}), ChunkPos.asLong(i, j), intSupplier));
	}

	@Override
	public void retainData(ChunkPos chunkPos, boolean bl) {
		this.addTask(
			chunkPos.x,
			chunkPos.z,
			() -> 0,
			ThreadedLevelLightEngine.TaskType.PRE_UPDATE,
			Util.name(() -> super.retainData(chunkPos, bl), () -> "retainData " + chunkPos)
		);
	}

	public CompletableFuture<ChunkAccess> lightChunk(ChunkAccess chunkAccess, boolean bl) {
		ChunkPos chunkPos = chunkAccess.getPos();
		chunkAccess.setLightCorrect(false);
		this.addTask(chunkPos.x, chunkPos.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
			LevelChunkSection[] levelChunkSections = chunkAccess.getSections();

			for (int i = 0; i < chunkAccess.getSectionsCount(); i++) {
				LevelChunkSection levelChunkSection = levelChunkSections[i];
				if (!LevelChunkSection.isEmpty(levelChunkSection)) {
					int j = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
					super.updateSectionStatus(SectionPos.of(chunkPos, j), false);
				}
			}

			super.enableLightSources(chunkPos, true);
			if (!bl) {
				chunkAccess.getLights().forEach(blockPos -> super.onBlockEmissionIncrease(blockPos, chunkAccess.getLightEmission(blockPos)));
			}

			this.chunkMap.releaseLightTicket(chunkPos);
		}, () -> "lightChunk " + chunkPos + " " + bl));
		return CompletableFuture.supplyAsync(() -> {
			chunkAccess.setLightCorrect(true);
			super.retainData(chunkPos, false);
			return chunkAccess;
		}, runnable -> this.addTask(chunkPos.x, chunkPos.z, ThreadedLevelLightEngine.TaskType.POST_UPDATE, runnable));
	}

	public void tryScheduleUpdate() {
		if ((!this.lightTasks.isEmpty() || super.hasLightWork()) && this.scheduled.compareAndSet(false, true)) {
			this.taskMailbox.tell(() -> {
				this.runUpdate();
				this.scheduled.set(false);
			});
		}
	}

	private void runUpdate() {
		int i = Math.min(this.lightTasks.size(), this.taskPerBatch);
		ObjectListIterator<Pair<ThreadedLevelLightEngine.TaskType, Runnable>> objectListIterator = this.lightTasks.iterator();

		int j;
		for (j = 0; objectListIterator.hasNext() && j < i; j++) {
			Pair<ThreadedLevelLightEngine.TaskType, Runnable> pair = (Pair<ThreadedLevelLightEngine.TaskType, Runnable>)objectListIterator.next();
			if (pair.getFirst() == ThreadedLevelLightEngine.TaskType.PRE_UPDATE) {
				pair.getSecond().run();
			}
		}

		objectListIterator.back(j);
		super.runUpdates(Integer.MAX_VALUE, true, true);

		for (int var5 = 0; objectListIterator.hasNext() && var5 < i; var5++) {
			Pair<ThreadedLevelLightEngine.TaskType, Runnable> pair = (Pair<ThreadedLevelLightEngine.TaskType, Runnable>)objectListIterator.next();
			if (pair.getFirst() == ThreadedLevelLightEngine.TaskType.POST_UPDATE) {
				pair.getSecond().run();
			}

			objectListIterator.remove();
		}
	}

	public void setTaskPerBatch(int i) {
		this.taskPerBatch = i;
	}

	static enum TaskType {
		PRE_UPDATE,
		POST_UPDATE;
	}
}
