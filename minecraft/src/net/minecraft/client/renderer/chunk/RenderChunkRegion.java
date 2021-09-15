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
	protected final LevelChunk[][] chunks;
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

		return isAllEmpty(blockPos, blockPos2, j, k, levelChunks) ? null : new RenderChunkRegion(level, j, k, levelChunks);
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

	public RenderChunkRegion(Level level, int i, int j, LevelChunk[][] levelChunks) {
		this.level = level;
		this.centerX = i;
		this.centerZ = j;
		this.chunks = levelChunks;
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
	public int getMinBuildHeight() {
		return this.level.getMinBuildHeight();
	}

	@Override
	public int getHeight() {
		return this.level.getHeight();
	}
}
