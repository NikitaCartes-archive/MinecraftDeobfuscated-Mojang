/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public class LavaSubmergedBlockProcessor
extends StructureProcessor {
    public static final Codec<LavaSubmergedBlockProcessor> CODEC = Codec.unit(() -> INSTANCE);
    public static final LavaSubmergedBlockProcessor INSTANCE = new LavaSubmergedBlockProcessor();

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        BlockPos blockPos3 = structureBlockInfo2.pos;
        boolean bl = levelReader.getBlockState(blockPos3).is(Blocks.LAVA);
        if (bl && !Block.isShapeFullBlock(structureBlockInfo2.state.getShape(levelReader, blockPos3))) {
            return new StructureTemplate.StructureBlockInfo(blockPos3, Blocks.LAVA.defaultBlockState(), structureBlockInfo2.nbt);
        }
        return structureBlockInfo2;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.LAVA_SUBMERGED_BLOCK;
    }
}

