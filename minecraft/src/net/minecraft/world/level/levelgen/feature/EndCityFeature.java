package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class EndCityFeature extends StructureFeature<NoneFeatureConfiguration> {
	public EndCityFeature(Codec<NoneFeatureConfiguration> codec) {
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
		return getYPositionForFeature(i, j, chunkGenerator) >= 60;
	}

	@Override
	public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return EndCityFeature.EndCityStart::new;
	}

	private static int getYPositionForFeature(int i, int j, ChunkGenerator chunkGenerator) {
		Random random = new Random((long)(i + j * 10387313));
		Rotation rotation = Rotation.getRandom(random);
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
		return Math.min(Math.min(o, p), Math.min(q, r));
	}

	public static class EndCityStart extends StructureStart<NoneFeatureConfiguration> {
		public EndCityStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, NoneFeatureConfiguration noneFeatureConfiguration
		) {
			Rotation rotation = Rotation.getRandom(this.random);
			int k = EndCityFeature.getYPositionForFeature(i, j, chunkGenerator);
			if (k >= 60) {
				BlockPos blockPos = new BlockPos(i * 16 + 8, k, j * 16 + 8);
				EndCityPieces.startHouseTower(structureManager, blockPos, rotation, this.pieces, this.random);
				this.calculateBoundingBox();
			}
		}
	}
}
