package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

public class ReplaceInTagProcessor extends StructureProcessor {
	public static final Codec<ReplaceInTagProcessor> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Tag.codec(() -> SerializationTags.getInstance().getOrEmpty(Registry.BLOCK_REGISTRY))
						.fieldOf("rottable_blocks")
						.forGetter(replaceInTagProcessor -> replaceInTagProcessor.rottableBlocks),
					Codec.FLOAT.fieldOf("integrity").forGetter(replaceInTagProcessor -> replaceInTagProcessor.integrity)
				)
				.apply(instance, ReplaceInTagProcessor::new)
	);
	private final Tag<Block> rottableBlocks;
	private final float integrity;

	public ReplaceInTagProcessor(Tag<Block> tag, float f) {
		this.integrity = f;
		this.rottableBlocks = tag;
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
		Random random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
		return structureBlockInfo.state.is(this.rottableBlocks) && !(this.integrity >= 1.0F) && !(random.nextFloat() <= this.integrity) ? null : structureBlockInfo2;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return StructureProcessorType.REPLACE_IN_TAG;
	}
}
