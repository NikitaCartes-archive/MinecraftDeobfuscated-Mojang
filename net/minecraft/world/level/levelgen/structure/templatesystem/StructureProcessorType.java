/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import net.minecraft.core.Registry;
import net.minecraft.util.Deserializer;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlackstoneReplaceProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.NopProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public interface StructureProcessorType
extends Deserializer<StructureProcessor> {
    public static final StructureProcessorType BLOCK_IGNORE = StructureProcessorType.register("block_ignore", BlockIgnoreProcessor::new);
    public static final StructureProcessorType BLOCK_ROT = StructureProcessorType.register("block_rot", BlockRotProcessor::new);
    public static final StructureProcessorType GRAVITY = StructureProcessorType.register("gravity", GravityProcessor::new);
    public static final StructureProcessorType JIGSAW_REPLACEMENT = StructureProcessorType.register("jigsaw_replacement", dynamic -> JigsawReplacementProcessor.INSTANCE);
    public static final StructureProcessorType RULE = StructureProcessorType.register("rule", RuleProcessor::new);
    public static final StructureProcessorType NOP = StructureProcessorType.register("nop", dynamic -> NopProcessor.INSTANCE);
    public static final StructureProcessorType BLOCK_AGE = StructureProcessorType.register("block_age", BlockAgeProcessor::new);
    public static final StructureProcessorType BLACKSTONE_REPLACE = StructureProcessorType.register("blackstone_replace", dynamic -> BlackstoneReplaceProcessor.INSTANCE);

    public static StructureProcessorType register(String string, StructureProcessorType structureProcessorType) {
        return Registry.register(Registry.STRUCTURE_PROCESSOR, string, structureProcessorType);
    }
}

