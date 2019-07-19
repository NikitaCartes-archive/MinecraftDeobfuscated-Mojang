package net.minecraft.world.level.levelgen.feature;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class VillagePieces {
	public static void addPieces(
		ChunkGenerator<?> chunkGenerator,
		StructureManager structureManager,
		BlockPos blockPos,
		List<StructurePiece> list,
		WorldgenRandom worldgenRandom,
		VillageConfiguration villageConfiguration
	) {
		PlainVillagePools.bootstrap();
		SnowyVillagePools.bootstrap();
		SavannaVillagePools.bootstrap();
		DesertVillagePools.bootstrap();
		TaigaVillagePools.bootstrap();
		JigsawPlacement.addPieces(
			villageConfiguration.startPool, villageConfiguration.size, VillagePieces.VillagePiece::new, chunkGenerator, structureManager, blockPos, list, worldgenRandom
		);
	}

	public static class VillagePiece extends PoolElementStructurePiece {
		public VillagePiece(
			StructureManager structureManager, StructurePoolElement structurePoolElement, BlockPos blockPos, int i, Rotation rotation, BoundingBox boundingBox
		) {
			super(StructurePieceType.VILLAGE, structureManager, structurePoolElement, blockPos, i, rotation, boundingBox);
		}

		public VillagePiece(StructureManager structureManager, CompoundTag compoundTag) {
			super(structureManager, compoundTag, StructurePieceType.VILLAGE);
		}
	}
}
