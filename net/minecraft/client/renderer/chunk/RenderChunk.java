/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.chunk.ChunkCompileTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RenderChunk {
    private volatile Level level;
    private final LevelRenderer renderer;
    public static int updateCounter;
    public CompiledChunk compiled = CompiledChunk.UNCOMPILED;
    private final ReentrantLock taskLock = new ReentrantLock();
    private final ReentrantLock compileLock = new ReentrantLock();
    private ChunkCompileTask pendingTask;
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    private final VertexBuffer[] buffers = new VertexBuffer[BlockLayer.values().length];
    public AABB bb;
    private int lastFrame = -1;
    private boolean dirty = true;
    private final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
    private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], mutableBlockPoss -> {
        for (int i = 0; i < ((BlockPos.MutableBlockPos[])mutableBlockPoss).length; ++i) {
            mutableBlockPoss[i] = new BlockPos.MutableBlockPos();
        }
    });
    private boolean playerChanged;

    public RenderChunk(Level level, LevelRenderer levelRenderer) {
        this.level = level;
        this.renderer = levelRenderer;
        for (int i = 0; i < BlockLayer.values().length; ++i) {
            this.buffers[i] = new VertexBuffer(DefaultVertexFormat.BLOCK);
        }
    }

    private static boolean doesChunkExistAt(BlockPos blockPos, Level level) {
        return !level.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4).isEmpty();
    }

    public boolean hasAllNeighbors() {
        int i = 24;
        if (this.getDistToPlayerSqr() > 576.0) {
            Level level = this.getLevel();
            return RenderChunk.doesChunkExistAt(this.relativeOrigins[Direction.WEST.ordinal()], level) && RenderChunk.doesChunkExistAt(this.relativeOrigins[Direction.NORTH.ordinal()], level) && RenderChunk.doesChunkExistAt(this.relativeOrigins[Direction.EAST.ordinal()], level) && RenderChunk.doesChunkExistAt(this.relativeOrigins[Direction.SOUTH.ordinal()], level);
        }
        return true;
    }

    public boolean setFrame(int i) {
        if (this.lastFrame == i) {
            return false;
        }
        this.lastFrame = i;
        return true;
    }

    public VertexBuffer getBuffer(int i) {
        return this.buffers[i];
    }

    public void setOrigin(int i, int j, int k) {
        if (i == this.origin.getX() && j == this.origin.getY() && k == this.origin.getZ()) {
            return;
        }
        this.reset();
        this.origin.set(i, j, k);
        this.bb = new AABB(i, j, k, i + 16, j + 16, k + 16);
        for (Direction direction : Direction.values()) {
            this.relativeOrigins[direction.ordinal()].set(this.origin).move(direction, 16);
        }
    }

    public void rebuildTransparent(float f, float g, float h, ChunkCompileTask chunkCompileTask) {
        CompiledChunk compiledChunk = chunkCompileTask.getCompiledChunk();
        if (compiledChunk.getTransparencyState() == null || compiledChunk.isEmpty(BlockLayer.TRANSLUCENT)) {
            return;
        }
        this.beginLayer(chunkCompileTask.getBuilders().builder(BlockLayer.TRANSLUCENT), this.origin);
        chunkCompileTask.getBuilders().builder(BlockLayer.TRANSLUCENT).restoreState(compiledChunk.getTransparencyState());
        this.preEndLayer(BlockLayer.TRANSLUCENT, f, g, h, chunkCompileTask.getBuilders().builder(BlockLayer.TRANSLUCENT), compiledChunk);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void compile(float f, float g, float h, ChunkCompileTask chunkCompileTask) {
        CompiledChunk compiledChunk = new CompiledChunk();
        boolean i = true;
        BlockPos blockPos = this.origin.immutable();
        BlockPos blockPos2 = blockPos.offset(15, 15, 15);
        Level level = this.level;
        if (level == null) {
            return;
        }
        chunkCompileTask.getStatusLock().lock();
        try {
            if (chunkCompileTask.getStatus() != ChunkCompileTask.Status.COMPILING) {
                return;
            }
            chunkCompileTask.setCompiledChunk(compiledChunk);
        } finally {
            chunkCompileTask.getStatusLock().unlock();
        }
        VisGraph visGraph = new VisGraph();
        HashSet<BlockEntity> set = Sets.newHashSet();
        RenderChunkRegion renderChunkRegion = chunkCompileTask.takeRegion();
        if (renderChunkRegion != null) {
            ++updateCounter;
            boolean[] bls = new boolean[BlockLayer.values().length];
            ModelBlockRenderer.enableCaching();
            Random random = new Random();
            BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
            for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
                BufferBuilder bufferBuilder;
                int j;
                BlockLayer blockLayer;
                FluidState fluidState;
                BlockEntityRenderer<BlockEntity> blockEntityRenderer;
                BlockEntity blockEntity;
                BlockState blockState = renderChunkRegion.getBlockState(blockPos3);
                Block block = blockState.getBlock();
                if (blockState.isSolidRender(renderChunkRegion, blockPos3)) {
                    visGraph.setOpaque(blockPos3);
                }
                if (block.isEntityBlock() && (blockEntity = renderChunkRegion.getBlockEntity(blockPos3, LevelChunk.EntityCreationType.CHECK)) != null && (blockEntityRenderer = BlockEntityRenderDispatcher.instance.getRenderer(blockEntity)) != null) {
                    compiledChunk.addRenderableBlockEntity(blockEntity);
                    if (blockEntityRenderer.shouldRenderOffScreen(blockEntity)) {
                        set.add(blockEntity);
                    }
                }
                if (!(fluidState = renderChunkRegion.getFluidState(blockPos3)).isEmpty()) {
                    blockLayer = fluidState.getRenderLayer();
                    j = blockLayer.ordinal();
                    bufferBuilder = chunkCompileTask.getBuilders().builder(j);
                    if (!compiledChunk.hasLayer(blockLayer)) {
                        compiledChunk.layerIsPresent(blockLayer);
                        this.beginLayer(bufferBuilder, blockPos);
                    }
                    int n = j;
                    bls[n] = bls[n] | blockRenderDispatcher.renderLiquid(blockPos3, renderChunkRegion, bufferBuilder, fluidState);
                }
                if (blockState.getRenderShape() == RenderShape.INVISIBLE) continue;
                blockLayer = block.getRenderLayer();
                j = blockLayer.ordinal();
                bufferBuilder = chunkCompileTask.getBuilders().builder(j);
                if (!compiledChunk.hasLayer(blockLayer)) {
                    compiledChunk.layerIsPresent(blockLayer);
                    this.beginLayer(bufferBuilder, blockPos);
                }
                int n = j;
                bls[n] = bls[n] | blockRenderDispatcher.renderBatched(blockState, blockPos3, renderChunkRegion, bufferBuilder, random);
            }
            for (BlockLayer blockLayer2 : BlockLayer.values()) {
                if (bls[blockLayer2.ordinal()]) {
                    compiledChunk.setChanged(blockLayer2);
                }
                if (!compiledChunk.hasLayer(blockLayer2)) continue;
                this.preEndLayer(blockLayer2, f, g, h, chunkCompileTask.getBuilders().builder(blockLayer2), compiledChunk);
            }
            ModelBlockRenderer.clearCache();
        }
        compiledChunk.setVisibilitySet(visGraph.resolve());
        this.taskLock.lock();
        try {
            HashSet<BlockEntity> set2 = Sets.newHashSet(set);
            HashSet<BlockEntity> set3 = Sets.newHashSet(this.globalBlockEntities);
            set2.removeAll(this.globalBlockEntities);
            set3.removeAll(set);
            this.globalBlockEntities.clear();
            this.globalBlockEntities.addAll(set);
            this.renderer.updateGlobalBlockEntities(set3, set2);
        } finally {
            this.taskLock.unlock();
        }
    }

    protected void cancelCompile() {
        this.taskLock.lock();
        try {
            if (this.pendingTask != null && this.pendingTask.getStatus() != ChunkCompileTask.Status.DONE) {
                this.pendingTask.cancel();
                this.pendingTask = null;
            }
        } finally {
            this.taskLock.unlock();
        }
    }

    public ReentrantLock getTaskLock() {
        return this.taskLock;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ChunkCompileTask createCompileTask() {
        this.taskLock.lock();
        try {
            this.cancelCompile();
            BlockPos blockPos = this.origin.immutable();
            boolean i = true;
            RenderChunkRegion renderChunkRegion = RenderChunkRegion.createIfNotEmpty(this.level, blockPos.offset(-1, -1, -1), blockPos.offset(16, 16, 16), 1);
            ChunkCompileTask chunkCompileTask = this.pendingTask = new ChunkCompileTask(this, ChunkCompileTask.Type.REBUILD_CHUNK, this.getDistToPlayerSqr(), renderChunkRegion);
            return chunkCompileTask;
        } finally {
            this.taskLock.unlock();
        }
    }

    @Nullable
    public ChunkCompileTask createTransparencySortTask() {
        this.taskLock.lock();
        try {
            if (this.pendingTask != null && this.pendingTask.getStatus() == ChunkCompileTask.Status.PENDING) {
                ChunkCompileTask chunkCompileTask = null;
                return chunkCompileTask;
            }
            if (this.pendingTask != null && this.pendingTask.getStatus() != ChunkCompileTask.Status.DONE) {
                this.pendingTask.cancel();
                this.pendingTask = null;
            }
            this.pendingTask = new ChunkCompileTask(this, ChunkCompileTask.Type.RESORT_TRANSPARENCY, this.getDistToPlayerSqr(), null);
            this.pendingTask.setCompiledChunk(this.compiled);
            ChunkCompileTask chunkCompileTask = this.pendingTask;
            return chunkCompileTask;
        } finally {
            this.taskLock.unlock();
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
        bufferBuilder.offset(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());
    }

    private void preEndLayer(BlockLayer blockLayer, float f, float g, float h, BufferBuilder bufferBuilder, CompiledChunk compiledChunk) {
        if (blockLayer == BlockLayer.TRANSLUCENT && !compiledChunk.isEmpty(blockLayer)) {
            bufferBuilder.sortQuads(f, g, h);
            compiledChunk.setTransparencyState(bufferBuilder.getState());
        }
        bufferBuilder.end();
    }

    public CompiledChunk getCompiledChunk() {
        return this.compiled;
    }

    public void setCompiledChunk(CompiledChunk compiledChunk) {
        this.compileLock.lock();
        try {
            this.compiled = compiledChunk;
        } finally {
            this.compileLock.unlock();
        }
    }

    public void reset() {
        this.cancelCompile();
        this.compiled = CompiledChunk.UNCOMPILED;
        this.dirty = true;
    }

    public void releaseBuffers() {
        this.reset();
        this.level = null;
        for (int i = 0; i < BlockLayer.values().length; ++i) {
            if (this.buffers[i] == null) continue;
            this.buffers[i].delete();
        }
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public void setDirty(boolean bl) {
        if (this.dirty) {
            bl |= this.playerChanged;
        }
        this.dirty = true;
        this.playerChanged = bl;
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

    public Level getLevel() {
        return this.level;
    }
}

