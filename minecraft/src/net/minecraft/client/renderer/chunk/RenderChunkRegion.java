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
		int j = SectionPos.blockToSectionCoord(blockPos.getX() - i);
		int k = SectionPos.blockToSectionCoord(blockPos.getZ() - i);
		int l = SectionPos.blockToSectionCoord(blockPos2.getX() + i);
		int m = SectionPos.blockToSectionCoord(blockPos2.getZ() + i);
		LevelChunk[][] levelChunks = new LevelChunk[l - j + 1][m - k + 1];

		for (int n = j; n <= l; n++) {
			for (int o = k; o <= m; o++) {
				levelChunks[n - j][o - k] = level.getChunk(n, o);
			}
		}

		if (isAllEmpty(blockPos, blockPos2, j, k, levelChunks)) {
			return null;
		} else {
			int n = 1;
			BlockPos blockPos3 = blockPos.offset(-1, -1, -1);
			BlockPos blockPos4 = blockPos2.offset(1, 1, 1);
			return new RenderChunkRegion(level, j, k, levelChunks, blockPos3, blockPos4);
		}
	}

	public static boolean isAllEmpty(BlockPos blockPos, BlockPos blockPos2, int i, int j, LevelChunk[][] levelChunks) {
		for (int k = SectionPos.blockToSectionCoord(blockPos.getX()); k <= SectionPos.blockToSectionCoord(blockPos2.getX()); k++) {
			for (int l = SectionPos.blockToSectionCoord(blockPos.getZ()); l <= SectionPos.blockToSectionCoord(blockPos2.getZ()); l++) {
				LevelChunk levelChunk = levelChunks[k - i][l - j];
				if (!levelChunk.isYSpaceEmpty(blockPos.getY(), blockPos2.getY())) {
					return false;
				}
			}
		}

		return true;
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
			int k = SectionPos.blockToSectionCoord(blockPos3.getX()) - i;
			int l = SectionPos.blockToSectionCoord(blockPos3.getZ()) - j;
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
		return this.getBlockEntity(blockPos, LevelChunk.EntityCreationType.IMMEDIATE);
	}

	@Nullable
	public BlockEntity getBlockEntity(BlockPos blockPos, LevelChunk.EntityCreationType entityCreationType) {
		int i = SectionPos.blockToSectionCoord(blockPos.getX()) - this.centerX;
		int j = SectionPos.blockToSectionCoord(blockPos.getZ()) - this.centerZ;
		return this.chunks[i][j].getBlockEntity(blockPos, entityCreationType);
	}

	@Override
	public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
		return this.level.getBlockTint(blockPos, colorResolver);
	}

	@Override
	public int getSectionsCount() {
		return this.level.getSectionsCount();
	}

	@Override
	public int getMinSection() {
		return this.level.getMinSection();
	}
}
