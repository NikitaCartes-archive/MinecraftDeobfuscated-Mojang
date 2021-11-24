package net.minecraft.client.renderer.chunk;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

@Environment(EnvType.CLIENT)
public class RenderChunkRegion implements BlockAndTintGetter {
	private final int centerX;
	private final int centerZ;
	protected final RenderChunkRegion.RenderChunk[][] chunks;
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
			RenderChunkRegion.RenderChunk[][] renderChunks = new RenderChunkRegion.RenderChunk[l - j + 1][m - k + 1];

			for (int o = j; o <= l; o++) {
				for (int p = k; p <= m; p++) {
					LevelChunk levelChunk = levelChunks[o - j][p - k];
					renderChunks[o - j][p - k] = new RenderChunkRegion.RenderChunk(levelChunk);
				}
			}

			return new RenderChunkRegion(level, j, k, renderChunks);
		}
	}

	private static boolean isAllEmpty(BlockPos blockPos, BlockPos blockPos2, int i, int j, LevelChunk[][] levelChunks) {
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

	private RenderChunkRegion(Level level, int i, int j, RenderChunkRegion.RenderChunk[][] renderChunks) {
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

	@Environment(EnvType.CLIENT)
	static final class RenderChunk {
		private final Map<BlockPos, BlockEntity> blockEntities;
		@Nullable
		private final List<PalettedContainer<BlockState>> sections;
		private final boolean debug;
		private final LevelChunk wrapped;

		RenderChunk(LevelChunk levelChunk) {
			this.wrapped = levelChunk;
			this.debug = levelChunk.getLevel().isDebug();
			this.blockEntities = ImmutableMap.copyOf(levelChunk.getBlockEntities());
			if (levelChunk instanceof EmptyLevelChunk) {
				this.sections = null;
			} else {
				LevelChunkSection[] levelChunkSections = levelChunk.getSections();
				this.sections = new ArrayList(levelChunkSections.length);

				for (LevelChunkSection levelChunkSection : levelChunkSections) {
					this.sections.add(levelChunkSection.hasOnlyAir() ? null : levelChunkSection.getStates().copy());
				}
			}
		}

		@Nullable
		public BlockEntity getBlockEntity(BlockPos blockPos) {
			return (BlockEntity)this.blockEntities.get(blockPos);
		}

		public BlockState getBlockState(BlockPos blockPos) {
			int i = blockPos.getX();
			int j = blockPos.getY();
			int k = blockPos.getZ();
			if (this.debug) {
				BlockState blockState = null;
				if (j == 60) {
					blockState = Blocks.BARRIER.defaultBlockState();
				}

				if (j == 70) {
					blockState = DebugLevelSource.getBlockStateFor(i, k);
				}

				return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
			} else if (this.sections == null) {
				return Blocks.AIR.defaultBlockState();
			} else {
				try {
					int l = this.wrapped.getSectionIndex(j);
					if (l >= 0 && l < this.sections.size()) {
						PalettedContainer<BlockState> palettedContainer = (PalettedContainer<BlockState>)this.sections.get(l);
						if (palettedContainer != null) {
							return palettedContainer.get(i & 15, j & 15, k & 15);
						}
					}

					return Blocks.AIR.defaultBlockState();
				} catch (Throwable var8) {
					CrashReport crashReport = CrashReport.forThrowable(var8, "Getting block state");
					CrashReportCategory crashReportCategory = crashReport.addCategory("Block being got");
					crashReportCategory.setDetail("Location", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(this.wrapped, i, j, k)));
					throw new ReportedException(crashReport);
				}
			}
		}
	}
}
