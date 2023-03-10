/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.chunk.VisibilitySet;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ChunkRenderDispatcher {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_WORKERS_32_BIT = 4;
    private static final VertexFormat VERTEX_FORMAT = DefaultVertexFormat.BLOCK;
    private static final int MAX_HIGH_PRIORITY_QUOTA = 2;
    private final PriorityBlockingQueue<RenderChunk.ChunkCompileTask> toBatchHighPriority = Queues.newPriorityBlockingQueue();
    private final Queue<RenderChunk.ChunkCompileTask> toBatchLowPriority = Queues.newLinkedBlockingDeque();
    private int highPriorityQuota = 2;
    private final Queue<ChunkBufferBuilderPack> freeBuffers;
    private final Queue<Runnable> toUpload = Queues.newConcurrentLinkedQueue();
    private volatile int toBatchCount;
    private volatile int freeBufferCount;
    final ChunkBufferBuilderPack fixedBuffers;
    private final ProcessorMailbox<Runnable> mailbox;
    private final Executor executor;
    ClientLevel level;
    final LevelRenderer renderer;
    private Vec3 camera = Vec3.ZERO;

    public ChunkRenderDispatcher(ClientLevel clientLevel, LevelRenderer levelRenderer, Executor executor, boolean bl, ChunkBufferBuilderPack chunkBufferBuilderPack) {
        this.level = clientLevel;
        this.renderer = levelRenderer;
        int i = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / (RenderType.chunkBufferLayers().stream().mapToInt(RenderType::bufferSize).sum() * 4) - 1);
        int j = Runtime.getRuntime().availableProcessors();
        int k = bl ? j : Math.min(j, 4);
        int l = Math.max(1, Math.min(k, i));
        this.fixedBuffers = chunkBufferBuilderPack;
        ArrayList<ChunkBufferBuilderPack> list = Lists.newArrayListWithExpectedSize(l);
        try {
            for (int m = 0; m < l; ++m) {
                list.add(new ChunkBufferBuilderPack());
            }
        } catch (OutOfMemoryError outOfMemoryError) {
            LOGGER.warn("Allocated only {}/{} buffers", (Object)list.size(), (Object)l);
            int n = Math.min(list.size() * 2 / 3, list.size() - 1);
            for (int o = 0; o < n; ++o) {
                list.remove(list.size() - 1);
            }
            System.gc();
        }
        this.freeBuffers = Queues.newArrayDeque(list);
        this.freeBufferCount = this.freeBuffers.size();
        this.executor = executor;
        this.mailbox = ProcessorMailbox.create(executor, "Chunk Renderer");
        this.mailbox.tell(this::runTask);
    }

    public void setLevel(ClientLevel clientLevel) {
        this.level = clientLevel;
    }

    private void runTask() {
        if (this.freeBuffers.isEmpty()) {
            return;
        }
        RenderChunk.ChunkCompileTask chunkCompileTask = this.pollTask();
        if (chunkCompileTask == null) {
            return;
        }
        ChunkBufferBuilderPack chunkBufferBuilderPack = this.freeBuffers.poll();
        this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
        this.freeBufferCount = this.freeBuffers.size();
        ((CompletableFuture)CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName(chunkCompileTask.name(), () -> chunkCompileTask.doTask(chunkBufferBuilderPack)), this.executor).thenCompose(completableFuture -> completableFuture)).whenComplete((chunkTaskResult, throwable) -> {
            if (throwable != null) {
                Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Batching chunks"));
                return;
            }
            this.mailbox.tell(() -> {
                if (chunkTaskResult == ChunkTaskResult.SUCCESSFUL) {
                    chunkBufferBuilderPack.clearAll();
                } else {
                    chunkBufferBuilderPack.discardAll();
                }
                this.freeBuffers.add(chunkBufferBuilderPack);
                this.freeBufferCount = this.freeBuffers.size();
                this.runTask();
            });
        });
    }

    @Nullable
    private RenderChunk.ChunkCompileTask pollTask() {
        RenderChunk.ChunkCompileTask chunkCompileTask;
        if (this.highPriorityQuota <= 0 && (chunkCompileTask = this.toBatchLowPriority.poll()) != null) {
            this.highPriorityQuota = 2;
            return chunkCompileTask;
        }
        chunkCompileTask = this.toBatchHighPriority.poll();
        if (chunkCompileTask != null) {
            --this.highPriorityQuota;
            return chunkCompileTask;
        }
        this.highPriorityQuota = 2;
        return this.toBatchLowPriority.poll();
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
        while ((runnable = this.toUpload.poll()) != null) {
            runnable.run();
        }
    }

    public void rebuildChunkSync(RenderChunk renderChunk, RenderRegionCache renderRegionCache) {
        renderChunk.compileSync(renderRegionCache);
    }

    public void blockUntilClear() {
        this.clearBatchQueue();
    }

    public void schedule(RenderChunk.ChunkCompileTask chunkCompileTask) {
        this.mailbox.tell(() -> {
            if (chunkCompileTask.isHighPriority) {
                this.toBatchHighPriority.offer(chunkCompileTask);
            } else {
                this.toBatchLowPriority.offer(chunkCompileTask);
            }
            this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
            this.runTask();
        });
    }

    public CompletableFuture<Void> uploadChunkLayer(BufferBuilder.RenderedBuffer renderedBuffer, VertexBuffer vertexBuffer) {
        return CompletableFuture.runAsync(() -> {
            if (vertexBuffer.isInvalid()) {
                return;
            }
            vertexBuffer.bind();
            vertexBuffer.upload(renderedBuffer);
            VertexBuffer.unbind();
        }, this.toUpload::add);
    }

    private void clearBatchQueue() {
        RenderChunk.ChunkCompileTask chunkCompileTask;
        while (!this.toBatchHighPriority.isEmpty()) {
            chunkCompileTask = this.toBatchHighPriority.poll();
            if (chunkCompileTask == null) continue;
            chunkCompileTask.cancel();
        }
        while (!this.toBatchLowPriority.isEmpty()) {
            chunkCompileTask = this.toBatchLowPriority.poll();
            if (chunkCompileTask == null) continue;
            chunkCompileTask.cancel();
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

    @Environment(value=EnvType.CLIENT)
    public class RenderChunk {
        public static final int SIZE = 16;
        public final int index;
        public final AtomicReference<CompiledChunk> compiled = new AtomicReference<CompiledChunk>(CompiledChunk.UNCOMPILED);
        final AtomicInteger initialCompilationCancelCount = new AtomicInteger(0);
        @Nullable
        private RebuildTask lastRebuildTask;
        @Nullable
        private ResortTransparencyTask lastResortTransparencyTask;
        private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
        private final Map<RenderType, VertexBuffer> buffers = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap(renderType -> renderType, renderType -> new VertexBuffer()));
        private AABB bb;
        private boolean dirty = true;
        final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
        private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], mutableBlockPoss -> {
            for (int i = 0; i < ((BlockPos.MutableBlockPos[])mutableBlockPoss).length; ++i) {
                mutableBlockPoss[i] = new BlockPos.MutableBlockPos();
            }
        });
        private boolean playerChanged;

        public RenderChunk(int i, int j, int k, int l) {
            this.index = i;
            this.setOrigin(j, k, l);
        }

        private boolean doesChunkExistAt(BlockPos blockPos) {
            return ChunkRenderDispatcher.this.level.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()), ChunkStatus.FULL, false) != null;
        }

        public boolean hasAllNeighbors() {
            int i = 24;
            if (this.getDistToPlayerSqr() > 576.0) {
                return this.doesChunkExistAt(this.relativeOrigins[Direction.WEST.ordinal()]) && this.doesChunkExistAt(this.relativeOrigins[Direction.NORTH.ordinal()]) && this.doesChunkExistAt(this.relativeOrigins[Direction.EAST.ordinal()]) && this.doesChunkExistAt(this.relativeOrigins[Direction.SOUTH.ordinal()]);
            }
            return true;
        }

        public AABB getBoundingBox() {
            return this.bb;
        }

        public VertexBuffer getBuffer(RenderType renderType) {
            return this.buffers.get(renderType);
        }

        public void setOrigin(int i, int j, int k) {
            this.reset();
            this.origin.set(i, j, k);
            this.bb = new AABB(i, j, k, i + 16, j + 16, k + 16);
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

        public CompiledChunk getCompiledChunk() {
            return this.compiled.get();
        }

        private void reset() {
            this.cancelTasks();
            this.compiled.set(CompiledChunk.UNCOMPILED);
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

        public boolean resortTransparency(RenderType renderType, ChunkRenderDispatcher chunkRenderDispatcher) {
            CompiledChunk compiledChunk = this.getCompiledChunk();
            if (this.lastResortTransparencyTask != null) {
                this.lastResortTransparencyTask.cancel();
            }
            if (!compiledChunk.hasBlocks.contains(renderType)) {
                return false;
            }
            this.lastResortTransparencyTask = new ResortTransparencyTask(this.getDistToPlayerSqr(), compiledChunk);
            chunkRenderDispatcher.schedule(this.lastResortTransparencyTask);
            return true;
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

        public ChunkCompileTask createCompileTask(RenderRegionCache renderRegionCache) {
            boolean bl2;
            boolean bl = this.cancelTasks();
            BlockPos blockPos = this.origin.immutable();
            boolean i = true;
            RenderChunkRegion renderChunkRegion = renderRegionCache.createRegion(ChunkRenderDispatcher.this.level, blockPos.offset(-1, -1, -1), blockPos.offset(16, 16, 16), 1);
            boolean bl3 = bl2 = this.compiled.get() == CompiledChunk.UNCOMPILED;
            if (bl2 && bl) {
                this.initialCompilationCancelCount.incrementAndGet();
            }
            this.lastRebuildTask = new RebuildTask(this.getDistToPlayerSqr(), renderChunkRegion, !bl2 || this.initialCompilationCancelCount.get() > 2);
            return this.lastRebuildTask;
        }

        public void rebuildChunkAsync(ChunkRenderDispatcher chunkRenderDispatcher, RenderRegionCache renderRegionCache) {
            ChunkCompileTask chunkCompileTask = this.createCompileTask(renderRegionCache);
            chunkRenderDispatcher.schedule(chunkCompileTask);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void updateGlobalBlockEntities(Collection<BlockEntity> collection) {
            HashSet<BlockEntity> set2;
            HashSet<BlockEntity> set = Sets.newHashSet(collection);
            Set<BlockEntity> set3 = this.globalBlockEntities;
            synchronized (set3) {
                set2 = Sets.newHashSet(this.globalBlockEntities);
                set.removeAll(this.globalBlockEntities);
                set2.removeAll(collection);
                this.globalBlockEntities.clear();
                this.globalBlockEntities.addAll(collection);
            }
            ChunkRenderDispatcher.this.renderer.updateGlobalBlockEntities(set2, set);
        }

        public void compileSync(RenderRegionCache renderRegionCache) {
            ChunkCompileTask chunkCompileTask = this.createCompileTask(renderRegionCache);
            chunkCompileTask.doTask(ChunkRenderDispatcher.this.fixedBuffers);
        }

        @Environment(value=EnvType.CLIENT)
        class ResortTransparencyTask
        extends ChunkCompileTask {
            private final CompiledChunk compiledChunk;

            public ResortTransparencyTask(double d, CompiledChunk compiledChunk) {
                super(d, true);
                this.compiledChunk = compiledChunk;
            }

            @Override
            protected String name() {
                return "rend_chk_sort";
            }

            @Override
            public CompletableFuture<ChunkTaskResult> doTask(ChunkBufferBuilderPack chunkBufferBuilderPack) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (!RenderChunk.this.hasAllNeighbors()) {
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
                float f = (float)vec3.x;
                float g = (float)vec3.y;
                float h = (float)vec3.z;
                BufferBuilder.SortState sortState = this.compiledChunk.transparencyState;
                if (sortState == null || this.compiledChunk.isEmpty(RenderType.translucent())) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                BufferBuilder bufferBuilder = chunkBufferBuilderPack.builder(RenderType.translucent());
                RenderChunk.this.beginLayer(bufferBuilder);
                bufferBuilder.restoreSortState(sortState);
                bufferBuilder.setQuadSortOrigin(f - (float)RenderChunk.this.origin.getX(), g - (float)RenderChunk.this.origin.getY(), h - (float)RenderChunk.this.origin.getZ());
                this.compiledChunk.transparencyState = bufferBuilder.getSortState();
                BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
                if (this.isCancelled.get()) {
                    renderedBuffer.release();
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                CompletionStage completableFuture = ChunkRenderDispatcher.this.uploadChunkLayer(renderedBuffer, RenderChunk.this.getBuffer(RenderType.translucent())).thenApply(void_ -> ChunkTaskResult.CANCELLED);
                return ((CompletableFuture)completableFuture).handle((chunkTaskResult, throwable) -> {
                    if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering chunk"));
                    }
                    return this.isCancelled.get() ? ChunkTaskResult.CANCELLED : ChunkTaskResult.SUCCESSFUL;
                });
            }

            @Override
            public void cancel() {
                this.isCancelled.set(true);
            }
        }

        @Environment(value=EnvType.CLIENT)
        abstract class ChunkCompileTask
        implements Comparable<ChunkCompileTask> {
            protected final double distAtCreation;
            protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
            protected final boolean isHighPriority;

            public ChunkCompileTask(double d, boolean bl) {
                this.distAtCreation = d;
                this.isHighPriority = bl;
            }

            public abstract CompletableFuture<ChunkTaskResult> doTask(ChunkBufferBuilderPack var1);

            public abstract void cancel();

            protected abstract String name();

            @Override
            public int compareTo(ChunkCompileTask chunkCompileTask) {
                return Doubles.compare(this.distAtCreation, chunkCompileTask.distAtCreation);
            }

            @Override
            public /* synthetic */ int compareTo(Object object) {
                return this.compareTo((ChunkCompileTask)object);
            }
        }

        @Environment(value=EnvType.CLIENT)
        class RebuildTask
        extends ChunkCompileTask {
            @Nullable
            protected RenderChunkRegion region;

            public RebuildTask(@Nullable double d, RenderChunkRegion renderChunkRegion, boolean bl) {
                super(d, bl);
                this.region = renderChunkRegion;
            }

            @Override
            protected String name() {
                return "rend_chk_rebuild";
            }

            @Override
            public CompletableFuture<ChunkTaskResult> doTask(ChunkBufferBuilderPack chunkBufferBuilderPack) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (!RenderChunk.this.hasAllNeighbors()) {
                    this.region = null;
                    RenderChunk.this.setDirty(false);
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
                float f = (float)vec3.x;
                float g = (float)vec3.y;
                float h = (float)vec3.z;
                CompileResults compileResults = this.compile(f, g, h, chunkBufferBuilderPack);
                RenderChunk.this.updateGlobalBlockEntities(compileResults.globalBlockEntities);
                if (this.isCancelled.get()) {
                    compileResults.renderedLayers.values().forEach(BufferBuilder.RenderedBuffer::release);
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                CompiledChunk compiledChunk = new CompiledChunk();
                compiledChunk.visibilitySet = compileResults.visibilitySet;
                compiledChunk.renderableBlockEntities.addAll(compileResults.blockEntities);
                compiledChunk.transparencyState = compileResults.transparencyState;
                ArrayList list2 = Lists.newArrayList();
                compileResults.renderedLayers.forEach((renderType, renderedBuffer) -> {
                    list2.add(ChunkRenderDispatcher.this.uploadChunkLayer((BufferBuilder.RenderedBuffer)renderedBuffer, RenderChunk.this.getBuffer((RenderType)renderType)));
                    compiledChunk.hasBlocks.add((RenderType)renderType);
                });
                return Util.sequenceFailFast(list2).handle((list, throwable) -> {
                    if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering chunk"));
                    }
                    if (this.isCancelled.get()) {
                        return ChunkTaskResult.CANCELLED;
                    }
                    RenderChunk.this.compiled.set(compiledChunk);
                    RenderChunk.this.initialCompilationCancelCount.set(0);
                    ChunkRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderChunk.this);
                    return ChunkTaskResult.SUCCESSFUL;
                });
            }

            private CompileResults compile(float f, float g, float h, ChunkBufferBuilderPack chunkBufferBuilderPack) {
                CompileResults compileResults = new CompileResults();
                boolean i = true;
                BlockPos blockPos = RenderChunk.this.origin.immutable();
                BlockPos blockPos2 = blockPos.offset(15, 15, 15);
                VisGraph visGraph = new VisGraph();
                RenderChunkRegion renderChunkRegion = this.region;
                this.region = null;
                PoseStack poseStack = new PoseStack();
                if (renderChunkRegion != null) {
                    BufferBuilder bufferBuilder2;
                    ModelBlockRenderer.enableCaching();
                    ReferenceArraySet set = new ReferenceArraySet(RenderType.chunkBufferLayers().size());
                    RandomSource randomSource = RandomSource.create();
                    BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
                    for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
                        BufferBuilder bufferBuilder;
                        RenderType renderType;
                        BlockState blockState2;
                        FluidState fluidState;
                        BlockEntity blockEntity;
                        BlockState blockState = renderChunkRegion.getBlockState(blockPos3);
                        if (blockState.isSolidRender(renderChunkRegion, blockPos3)) {
                            visGraph.setOpaque(blockPos3);
                        }
                        if (blockState.hasBlockEntity() && (blockEntity = renderChunkRegion.getBlockEntity(blockPos3)) != null) {
                            this.handleBlockEntity(compileResults, blockEntity);
                        }
                        if (!(fluidState = (blockState2 = renderChunkRegion.getBlockState(blockPos3)).getFluidState()).isEmpty()) {
                            renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
                            bufferBuilder = chunkBufferBuilderPack.builder(renderType);
                            if (set.add(renderType)) {
                                RenderChunk.this.beginLayer(bufferBuilder);
                            }
                            blockRenderDispatcher.renderLiquid(blockPos3, renderChunkRegion, bufferBuilder, blockState2, fluidState);
                        }
                        if (blockState.getRenderShape() == RenderShape.INVISIBLE) continue;
                        renderType = ItemBlockRenderTypes.getChunkRenderType(blockState);
                        bufferBuilder = chunkBufferBuilderPack.builder(renderType);
                        if (set.add(renderType)) {
                            RenderChunk.this.beginLayer(bufferBuilder);
                        }
                        poseStack.pushPose();
                        poseStack.translate(blockPos3.getX() & 0xF, blockPos3.getY() & 0xF, blockPos3.getZ() & 0xF);
                        blockRenderDispatcher.renderBatched(blockState, blockPos3, renderChunkRegion, poseStack, bufferBuilder, true, randomSource);
                        poseStack.popPose();
                    }
                    if (set.contains(RenderType.translucent()) && !(bufferBuilder2 = chunkBufferBuilderPack.builder(RenderType.translucent())).isCurrentBatchEmpty()) {
                        bufferBuilder2.setQuadSortOrigin(f - (float)blockPos.getX(), g - (float)blockPos.getY(), h - (float)blockPos.getZ());
                        compileResults.transparencyState = bufferBuilder2.getSortState();
                    }
                    for (RenderType renderType2 : set) {
                        BufferBuilder.RenderedBuffer renderedBuffer = chunkBufferBuilderPack.builder(renderType2).endOrDiscardIfEmpty();
                        if (renderedBuffer == null) continue;
                        compileResults.renderedLayers.put(renderType2, renderedBuffer);
                    }
                    ModelBlockRenderer.clearCache();
                }
                compileResults.visibilitySet = visGraph.resolve();
                return compileResults;
            }

            private <E extends BlockEntity> void handleBlockEntity(CompileResults compileResults, E blockEntity) {
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
                    RenderChunk.this.setDirty(false);
                }
            }

            @Environment(value=EnvType.CLIENT)
            static final class CompileResults {
                public final List<BlockEntity> globalBlockEntities = new ArrayList<BlockEntity>();
                public final List<BlockEntity> blockEntities = new ArrayList<BlockEntity>();
                public final Map<RenderType, BufferBuilder.RenderedBuffer> renderedLayers = new Reference2ObjectArrayMap<RenderType, BufferBuilder.RenderedBuffer>();
                public VisibilitySet visibilitySet = new VisibilitySet();
                @Nullable
                public BufferBuilder.SortState transparencyState;

                CompileResults() {
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum ChunkTaskResult {
        SUCCESSFUL,
        CANCELLED;

    }

    @Environment(value=EnvType.CLIENT)
    public static class CompiledChunk {
        public static final CompiledChunk UNCOMPILED = new CompiledChunk(){

            @Override
            public boolean facesCanSeeEachother(Direction direction, Direction direction2) {
                return false;
            }
        };
        final Set<RenderType> hasBlocks = new ObjectArraySet<RenderType>(RenderType.chunkBufferLayers().size());
        final List<BlockEntity> renderableBlockEntities = Lists.newArrayList();
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
}

