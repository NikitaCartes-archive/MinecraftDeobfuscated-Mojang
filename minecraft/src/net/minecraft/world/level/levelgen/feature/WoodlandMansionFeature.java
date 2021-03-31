package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
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

	protected boolean isFeatureChunk(
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		long l,
		WorldgenRandom worldgenRandom,
		ChunkPos chunkPos,
		Biome biome,
		ChunkPos chunkPos2,
		NoneFeatureConfiguration noneFeatureConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		for (Biome biome2 : biomeSource.getBiomesWithin(chunkPos.getBlockX(9), chunkGenerator.getSeaLevel(), chunkPos.getBlockZ(9), 32)) {
			if (!biome2.getGenerationSettings().isValidStart(this)) {
				return false;
			}
		}

		return true;
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
			Biome biome,
			NoneFeatureConfiguration noneFeatureConfiguration,
			LevelHeightAccessor levelHeightAccessor
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
			int m = chunkGenerator.getFirstOccupiedHeight(k, l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
			int n = chunkGenerator.getFirstOccupiedHeight(k, l + j, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
			int o = chunkGenerator.getFirstOccupiedHeight(k + i, l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
			int p = chunkGenerator.getFirstOccupiedHeight(k + i, l + j, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
			int q = Math.min(Math.min(m, n), Math.min(o, p));
			if (q >= 60) {
				BlockPos blockPos = new BlockPos(chunkPos.getBlockX(8), q + 1, chunkPos.getBlockZ(8));
				List<WoodlandMansionPieces.WoodlandMansionPiece> list = Lists.<WoodlandMansionPieces.WoodlandMansionPiece>newLinkedList();
				WoodlandMansionPieces.generateMansion(structureManager, blockPos, rotation, list, this.random);
				list.forEach(this::addPiece);
			}
		}

		@Override
		public void placeInChunk(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos
		) {
			super.placeInChunk(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos);
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
