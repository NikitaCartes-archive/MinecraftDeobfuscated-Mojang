package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanMonumentFeature extends StructureFeature<NoneFeatureConfiguration> {
	public static final WeightedRandomList<MobSpawnSettings.SpawnerData> MONUMENT_ENEMIES = WeightedRandomList.create(
		new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4)
	);

	public OceanMonumentFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, OceanMonumentFeature::generatePieces);
	}

	@Override
	protected boolean linearSeparation() {
		return false;
	}

	protected boolean isFeatureChunk(
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		long l,
		ChunkPos chunkPos,
		NoneFeatureConfiguration noneFeatureConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		int i = chunkPos.getBlockX(9);
		int j = chunkPos.getBlockZ(9);

		for (Biome biome : biomeSource.getBiomesWithin(i, chunkGenerator.getSeaLevel(), j, 29, chunkGenerator.climateSampler())) {
			if (biome.getBiomeCategory() != Biome.BiomeCategory.OCEAN && biome.getBiomeCategory() != Biome.BiomeCategory.RIVER) {
				return false;
			}
		}

		return true;
	}

	private static StructurePiece createTopPiece(ChunkPos chunkPos, WorldgenRandom worldgenRandom) {
		int i = chunkPos.getMinBlockX() - 29;
		int j = chunkPos.getMinBlockZ() - 29;
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(worldgenRandom);
		return new OceanMonumentPieces.MonumentBuilding(worldgenRandom, i, j, direction);
	}

	private static void generatePieces(
		StructurePiecesBuilder structurePiecesBuilder, NoneFeatureConfiguration noneFeatureConfiguration, PieceGenerator.Context context
	) {
		generatePieces(structurePiecesBuilder, context);
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, PieceGenerator.Context context) {
		if (context.validBiomeOnTop(Heightmap.Types.OCEAN_FLOOR_WG)) {
			structurePiecesBuilder.addPiece(createTopPiece(context.chunkPos(), context.random()));
		}
	}

	public static PiecesContainer regeneratePiecesAfterLoad(ChunkPos chunkPos, long l, PiecesContainer piecesContainer) {
		if (piecesContainer.isEmpty()) {
			return piecesContainer;
		} else {
			WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
			worldgenRandom.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
			StructurePiece structurePiece = createTopPiece(chunkPos, worldgenRandom);
			StructurePiecesBuilder structurePiecesBuilder = new StructurePiecesBuilder();
			structurePiecesBuilder.addPiece(structurePiece);
			return structurePiecesBuilder.build();
		}
	}
}
