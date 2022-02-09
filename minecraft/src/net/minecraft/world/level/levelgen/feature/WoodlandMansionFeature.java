package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

public class WoodlandMansionFeature extends StructureFeature<NoneFeatureConfiguration> {
	public WoodlandMansionFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec, WoodlandMansionFeature::pieceGeneratorSupplier, WoodlandMansionFeature::afterPlace);
	}

	private static Optional<PieceGenerator<NoneFeatureConfiguration>> pieceGeneratorSupplier(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> context) {
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
		Rotation rotation = Rotation.getRandom(worldgenRandom);
		int i = 5;
		int j = 5;
		if (rotation == Rotation.CLOCKWISE_90) {
			i = -5;
		} else if (rotation == Rotation.CLOCKWISE_180) {
			i = -5;
			j = -5;
		} else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
			j = -5;
		}

		int k = context.chunkPos().getBlockX(7);
		int l = context.chunkPos().getBlockZ(7);
		int[] is = context.getCornerHeights(k, i, l, j);
		int m = Math.min(Math.min(is[0], is[1]), Math.min(is[2], is[3]));
		if (m < 60) {
			return Optional.empty();
		} else if (!context.validBiome().test(context.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(k), QuartPos.fromBlock(is[0]), QuartPos.fromBlock(l)))) {
			return Optional.empty();
		} else {
			BlockPos blockPos = new BlockPos(context.chunkPos().getMiddleBlockX(), m + 1, context.chunkPos().getMiddleBlockZ());
			return Optional.of((PieceGenerator<>)(structurePiecesBuilder, contextx) -> {
				List<WoodlandMansionPieces.WoodlandMansionPiece> list = Lists.<WoodlandMansionPieces.WoodlandMansionPiece>newLinkedList();
				WoodlandMansionPieces.generateMansion(contextx.structureManager(), blockPos, rotation, list, worldgenRandom);
				list.forEach(structurePiecesBuilder::addPiece);
			});
		}
	}

	private static void afterPlace(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BoundingBox boundingBox,
		ChunkPos chunkPos,
		PiecesContainer piecesContainer
	) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int i = worldGenLevel.getMinBuildHeight();
		BoundingBox boundingBox2 = piecesContainer.calculateBoundingBox();
		int j = boundingBox2.minY();

		for (int k = boundingBox.minX(); k <= boundingBox.maxX(); k++) {
			for (int l = boundingBox.minZ(); l <= boundingBox.maxZ(); l++) {
				mutableBlockPos.set(k, j, l);
				if (!worldGenLevel.isEmptyBlock(mutableBlockPos) && boundingBox2.isInside(mutableBlockPos) && piecesContainer.isInsidePiece(mutableBlockPos)) {
					for (int m = j - 1; m > i; m--) {
						mutableBlockPos.setY(m);
						if (!worldGenLevel.isEmptyBlock(mutableBlockPos) && !worldGenLevel.getBlockState(mutableBlockPos).getMaterial().isLiquid()) {
							break;
						}

						worldGenLevel.setBlock(mutableBlockPos, Blocks.COBBLESTONE.defaultBlockState(), 2);
					}
				}
			}
		}
	}
}
