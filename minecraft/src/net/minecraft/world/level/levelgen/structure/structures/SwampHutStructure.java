package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class SwampHutStructure extends Structure {
	public static final MapCodec<SwampHutStructure> CODEC = simpleCodec(SwampHutStructure::new);

	public SwampHutStructure(Structure.StructureSettings structureSettings) {
		super(structureSettings);
	}

	@Override
	public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
		return onTopOfChunkCenter(
			generationContext, Heightmap.Types.WORLD_SURFACE_WG, structurePiecesBuilder -> generatePieces(structurePiecesBuilder, generationContext)
		);
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
		structurePiecesBuilder.addPiece(
			new SwampHutPiece(generationContext.random(), generationContext.chunkPos().getMinBlockX(), generationContext.chunkPos().getMinBlockZ())
		);
	}

	@Override
	public StructureType<?> type() {
		return StructureType.SWAMP_HUT;
	}
}
