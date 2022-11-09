package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;

public class ProtectedBlockProcessor extends StructureProcessor {
	public final TagKey<Block> cannotReplace;
	public static final Codec<ProtectedBlockProcessor> CODEC = TagKey.hashedCodec(Registries.BLOCK)
		.xmap(ProtectedBlockProcessor::new, protectedBlockProcessor -> protectedBlockProcessor.cannotReplace);

	public ProtectedBlockProcessor(TagKey<Block> tagKey) {
		this.cannotReplace = tagKey;
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
