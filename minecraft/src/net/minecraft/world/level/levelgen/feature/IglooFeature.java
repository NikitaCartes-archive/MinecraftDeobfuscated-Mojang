package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.IglooPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class IglooFeature extends StructureFeature<NoneFeatureConfiguration> {
	public IglooFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, IglooFeature::generatePieces);
	}

	private static void generatePieces(
		StructurePiecesBuilder structurePiecesBuilder, NoneFeatureConfiguration noneFeatureConfiguration, PieceGenerator.Context context
	) {
		if (context.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG)) {
			BlockPos blockPos = new BlockPos(context.chunkPos().getMinBlockX(), 90, context.chunkPos().getMinBlockZ());
			Rotation rotation = Rotation.getRandom(context.random());
			IglooPieces.addPieces(context.structureManager(), blockPos, rotation, structurePiecesBuilder, context.random());
		}
	}
}
