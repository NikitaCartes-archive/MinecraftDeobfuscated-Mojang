package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanMonumentFeature extends StructureFeature<NoneFeatureConfiguration> {
	public OceanMonumentFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, PieceGeneratorSupplier.simple(OceanMonumentFeature::checkLocation, OceanMonumentFeature::generatePieces));
	}

	private static boolean checkLocation(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> context) {
		int i = context.chunkPos().getBlockX(9);
		int j = context.chunkPos().getBlockZ(9);

		for (Holder<Biome> holder : context.biomeSource()
			.getBiomesWithin(i, context.chunkGenerator().getSeaLevel(), j, 29, context.chunkGenerator().climateSampler())) {
			if (Biome.getBiomeCategory(holder) != Biome.BiomeCategory.OCEAN && Biome.getBiomeCategory(holder) != Biome.BiomeCategory.RIVER) {
				return false;
			}
		}

		return context.validBiomeOnTop(Heightmap.Types.OCEAN_FLOOR_WG);
	}

	private static StructurePiece createTopPiece(ChunkPos chunkPos, WorldgenRandom worldgenRandom) {
		int i = chunkPos.getMinBlockX() - 29;
		int j = chunkPos.getMinBlockZ() - 29;
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(worldgenRandom);
		return new OceanMonumentPieces.MonumentBuilding(worldgenRandom, i, j, direction);
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, PieceGenerator.Context<NoneFeatureConfiguration> context) {
		structurePiecesBuilder.addPiece(createTopPiece(context.chunkPos(), context.random()));
	}

	public static PiecesContainer regeneratePiecesAfterLoad(ChunkPos chunkPos, long l, PiecesContainer piecesContainer) {
		if (piecesContainer.isEmpty()) {
			return piecesContainer;
		} else {
			WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
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
}
