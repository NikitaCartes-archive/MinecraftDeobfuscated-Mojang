package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public interface StructureProcessorType<P extends StructureProcessor> {
	StructureProcessorType<BlockIgnoreProcessor> BLOCK_IGNORE = register("block_ignore", BlockIgnoreProcessor.CODEC);
	StructureProcessorType<BlockRotProcessor> BLOCK_ROT = register("block_rot", BlockRotProcessor.CODEC);
	StructureProcessorType<GravityProcessor> GRAVITY = register("gravity", GravityProcessor.CODEC);
	StructureProcessorType<JigsawReplacementProcessor> JIGSAW_REPLACEMENT = register("jigsaw_replacement", JigsawReplacementProcessor.CODEC);
	StructureProcessorType<RuleProcessor> RULE = register("rule", RuleProcessor.CODEC);
	StructureProcessorType<NopProcessor> NOP = register("nop", NopProcessor.CODEC);
	StructureProcessorType<BlockAgeProcessor> BLOCK_AGE = register("block_age", BlockAgeProcessor.CODEC);
	StructureProcessorType<BlackstoneReplaceProcessor> BLACKSTONE_REPLACE = register("blackstone_replace", BlackstoneReplaceProcessor.CODEC);
	StructureProcessorType<LavaSubmergedBlockProcessor> LAVA_SUBMERGED_BLOCK = register("lava_submerged_block", LavaSubmergedBlockProcessor.CODEC);
	Codec<StructureProcessor> CODEC = Registry.STRUCTURE_PROCESSOR.dispatch("processor_type", StructureProcessor::getType, StructureProcessorType::codec);

	Codec<P> codec();

	static <P extends StructureProcessor> StructureProcessorType<P> register(String string, Codec<P> codec) {
		return Registry.register(Registry.STRUCTURE_PROCESSOR, string, () -> codec);
	}
}
