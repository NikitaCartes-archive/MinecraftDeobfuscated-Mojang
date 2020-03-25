package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
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

	protected abstract StructureProcessorType getType();

	protected abstract <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps);

	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.mergeInto(
				this.getDynamic(dynamicOps).getValue(),
				dynamicOps.createString("processor_type"),
				dynamicOps.createString(Registry.STRUCTURE_PROCESSOR.getKey(this.getType()).toString())
			)
		);
	}
}
