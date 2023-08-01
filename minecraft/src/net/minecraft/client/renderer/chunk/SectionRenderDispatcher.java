package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SectionRenderDispatcher {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_WORKERS_32_BIT = 4;
	private static final int MAX_HIGH_PRIORITY_QUOTA = 2;
	private final PriorityBlockingQueue<SectionRenderDispatcher.RenderSection.CompileTask> toBatchHighPriority = Queues.newPriorityBlockingQueue();
	private final Queue<SectionRenderDispatcher.RenderSection.CompileTask> toBatchLowPriority = Queues.<SectionRenderDispatcher.RenderSection.CompileTask>newLinkedBlockingDeque();
	private int highPriorityQuota = 2;
	private final Queue<SectionBufferBuilderPack> freeBuffers;
	private final Queue<Runnable> toUpload = Queues.<Runnable>newConcurrentLinkedQueue();
	private volatile int toBatchCount;
	private volatile int freeBufferCount;
	final SectionBufferBuilderPack fixedBuffers;
	private final ProcessorMailbox<Runnable> mailbox;
	private final Executor executor;
	ClientLevel level;
	final LevelRenderer renderer;
	private Vec3 camera = Vec3.ZERO;

	public SectionRenderDispatcher(
		ClientLevel clientLevel, LevelRenderer levelRenderer, Executor executor, boolean bl, SectionBufferBuilderPack sectionBufferBuilderPack
	) {
		this.level = clientLevel;
		this.renderer = levelRenderer;
		int i = Math.max(
			1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / (RenderType.chunkBufferLayers().stream().mapToInt(RenderType::bufferSize).sum() * 4) - 1
		);
		int j = Runtime.getRuntime().availableProcessors();
		int k = bl ? j : Math.min(j, 4);
		int l = Math.max(1, Math.min(k, i));
		this.fixedBuffers = sectionBufferBuilderPack;
		List<SectionBufferBuilderPack> list = Lists.<SectionBufferBuilderPack>newArrayListWithExpectedSize(l);

		try {
			for (int m = 0; m < l; m++) {
				list.add(new SectionBufferBuilderPack());
			}
		} catch (OutOfMemoryError var14) {
			LOGGER.warn("Allocated only {}/{} buffers", list.size(), l);
			int n = Math.min(list.size() * 2 / 3, list.size() - 1);

			for (int o = 0; o < n; o++) {
				list.remove(list.size() - 1);
			}

			System.gc();
		}

		this.freeBuffers = Queues.<SectionBufferBuilderPack>newArrayDeque(list);
		this.freeBufferCount = this.freeBuffers.size();
		this.executor = executor;
		this.mailbox = ProcessorMailbox.create(executor, "Section Renderer");
		this.mailbox.tell(this::runTask);
	}

	public void setLevel(ClientLevel clientLevel) {
		this.level = clientLevel;
	}

	private void runTask() {
		if (!this.freeBuffers.isEmpty()) {
			SectionRenderDispatcher.RenderSection.CompileTask compileTask = this.pollTask();
			if (compileTask != null) {
				SectionBufferBuilderPack sectionBufferBuilderPack = (SectionBufferBuilderPack)this.freeBuffers.poll();
				this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
				this.freeBufferCount = this.freeBuffers.size();
				CompletableFuture.supplyAsync(
						Util.wrapThreadWithTaskName(compileTask.name(), (Supplier)(() -> compileTask.doTask(sectionBufferBuilderPack))), this.executor
					)
					.thenCompose(completableFuture -> completableFuture)
					.whenComplete((sectionTaskResult, throwable) -> {
						if (throwable != null) {
							Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Batching sections"));
						} else {
							this.mailbox.tell(() -> {
								if (sectionTaskResult == SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL) {
									sectionBufferBuilderPack.clearAll();
								} else {
									sectionBufferBuilderPack.discardAll();
								}

								this.freeBuffers.add(sectionBufferBuilderPack);
								this.freeBufferCount = this.freeBuffers.size();
								this.runTask();
							});
						}
					});
			}
		}
	}

	@Nullable
	private SectionRenderDispatcher.RenderSection.CompileTask pollTask() {
		if (this.highPriorityQuota <= 0) {
			SectionRenderDispatcher.RenderSection.CompileTask compileTask = (SectionRenderDispatcher.RenderSection.CompileTask)this.toBatchLowPriority.poll();
			if (compileTask != null) {
				this.highPriorityQuota = 2;
				return compileTask;
			}
		}

		SectionRenderDispatcher.RenderSection.CompileTask compileTask = (SectionRenderDispatcher.RenderSection.CompileTask)this.toBatchHighPriority.poll();
		if (compileTask != null) {
			this.highPriorityQuota--;
			return compileTask;
		} else {
			this.highPriorityQuota = 2;
			return (SectionRenderDispatcher.RenderSection.CompileTask)this.toBatchLowPriority.poll();
		}
	}

	public String getStats() {
		return String.format(Locale.ROOT, "pC: %03d, pU: %02d, aB: %02d", this.toBatchCount, this.toUpload.size(), this.freeBufferCount);
	}

	public int getToBatchCount() {
		return this.toBatchCount;
	}

	public int getToUpload() {
		return this.toUpload.size();
	}

	public int getFreeBufferCount() {
		return this.freeBufferCount;
	}

	public void setCamera(Vec3 vec3) {
		this.camera = vec3;
	}

	public Vec3 getCameraPosition() {
		return this.camera;
	}

	public void uploadAllPendingUploads() {
		Runnable runnable;
		while ((runnable = (Runnable)this.toUpload.poll()) != null) {
			runnable.run();
		}
	}

	public void rebuildSectionSync(SectionRenderDispatcher.RenderSection renderSection, RenderRegionCache renderRegionCache) {
		renderSection.compileSync(renderRegionCache);
	}

	public void blockUntilClear() {
		this.clearBatchQueue();
	}

	public void schedule(SectionRenderDispatcher.RenderSection.CompileTask compileTask) {
		this.mailbox.tell(() -> {
			if (compileTask.isHighPriority) {
				this.toBatchHighPriority.offer(compileTask);
			} else {
				this.toBatchLowPriority.offer(compileTask);
			}

			this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
			this.runTask();
		});
	}

	public CompletableFuture<Void> uploadSectionLayer(BufferBuilder.RenderedBuffer renderedBuffer, VertexBuffer vertexBuffer) {
		return CompletableFuture.runAsync(() -> {
			if (!vertexBuffer.isInvalid()) {
				vertexBuffer.bind();
				vertexBuffer.upload(renderedBuffer);
				VertexBuffer.unbind();
			}
		}, this.toUpload::add);
	}

	private void clearBatchQueue() {
		while (!this.toBatchHighPriority.isEmpty()) {
			SectionRenderDispatcher.RenderSection.CompileTask compileTask = (SectionRenderDispatcher.RenderSection.CompileTask)this.toBatchHighPriority.poll();
			if (compileTask != null) {
				compileTask.cancel();
			}
		}

		while (!this.toBatchLowPriority.isEmpty()) {
			SectionRenderDispatcher.RenderSection.CompileTask compileTask = (SectionRenderDispatcher.RenderSection.CompileTask)this.toBatchLowPriority.poll();
			if (compileTask != null) {
				compileTask.cancel();
			}
		}

		this.toBatchCount = 0;
	}

	public boolean isQueueEmpty() {
		return this.toBatchCount == 0 && this.toUpload.isEmpty();
	}

	public void dispose() {
		this.clearBatchQueue();
		this.mailbox.close();
		this.freeBuffers.clear();
	}

	@Environment(EnvType.CLIENT)
	public static class CompiledSection {
		public static final SectionRenderDispatcher.CompiledSection UNCOMPILED = new SectionRenderDispatcher.CompiledSection() {
			@Override
			public boolean facesCanSeeEachother(Direction direction, Direction direction2) {
				return false;
			}
		};
		final Set<RenderType> hasBlocks = new ObjectArraySet<>(RenderType.chunkBufferLayers().size());
		final List<BlockEntity> renderableBlockEntities = Lists.<BlockEntity>newArrayList();
		VisibilitySet visibilitySet = new VisibilitySet();
		@Nullable
		BufferBuilder.SortState transparencyState;

		public boolean hasNoRenderableLayers() {
			return this.hasBlocks.isEmpty();
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
	public class RenderSection {
		public static final int SIZE = 16;
		public final int index;
		public final AtomicReference<SectionRenderDispatcher.CompiledSection> compiled = new AtomicReference(SectionRenderDispatcher.CompiledSection.UNCOMPILED);
		final AtomicInteger initialCompilationCancelCount = new AtomicInteger(0);
		@Nullable
		private SectionRenderDispatcher.RenderSection.RebuildTask lastRebuildTask;
		@Nullable
		private SectionRenderDispatcher.RenderSection.ResortTransparencyTask lastResortTransparencyTask;
		private final Set<BlockEntity> globalBlockEntities = Sets.<BlockEntity>newHashSet();
		private final Map<RenderType, VertexBuffer> buffers = (Map<RenderType, VertexBuffer>)RenderType.chunkBufferLayers()
			.stream()
			.collect(Collectors.toMap(renderType -> renderType, renderType -> new VertexBuffer(VertexBuffer.Usage.STATIC)));
		private AABB bb;
		private boolean dirty = true;
		final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
		private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], mutableBlockPoss -> {
			for (int ix = 0; ix < mutableBlockPoss.length; ix++) {
				mutableBlockPoss[ix] = new BlockPos.MutableBlockPos();
			}
		});
		private boolean playerChanged;

		public RenderSection(int i, int j, int k, int l) {
			this.index = i;
			this.setOrigin(j, k, l);
		}

		private boolean doesChunkExistAt(BlockPos blockPos) {
			return SectionRenderDispatcher.this.level
					.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()), ChunkStatus.FULL, false)
				!= null;
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

		public AABB getBoundingBox() {
			return this.bb;
		}

		public VertexBuffer getBuffer(RenderType renderType) {
			return (VertexBuffer)this.buffers.get(renderType);
		}

		public void setOrigin(int i, int j, int k) {
			this.reset();
			this.origin.set(i, j, k);
			this.bb = new AABB((double)i, (double)j, (double)k, (double)(i + 16), (double)(j + 16), (double)(k + 16));

			for (Direction direction : Direction.values()) {
				this.relativeOrigins[direction.ordinal()].set(this.origin).move(direction, 16);
			}
		}

		protected double getDistToPlayerSqr() {
			Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
			double d = this.bb.minX + 8.0 - camera.getPosition().x;
			double e = this.bb.minY + 8.0 - camera.getPosition().y;
			double f = this.bb.minZ + 8.0 - camera.getPosition().z;
			return d * d + e * e + f * f;
		}

		void beginLayer(BufferBuilder bufferBuilder) {
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		}

		public SectionRenderDispatcher.CompiledSection getCompiled() {
			return (SectionRenderDispatcher.CompiledSection)this.compiled.get();
		}

		private void reset() {
			this.cancelTasks();
			this.compiled.set(SectionRenderDispatcher.CompiledSection.UNCOMPILED);
			this.dirty = true;
		}

		public void releaseBuffers() {
			this.reset();
			this.buffers.values().forEach(VertexBuffer::close);
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

		public boolean resortTransparency(RenderType renderType, SectionRenderDispatcher sectionRenderDispatcher) {
			SectionRenderDispatcher.CompiledSection compiledSection = this.getCompiled();
			if (this.lastResortTransparencyTask != null) {
				this.lastResortTransparencyTask.cancel();
			}

			if (!compiledSection.hasBlocks.contains(renderType)) {
				return false;
			} else {
				this.lastResortTransparencyTask = new SectionRenderDispatcher.RenderSection.ResortTransparencyTask(this.getDistToPlayerSqr(), compiledSection);
				sectionRenderDispatcher.schedule(this.lastResortTransparencyTask);
				return true;
			}
		}

		protected boolean cancelTasks() {
			boolean bl = false;
			if (this.lastRebuildTask != null) {
				this.lastRebuildTask.cancel();
				this.lastRebuildTask = null;
				bl = true;
			}

			if (this.lastResortTransparencyTask != null) {
				this.lastResortTransparencyTask.cancel();
				this.lastResortTransparencyTask = null;
			}

			return bl;
		}

		public SectionRenderDispatcher.RenderSection.CompileTask createCompileTask(RenderRegionCache renderRegionCache) {
			boolean bl = this.cancelTasks();
			BlockPos blockPos = this.origin.immutable();
			int i = 1;
			RenderChunkRegion renderChunkRegion = renderRegionCache.createRegion(
				SectionRenderDispatcher.this.level, blockPos.offset(-1, -1, -1), blockPos.offset(16, 16, 16), 1
			);
			boolean bl2 = this.compiled.get() == SectionRenderDispatcher.CompiledSection.UNCOMPILED;
			if (bl2 && bl) {
				this.initialCompilationCancelCount.incrementAndGet();
			}

			this.lastRebuildTask = new SectionRenderDispatcher.RenderSection.RebuildTask(
				this.getDistToPlayerSqr(), renderChunkRegion, !bl2 || this.initialCompilationCancelCount.get() > 2
			);
			return this.lastRebuildTask;
		}

		public void rebuildSectionAsync(SectionRenderDispatcher sectionRenderDispatcher, RenderRegionCache renderRegionCache) {
			SectionRenderDispatcher.RenderSection.CompileTask compileTask = this.createCompileTask(renderRegionCache);
			sectionRenderDispatcher.schedule(compileTask);
		}

		void updateGlobalBlockEntities(Collection<BlockEntity> collection) {
			Set<BlockEntity> set = Sets.<BlockEntity>newHashSet(collection);
			Set<BlockEntity> set2;
			synchronized (this.globalBlockEntities) {
				set2 = Sets.<BlockEntity>newHashSet(this.globalBlockEntities);
				set.removeAll(this.globalBlockEntities);
				set2.removeAll(collection);
				this.globalBlockEntities.clear();
				this.globalBlockEntities.addAll(collection);
			}

			SectionRenderDispatcher.this.renderer.updateGlobalBlockEntities(set2, set);
		}

		public void compileSync(RenderRegionCache renderRegionCache) {
			SectionRenderDispatcher.RenderSection.CompileTask compileTask = this.createCompileTask(renderRegionCache);
			compileTask.doTask(SectionRenderDispatcher.this.fixedBuffers);
		}

		public boolean isAxisAlignedWith(int i, int j, int k) {
			BlockPos blockPos = this.getOrigin();
			return i == SectionPos.blockToSectionCoord(blockPos.getX())
				|| k == SectionPos.blockToSectionCoord(blockPos.getZ())
				|| j == SectionPos.blockToSectionCoord(blockPos.getY());
		}

		@Environment(EnvType.CLIENT)
		abstract class CompileTask implements Comparable<SectionRenderDispatcher.RenderSection.CompileTask> {
			protected final double distAtCreation;
			protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
			protected final boolean isHighPriority;

			public CompileTask(double d, boolean bl) {
				this.distAtCreation = d;
				this.isHighPriority = bl;
			}

			public abstract CompletableFuture<SectionRenderDispatcher.SectionTaskResult> doTask(SectionBufferBuilderPack sectionBufferBuilderPack);

			public abstract void cancel();

			protected abstract String name();

			public int compareTo(SectionRenderDispatcher.RenderSection.CompileTask compileTask) {
				return Doubles.compare(this.distAtCreation, compileTask.distAtCreation);
			}
		}

		@Environment(EnvType.CLIENT)
		class RebuildTask extends SectionRenderDispatcher.RenderSection.CompileTask {
			@Nullable
			protected RenderChunkRegion region;

			public RebuildTask(double d, @Nullable RenderChunkRegion renderChunkRegion, boolean bl) {
				super(d, bl);
				this.region = renderChunkRegion;
			}

			@Override
			protected String name() {
				return "rend_chk_rebuild";
			}

			@Override
			public CompletableFuture<SectionRenderDispatcher.SectionTaskResult> doTask(SectionBufferBuilderPack sectionBufferBuilderPack) {
				if (this.isCancelled.get()) {
					return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
				} else if (!RenderSection.this.hasAllNeighbors()) {
					this.region = null;
					RenderSection.this.setDirty(false);
					this.isCancelled.set(true);
					return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
				} else if (this.isCancelled.get()) {
					return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
				} else {
					Vec3 vec3 = SectionRenderDispatcher.this.getCameraPosition();
					float f = (float)vec3.x;
					float g = (float)vec3.y;
					float h = (float)vec3.z;
					SectionRenderDispatcher.RenderSection.RebuildTask.CompileResults compileResults = this.compile(f, g, h, sectionBufferBuilderPack);
					RenderSection.this.updateGlobalBlockEntities(compileResults.globalBlockEntities);
					if (this.isCancelled.get()) {
						compileResults.renderedLayers.values().forEach(BufferBuilder.RenderedBuffer::release);
						return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
					} else {
						SectionRenderDispatcher.CompiledSection compiledSection = new SectionRenderDispatcher.CompiledSection();
						compiledSection.visibilitySet = compileResults.visibilitySet;
						compiledSection.renderableBlockEntities.addAll(compileResults.blockEntities);
						compiledSection.transparencyState = compileResults.transparencyState;
						List<CompletableFuture<Void>> list = Lists.<CompletableFuture<Void>>newArrayList();
						compileResults.renderedLayers.forEach((renderType, renderedBuffer) -> {
							list.add(SectionRenderDispatcher.this.uploadSectionLayer(renderedBuffer, RenderSection.this.getBuffer(renderType)));
							compiledSection.hasBlocks.add(renderType);
						});
						return Util.sequenceFailFast(list).handle((listx, throwable) -> {
							if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
								Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering section"));
							}

							if (this.isCancelled.get()) {
								return SectionRenderDispatcher.SectionTaskResult.CANCELLED;
							} else {
								RenderSection.this.compiled.set(compiledSection);
								RenderSection.this.initialCompilationCancelCount.set(0);
								SectionRenderDispatcher.this.renderer.addRecentlyCompiledSection(RenderSection.this);
								return SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL;
							}
						});
					}
				}
			}

			private SectionRenderDispatcher.RenderSection.RebuildTask.CompileResults compile(
				float f, float g, float h, SectionBufferBuilderPack sectionBufferBuilderPack
			) {
				SectionRenderDispatcher.RenderSection.RebuildTask.CompileResults compileResults = new SectionRenderDispatcher.RenderSection.RebuildTask.CompileResults();
				int i = 1;
				BlockPos blockPos = RenderSection.this.origin.immutable();
				BlockPos blockPos2 = blockPos.offset(15, 15, 15);
				VisGraph visGraph = new VisGraph();
				RenderChunkRegion renderChunkRegion = this.region;
				this.region = null;
				PoseStack poseStack = new PoseStack();
				if (renderChunkRegion != null) {
					ModelBlockRenderer.enableCaching();
					Set<RenderType> set = new ReferenceArraySet<>(RenderType.chunkBufferLayers().size());
					RandomSource randomSource = RandomSource.create();
					BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();

					for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
						BlockState blockState = renderChunkRegion.getBlockState(blockPos3);
						if (blockState.isSolidRender(renderChunkRegion, blockPos3)) {
							visGraph.setOpaque(blockPos3);
						}

						if (blockState.hasBlockEntity()) {
							BlockEntity blockEntity = renderChunkRegion.getBlockEntity(blockPos3);
							if (blockEntity != null) {
								this.handleBlockEntity(compileResults, blockEntity);
							}
						}

						FluidState fluidState = blockState.getFluidState();
						if (!fluidState.isEmpty()) {
							RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
							BufferBuilder bufferBuilder = sectionBufferBuilderPack.builder(renderType);
							if (set.add(renderType)) {
								RenderSection.this.beginLayer(bufferBuilder);
							}

							blockRenderDispatcher.renderLiquid(blockPos3, renderChunkRegion, bufferBuilder, blockState, fluidState);
						}

						if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
							RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(blockState);
							BufferBuilder bufferBuilder = sectionBufferBuilderPack.builder(renderType);
							if (set.add(renderType)) {
								RenderSection.this.beginLayer(bufferBuilder);
							}

							poseStack.pushPose();
							poseStack.translate((float)(blockPos3.getX() & 15), (float)(blockPos3.getY() & 15), (float)(blockPos3.getZ() & 15));
							blockRenderDispatcher.renderBatched(blockState, blockPos3, renderChunkRegion, poseStack, bufferBuilder, true, randomSource);
							poseStack.popPose();
						}
					}

					if (set.contains(RenderType.translucent())) {
						BufferBuilder bufferBuilder2 = sectionBufferBuilderPack.builder(RenderType.translucent());
						if (!bufferBuilder2.isCurrentBatchEmpty()) {
							bufferBuilder2.setQuadSorting(VertexSorting.byDistance(f - (float)blockPos.getX(), g - (float)blockPos.getY(), h - (float)blockPos.getZ()));
							compileResults.transparencyState = bufferBuilder2.getSortState();
						}
					}

					for (RenderType renderType2 : set) {
						BufferBuilder.RenderedBuffer renderedBuffer = sectionBufferBuilderPack.builder(renderType2).endOrDiscardIfEmpty();
						if (renderedBuffer != null) {
							compileResults.renderedLayers.put(renderType2, renderedBuffer);
						}
					}

					ModelBlockRenderer.clearCache();
				}

				compileResults.visibilitySet = visGraph.resolve();
				return compileResults;
			}

			private <E extends BlockEntity> void handleBlockEntity(SectionRenderDispatcher.RenderSection.RebuildTask.CompileResults compileResults, E blockEntity) {
				BlockEntityRenderer<E> blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
				if (blockEntityRenderer != null) {
					compileResults.blockEntities.add(blockEntity);
					if (blockEntityRenderer.shouldRenderOffScreen(blockEntity)) {
						compileResults.globalBlockEntities.add(blockEntity);
					}
				}
			}

			@Override
			public void cancel() {
				this.region = null;
				if (this.isCancelled.compareAndSet(false, true)) {
					RenderSection.this.setDirty(false);
				}
			}

			@Environment(EnvType.CLIENT)
			static final class CompileResults {
				public final List<BlockEntity> globalBlockEntities = new ArrayList();
				public final List<BlockEntity> blockEntities = new ArrayList();
				public final Map<RenderType, BufferBuilder.RenderedBuffer> renderedLayers = new Reference2ObjectArrayMap<>();
				public VisibilitySet visibilitySet = new VisibilitySet();
				@Nullable
				public BufferBuilder.SortState transparencyState;
			}
		}

		@Environment(EnvType.CLIENT)
		class ResortTransparencyTask extends SectionRenderDispatcher.RenderSection.CompileTask {
			private final SectionRenderDispatcher.CompiledSection compiledSection;

			public ResortTransparencyTask(double d, SectionRenderDispatcher.CompiledSection compiledSection) {
				super(d, true);
				this.compiledSection = compiledSection;
			}

			@Override
			protected String name() {
				return "rend_chk_sort";
			}

			@Override
			public CompletableFuture<SectionRenderDispatcher.SectionTaskResult> doTask(SectionBufferBuilderPack sectionBufferBuilderPack) {
				if (this.isCancelled.get()) {
					return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
				} else if (!RenderSection.this.hasAllNeighbors()) {
					this.isCancelled.set(true);
					return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
				} else if (this.isCancelled.get()) {
					return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
				} else {
					Vec3 vec3 = SectionRenderDispatcher.this.getCameraPosition();
					float f = (float)vec3.x;
					float g = (float)vec3.y;
					float h = (float)vec3.z;
					BufferBuilder.SortState sortState = this.compiledSection.transparencyState;
					if (sortState != null && !this.compiledSection.isEmpty(RenderType.translucent())) {
						BufferBuilder bufferBuilder = sectionBufferBuilderPack.builder(RenderType.translucent());
						RenderSection.this.beginLayer(bufferBuilder);
						bufferBuilder.restoreSortState(sortState);
						bufferBuilder.setQuadSorting(
							VertexSorting.byDistance(
								f - (float)RenderSection.this.origin.getX(), g - (float)RenderSection.this.origin.getY(), h - (float)RenderSection.this.origin.getZ()
							)
						);
						this.compiledSection.transparencyState = bufferBuilder.getSortState();
						BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
						if (this.isCancelled.get()) {
							renderedBuffer.release();
							return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
						} else {
							CompletableFuture<SectionRenderDispatcher.SectionTaskResult> completableFuture = SectionRenderDispatcher.this.uploadSectionLayer(
									renderedBuffer, RenderSection.this.getBuffer(RenderType.translucent())
								)
								.thenApply(void_ -> SectionRenderDispatcher.SectionTaskResult.CANCELLED);
							return completableFuture.handle((sectionTaskResult, throwable) -> {
								if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
									Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering section"));
								}

								return this.isCancelled.get() ? SectionRenderDispatcher.SectionTaskResult.CANCELLED : SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL;
							});
						}
					} else {
						return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
					}
				}
			}

			@Override
			public void cancel() {
				this.isCancelled.set(true);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static enum SectionTaskResult {
		SUCCESSFUL,
		CANCELLED;
	}
}
