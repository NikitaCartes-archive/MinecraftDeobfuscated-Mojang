package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.feature.Feature;

public class ProtectedBlockProcessor extends StructureProcessor {
	public final ResourceLocation cannotReplace;
	public static final Codec<ProtectedBlockProcessor> CODEC = ResourceLocation.CODEC
		.xmap(ProtectedBlockProcessor::new, protectedBlockProcessor -> protectedBlockProcessor.cannotReplace);

	public ProtectedBlockProcessor(ResourceLocation resourceLocation) {
		this.cannotReplace = resourceLocation;
	}

	@Nullable
	@Override
	public StructureTemplate.StructureBlockInfo processBlock(
		LevelReader levelReader,
		BlockPos blockPos,
		BlockPos blockPos2,
		StructureTemplate.StructureBlockInfo structureBlockInfo,
		StructureTemplate.StructureBlockInfo structureBlockInfo2,
		StructurePlaceSettings structurePlaceSettings
	) {
		return Feature.isReplaceable(this.cannotReplace).test(levelReader.getBlockState(structureBlockInfo2.pos)) ? structureBlockInfo2 : null;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return StructureProcessorType.PROTECTED_BLOCKS;
	}
}
