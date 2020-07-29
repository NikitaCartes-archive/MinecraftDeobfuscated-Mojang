/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

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
    public static final Codec<StructureProcessor> SINGLE_CODEC = Registry.STRUCTURE_PROCESSOR.dispatch("processor_type", StructureProcessor::getType, StructureProcessorType::codec);
    public static final Codec<StructureProcessorList> LIST_OBJECT_CODEC = SINGLE_CODEC.listOf().xmap(StructureProcessorList::new, StructureProcessorList::list);
    public static final Codec<StructureProcessorList> DIRECT_CODEC = Codec.either(((MapCodec)LIST_OBJECT_CODEC.fieldOf("processors")).codec(), LIST_OBJECT_CODEC).xmap(either -> either.map(structureProcessorList -> structureProcessorList, structureProcessorList -> structureProcessorList), Either::left);
    public static final Codec<Supplier<StructureProcessorList>> LIST_CODEC = RegistryFileCodec.create(Registry.PROCESSOR_LIST_REGISTRY, DIRECT_CODEC);

    public Codec<P> codec();

    public static <P extends StructureProcessor> StructureProcessorType<P> register(String string, Codec<P> codec) {
        return Registry.register(Registry.STRUCTURE_PROCESSOR, string, () -> codec);
    }
}

