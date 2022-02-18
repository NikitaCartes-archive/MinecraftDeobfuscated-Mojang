package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;

public record ConcentricRingsStructurePlacement(int distance, int spread, int count) implements StructurePlacement {
	public static final Codec<ConcentricRingsStructurePlacement> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.intRange(0, 1023).fieldOf("distance").forGetter(ConcentricRingsStructurePlacement::distance),
					Codec.intRange(0, 1023).fieldOf("spread").forGetter(ConcentricRingsStructurePlacement::spread),
					Codec.intRange(1, 4095).fieldOf("count").forGetter(ConcentricRingsStructurePlacement::count)
				)
				.apply(instance, ConcentricRingsStructurePlacement::new)
	);

	@Override
	public boolean isFeatureChunk(ChunkGenerator chunkGenerator, int i, int j) {
		List<ChunkPos> list = chunkGenerator.getRingPositionsFor(this);
		return list == null ? false : list.contains(new ChunkPos(i, j));
	}

	@Override
	public StructurePlacementType<?> type() {
		return StructurePlacementType.CONCENTRIC_RINGS;
	}
}
