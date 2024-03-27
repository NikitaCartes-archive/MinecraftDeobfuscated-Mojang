package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;

public class GravityProcessor extends StructureProcessor {
	public static final MapCodec<GravityProcessor> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Heightmap.Types.CODEC.fieldOf("heightmap").orElse(Heightmap.Types.WORLD_SURFACE_WG).forGetter(gravityProcessor -> gravityProcessor.heightmap),
					Codec.INT.fieldOf("offset").orElse(0).forGetter(gravityProcessor -> gravityProcessor.offset)
				)
				.apply(instance, GravityProcessor::new)
	);
	private final Heightmap.Types heightmap;
	private final int offset;

	public GravityProcessor(Heightmap.Types types, int i) {
		this.heightmap = types;
		this.offset = i;
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
		Heightmap.Types types;
		if (levelReader instanceof ServerLevel) {
			if (this.heightmap == Heightmap.Types.WORLD_SURFACE_WG) {
				types = Heightmap.Types.WORLD_SURFACE;
			} else if (this.heightmap == Heightmap.Types.OCEAN_FLOOR_WG) {
				types = Heightmap.Types.OCEAN_FLOOR;
			} else {
				types = this.heightmap;
			}
		} else {
			types = this.heightmap;
		}

		BlockPos blockPos3 = structureBlockInfo2.pos();
		int i = levelReader.getHeight(types, blockPos3.getX(), blockPos3.getZ()) + this.offset;
		int j = structureBlockInfo.pos().getY();
		return new StructureTemplate.StructureBlockInfo(
			new BlockPos(blockPos3.getX(), i + j, blockPos3.getZ()), structureBlockInfo2.state(), structureBlockInfo2.nbt()
		);
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return StructureProcessorType.GRAVITY;
	}
}
