package net.minecraft.client.renderer.chunk;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

@Environment(EnvType.CLIENT)
public class RenderChunkRegion implements BlockAndTintGetter {
	protected final int centerX;
	protected final int centerZ;
	protected final BlockPos start;
	protected final int xLength;
	protected final int yLength;
	protected final int zLength;
	protected final LevelChunk[][] chunks;
	protected final BlockState[] blockStates;
	protected final FluidState[] fluidStates;
	protected final Level level;

	@Nullable
	public static RenderChunkRegion createIfNotEmpty(Level level, BlockPos blockPos, BlockPos blockPos2, int i) {
		int j = blockPos.getX() - i >> 4;
		int k = blockPos.getZ() - i >> 4;
		int l = blockPos2.getX() + i >> 4;
		int m = blockPos2.getZ() + i >> 4;
		LevelChunk[][] levelChunks = new LevelChunk[l - j + 1][m - k + 1];

		for (int n = j; n <= l; n++) {
			for (int o = k; o <= m; o++) {
				levelChunks[n - j][o - k] = level.getChunk(n, o);
			}
		}

		boolean bl = true;

		for (int o = blockPos.getX() >> 4; o <= blockPos2.getX() >> 4; o++) {
			for (int p = blockPos.getZ() >> 4; p <= blockPos2.getZ() >> 4; p++) {
				LevelChunk levelChunk = levelChunks[o - j][p - k];
				if (!levelChunk.isYSpaceEmpty(blockPos.getY(), blockPos2.getY())) {
					bl = false;
				}
			}
		}

		if (bl) {
			return null;
		} else {
			int o = 1;
			BlockPos blockPos3 = blockPos.offset(-1, -1, -1);
			BlockPos blockPos4 = blockPos2.offset(1, 1, 1);
			return new RenderChunkRegion(level, j, k, levelChunks, blockPos3, blockPos4);
		}
	}

	public RenderChunkRegion(Level level, int i, int j, LevelChunk[][] levelChunks, BlockPos blockPos, BlockPos blockPos2) {
		this.level = level;
		this.centerX = i;
		this.centerZ = j;
		this.chunks = levelChunks;
		this.start = blockPos;
		this.xLength = blockPos2.getX() - blockPos.getX() + 1;
		this.yLength = blockPos2.getY() - blockPos.getY() + 1;
		this.zLength = blockPos2.getZ() - blockPos.getZ() + 1;
		this.blockStates = new BlockState[this.xLength * this.yLength * this.zLength];
		this.fluidStates = new FluidState[this.xLength * this.yLength * this.zLength];

		for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
			int k = (blockPos3.getX() >> 4) - i;
			int l = (blockPos3.getZ() >> 4) - j;
			LevelChunk levelChunk = levelChunks[k][l];
			int m = this.index(blockPos3);
			this.blockStates[m] = levelChunk.getBlockState(blockPos3);
			this.fluidStates[m] = levelChunk.getFluidState(blockPos3);
		}
	}

	protected final int index(BlockPos blockPos) {
		return this.index(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	protected int index(int i, int j, int k) {
		int l = i - this.start.getX();
		int m = j - this.start.getY();
		int n = k - this.start.getZ();
		return n * this.xLength * this.yLength + m * this.xLength + l;
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		return this.blockStates[this.index(blockPos)];
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		return this.fluidStates[this.index(blockPos)];
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return this.level.getLightEngine();
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		return this.getBlockEntity(blockPos, LevelChunk.EntityCreationType.IMMEDIATE);
	}

	@Nullable
	public BlockEntity getBlockEntity(BlockPos blockPos, LevelChunk.EntityCreationType entityCreationType) {
		int i = (blockPos.getX() >> 4) - this.centerX;
		int j = (blockPos.getZ() >> 4) - this.centerZ;
		return this.chunks[i][j].getBlockEntity(blockPos, entityCreationType);
	}

	@Override
	public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
		return this.level.getBlockTint(blockPos, colorResolver);
	}
}
