package net.minecraft.world.level.levelgen.structure.templatesystem;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class StructureProcessor {
	@Nullable
	public StructureTemplate.StructureBlockInfo processBlock(
		LevelReader levelReader,
		BlockPos blockPos,
		BlockPos blockPos2,
		StructureTemplate.StructureBlockInfo structureBlockInfo,
		StructureTemplate.StructureBlockInfo structureBlockInfo2,
		StructurePlaceSettings structurePlaceSettings
	) {
		return structureBlockInfo2;
	}

	protected abstract StructureProcessorType<?> getType();

	public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
		ServerLevelAccessor serverLevelAccessor,
		BlockPos blockPos,
		BlockPos blockPos2,
		List<StructureTemplate.StructureBlockInfo> list,
		List<StructureTemplate.StructureBlockInfo> list2,
		StructurePlaceSettings structurePlaceSettings
	) {
		return list2;
	}
}
