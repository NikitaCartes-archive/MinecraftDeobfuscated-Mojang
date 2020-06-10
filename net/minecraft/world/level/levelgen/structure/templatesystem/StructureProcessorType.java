/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlackstoneReplaceProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LavaSubmergedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.NopProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public interface StructureProcessorType<P extends StructureProcessor> {
    public static final StructureProcessorType<BlockIgnoreProcessor> BLOCK_IGNORE = StructureProcessorType.register("block_ignore", BlockIgnoreProcessor.CODEC);
    public static final StructureProcessorType<BlockRotProcessor> BLOCK_ROT = StructureProcessorType.register("block_rot", BlockRotProcessor.CODEC);
    public static final StructureProcessorType<GravityProcessor> GRAVITY = StructureProcessorType.register("gravity", GravityProcessor.CODEC);
    public static final StructureProcessorType<JigsawReplacementProcessor> JIGSAW_REPLACEMENT = StructureProcessorType.register("jigsaw_replacement", JigsawReplacementProcessor.CODEC);
    public static final StructureProcessorType<RuleProcessor> RULE = StructureProcessorType.register("rule", RuleProcessor.CODEC);
    public static final StructureProcessorType<NopProcessor> NOP = StructureProcessorType.register("nop", NopProcessor.CODEC);
    public static final StructureProcessorType<BlockAgeProcessor> BLOCK_AGE = StructureProcessorType.register("block_age", BlockAgeProcessor.CODEC);
    public static final StructureProcessorType<BlackstoneReplaceProcessor> BLACKSTONE_REPLACE = StructureProcessorType.register("blackstone_replace", BlackstoneReplaceProcessor.CODEC);
    public static final StructureProcessorType<LavaSubmergedBlockProcessor> LAVA_SUBMERGED_BLOCK = StructureProcessorType.register("lava_submerged_block", LavaSubmergedBlockProcessor.CODEC);
    public static final Codec<StructureProcessor> CODEC = Registry.STRUCTURE_PROCESSOR.dispatch("processor_type", StructureProcessor::getType, StructureProcessorType::codec);

    public Codec<P> codec();

    public static <P extends StructureProcessor> StructureProcessorType<P> register(String string, Codec<P> codec) {
        return Registry.register(Registry.STRUCTURE_PROCESSOR, string, () -> codec);
    }
}

