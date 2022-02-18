package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.SwamplandHutPiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class SwamplandHutFeature extends StructureFeature<NoneFeatureConfiguration> {
	public SwamplandHutFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, PieceGeneratorSupplier.simple(PieceGeneratorSupplier.checkForBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG), SwamplandHutFeature::generatePieces));
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, PieceGenerator.Context<NoneFeatureConfiguration> context) {
		structurePiecesBuilder.addPiece(new SwamplandHutPiece(context.random(), context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ()));
	}
}
