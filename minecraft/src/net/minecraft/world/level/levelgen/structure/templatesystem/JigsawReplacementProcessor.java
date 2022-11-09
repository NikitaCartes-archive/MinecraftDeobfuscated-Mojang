package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class JigsawReplacementProcessor extends StructureProcessor {
	public static final Codec<JigsawReplacementProcessor> CODEC = Codec.unit((Supplier<JigsawReplacementProcessor>)(() -> JigsawReplacementProcessor.INSTANCE));
	public static final JigsawReplacementProcessor INSTANCE = new JigsawReplacementProcessor();

	private JigsawReplacementProcessor() {
	}

	@Nullable
	@Override
	public StructureTemplate.StructureBlockInfo processBlock(
		LevelReader levelReader,
		BlockPos blockPos,
		BlockPos blockPos2,
		StructureTemplate.StructureBlockInfo structureBlockInfo,
		StructureTemplate.StructureBlockInfo structureBlockInfo2,
		StructurePlaceSettings structurePlaceSettings
	) {
		BlockState blockState = structureBlockInfo2.state;
		if (blockState.is(Blocks.JIGSAW)) {
			String string = structureBlockInfo2.nbt.getString("final_state");

			BlockState blockState2;
			try {
				BlockStateParser.BlockResult blockResult = BlockStateParser.parseForBlock(levelReader.holderLookup(Registries.BLOCK), string, true);
				blockState2 = blockResult.blockState();
			} catch (CommandSyntaxException var11) {
				throw new RuntimeException(var11);
			}

			return blockState2.is(Blocks.STRUCTURE_VOID) ? null : new StructureTemplate.StructureBlockInfo(structureBlockInfo2.pos, blockState2, null);
		} else {
			return structureBlockInfo2;
		}
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return StructureProcessorType.JIGSAW_REPLACEMENT;
	}
}
