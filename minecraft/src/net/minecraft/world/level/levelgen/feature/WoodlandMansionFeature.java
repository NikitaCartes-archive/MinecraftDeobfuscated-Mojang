package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class WoodlandMansionFeature extends StructureFeature<NoneFeatureConfiguration> {
	public WoodlandMansionFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	protected boolean linearSeparation() {
		return false;
	}

	@Override
	public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return WoodlandMansionFeature.WoodlandMansionStart::new;
	}

	public static class WoodlandMansionStart extends StructureStart<NoneFeatureConfiguration> {
		public WoodlandMansionStart(StructureFeature<NoneFeatureConfiguration> structureFeature, ChunkPos chunkPos, int i, long l) {
			super(structureFeature, chunkPos, i, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			NoneFeatureConfiguration noneFeatureConfiguration,
			LevelHeightAccessor levelHeightAccessor,
			Predicate<Biome> predicate
		) {
			Rotation rotation = Rotation.getRandom(this.random);
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

			int k = chunkPos.getBlockX(7);
			int l = chunkPos.getBlockZ(7);
			int[] is = StructureFeature.getCornerHeights(chunkGenerator, k, i, l, j, levelHeightAccessor);
			int m = Math.min(Math.min(is[0], is[1]), Math.min(is[2], is[3]));
			if (m >= 60) {
				if (predicate.test(chunkGenerator.getNoiseBiome(QuartPos.fromBlock(k), QuartPos.fromBlock(is[0]), QuartPos.fromBlock(l)))) {
					BlockPos blockPos = new BlockPos(chunkPos.getMiddleBlockX(), m + 1, chunkPos.getMiddleBlockZ());
					List<WoodlandMansionPieces.WoodlandMansionPiece> list = Lists.<WoodlandMansionPieces.WoodlandMansionPiece>newLinkedList();
					WoodlandMansionPieces.generateMansion(structureManager, blockPos, rotation, list, this.random);
					list.forEach(this::addPiece);
				}
			}
		}

		@Override
		public void placeInChunk(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			Predicate<Biome> predicate,
			BoundingBox boundingBox,
			ChunkPos chunkPos
		) {
			super.placeInChunk(worldGenLevel, structureFeatureManager, chunkGenerator, random, predicate, boundingBox, chunkPos);
			BoundingBox boundingBox2 = this.getBoundingBox();
			int i = boundingBox2.minY();

			for (int j = boundingBox.minX(); j <= boundingBox.maxX(); j++) {
				for (int k = boundingBox.minZ(); k <= boundingBox.maxZ(); k++) {
					BlockPos blockPos = new BlockPos(j, i, k);
					if (!worldGenLevel.isEmptyBlock(blockPos) && boundingBox2.isInside(blockPos) && this.isInsidePiece(blockPos)) {
						for (int l = i - 1; l > 1; l--) {
							BlockPos blockPos2 = new BlockPos(j, l, k);
							if (!worldGenLevel.isEmptyBlock(blockPos2) && !worldGenLevel.getBlockState(blockPos2).getMaterial().isLiquid()) {
								break;
							}

							worldGenLevel.setBlock(blockPos2, Blocks.COBBLESTONE.defaultBlockState(), 2);
						}
					}
				}
			}
		}
	}
}
