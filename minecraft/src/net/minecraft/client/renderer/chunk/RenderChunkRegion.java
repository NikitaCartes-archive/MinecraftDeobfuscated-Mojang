package net.minecraft.client.renderer.chunk;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

@Environment(EnvType.CLIENT)
public class RenderChunkRegion implements BlockAndTintGetter {
	private final int centerX;
	private final int centerZ;
	protected final RenderChunk[][] chunks;
	protected final Level level;

	RenderChunkRegion(Level level, int i, int j, RenderChunk[][] renderChunks) {
		this.level = level;
		this.centerX = i;
		this.centerZ = j;
		this.chunks = renderChunks;
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		int i = SectionPos.blockToSectionCoord(blockPos.getX()) - this.centerX;
		int j = SectionPos.blockToSectionCoord(blockPos.getZ()) - this.centerZ;
		return this.chunks[i][j].getBlockState(blockPos);
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		int i = SectionPos.blockToSectionCoord(blockPos.getX()) - this.centerX;
		int j = SectionPos.blockToSectionCoord(blockPos.getZ()) - this.centerZ;
		return this.chunks[i][j].getBlockState(blockPos).getFluidState();
	}

	@Override
	public float getShade(Direction direction, boolean bl) {
		return this.level.getShade(direction, bl);
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return this.level.getLightEngine();
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		int i = SectionPos.blockToSectionCoord(blockPos.getX()) - this.centerX;
		int j = SectionPos.blockToSectionCoord(blockPos.getZ()) - this.centerZ;
		return this.chunks[i][j].getBlockEntity(blockPos);
	}

	@Override
	public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
		return this.level.getBlockTint(blockPos, colorResolver);
	}

	@Override
	public int getMinBuildHeight() {
		return this.level.getMinBuildHeight();
	}

	@Override
	public int getHeight() {
		return this.level.getHeight();
	}
}
