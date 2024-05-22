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
	public static final int RADIUS = 1;
	public static final int SIZE = 3;
	private final int minChunkX;
	private final int minChunkZ;
	protected final RenderChunk[] chunks;
	protected final Level level;

	RenderChunkRegion(Level level, int i, int j, RenderChunk[] renderChunks) {
		this.level = level;
		this.minChunkX = i;
		this.minChunkZ = j;
		this.chunks = renderChunks;
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		return this.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ())).getBlockState(blockPos);
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		return this.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()))
			.getBlockState(blockPos)
			.getFluidState();
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
		return this.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ())).getBlockEntity(blockPos);
	}

	private RenderChunk getChunk(int i, int j) {
		return this.chunks[index(this.minChunkX, this.minChunkZ, i, j)];
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

	public static int index(int i, int j, int k, int l) {
		return k - i + (l - j) * 3;
	}
}
