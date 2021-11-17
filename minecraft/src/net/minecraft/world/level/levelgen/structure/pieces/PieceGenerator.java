package net.minecraft.world.level.levelgen.structure.pieces;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

@FunctionalInterface
public interface PieceGenerator<C extends FeatureConfiguration> {
	void generatePieces(StructurePiecesBuilder structurePiecesBuilder, PieceGenerator.Context<C> context);

	public static record Context() {
		private final C config;
		private final ChunkGenerator chunkGenerator;
		private final StructureManager structureManager;
		private final ChunkPos chunkPos;
		private final LevelHeightAccessor heightAccessor;
		private final WorldgenRandom random;
		private final long seed;

		public Context(
			C featureConfiguration,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			LevelHeightAccessor levelHeightAccessor,
			WorldgenRandom worldgenRandom,
			long l
		) {
			this.config = featureConfiguration;
			this.chunkGenerator = chunkGenerator;
			this.structureManager = structureManager;
			this.chunkPos = chunkPos;
			this.heightAccessor = levelHeightAccessor;
			this.random = worldgenRandom;
			this.seed = l;
		}
	}
}
