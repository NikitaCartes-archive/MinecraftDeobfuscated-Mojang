package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
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

@Environment(EnvType.CLIENT)
public class RenderChunk {
	private volatile Level level;
	private final LevelRenderer renderer;
	public static int updateCounter;
	public CompiledChunk compiled = CompiledChunk.UNCOMPILED;
	private final ReentrantLock taskLock = new ReentrantLock();
	private final ReentrantLock compileLock = new ReentrantLock();
	private ChunkCompileTask pendingTask;
	private final Set<BlockEntity> globalBlockEntities = Sets.<BlockEntity>newHashSet();
	private final VertexBuffer[] buffers = new VertexBuffer[BlockLayer.values().length];
	public AABB bb;
	private int lastFrame = -1;
	private boolean dirty = true;
	private final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
	private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], mutableBlockPoss -> {
		for (int ix = 0; ix < mutableBlockPoss.length; ix++) {
			mutableBlockPoss[ix] = new BlockPos.MutableBlockPos();
		}
	});
	private boolean playerChanged;

	public RenderChunk(Level level, LevelRenderer levelRenderer) {
		this.level = level;
		this.renderer = levelRenderer;
		if (GLX.useVbo()) {
			for (int i = 0; i < BlockLayer.values().length; i++) {
				this.buffers[i] = new VertexBuffer(DefaultVertexFormat.BLOCK);
			}
		}
	}

	private static boolean doesChunkExistAt(BlockPos blockPos, Level level) {
		return !level.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4).isEmpty();
	}

	public boolean hasAllNeighbors() {
		int i = 24;
		if (!(this.getDistToPlayerSqr() > 576.0)) {
			return true;
		} else {
			Level level = this.getLevel();
			return doesChunkExistAt(this.relativeOrigins[Direction.WEST.ordinal()], level)
				&& doesChunkExistAt(this.relativeOrigins[Direction.NORTH.ordinal()], level)
				&& doesChunkExistAt(this.relativeOrigins[Direction.EAST.ordinal()], level)
				&& doesChunkExistAt(this.relativeOrigins[Direction.SOUTH.ordinal()], level);
		}
	}

	public boolean setFrame(int i) {
		if (this.lastFrame == i) {
			return false;
		} else {
			this.lastFrame = i;
			return true;
		}
	}

	public VertexBuffer getBuffer(int i) {
		return this.buffers[i];
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

	public void rebuildTransparent(float f, float g, float h, ChunkCompileTask chunkCompileTask) {
		CompiledChunk compiledChunk = chunkCompileTask.getCompiledChunk();
		if (compiledChunk.getTransparencyState() != null && !compiledChunk.isEmpty(BlockLayer.TRANSLUCENT)) {
			this.beginLayer(chunkCompileTask.getBuilders().builder(BlockLayer.TRANSLUCENT), this.origin);
			chunkCompileTask.getBuilders().builder(BlockLayer.TRANSLUCENT).restoreState(compiledChunk.getTransparencyState());
			this.preEndLayer(BlockLayer.TRANSLUCENT, f, g, h, chunkCompileTask.getBuilders().builder(BlockLayer.TRANSLUCENT), compiledChunk);
		}
	}

	public void compile(float f, float g, float h, ChunkCompileTask chunkCompileTask) {
		CompiledChunk compiledChunk = new CompiledChunk();
		int i = 1;
		BlockPos blockPos = this.origin.immutable();
		BlockPos blockPos2 = blockPos.offset(15, 15, 15);
		Level level = this.level;
		if (level != null) {
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
			HashSet set = Sets.newHashSet();
			RenderChunkRegion renderChunkRegion = chunkCompileTask.takeRegion();
			if (renderChunkRegion != null) {
				updateCounter++;
				boolean[] bls = new boolean[BlockLayer.values().length];
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
								compiledChunk.addRenderableBlockEntity(blockEntity);
								if (blockEntityRenderer.shouldRenderOffScreen(blockEntity)) {
									set.add(blockEntity);
								}
							}
						}
					}

					FluidState fluidState = renderChunkRegion.getFluidState(blockPos3);
					if (!fluidState.isEmpty()) {
						BlockLayer blockLayer = fluidState.getRenderLayer();
						int j = blockLayer.ordinal();
						BufferBuilder bufferBuilder = chunkCompileTask.getBuilders().builder(j);
						if (!compiledChunk.hasLayer(blockLayer)) {
							compiledChunk.layerIsPresent(blockLayer);
							this.beginLayer(bufferBuilder, blockPos);
						}

						bls[j] |= blockRenderDispatcher.renderLiquid(blockPos3, renderChunkRegion, bufferBuilder, fluidState);
					}

					if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
						BlockLayer blockLayer = block.getRenderLayer();
						int j = blockLayer.ordinal();
						BufferBuilder bufferBuilder = chunkCompileTask.getBuilders().builder(j);
						if (!compiledChunk.hasLayer(blockLayer)) {
							compiledChunk.layerIsPresent(blockLayer);
							this.beginLayer(bufferBuilder, blockPos);
						}

						bls[j] |= blockRenderDispatcher.renderBatched(blockState, blockPos3, renderChunkRegion, bufferBuilder, random);
					}
				}

				for (BlockLayer blockLayer2 : BlockLayer.values()) {
					if (bls[blockLayer2.ordinal()]) {
						compiledChunk.setChanged(blockLayer2);
					}

					if (compiledChunk.hasLayer(blockLayer2)) {
						this.preEndLayer(blockLayer2, f, g, h, chunkCompileTask.getBuilders().builder(blockLayer2), compiledChunk);
					}
				}

				ModelBlockRenderer.clearCache();
			}

			compiledChunk.setVisibilitySet(visGraph.resolve());
			this.taskLock.lock();

			try {
				Set<BlockEntity> set2 = Sets.<BlockEntity>newHashSet(set);
				Set<BlockEntity> set3 = Sets.<BlockEntity>newHashSet(this.globalBlockEntities);
				set2.removeAll(this.globalBlockEntities);
				set3.removeAll(set);
				this.globalBlockEntities.clear();
				this.globalBlockEntities.addAll(set);
				this.renderer.updateGlobalBlockEntities(set3, set2);
			} finally {
				this.taskLock.unlock();
			}
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

	public ChunkCompileTask createCompileTask() {
		this.taskLock.lock();

		ChunkCompileTask var4;
		try {
			this.cancelCompile();
			BlockPos blockPos = this.origin.immutable();
			int i = 1;
			RenderChunkRegion renderChunkRegion = RenderChunkRegion.createIfNotEmpty(this.level, blockPos.offset(-1, -1, -1), blockPos.offset(16, 16, 16), 1);
			this.pendingTask = new ChunkCompileTask(this, ChunkCompileTask.Type.REBUILD_CHUNK, this.getDistToPlayerSqr(), renderChunkRegion);
			var4 = this.pendingTask;
		} finally {
			this.taskLock.unlock();
		}

		return var4;
	}

	@Nullable
	public ChunkCompileTask createTransparencySortTask() {
		this.taskLock.lock();

		Object var1;
		try {
			if (this.pendingTask == null || this.pendingTask.getStatus() != ChunkCompileTask.Status.PENDING) {
				if (this.pendingTask != null && this.pendingTask.getStatus() != ChunkCompileTask.Status.DONE) {
					this.pendingTask.cancel();
					this.pendingTask = null;
				}

				this.pendingTask = new ChunkCompileTask(this, ChunkCompileTask.Type.RESORT_TRANSPARENCY, this.getDistToPlayerSqr(), null);
				this.pendingTask.setCompiledChunk(this.compiled);
				return this.pendingTask;
			}

			var1 = null;
		} finally {
			this.taskLock.unlock();
		}

		return (ChunkCompileTask)var1;
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

		for (int i = 0; i < BlockLayer.values().length; i++) {
			if (this.buffers[i] != null) {
				this.buffers[i].delete();
			}
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
