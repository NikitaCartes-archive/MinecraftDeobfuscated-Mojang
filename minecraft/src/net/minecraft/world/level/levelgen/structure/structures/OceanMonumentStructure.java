package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanMonumentStructure extends Structure {
	public static final MapCodec<OceanMonumentStructure> CODEC = simpleCodec(OceanMonumentStructure::new);

	public OceanMonumentStructure(Structure.StructureSettings structureSettings) {
		super(structureSettings);
	}

	@Override
	public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
		int i = generationContext.chunkPos().getBlockX(9);
		int j = generationContext.chunkPos().getBlockZ(9);

		for (Holder<Biome> holder : generationContext.biomeSource()
			.getBiomesWithin(i, generationContext.chunkGenerator().getSeaLevel(), j, 29, generationContext.randomState().sampler())) {
			if (!holder.is(BiomeTags.REQUIRED_OCEAN_MONUMENT_SURROUNDING)) {
				return Optional.empty();
			}
		}

		return onTopOfChunkCenter(
			generationContext, Heightmap.Types.OCEAN_FLOOR_WG, structurePiecesBuilder -> generatePieces(structurePiecesBuilder, generationContext)
		);
	}

	private static StructurePiece createTopPiece(ChunkPos chunkPos, WorldgenRandom worldgenRandom) {
		int i = chunkPos.getMinBlockX() - 29;
		int j = chunkPos.getMinBlockZ() - 29;
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(worldgenRandom);
		return new OceanMonumentPieces.MonumentBuilding(worldgenRandom, i, j, direction);
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
		structurePiecesBuilder.addPiece(createTopPiece(generationContext.chunkPos(), generationContext.random()));
	}

	public static PiecesContainer regeneratePiecesAfterLoad(ChunkPos chunkPos, long l, PiecesContainer piecesContainer) {
		if (piecesContainer.isEmpty()) {
			return piecesContainer;
		} else {
			WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
			worldgenRandom.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
			StructurePiece structurePiece = (StructurePiece)piecesContainer.pieces().get(0);
			BoundingBox boundingBox = structurePiece.getBoundingBox();
			int i = boundingBox.minX();
			int j = boundingBox.minZ();
			Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(worldgenRandom);
			Direction direction2 = (Direction)Objects.requireNonNullElse(structurePiece.getOrientation(), direction);
			StructurePiece structurePiece2 = new OceanMonumentPieces.MonumentBuilding(worldgenRandom, i, j, direction2);
			StructurePiecesBuilder structurePiecesBuilder = new StructurePiecesBuilder();
			structurePiecesBuilder.addPiece(structurePiece2);
			return structurePiecesBuilder.build();
		}
	}

	@Override
	public StructureType<?> type() {
		return StructureType.OCEAN_MONUMENT;
	}
}
