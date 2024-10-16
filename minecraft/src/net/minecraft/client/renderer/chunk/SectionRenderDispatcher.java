package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.TracingExecutor;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.SectionBufferBuilderPool;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.util.thread.ConsecutiveExecutor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class SectionRenderDispatcher {
	private final CompileTaskDynamicQueue compileQueue = new CompileTaskDynamicQueue();
	private final Queue<Runnable> toUpload = Queues.<Runnable>newConcurrentLinkedQueue();
	final SectionBufferBuilderPack fixedBuffers;
	private final SectionBufferBuilderPool bufferPool;
	private volatile int toBatchCount;
	private volatile boolean closed;
	private final ConsecutiveExecutor consecutiveExecutor;
	private final TracingExecutor executor;
	ClientLevel level;
	final LevelRenderer renderer;
	private Vec3 camera = Vec3.ZERO;
	final SectionCompiler sectionCompiler;

	public SectionRenderDispatcher(
		ClientLevel clientLevel,
		LevelRenderer levelRenderer,
		TracingExecutor tracingExecutor,
		RenderBuffers renderBuffers,
		BlockRenderDispatcher blockRenderDispatcher,
		BlockEntityRenderDispatcher blockEntityRenderDispatcher
	) {
		this.level = clientLevel;
		this.renderer = levelRenderer;
		this.fixedBuffers = renderBuffers.fixedBufferPack();
		this.bufferPool = renderBuffers.sectionBufferPool();
		this.executor = tracingExecutor;
		this.consecutiveExecutor = new ConsecutiveExecutor(tracingExecutor, "Section Renderer");
		this.consecutiveExecutor.schedule(this::runTask);
		this.sectionCompiler = new SectionCompiler(blockRenderDispatcher, blockEntityRenderDispatcher);
	}

	public void setLevel(ClientLevel clientLevel) {
		this.level = clientLevel;
	}

	private void runTask() {
		if (!this.closed && !this.bufferPool.isEmpty()) {
			SectionRenderDispatcher.RenderSection.CompileTask compileTask = this.compileQueue.poll(this.getCameraPosition());
			if (compileTask != null) {
				SectionBufferBuilderPack sectionBufferBuilderPack = (SectionBufferBuilderPack)Objects.requireNonNull(this.bufferPool.acquire());
				this.toBatchCount = this.compileQueue.size();
				CompletableFuture.supplyAsync(() -> compileTask.doTask(sectionBufferBuilderPack), this.executor.forName(compileTask.name()))
					.thenCompose(completableFuture -> completableFuture)
					.whenComplete((sectionTaskResult, throwable) -> {
						if (throwable != null) {
							Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Batching sections"));
						} else {
							compileTask.isCompleted.set(true);
							this.consecutiveExecutor.schedule(() -> {
								if (sectionTaskResult == SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL) {
									sectionBufferBuilderPack.clearAll();
								} else {
									sectionBufferBuilderPack.discardAll();
								}

								this.bufferPool.release(sectionBufferBuilderPack);
								this.runTask();
							});
						}
					});
			}
		}
	}

	public String getStats() {
		return String.format(Locale.ROOT, "pC: %03d, pU: %02d, aB: %02d", this.toBatchCount, this.toUpload.size(), this.bufferPool.getFreeBufferCount());
	}

	public int getToBatchCount() {
		return this.toBatchCount;
	}

	public int getToUpload() {
		return this.toUpload.size();
	}

	public int getFreeBufferCount() {
		return this.bufferPool.getFreeBufferCount();
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
		if (!this.closed) {
			this.consecutiveExecutor.schedule(() -> {
				if (!this.closed) {
					this.compileQueue.add(compileTask);
					this.toBatchCount = this.compileQueue.size();
					this.runTask();
				}
			});
		}
	}

	public CompletableFuture<Void> uploadSectionLayer(MeshData meshData, VertexBuffer vertexBuffer) {
		return this.closed ? CompletableFuture.completedFuture(null) : CompletableFuture.runAsync(() -> {
			if (vertexBuffer.isInvalid()) {
				meshData.close();
			} else {
				try (Zone zone = Profiler.get().zone("Upload Section Layer")) {
					vertexBuffer.bind();
					vertexBuffer.upload(meshData);
					VertexBuffer.unbind();
				}
			}
		}, this.toUpload::add);
	}

	public CompletableFuture<Void> uploadSectionIndexBuffer(ByteBufferBuilder.Result result, VertexBuffer vertexBuffer) {
		return this.closed ? CompletableFuture.completedFuture(null) : CompletableFuture.runAsync(() -> {
			if (vertexBuffer.isInvalid()) {
				result.close();
			} else {
				try (Zone zone = Profiler.get().zone("Upload Section Indices")) {
					vertexBuffer.bind();
					vertexBuffer.uploadIndexBuffer(result);
					VertexBuffer.unbind();
				}
			}
		}, this.toUpload::add);
	}

	private void clearBatchQueue() {
		this.compileQueue.clear();
		this.toBatchCount = 0;
	}

	public boolean isQueueEmpty() {
		return this.toBatchCount == 0 && this.toUpload.isEmpty();
	}

	public void dispose() {
		this.closed = true;
		this.clearBatchQueue();
		this.uploadAllPendingUploads();
	}

	@Environment(EnvType.CLIENT)
	public static class CompiledSection {
		public static final SectionRenderDispatcher.CompiledSection UNCOMPILED = new SectionRenderDispatcher.CompiledSection() {
			@Override
			public boolean facesCanSeeEachother(Direction direction, Direction direction2) {
				return false;
			}
		};
		public static final SectionRenderDispatcher.CompiledSection EMPTY = new SectionRenderDispatcher.CompiledSection() {
			@Override
			public boolean facesCanSeeEachother(Direction direction, Direction direction2) {
				return true;
			}
		};
		final Set<RenderType> hasBlocks = new ObjectArraySet<>(RenderType.chunkBufferLayers().size());
		final List<BlockEntity> renderableBlockEntities = Lists.<BlockEntity>newArrayList();
		VisibilitySet visibilitySet = new VisibilitySet();
		@Nullable
		MeshData.SortState transparencyState;

		public boolean hasRenderableLayers() {
			return !this.hasBlocks.isEmpty();
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
		public final AtomicReference<SectionRenderDispatcher.TranslucencyPointOfView> pointOfView = new AtomicReference(null);
		@Nullable
		private SectionRenderDispatcher.RenderSection.RebuildTask lastRebuildTask;
		@Nullable
		private SectionRenderDispatcher.RenderSection.ResortTransparencyTask lastResortTransparencyTask;
		private final Set<BlockEntity> globalBlockEntities = Sets.<BlockEntity>newHashSet();
		private final Map<RenderType, VertexBuffer> buffers = (Map<RenderType, VertexBuffer>)RenderType.chunkBufferLayers()
			.stream()
			.collect(Collectors.toMap(renderType -> renderType, renderType -> new VertexBuffer(BufferUsage.STATIC_WRITE)));
		private AABB bb;
		private boolean dirty = true;
		long sectionNode = SectionPos.asLong(-1, -1, -1);
		final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
		private boolean playerChanged;

		public RenderSection(final int i, final long l) {
			this.index = i;
			this.setSectionNode(l);
		}

		private boolean doesChunkExistAt(long l) {
			return SectionRenderDispatcher.this.level.getChunk(SectionPos.x(l), SectionPos.z(l), ChunkStatus.FULL, false) != null;
		}

		public boolean hasAllNeighbors() {
			int i = 24;
			return !(this.getDistToPlayerSqr() > 576.0)
				? true
				: this.doesChunkExistAt(SectionPos.offset(this.sectionNode, Direction.WEST))
					&& this.doesChunkExistAt(SectionPos.offset(this.sectionNode, Direction.NORTH))
					&& this.doesChunkExistAt(SectionPos.offset(this.sectionNode, Direction.EAST))
					&& this.doesChunkExistAt(SectionPos.offset(this.sectionNode, Direction.SOUTH));
		}

		public AABB getBoundingBox() {
			return this.bb;
		}

		public VertexBuffer getBuffer(RenderType renderType) {
			return (VertexBuffer)this.buffers.get(renderType);
		}

		public void setSectionNode(long l) {
			this.reset();
			this.sectionNode = l;
			int i = SectionPos.sectionToBlockCoord(SectionPos.x(l));
			int j = SectionPos.sectionToBlockCoord(SectionPos.y(l));
			int k = SectionPos.sectionToBlockCoord(SectionPos.z(l));
			this.origin.set(i, j, k);
			this.bb = new AABB((double)i, (double)j, (double)k, (double)(i + 16), (double)(j + 16), (double)(k + 16));
		}

		protected double getDistToPlayerSqr() {
			Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
			double d = this.bb.minX + 8.0 - camera.getPosition().x;
			double e = this.bb.minY + 8.0 - camera.getPosition().y;
			double f = this.bb.minZ + 8.0 - camera.getPosition().z;
			return d * d + e * e + f * f;
		}

		public SectionRenderDispatcher.CompiledSection getCompiled() {
			return (SectionRenderDispatcher.CompiledSection)this.compiled.get();
		}

		private void reset() {
			this.cancelTasks();
			this.compiled.set(SectionRenderDispatcher.CompiledSection.UNCOMPILED);
			this.pointOfView.set(null);
			this.dirty = true;
		}

		public void releaseBuffers() {
			this.reset();
			this.buffers.values().forEach(VertexBuffer::close);
		}

		public BlockPos getOrigin() {
			return this.origin;
		}

		public long getSectionNode() {
			return this.sectionNode;
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

		public long getNeighborSectionNode(Direction direction) {
			return SectionPos.offset(this.sectionNode, direction);
		}

		public void resortTransparency(SectionRenderDispatcher sectionRenderDispatcher) {
			this.lastResortTransparencyTask = new SectionRenderDispatcher.RenderSection.ResortTransparencyTask(this.getCompiled());
			sectionRenderDispatcher.schedule(this.lastResortTransparencyTask);
		}

		public boolean hasTranslucentGeometry() {
			return this.getCompiled().hasBlocks.contains(RenderType.translucent());
		}

		public boolean transparencyResortingScheduled() {
			return this.lastResortTransparencyTask != null && !this.lastResortTransparencyTask.isCompleted.get();
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

		public SectionRenderDispatcher.RenderSection.CompileTask createCompileTask(RenderRegionCache renderRegionCache) {
			this.cancelTasks();
			RenderChunkRegion renderChunkRegion = renderRegionCache.createRegion(SectionRenderDispatcher.this.level, SectionPos.of(this.sectionNode));
			boolean bl = this.compiled.get() != SectionRenderDispatcher.CompiledSection.UNCOMPILED;
			this.lastRebuildTask = new SectionRenderDispatcher.RenderSection.RebuildTask(renderChunkRegion, bl);
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

		void setCompiled(SectionRenderDispatcher.CompiledSection compiledSection) {
			this.compiled.set(compiledSection);
			SectionRenderDispatcher.this.renderer.addRecentlyCompiledSection(this);
		}

		VertexSorting createVertexSorting() {
			Vec3 vec3 = SectionRenderDispatcher.this.getCameraPosition();
			return VertexSorting.byDistance(
				(float)(vec3.x - (double)this.origin.getX()), (float)(vec3.y - (double)this.origin.getY()), (float)(vec3.z - (double)this.origin.getZ())
			);
		}

		@Environment(EnvType.CLIENT)
		public abstract class CompileTask {
			protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
			protected final AtomicBoolean isCompleted = new AtomicBoolean(false);
			protected final boolean isRecompile;

			public CompileTask(final boolean bl) {
				this.isRecompile = bl;
			}

			public abstract CompletableFuture<SectionRenderDispatcher.SectionTaskResult> doTask(SectionBufferBuilderPack sectionBufferBuilderPack);

			public abstract void cancel();

			protected abstract String name();

			public boolean isRecompile() {
				return this.isRecompile;
			}

			public BlockPos getOrigin() {
				return RenderSection.this.origin;
			}
		}

		@Environment(EnvType.CLIENT)
		class RebuildTask extends SectionRenderDispatcher.RenderSection.CompileTask {
			@Nullable
			protected volatile RenderChunkRegion region;

			public RebuildTask(@Nullable final RenderChunkRegion renderChunkRegion, final boolean bl) {
				super(bl);
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
					this.cancel();
					return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
				} else if (this.isCancelled.get()) {
					return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
				} else {
					RenderChunkRegion renderChunkRegion = this.region;
					this.region = null;
					if (renderChunkRegion == null) {
						RenderSection.this.setCompiled(SectionRenderDispatcher.CompiledSection.EMPTY);
						return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL);
					} else {
						SectionPos sectionPos = SectionPos.of(RenderSection.this.origin);
						if (this.isCancelled.get()) {
							return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
						} else {
							SectionCompiler.Results results;
							try (Zone zone = Profiler.get().zone("Compile Section")) {
								results = SectionRenderDispatcher.this.sectionCompiler
									.compile(sectionPos, renderChunkRegion, RenderSection.this.createVertexSorting(), sectionBufferBuilderPack);
							}

							SectionRenderDispatcher.TranslucencyPointOfView translucencyPointOfView = SectionRenderDispatcher.TranslucencyPointOfView.of(
								SectionRenderDispatcher.this.getCameraPosition(), RenderSection.this.sectionNode
							);
							RenderSection.this.updateGlobalBlockEntities(results.globalBlockEntities);
							if (this.isCancelled.get()) {
								results.release();
								return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
							} else {
								SectionRenderDispatcher.CompiledSection compiledSection = new SectionRenderDispatcher.CompiledSection();
								compiledSection.visibilitySet = results.visibilitySet;
								compiledSection.renderableBlockEntities.addAll(results.blockEntities);
								compiledSection.transparencyState = results.transparencyState;
								List<CompletableFuture<Void>> list = new ArrayList(results.renderedLayers.size());
								results.renderedLayers.forEach((renderType, meshData) -> {
									list.add(SectionRenderDispatcher.this.uploadSectionLayer(meshData, RenderSection.this.getBuffer(renderType)));
									compiledSection.hasBlocks.add(renderType);
								});
								return Util.sequenceFailFast(list).handle((listx, throwable) -> {
									if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
										Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering section"));
									}

									if (this.isCancelled.get()) {
										return SectionRenderDispatcher.SectionTaskResult.CANCELLED;
									} else {
										RenderSection.this.setCompiled(compiledSection);
										RenderSection.this.pointOfView.set(translucencyPointOfView);
										return SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL;
									}
								});
							}
						}
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
		}

		@Environment(EnvType.CLIENT)
		class ResortTransparencyTask extends SectionRenderDispatcher.RenderSection.CompileTask {
			private final SectionRenderDispatcher.CompiledSection compiledSection;

			public ResortTransparencyTask(final SectionRenderDispatcher.CompiledSection compiledSection) {
				super(true);
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
					MeshData.SortState sortState = this.compiledSection.transparencyState;
					if (sortState != null && !this.compiledSection.isEmpty(RenderType.translucent())) {
						VertexSorting vertexSorting = RenderSection.this.createVertexSorting();
						SectionRenderDispatcher.TranslucencyPointOfView translucencyPointOfView = SectionRenderDispatcher.TranslucencyPointOfView.of(
							SectionRenderDispatcher.this.getCameraPosition(), RenderSection.this.sectionNode
						);
						if (translucencyPointOfView.equals(RenderSection.this.pointOfView.get()) && !translucencyPointOfView.isAxisAligned()) {
							return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
						} else {
							ByteBufferBuilder.Result result = sortState.buildSortedIndexBuffer(sectionBufferBuilderPack.buffer(RenderType.translucent()), vertexSorting);
							if (result == null) {
								return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
							} else if (this.isCancelled.get()) {
								result.close();
								return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
							} else {
								CompletableFuture<SectionRenderDispatcher.SectionTaskResult> completableFuture = SectionRenderDispatcher.this.uploadSectionIndexBuffer(
										result, RenderSection.this.getBuffer(RenderType.translucent())
									)
									.thenApply(void_ -> SectionRenderDispatcher.SectionTaskResult.CANCELLED);
								return completableFuture.handle((sectionTaskResult, throwable) -> {
									if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
										Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering section"));
									}

									if (this.isCancelled.get()) {
										return SectionRenderDispatcher.SectionTaskResult.CANCELLED;
									} else {
										RenderSection.this.pointOfView.set(translucencyPointOfView);
										return SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL;
									}
								});
							}
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

	@Environment(EnvType.CLIENT)
	public static final class TranslucencyPointOfView {
		private int x;
		private int y;
		private int z;

		public static SectionRenderDispatcher.TranslucencyPointOfView of(Vec3 vec3, long l) {
			return new SectionRenderDispatcher.TranslucencyPointOfView().set(vec3, l);
		}

		public SectionRenderDispatcher.TranslucencyPointOfView set(Vec3 vec3, long l) {
			this.x = getCoordinate(vec3.x(), SectionPos.x(l));
			this.y = getCoordinate(vec3.y(), SectionPos.y(l));
			this.z = getCoordinate(vec3.z(), SectionPos.z(l));
			return this;
		}

		private static int getCoordinate(double d, int i) {
			int j = SectionPos.blockToSectionCoord(d) - i;
			return Mth.clamp(j, -1, 1);
		}

		public boolean isAxisAligned() {
			return this.x == 0 || this.y == 0 || this.z == 0;
		}

		public boolean equals(Object object) {
			if (object == this) {
				return true;
			} else {
				return !(object instanceof SectionRenderDispatcher.TranslucencyPointOfView translucencyPointOfView)
					? false
					: this.x == translucencyPointOfView.x && this.y == translucencyPointOfView.y && this.z == translucencyPointOfView.z;
			}
		}
	}
}
