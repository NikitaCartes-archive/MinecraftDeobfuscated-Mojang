package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
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
import net.minecraft.world.level.levelgen.structure.StructurePiece;
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
		int i,
		int j,
		Biome biome,
		ChunkPos chunkPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		for (Biome biome2 : biomeSource.getBiomesWithin(i * 16 + 9, chunkGenerator.getSeaLevel(), j * 16 + 9, 32)) {
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
		public WoodlandMansionStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			int i,
			int j,
			Biome biome,
			NoneFeatureConfiguration noneFeatureConfiguration
		) {
			Rotation rotation = Rotation.getRandom(this.random);
			int k = 5;
			int l = 5;
			if (rotation == Rotation.CLOCKWISE_90) {
				k = -5;
			} else if (rotation == Rotation.CLOCKWISE_180) {
				k = -5;
				l = -5;
			} else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
				l = -5;
			}

			int m = (i << 4) + 7;
			int n = (j << 4) + 7;
			int o = chunkGenerator.getFirstOccupiedHeight(m, n, Heightmap.Types.WORLD_SURFACE_WG);
			int p = chunkGenerator.getFirstOccupiedHeight(m, n + l, Heightmap.Types.WORLD_SURFACE_WG);
			int q = chunkGenerator.getFirstOccupiedHeight(m + k, n, Heightmap.Types.WORLD_SURFACE_WG);
			int r = chunkGenerator.getFirstOccupiedHeight(m + k, n + l, Heightmap.Types.WORLD_SURFACE_WG);
			int s = Math.min(Math.min(o, p), Math.min(q, r));
			if (s >= 60) {
				BlockPos blockPos = new BlockPos(i * 16 + 8, s + 1, j * 16 + 8);
				List<WoodlandMansionPieces.WoodlandMansionPiece> list = Lists.<WoodlandMansionPieces.WoodlandMansionPiece>newLinkedList();
				WoodlandMansionPieces.generateMansion(structureManager, blockPos, rotation, list, this.random);
				this.pieces.addAll(list);
				this.calculateBoundingBox();
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
			int i = this.boundingBox.y0;

			for (int j = boundingBox.x0; j <= boundingBox.x1; j++) {
				for (int k = boundingBox.z0; k <= boundingBox.z1; k++) {
					BlockPos blockPos = new BlockPos(j, i, k);
					if (!worldGenLevel.isEmptyBlock(blockPos) && this.boundingBox.isInside(blockPos)) {
						boolean bl = false;

						for (StructurePiece structurePiece : this.pieces) {
							if (structurePiece.getBoundingBox().isInside(blockPos)) {
								bl = true;
								break;
							}
						}

						if (bl) {
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
}
