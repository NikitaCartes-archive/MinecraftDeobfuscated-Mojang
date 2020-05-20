/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public class JigsawReplacementProcessor
extends StructureProcessor {
    public static final Codec<JigsawReplacementProcessor> CODEC = Codec.unit(() -> INSTANCE);
    public static final JigsawReplacementProcessor INSTANCE = new JigsawReplacementProcessor();

    private JigsawReplacementProcessor() {
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        BlockState blockState = structureBlockInfo2.state;
        if (!blockState.is(Blocks.JIGSAW)) {
            return structureBlockInfo2;
        }
        String string = structureBlockInfo2.nbt.getString("final_state");
        BlockStateParser blockStateParser = new BlockStateParser(new StringReader(string), false);
        try {
            blockStateParser.parse(true);
        } catch (CommandSyntaxException commandSyntaxException) {
            throw new RuntimeException(commandSyntaxException);
        }
        if (blockStateParser.getState().is(Blocks.STRUCTURE_VOID)) {
            return null;
        }
        return new StructureTemplate.StructureBlockInfo(structureBlockInfo2.pos, blockStateParser.getState(), null);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.JIGSAW_REPLACEMENT;
    }
}

