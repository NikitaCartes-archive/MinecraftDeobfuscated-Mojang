package net.minecraft.world.level.levelgen.structure.templatesystem;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;

public abstract class StructureProcessor {
	@Nullable
	public abstract StructureTemplate.StructureBlockInfo processBlock(
		LevelReader levelReader,
		BlockPos blockPos,
		BlockPos blockPos2,
		StructureTemplate.StructureBlockInfo structureBlockInfo,
		StructureTemplate.StructureBlockInfo structureBlockInfo2,
		StructurePlaceSettings structurePlaceSettings
	);

	protected abstract StructureProcessorType<?> getType();

	public void finalizeStructure(
		LevelAccessor levelAccessor,
		BlockPos blockPos,
		BlockPos blockPos2,
		StructurePlaceSettings structurePlaceSettings,
		List<StructureTemplate.StructureBlockInfo> list
	) {
	}
}
