package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.ShipwreckPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class ShipwreckFeature extends StructureFeature<ShipwreckConfiguration> {
	public ShipwreckFeature(Codec<ShipwreckConfiguration> codec) {
		super(codec, ShipwreckFeature::generatePieces);
	}

	private static void generatePieces(
		StructurePiecesBuilder structurePiecesBuilder, ShipwreckConfiguration shipwreckConfiguration, PieceGenerator.Context context
	) {
		Heightmap.Types types = shipwreckConfiguration.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
		if (context.validBiomeOnTop(types)) {
			Rotation rotation = Rotation.getRandom(context.random());
			BlockPos blockPos = new BlockPos(context.chunkPos().getMinBlockX(), 90, context.chunkPos().getMinBlockZ());
			ShipwreckPieces.addPieces(context.structureManager(), blockPos, rotation, structurePiecesBuilder, context.random(), shipwreckConfiguration);
		}
	}
}
