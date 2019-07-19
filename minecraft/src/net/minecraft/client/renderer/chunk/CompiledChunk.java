package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.block.entity.BlockEntity;

@Environment(EnvType.CLIENT)
public class CompiledChunk {
	public static final CompiledChunk UNCOMPILED = new CompiledChunk() {
		@Override
		protected void setChanged(BlockLayer blockLayer) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void layerIsPresent(BlockLayer blockLayer) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean facesCanSeeEachother(Direction direction, Direction direction2) {
			return false;
		}
	};
	private final boolean[] hasBlocks = new boolean[BlockLayer.values().length];
	private final boolean[] hasLayer = new boolean[BlockLayer.values().length];
	private boolean isCompletelyEmpty = true;
	private final List<BlockEntity> renderableBlockEntities = Lists.<BlockEntity>newArrayList();
	private VisibilitySet visibilitySet = new VisibilitySet();
	private BufferBuilder.State transparencyState;

	public boolean hasNoRenderableLayers() {
		return this.isCompletelyEmpty;
	}

	protected void setChanged(BlockLayer blockLayer) {
		this.isCompletelyEmpty = false;
		this.hasBlocks[blockLayer.ordinal()] = true;
	}

	public boolean isEmpty(BlockLayer blockLayer) {
		return !this.hasBlocks[blockLayer.ordinal()];
	}

	public void layerIsPresent(BlockLayer blockLayer) {
		this.hasLayer[blockLayer.ordinal()] = true;
	}

	public boolean hasLayer(BlockLayer blockLayer) {
		return this.hasLayer[blockLayer.ordinal()];
	}

	public List<BlockEntity> getRenderableBlockEntities() {
		return this.renderableBlockEntities;
	}

	public void addRenderableBlockEntity(BlockEntity blockEntity) {
		this.renderableBlockEntities.add(blockEntity);
	}

	public boolean facesCanSeeEachother(Direction direction, Direction direction2) {
		return this.visibilitySet.visibilityBetween(direction, direction2);
	}

	public void setVisibilitySet(VisibilitySet visibilitySet) {
		this.visibilitySet = visibilitySet;
	}

	public BufferBuilder.State getTransparencyState() {
		return this.transparencyState;
	}

	public void setTransparencyState(BufferBuilder.State state) {
		this.transparencyState = state;
	}
}
