package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertPyramidStructure extends SinglePieceStructure {
	public static final MapCodec<DesertPyramidStructure> CODEC = simpleCodec(DesertPyramidStructure::new);

	public DesertPyramidStructure(Structure.StructureSettings structureSettings) {
		super(DesertPyramidPiece::new, 21, 21, structureSettings);
	}

	@Override
	public void afterPlace(
		WorldGenLevel worldGenLevel,
		StructureManager structureManager,
		ChunkGenerator chunkGenerator,
		RandomSource randomSource,
		BoundingBox boundingBox,
		ChunkPos chunkPos,
		PiecesContainer piecesContainer
	) {
		Set<BlockPos> set = SortedArraySet.<BlockPos>create(Vec3i::compareTo);

		for (StructurePiece structurePiece : piecesContainer.pieces()) {
			if (structurePiece instanceof DesertPyramidPiece desertPyramidPiece) {
				set.addAll(desertPyramidPiece.getPotentialSuspiciousSandWorldPositions());
				placeSuspiciousSand(boundingBox, worldGenLevel, desertPyramidPiece.getRandomCollapsedRoofPos());
			}
		}

		ObjectArrayList<BlockPos> objectArrayList = new ObjectArrayList<>(set.stream().toList());
		RandomSource randomSource2 = RandomSource.create(worldGenLevel.getSeed()).forkPositional().at(piecesContainer.calculateBoundingBox().getCenter());
		Util.shuffle(objectArrayList, randomSource2);
		int i = Math.min(set.size(), randomSource2.nextInt(5, 8));

		for (BlockPos blockPos : objectArrayList) {
			if (i > 0) {
				i--;
				placeSuspiciousSand(boundingBox, worldGenLevel, blockPos);
			} else if (boundingBox.isInside(blockPos)) {
				worldGenLevel.setBlock(blockPos, Blocks.SAND.defaultBlockState(), 2);
			}
		}
	}

	private static void placeSuspiciousSand(BoundingBox boundingBox, WorldGenLevel worldGenLevel, BlockPos blockPos) {
		if (boundingBox.isInside(blockPos)) {
			worldGenLevel.setBlock(blockPos, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 2);
			worldGenLevel.getBlockEntity(blockPos, BlockEntityType.BRUSHABLE_BLOCK)
				.ifPresent(brushableBlockEntity -> brushableBlockEntity.setLootTable(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY, blockPos.asLong()));
		}
	}

	@Override
	public StructureType<?> type() {
		return StructureType.DESERT_PYRAMID;
	}
}
