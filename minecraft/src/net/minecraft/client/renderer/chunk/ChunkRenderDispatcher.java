package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ChunkRenderDispatcher {
	private static final Logger LOGGER = LogManager.getLogger();
	private final PriorityBlockingQueue<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> chunksToBatch = Queues.newPriorityBlockingQueue();
	private final Queue<ChunkBufferBuilderPack> availableChunkBuffers;
	private final Queue<Runnable> pendingUploads = Queues.<Runnable>newConcurrentLinkedQueue();
	private final ChunkBufferBuilderPack fixedBuffers;
	private final ProcessorMailbox<Runnable> mailbox;
	private final Executor executor;
	private Level level;
	private final LevelRenderer renderer;
	private Vec3 camera = Vec3.ZERO;

	public ChunkRenderDispatcher(Level level, LevelRenderer levelRenderer, Executor executor, boolean bl) {
		this.level = level;
		this.renderer = levelRenderer;
		int i = Math.max(
			1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / (RenderType.chunkBufferLayers().stream().mapToInt(RenderType::bufferSize).sum() * 4) - 1
		);
		int j = Runtime.getRuntime().availableProcessors();
		int k = bl ? j : Math.min(j, 4);
		int l = Math.max(1, Math.min(k, i));
		this.fixedBuffers = new ChunkBufferBuilderPack();
		List<ChunkBufferBuilderPack> list = Lists.<ChunkBufferBuilderPack>newArrayListWithExpectedSize(l);

		try {
			for (int m = 0; m < l; m++) {
				list.add(new ChunkBufferBuilderPack());
			}
		} catch (OutOfMemoryError var13) {
			LOGGER.warn("Allocated only {}/{} buffers", list.size(), l);
			int n = Math.min(list.size() * 2 / 3, list.size() - 1);

			for (int o = 0; o < n; o++) {
				list.remove(list.size() - 1);
			}

			System.gc();
		}

		this.availableChunkBuffers = Queues.<ChunkBufferBuilderPack>newArrayDeque(list);
		this.executor = executor;
		this.mailbox = ProcessorMailbox.create(executor, "Chunk Renderer");
		this.mailbox.tell(this::runTask);
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	private void runTask() {
		if (!this.availableChunkBuffers.isEmpty()) {
			ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkCompileTask = (ChunkRenderDispatcher.RenderChunk.ChunkCompileTask)this.chunksToBatch.poll();
			if (chunkCompileTask != null) {
				ChunkBufferBuilderPack chunkBufferBuilderPack = (ChunkBufferBuilderPack)this.availableChunkBuffers.poll();
				CompletableFuture.runAsync(() -> {
				}, this.executor).thenCompose(void_ -> chunkCompileTask.doTask(chunkBufferBuilderPack)).whenComplete((unit, throwable) -> {
					this.mailbox.tell(() -> {
						chunkBufferBuilderPack.clearAll();
						this.availableChunkBuffers.add(chunkBufferBuilderPack);
					});
					this.mailbox.tell(this::runTask);
					if (throwable != null) {
						CrashReport crashReport = CrashReport.forThrowable(throwable, "Batching chunks");
						Minecraft.getInstance().delayCrash(Minecraft.getInstance().fillReport(crashReport));
					}
				});
			}
		}
	}

	public String getStats() {
		return String.format("pC: %03d, pU: %02d, aB: %02d", this.chunksToBatch.size(), this.pendingUploads.size(), this.availableChunkBuffers.size());
	}

	public void setCamera(Vec3 vec3) {
		this.camera = vec3;
	}

	public Vec3 getCameraPosition() {
		return this.camera;
	}

	public boolean uploadAllPendingUploads() {
		boolean bl;
		Runnable runnable;
		for (bl = false; (runnable = (Runnable)this.pendingUploads.poll()) != null; bl = true) {
			runnable.run();
		}

		return bl;
	}

	public void rebuildChunkSync(ChunkRenderDispatcher.RenderChunk renderChunk) {
		renderChunk.compileSync();
	}

	public void blockUntilClear() {
		this.clearBatchQueue();
	}

	public void schedule(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkCompileTask) {
		this.chunksToBatch.offer(chunkCompileTask);
		this.mailbox.tell(this::runTask);
	}

	public CompletableFuture<Void> uploadChunkLayer(BufferBuilder bufferBuilder, VertexBuffer vertexBuffer) {
		return Minecraft.getInstance().submit(() -> {
		}).thenCompose(void_ -> this.doUploadChunkLayer(bufferBuilder, vertexBuffer));
	}

	private CompletableFuture<Void> doUploadChunkLayer(BufferBuilder bufferBuilder, VertexBuffer vertexBuffer) {
		return vertexBuffer.uploadLater(bufferBuilder);
	}

	private void clearBatchQueue() {
		while (!this.chunksToBatch.isEmpty()) {
			ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkCompileTask = (ChunkRenderDispatcher.RenderChunk.ChunkCompileTask)this.chunksToBatch.poll();
			if (chunkCompileTask != null) {
				chunkCompileTask.cancel();
			}
		}
	}

	public boolean isQueueEmpty() {
		return this.chunksToBatch.isEmpty() && this.pendingUploads.isEmpty();
	}

	public void dispose() {
		this.clearBatchQueue();
		this.mailbox.close();
		this.availableChunkBuffers.clear();
	}

	@Environment(EnvType.CLIENT)
	public static class CompiledChunk {
		public static final ChunkRenderDispatcher.CompiledChunk UNCOMPILED = new ChunkRenderDispatcher.CompiledChunk() {
			@Override
			public boolean facesCanSeeEachother(Direction direction, Direction direction2) {
				return false;
			}
		};
		private final Set<RenderType> hasBlocks = Sets.<RenderType>newHashSet();
		private final Set<RenderType> hasLayer = Sets.<RenderType>newHashSet();
		private boolean isCompletelyEmpty = true;
		private final List<BlockEntity> renderableBlockEntities = Lists.<BlockEntity>newArrayList();
		private VisibilitySet visibilitySet = new VisibilitySet();
		@Nullable
		private BufferBuilder.State transparencyState;

		public boolean hasNoRenderableLayers() {
			return this.isCompletelyEmpty;
		}

		public boolean isEmpty(RenderType renderType) {
			return !this.hasBlocks.contains(renderType);
		}

		public List<BlockEntity> getRenderableBlockEntities() {
			return this.renderableBlockEntities;
		}

		public boolean facesCanSeeEachother(Direction direction, Direction direction2) {
			return this.visibilitySet.visibilityBetween(direction, direction2);
		}
	}

	@Environment(EnvType.CLIENT)
	public class RenderChunk {
		public final AtomicReference<ChunkRenderDispatcher.CompiledChunk> compiled = new AtomicReference(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
		@Nullable
		private ChunkRenderDispatcher.RenderChunk.RebuildTask lastRebuildTask;
		@Nullable
		private ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask lastResortTransparencyTask;
		private final Set<BlockEntity> globalBlockEntities = Sets.<BlockEntity>newHashSet();
		private final Map<RenderType, VertexBuffer> buffers = (Map<RenderType, VertexBuffer>)RenderType.chunkBufferLayers()
			.stream()
			.collect(Collectors.toMap(renderType -> renderType, renderType -> new VertexBuffer(DefaultVertexFormat.BLOCK)));
		public AABB bb;
		private int lastFrame = -1;
		private boolean dirty = true;
		private final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
		private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], mutableBlockPoss -> {
			for (int i = 0; i < mutableBlockPoss.length; i++) {
				mutableBlockPoss[i] = new BlockPos.MutableBlockPos();
			}
		});
		private boolean playerChanged;

		private boolean doesChunkExistAt(BlockPos blockPos) {
			return !ChunkRenderDispatcher.this.level.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4).isEmpty();
		}

		public boolean hasAllNeighbors() {
			int i = 24;
			return !(this.getDistToPlayerSqr() > 576.0)
				? true
				: this.doesChunkExistAt(this.relativeOrigins[Direction.WEST.ordinal()])
					&& this.doesChunkExistAt(this.relativeOrigins[Direction.NORTH.ordinal()])
					&& this.doesChunkExistAt(this.relativeOrigins[Direction.EAST.ordinal()])
					&& this.doesChunkExistAt(this.relativeOrigins[Direction.SOUTH.ordinal()]);
		}

		public boolean setFrame(int i) {
			if (this.lastFrame == i) {
				return false;
			} else {
				this.lastFrame = i;
				return true;
			}
		}

		public VertexBuffer getBuffer(RenderType renderType) {
			return (VertexBuffer)this.buffers.get(renderType);
		}

		public void setOrigin(int i, int j, int k) {
			if (i != this.origin.getX() || j != this.origin.getY() || k != this.origin.getZ()) {
				this.reset();
				this.origin.set(i, j, k);
				this.bb = new AABB((double)i, (double)j, (double)k, (double)(i + 16), (double)(j + 16), (double)(k + 16));

				for (Direction direction : Direction.values()) {
					this.relativeOrigins[direction.ordinal()].set(this.origin).move(direction, 16);
				}
			}
		}

		protected double getDistToPlayerSqr() {
			Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
			double d = this.bb.minX + 8.0 - camera.getPosition().x;
			double e = this.bb.minY + 8.0 - camera.getPosition().y;
			double f = this.bb.minZ + 8.0 - camera.getPosition().z;
			return d * d + e * e + f * f;
		}

		private void beginLayer(BufferBuilder bufferBuilder, BlockPos blockPos) {
			bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
			bufferBuilder.offset((double)(-blockPos.getX()), (double)(-blockPos.getY()), (double)(-blockPos.getZ()));
		}

		public ChunkRenderDispatcher.CompiledChunk getCompiledChunk() {
			return (ChunkRenderDispatcher.CompiledChunk)this.compiled.get();
		}

		private void reset() {
			this.cancelTasks();
			this.compiled.set(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
			this.dirty = true;
		}

		public void releaseBuffers() {
			this.reset();
			this.buffers.values().forEach(VertexBuffer::delete);
		}

		public BlockPos getOrigin() {
			return this.origin;
		}

		public void setDirty(boolean bl) {
			boolean bl2 = this.dirty;
			this.dirty = true;
			this.playerChanged = bl | (bl2 && this.playerChanged);
		}

		public void setNotDirty() {
			this.dirty = false;
			this.playerChanged = false;
		}

		public boolean isDirty() {
			return this.dirty;
		}

		public boolean isDirtyFromPlayer() {
			return this.dirty && this.playerChanged;
		}

		public BlockPos getRelativeOrigin(Direction direction) {
			return this.relativeOrigins[direction.ordinal()];
		}

		public boolean resortTransparency(RenderType renderType, ChunkRenderDispatcher chunkRenderDispatcher) {
			ChunkRenderDispatcher.CompiledChunk compiledChunk = this.getCompiledChunk();
			if (this.lastResortTransparencyTask != null) {
				this.lastResortTransparencyTask.cancel();
			}

			if (!compiledChunk.hasLayer.contains(renderType)) {
				return false;
			} else {
				this.lastResortTransparencyTask = new ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask(this.getDistToPlayerSqr(), compiledChunk);
				chunkRenderDispatcher.schedule(this.lastResortTransparencyTask);
				return true;
			}
		}

		protected void cancelTasks() {
			if (this.lastRebuildTask != null) {
				this.lastRebuildTask.cancel();
				this.lastRebuildTask = null;
			}

			if (this.lastResortTransparencyTask != null) {
				this.lastResortTransparencyTask.cancel();
				this.lastResortTransparencyTask = null;
			}
		}

		public ChunkRenderDispatcher.RenderChunk.ChunkCompileTask createCompileTask() {
			this.cancelTasks();
			BlockPos blockPos = this.origin.immutable();
			int i = 1;
			RenderChunkRegion renderChunkRegion = RenderChunkRegion.createIfNotEmpty(
				ChunkRenderDispatcher.this.level, blockPos.offset(-1, -1, -1), blockPos.offset(16, 16, 16), 1
			);
			this.lastRebuildTask = new ChunkRenderDispatcher.RenderChunk.RebuildTask(this.getDistToPlayerSqr(), renderChunkRegion);
			return this.lastRebuildTask;
		}

		public void rebuildChunkAsync(ChunkRenderDispatcher chunkRenderDispatcher) {
			ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkCompileTask = this.createCompileTask();
			chunkRenderDispatcher.schedule(chunkCompileTask);
		}

		private void updateGlobalBlockEntities(Set<BlockEntity> set) {
			Set<BlockEntity> set2 = Sets.<BlockEntity>newHashSet(set);
			Set<BlockEntity> set3 = Sets.<BlockEntity>newHashSet(this.globalBlockEntities);
			set2.removeAll(this.globalBlockEntities);
			set3.removeAll(set);
			this.globalBlockEntities.clear();
			this.globalBlockEntities.addAll(set);
			ChunkRenderDispatcher.this.renderer.updateGlobalBlockEntities(set3, set2);
		}

		public void compileSync() {
			ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkCompileTask = this.createCompileTask();
			chunkCompileTask.doTask(ChunkRenderDispatcher.this.fixedBuffers);
		}

		@Environment(EnvType.CLIENT)
		abstract class ChunkCompileTask implements Comparable<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> {
			protected final double distAtCreation;
			protected final AtomicBoolean isCancelled = new AtomicBoolean(false);

			public ChunkCompileTask(double d) {
				this.distAtCreation = d;
			}

			public abstract CompletableFuture<Unit> doTask(ChunkBufferBuilderPack chunkBufferBuilderPack);

			public abstract void cancel();

			public int compareTo(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkCompileTask) {
				return Doubles.compare(this.distAtCreation, chunkCompileTask.distAtCreation);
			}
		}

		@Environment(EnvType.CLIENT)
		class RebuildTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask {
			@Nullable
			protected RenderChunkRegion region;

			public RebuildTask(double d, @Nullable RenderChunkRegion renderChunkRegion) {
				super(d);
				this.region = renderChunkRegion;
			}

			@Override
			public CompletableFuture<Unit> doTask(ChunkBufferBuilderPack chunkBufferBuilderPack) {
				if (this.isCancelled.get()) {
					return CompletableFuture.completedFuture(Unit.INSTANCE);
				} else if (!RenderChunk.this.hasAllNeighbors()) {
					this.region = null;
					RenderChunk.this.setDirty(false);
					this.isCancelled.set(true);
					return CompletableFuture.completedFuture(Unit.INSTANCE);
				} else if (this.isCancelled.get()) {
					return CompletableFuture.completedFuture(Unit.INSTANCE);
				} else {
					Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
					float f = (float)vec3.x;
					float g = (float)vec3.y;
					float h = (float)vec3.z;
					ChunkRenderDispatcher.CompiledChunk compiledChunk = new ChunkRenderDispatcher.CompiledChunk();
					Set<BlockEntity> set = this.compile(f, g, h, compiledChunk, chunkBufferBuilderPack);
					RenderChunk.this.updateGlobalBlockEntities(set);
					if (this.isCancelled.get()) {
						return CompletableFuture.completedFuture(Unit.INSTANCE);
					} else {
						List<CompletableFuture<Void>> list = Lists.<CompletableFuture<Void>>newArrayList();
						compiledChunk.hasLayer
							.forEach(
								renderType -> list.add(ChunkRenderDispatcher.this.uploadChunkLayer(chunkBufferBuilderPack.builder(renderType), RenderChunk.this.getBuffer(renderType)))
							);
						CompletableFuture<Unit> completableFuture = Util.sequence(list).thenApply(listx -> Unit.INSTANCE);
						return completableFuture.whenComplete((unit, throwable) -> {
							if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
								Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering chunk"));
							}

							if (!this.isCancelled.get()) {
								RenderChunk.this.compiled.set(compiledChunk);
							}
						});
					}
				}
			}

			private Set<BlockEntity> compile(float f, float g, float h, ChunkRenderDispatcher.CompiledChunk compiledChunk, ChunkBufferBuilderPack chunkBufferBuilderPack) {
				int i = 1;
				BlockPos blockPos = RenderChunk.this.origin.immutable();
				BlockPos blockPos2 = blockPos.offset(15, 15, 15);
				VisGraph visGraph = new VisGraph();
				Set<BlockEntity> set = Sets.<BlockEntity>newHashSet();
				RenderChunkRegion renderChunkRegion = this.region;
				this.region = null;
				if (renderChunkRegion != null) {
					ModelBlockRenderer.enableCaching();
					Random random = new Random();
					BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();

					for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
						BlockState blockState = renderChunkRegion.getBlockState(blockPos3);
						Block block = blockState.getBlock();
						if (blockState.isSolidRender(renderChunkRegion, blockPos3)) {
							visGraph.setOpaque(blockPos3);
						}

						if (block.isEntityBlock()) {
							BlockEntity blockEntity = renderChunkRegion.getBlockEntity(blockPos3, LevelChunk.EntityCreationType.CHECK);
							if (blockEntity != null) {
								BlockEntityRenderer<BlockEntity> blockEntityRenderer = BlockEntityRenderDispatcher.instance.getRenderer(blockEntity);
								if (blockEntityRenderer != null) {
									compiledChunk.renderableBlockEntities.add(blockEntity);
									if (blockEntityRenderer.shouldRenderOffScreen(blockEntity)) {
										set.add(blockEntity);
									}
								}
							}
						}

						FluidState fluidState = renderChunkRegion.getFluidState(blockPos3);
						if (!fluidState.isEmpty()) {
							RenderType renderType = RenderType.getRenderLayer(fluidState);
							BufferBuilder bufferBuilder = chunkBufferBuilderPack.builder(renderType);
							if (compiledChunk.hasLayer.add(renderType)) {
								RenderChunk.this.beginLayer(bufferBuilder, blockPos);
							}

							if (blockRenderDispatcher.renderLiquid(blockPos3, renderChunkRegion, bufferBuilder, fluidState)) {
								compiledChunk.isCompletelyEmpty = false;
								compiledChunk.hasBlocks.add(renderType);
							}
						}

						if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
							RenderType renderTypex = RenderType.getRenderLayer(blockState);
							BufferBuilder bufferBuilderx = chunkBufferBuilderPack.builder(renderTypex);
							if (compiledChunk.hasLayer.add(renderTypex)) {
								RenderChunk.this.beginLayer(bufferBuilderx, blockPos);
							}

							if (blockRenderDispatcher.renderBatched(blockState, blockPos3, renderChunkRegion, bufferBuilderx, random)) {
								compiledChunk.isCompletelyEmpty = false;
								compiledChunk.hasBlocks.add(renderTypex);
							}
						}
					}

					if (compiledChunk.hasBlocks.contains(RenderType.TRANSLUCENT)) {
						BufferBuilder bufferBuilder2 = chunkBufferBuilderPack.builder(RenderType.TRANSLUCENT);
						bufferBuilder2.sortQuads(f, g, h);
						compiledChunk.transparencyState = bufferBuilder2.getState();
					}

					compiledChunk.hasLayer.stream().map(chunkBufferBuilderPack::builder).forEach(BufferBuilder::end);
					ModelBlockRenderer.clearCache();
				}

				compiledChunk.visibilitySet = visGraph.resolve();
				return set;
			}

			@Override
			public void cancel() {
				this.region = null;
				if (this.isCancelled.compareAndSet(false, true)) {
					RenderChunk.this.setDirty(false);
				}
			}
		}

		@Environment(EnvType.CLIENT)
		class ResortTransparencyTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask {
			private final ChunkRenderDispatcher.CompiledChunk compiledChunk;

			public ResortTransparencyTask(double d, ChunkRenderDispatcher.CompiledChunk compiledChunk) {
				super(d);
				this.compiledChunk = compiledChunk;
			}

			@Override
			public CompletableFuture<Unit> doTask(ChunkBufferBuilderPack chunkBufferBuilderPack) {
				if (this.isCancelled.get()) {
					return CompletableFuture.completedFuture(Unit.INSTANCE);
				} else if (!RenderChunk.this.hasAllNeighbors()) {
					this.isCancelled.set(true);
					return CompletableFuture.completedFuture(Unit.INSTANCE);
				} else if (this.isCancelled.get()) {
					return CompletableFuture.completedFuture(Unit.INSTANCE);
				} else {
					Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
					float f = (float)vec3.x;
					float g = (float)vec3.y;
					float h = (float)vec3.z;
					BufferBuilder.State state = this.compiledChunk.transparencyState;
					if (state != null && this.compiledChunk.hasBlocks.contains(RenderType.TRANSLUCENT)) {
						BufferBuilder bufferBuilder = chunkBufferBuilderPack.builder(RenderType.TRANSLUCENT);
						RenderChunk.this.beginLayer(bufferBuilder, RenderChunk.this.origin);
						bufferBuilder.restoreState(state);
						bufferBuilder.sortQuads(f, g, h);
						this.compiledChunk.transparencyState = bufferBuilder.getState();
						bufferBuilder.end();
						if (this.isCancelled.get()) {
							return CompletableFuture.completedFuture(Unit.INSTANCE);
						} else {
							CompletableFuture<Unit> completableFuture = ChunkRenderDispatcher.this.uploadChunkLayer(
									chunkBufferBuilderPack.builder(RenderType.TRANSLUCENT), RenderChunk.this.getBuffer(RenderType.TRANSLUCENT)
								)
								.thenApply(void_ -> Unit.INSTANCE);
							return completableFuture.whenComplete((unit, throwable) -> {
								if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
									Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering chunk"));
								}
							});
						}
					} else {
						return CompletableFuture.completedFuture(Unit.INSTANCE);
					}
				}
			}

			@Override
			public void cancel() {
				this.isCancelled.set(true);
			}
		}
	}
}
