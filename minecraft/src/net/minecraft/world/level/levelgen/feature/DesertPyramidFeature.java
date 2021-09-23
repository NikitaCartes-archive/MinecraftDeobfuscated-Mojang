package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.DesertPyramidPiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class DesertPyramidFeature extends StructureFeature<NoneFeatureConfiguration> {
	public DesertPyramidFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, DesertPyramidFeature::generatePieces);
	}

	private static void generatePieces(
		StructurePiecesBuilder structurePiecesBuilder, NoneFeatureConfiguration noneFeatureConfiguration, PieceGenerator.Context context
	) {
		if (context.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG)) {
			if (context.getLowestY(21, 21) >= context.chunkGenerator().getSeaLevel()) {
				structurePiecesBuilder.addPiece(new DesertPyramidPiece(context.random(), context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ()));
			}
		}
	}
}
