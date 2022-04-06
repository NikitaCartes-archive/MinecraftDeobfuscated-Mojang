/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public class BlockAgeProcessor
extends StructureProcessor {
    public static final Codec<BlockAgeProcessor> CODEC = ((MapCodec)Codec.FLOAT.fieldOf("mossiness")).xmap(BlockAgeProcessor::new, blockAgeProcessor -> Float.valueOf(blockAgeProcessor.mossiness)).codec();
    private static final float PROBABILITY_OF_REPLACING_FULL_BLOCK = 0.5f;
    private static final float PROBABILITY_OF_REPLACING_STAIRS = 0.5f;
    private static final float PROBABILITY_OF_REPLACING_OBSIDIAN = 0.15f;
    private static final BlockState[] NON_MOSSY_REPLACEMENTS = new BlockState[]{Blocks.STONE_SLAB.defaultBlockState(), Blocks.STONE_BRICK_SLAB.defaultBlockState()};
    private final float mossiness;

    public BlockAgeProcessor(float f) {
        this.mossiness = f;
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        RandomSource randomSource = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        BlockState blockState = structureBlockInfo2.state;
        BlockPos blockPos3 = structureBlockInfo2.pos;
        BlockState blockState2 = null;
        if (blockState.is(Blocks.STONE_BRICKS) || blockState.is(Blocks.STONE) || blockState.is(Blocks.CHISELED_STONE_BRICKS)) {
            blockState2 = this.maybeReplaceFullStoneBlock(randomSource);
        } else if (blockState.is(BlockTags.STAIRS)) {
            blockState2 = this.maybeReplaceStairs(randomSource, structureBlockInfo2.state);
        } else if (blockState.is(BlockTags.SLABS)) {
            blockState2 = this.maybeReplaceSlab(randomSource);
        } else if (blockState.is(BlockTags.WALLS)) {
            blockState2 = this.maybeReplaceWall(randomSource);
        } else if (blockState.is(Blocks.OBSIDIAN)) {
            blockState2 = this.maybeReplaceObsidian(randomSource);
        }
        if (blockState2 != null) {
            return new StructureTemplate.StructureBlockInfo(blockPos3, blockState2, structureBlockInfo2.nbt);
        }
        return structureBlockInfo2;
    }

    @Nullable
    private BlockState maybeReplaceFullStoneBlock(RandomSource randomSource) {
        if (randomSource.nextFloat() >= 0.5f) {
            return null;
        }
        BlockState[] blockStates = new BlockState[]{Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), BlockAgeProcessor.getRandomFacingStairs(randomSource, Blocks.STONE_BRICK_STAIRS)};
        BlockState[] blockStates2 = new BlockState[]{Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), BlockAgeProcessor.getRandomFacingStairs(randomSource, Blocks.MOSSY_STONE_BRICK_STAIRS)};
        return this.getRandomBlock(randomSource, blockStates, blockStates2);
    }

    @Nullable
    private BlockState maybeReplaceStairs(RandomSource randomSource, BlockState blockState) {
        Direction direction = blockState.getValue(StairBlock.FACING);
        Half half = blockState.getValue(StairBlock.HALF);
        if (randomSource.nextFloat() >= 0.5f) {
            return null;
        }
        BlockState[] blockStates = new BlockState[]{(BlockState)((BlockState)Blocks.MOSSY_STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, direction)).setValue(StairBlock.HALF, half), Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState()};
        return this.getRandomBlock(randomSource, NON_MOSSY_REPLACEMENTS, blockStates);
    }

    @Nullable
    private BlockState maybeReplaceSlab(RandomSource randomSource) {
        if (randomSource.nextFloat() < this.mossiness) {
            return Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState();
        }
        return null;
    }

    @Nullable
    private BlockState maybeReplaceWall(RandomSource randomSource) {
        if (randomSource.nextFloat() < this.mossiness) {
            return Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState();
        }
        return null;
    }

    @Nullable
    private BlockState maybeReplaceObsidian(RandomSource randomSource) {
        if (randomSource.nextFloat() < 0.15f) {
            return Blocks.CRYING_OBSIDIAN.defaultBlockState();
        }
        return null;
    }

    private static BlockState getRandomFacingStairs(RandomSource randomSource, Block block) {
        return (BlockState)((BlockState)block.defaultBlockState().setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(randomSource))).setValue(StairBlock.HALF, Half.values()[randomSource.nextInt(Half.values().length)]);
    }

    private BlockState getRandomBlock(RandomSource randomSource, BlockState[] blockStates, BlockState[] blockStates2) {
        if (randomSource.nextFloat() < this.mossiness) {
            return BlockAgeProcessor.getRandomBlock(randomSource, blockStates2);
        }
        return BlockAgeProcessor.getRandomBlock(randomSource, blockStates);
    }

    private static BlockState getRandomBlock(RandomSource randomSource, BlockState[] blockStates) {
        return blockStates[randomSource.nextInt(blockStates.length)];
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_AGE;
    }
}

