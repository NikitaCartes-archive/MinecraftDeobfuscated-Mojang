package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;

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
	Codec<StructureProcessor> SINGLE_CODEC = Registry.STRUCTURE_PROCESSOR.dispatch("processor_type", StructureProcessor::getType, StructureProcessorType::codec);
	MapCodec<ImmutableList<StructureProcessor>> DIRECT_CODEC = SINGLE_CODEC.listOf()
		.<ImmutableList<StructureProcessor>>xmap(ImmutableList::copyOf, Function.identity())
		.fieldOf("processors");
	Codec<Supplier<ImmutableList<StructureProcessor>>> LIST_CODEC = RegistryFileCodec.create(Registry.PROCESSOR_LIST_REGISTRY, DIRECT_CODEC);

	Codec<P> codec();

	static <P extends StructureProcessor> StructureProcessorType<P> register(String string, Codec<P> codec) {
		return Registry.register(Registry.STRUCTURE_PROCESSOR, string, () -> codec);
	}
}
