package net.minecraft.world.level.levelgen.structure.pieces;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

@FunctionalInterface
public interface PieceGenerator<C extends FeatureConfiguration> {
	void generatePieces(StructurePiecesBuilder structurePiecesBuilder, PieceGenerator.Context<C> context);

	public static record Context<C extends FeatureConfiguration>(
		C config,
		ChunkGenerator chunkGenerator,
		StructureTemplateManager structureTemplateManager,
		ChunkPos chunkPos,
		LevelHeightAccessor heightAccessor,
		WorldgenRandom random,
		long seed
	) {
	}
}
