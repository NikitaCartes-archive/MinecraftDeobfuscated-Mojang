package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MultiJigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class BastionPieces {
	public static final ImmutableMap<String, Integer> POOLS = ImmutableMap.<String, Integer>builder()
		.put("bastion/units/base", 60)
		.put("bastion/hoglin_stable/origin", 60)
		.put("bastion/treasure/starters", 60)
		.put("bastion/bridge/start", 60)
		.build();

	public static void bootstrap() {
		BastionHousingUnitsPools.bootstrap();
		BastionHoglinStablePools.bootstrap();
		BastionTreasureRoomPools.bootstrap();
		BastionBridgePools.bootstrap();
		BastionSharedPools.bootstrap();
	}

	public static void addPieces(
		ChunkGenerator<?> chunkGenerator,
		StructureManager structureManager,
		BlockPos blockPos,
		List<StructurePiece> list,
		WorldgenRandom worldgenRandom,
		MultiJigsawConfiguration multiJigsawConfiguration
	) {
		bootstrap();
		JigsawConfiguration jigsawConfiguration = multiJigsawConfiguration.getRandomPool(worldgenRandom);
		JigsawPlacement.addPieces(
			jigsawConfiguration.startPool,
			jigsawConfiguration.size,
			BastionPieces.BastionPiece::new,
			chunkGenerator,
			structureManager,
			blockPos,
			list,
			worldgenRandom,
			false,
			false
		);
	}

	public static class BastionPiece extends PoolElementStructurePiece {
		public BastionPiece(
			StructureManager structureManager, StructurePoolElement structurePoolElement, BlockPos blockPos, int i, Rotation rotation, BoundingBox boundingBox
		) {
			super(StructurePieceType.BASTION_REMNANT, structureManager, structurePoolElement, blockPos, i, rotation, boundingBox);
		}

		public BastionPiece(StructureManager structureManager, CompoundTag compoundTag) {
			super(structureManager, compoundTag, StructurePieceType.BASTION_REMNANT);
		}
	}
}
