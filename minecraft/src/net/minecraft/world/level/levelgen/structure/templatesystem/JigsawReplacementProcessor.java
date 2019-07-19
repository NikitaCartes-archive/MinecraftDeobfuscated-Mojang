package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class JigsawReplacementProcessor extends StructureProcessor {
	public static final JigsawReplacementProcessor INSTANCE = new JigsawReplacementProcessor();

	private JigsawReplacementProcessor() {
	}

	@Nullable
	@Override
	public StructureTemplate.StructureBlockInfo processBlock(
		LevelReader levelReader,
		BlockPos blockPos,
		StructureTemplate.StructureBlockInfo structureBlockInfo,
		StructureTemplate.StructureBlockInfo structureBlockInfo2,
		StructurePlaceSettings structurePlaceSettings
	) {
		Block block = structureBlockInfo2.state.getBlock();
		if (block != Blocks.JIGSAW_BLOCK) {
			return structureBlockInfo2;
		} else {
			String string = structureBlockInfo2.nbt.getString("final_state");
			BlockStateParser blockStateParser = new BlockStateParser(new StringReader(string), false);

			try {
				blockStateParser.parse(true);
			} catch (CommandSyntaxException var10) {
				throw new RuntimeException(var10);
			}

			return blockStateParser.getState().getBlock() == Blocks.STRUCTURE_VOID
				? null
				: new StructureTemplate.StructureBlockInfo(structureBlockInfo2.pos, blockStateParser.getState(), null);
		}
	}

	@Override
	protected StructureProcessorType getType() {
		return StructureProcessorType.JIGSAW_REPLACEMENT;
	}

	@Override
	protected <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.emptyMap());
	}
}
