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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.DebugLevelSource;

@Environment(EnvType.CLIENT)
class RenderChunk {
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
