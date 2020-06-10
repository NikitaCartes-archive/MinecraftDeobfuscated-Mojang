package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class LavaSubmergedBlockProcessor extends StructureProcessor {
	public static final Codec<LavaSubmergedBlockProcessor> CODEC = Codec.unit((Supplier<LavaSubmergedBlockProcessor>)(() -> LavaSubmergedBlockProcessor.INSTANCE));
	public static final LavaSubmergedBlockProcessor INSTANCE = new LavaSubmergedBlockProcessor();

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
		BlockPos blockPos3 = structureBlockInfo2.pos;
		boolean bl = levelReader.getBlockState(blockPos3).is(Blocks.LAVA);
		return bl && !Block.isShapeFullBlock(structureBlockInfo2.state.getShape(levelReader, blockPos3))
			? new StructureTemplate.StructureBlockInfo(blockPos3, Blocks.LAVA.defaultBlockState(), structureBlockInfo2.nbt)
			: structureBlockInfo2;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return StructureProcessorType.LAVA_SUBMERGED_BLOCK;
	}
}
