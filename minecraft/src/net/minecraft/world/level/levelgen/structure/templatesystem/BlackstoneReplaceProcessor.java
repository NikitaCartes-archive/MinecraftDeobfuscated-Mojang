package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlackstoneReplaceProcessor extends StructureProcessor {
	public static final Codec<BlackstoneReplaceProcessor> CODEC = Codec.unit((Supplier<BlackstoneReplaceProcessor>)(() -> BlackstoneReplaceProcessor.INSTANCE));
	public static final BlackstoneReplaceProcessor INSTANCE = new BlackstoneReplaceProcessor();
	private final Map<Block, Block> replacements = Util.make(Maps.<Block, Block>newHashMap(), hashMap -> {
		hashMap.put(Blocks.COBBLESTONE, Blocks.BLACKSTONE);
		hashMap.put(Blocks.MOSSY_COBBLESTONE, Blocks.BLACKSTONE);
		hashMap.put(Blocks.STONE, Blocks.POLISHED_BLACKSTONE);
		hashMap.put(Blocks.STONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS);
		hashMap.put(Blocks.MOSSY_STONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS);
		hashMap.put(Blocks.COBBLESTONE_STAIRS, Blocks.BLACKSTONE_STAIRS);
		hashMap.put(Blocks.MOSSY_COBBLESTONE_STAIRS, Blocks.BLACKSTONE_STAIRS);
		hashMap.put(Blocks.STONE_STAIRS, Blocks.POLISHED_BLACKSTONE_STAIRS);
		hashMap.put(Blocks.STONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
		hashMap.put(Blocks.MOSSY_STONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
		hashMap.put(Blocks.COBBLESTONE_SLAB, Blocks.BLACKSTONE_SLAB);
		hashMap.put(Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.BLACKSTONE_SLAB);
		hashMap.put(Blocks.SMOOTH_STONE_SLAB, Blocks.POLISHED_BLACKSTONE_SLAB);
		hashMap.put(Blocks.STONE_SLAB, Blocks.POLISHED_BLACKSTONE_SLAB);
		hashMap.put(Blocks.STONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
		hashMap.put(Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
		hashMap.put(Blocks.STONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
		hashMap.put(Blocks.MOSSY_STONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
		hashMap.put(Blocks.COBBLESTONE_WALL, Blocks.BLACKSTONE_WALL);
		hashMap.put(Blocks.MOSSY_COBBLESTONE_WALL, Blocks.BLACKSTONE_WALL);
		hashMap.put(Blocks.CHISELED_STONE_BRICKS, Blocks.CHISELED_POLISHED_BLACKSTONE);
		hashMap.put(Blocks.CRACKED_STONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
		hashMap.put(Blocks.IRON_BARS, Blocks.CHAIN);
	});

	private BlackstoneReplaceProcessor() {
	}

	@Override
	public StructureTemplate.StructureBlockInfo processBlock(
		LevelReader levelReader,
		BlockPos blockPos,
		BlockPos blockPos2,
		StructureTemplate.StructureBlockInfo structureBlockInfo,
		StructureTemplate.StructureBlockInfo structureBlockInfo2,
		StructurePlaceSettings structurePlaceSettings
	) {
		Block block = (Block)this.replacements.get(structureBlockInfo2.state.getBlock());
		if (block == null) {
			return structureBlockInfo2;
		} else {
			BlockState blockState = structureBlockInfo2.state;
			BlockState blockState2 = block.defaultBlockState();
			if (blockState.hasProperty(StairBlock.FACING)) {
				blockState2 = blockState2.setValue(StairBlock.FACING, blockState.getValue(StairBlock.FACING));
			}

			if (blockState.hasProperty(StairBlock.HALF)) {
				blockState2 = blockState2.setValue(StairBlock.HALF, blockState.getValue(StairBlock.HALF));
			}

			if (blockState.hasProperty(SlabBlock.TYPE)) {
				blockState2 = blockState2.setValue(SlabBlock.TYPE, blockState.getValue(SlabBlock.TYPE));
			}

			return new StructureTemplate.StructureBlockInfo(structureBlockInfo2.pos, blockState2, structureBlockInfo2.nbt);
		}
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return StructureProcessorType.BLACKSTONE_REPLACE;
	}
}
