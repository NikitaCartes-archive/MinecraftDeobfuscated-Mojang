package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;

public interface StructureProcessorType<P extends StructureProcessor> {
	Codec<StructureProcessor> SINGLE_CODEC = BuiltInRegistries.STRUCTURE_PROCESSOR
		.byNameCodec()
		.dispatch("processor_type", StructureProcessor::getType, StructureProcessorType::codec);
	Codec<StructureProcessorList> LIST_OBJECT_CODEC = SINGLE_CODEC.listOf().xmap(StructureProcessorList::new, StructureProcessorList::list);
	Codec<StructureProcessorList> DIRECT_CODEC = Codec.withAlternative(LIST_OBJECT_CODEC.fieldOf("processors").codec(), LIST_OBJECT_CODEC);
	Codec<Holder<StructureProcessorList>> LIST_CODEC = RegistryFileCodec.create(Registries.PROCESSOR_LIST, DIRECT_CODEC);
	StructureProcessorType<BlockIgnoreProcessor> BLOCK_IGNORE = register("block_ignore", BlockIgnoreProcessor.CODEC);
	StructureProcessorType<BlockRotProcessor> BLOCK_ROT = register("block_rot", BlockRotProcessor.CODEC);
	StructureProcessorType<GravityProcessor> GRAVITY = register("gravity", GravityProcessor.CODEC);
	StructureProcessorType<JigsawReplacementProcessor> JIGSAW_REPLACEMENT = register("jigsaw_replacement", JigsawReplacementProcessor.CODEC);
	StructureProcessorType<RuleProcessor> RULE = register("rule", RuleProcessor.CODEC);
	StructureProcessorType<NopProcessor> NOP = register("nop", NopProcessor.CODEC);
	StructureProcessorType<BlockAgeProcessor> BLOCK_AGE = register("block_age", BlockAgeProcessor.CODEC);
	StructureProcessorType<BlackstoneReplaceProcessor> BLACKSTONE_REPLACE = register("blackstone_replace", BlackstoneReplaceProcessor.CODEC);
	StructureProcessorType<LavaSubmergedBlockProcessor> LAVA_SUBMERGED_BLOCK = register("lava_submerged_block", LavaSubmergedBlockProcessor.CODEC);
	StructureProcessorType<ProtectedBlockProcessor> PROTECTED_BLOCKS = register("protected_blocks", ProtectedBlockProcessor.CODEC);
	StructureProcessorType<CappedProcessor> CAPPED = register("capped", CappedProcessor.CODEC);

	MapCodec<P> codec();

	static <P extends StructureProcessor> StructureProcessorType<P> register(String string, MapCodec<P> mapCodec) {
		return Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, string, () -> mapCodec);
	}
}
