package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.ShipwreckPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class ShipwreckFeature extends StructureFeature<ShipwreckConfiguration> {
	public ShipwreckFeature(Codec<ShipwreckConfiguration> codec) {
		super(codec, PieceGeneratorSupplier.simple(ShipwreckFeature::checkLocation, ShipwreckFeature::generatePieces));
	}

	private static boolean checkLocation(PieceGeneratorSupplier.Context<ShipwreckConfiguration> context) {
		Heightmap.Types types = context.config().isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
		return context.validBiomeOnTop(types);
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, PieceGenerator.Context<ShipwreckConfiguration> context) {
		Rotation rotation = Rotation.getRandom(context.random());
		BlockPos blockPos = new BlockPos(context.chunkPos().getMinBlockX(), 90, context.chunkPos().getMinBlockZ());
		ShipwreckPieces.addPieces(context.structureManager(), blockPos, rotation, structurePiecesBuilder, context.random(), context.config());
	}
}
