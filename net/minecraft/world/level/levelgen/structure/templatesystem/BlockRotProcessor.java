/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public class BlockRotProcessor
extends StructureProcessor {
    public static final Codec<BlockRotProcessor> CODEC = RecordCodecBuilder.create(instance -> instance.group(TagKey.codec(Registry.BLOCK_REGISTRY).optionalFieldOf("rottable_blocks").forGetter(blockRotProcessor -> blockRotProcessor.rottableBlocks), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("integrity")).forGetter(blockRotProcessor -> Float.valueOf(blockRotProcessor.integrity))).apply((Applicative<BlockRotProcessor, ?>)instance, BlockRotProcessor::new));
    private Optional<TagKey<Block>> rottableBlocks;
    private final float integrity;

    public BlockRotProcessor(TagKey<Block> tagKey, float f) {
        this(Optional.of(tagKey), f);
    }

    public BlockRotProcessor(float f) {
        this(Optional.empty(), f);
    }

    private BlockRotProcessor(Optional<TagKey<Block>> optional, float f) {
        this.integrity = f;
        this.rottableBlocks = optional;
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        Random random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        if (this.rottableBlocks.isPresent() && !structureBlockInfo.state.is(this.rottableBlocks.get()) || random.nextFloat() <= this.integrity) {
            return structureBlockInfo2;
        }
        return null;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_ROT;
    }
}

